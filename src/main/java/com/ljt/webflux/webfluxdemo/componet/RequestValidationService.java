package com.ljt.webflux.webfluxdemo.componet;

import io.netty.buffer.ByteBufAllocator;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebExchangeDecorator;
import org.springframework.web.server.adapter.DefaultServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import sun.misc.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.function.BiPredicate;

/**
 * @author lijuntao1
 * @date 2021/2/20 11:43
 */
@Component
public class RequestValidationService {

    private DataBuffer stringBuffer(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);

        NettyDataBufferFactory nettyDataBufferFactory = new NettyDataBufferFactory(ByteBufAllocator.DEFAULT);
        DataBuffer buffer = nettyDataBufferFactory.allocateBuffer(bytes.length);
        buffer.write(bytes);
        return buffer;
    }

    private String bodyToString(InputStream bodyBytes) {
        byte[] currArr = null;


        try {
            currArr = IOUtils.readFully(bodyBytes,-1, false);
            //            currArr = bodyBytes.readAllBytes();
//            bodyBytes.read(currArr);
        } catch (IOException ioe) {
            throw new RuntimeException("could not parse body");
        }

        if (currArr.length == 0) {
            return null;
        }

        return new String(currArr, StandardCharsets.UTF_8);
    }

    private ServerHttpRequestDecorator requestWrapper(ServerHttpRequest request, String bodyStr) {
        URI uri = request.getURI();
//        ServerHttpRequest newRequest = request.mutate().uri(uri).build();
        final DataBuffer bodyDataBuffer = stringBuffer(bodyStr);
        Flux<DataBuffer> newBodyFlux = Flux.just(bodyDataBuffer);
        ServerHttpRequestDecorator requestDecorator = new ServerHttpRequestDecorator(request) {
            @Override
            public Flux<DataBuffer> getBody() {
                return newBodyFlux;
            }
        };

        return requestDecorator;
    }

    private InputStream newInputStream() {
        return new InputStream() {
            @Override
            public int read() {
                return -1;
            }
        };
    }

    private InputStream processRequestBody(InputStream s, DataBuffer d) {
        SequenceInputStream seq = new SequenceInputStream(s, d.asInputStream());
        return seq;
    }

    private Mono<ServerWebExchange> processInputStream(InputStream aggregatedBodyBytes, ServerWebExchange exchange,
        BiPredicate<ServerHttpRequest, String> predicate) {

        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders headers = request.getHeaders();

        String bodyStr = bodyToString(aggregatedBodyBytes);

        ServerWebExchange mutatedExchange = exchange;

        // if the body exists on the request we need to mutate the ServerWebExchange to not
        // reparse the body because DataBuffers can only be read once;
        if (bodyStr != null) {
            mutatedExchange = exchange.mutate().request(requestWrapper(request, bodyStr)).build();
        }

        ServerHttpRequest mutatedRequest = mutatedExchange.getRequest();

        if (predicate.test(mutatedRequest, bodyStr)) {
            return Mono.just(mutatedExchange);
        }

        return Mono.error(new RuntimeException("invalid signature"));
    }

    /*
     * Because the DataBuffer is in a Flux we must reduce it to a Mono type via Flux.reduce
     * This covers large payloads or requests bodies that get sent in multiple byte chunks
     * and need to be concatentated.
     *
     * 1. The reduce is initialized with a newInputStream
     * 2. processRequestBody is called on each step of the Flux where a step is a body byte
     *    chunk. The method processRequestBody casts the Inbound DataBuffer to a InputStream
     *    and concats the new InputStream with the existing one
     * 3. Once the Flux is complete flatMap is executed with the resulting InputStream which is
     *    passed with the ServerWebExchange to processInputStream which will do the request validation
     */
    public Mono<ServerWebExchange> validate(ServerWebExchange exchange, BiPredicate<ServerHttpRequest, String> p) {
        Flux<DataBuffer> body = exchange.getRequest().getBody();

        return body.reduce(newInputStream(), this::processRequestBody)
            .flatMap((InputStream b) -> processInputStream(b, exchange, p));
    }
}
