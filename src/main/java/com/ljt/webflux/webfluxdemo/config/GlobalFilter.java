package com.ljt.webflux.webfluxdemo.config;

import io.netty.buffer.ByteBufAllocator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author lijuntao1
 * @date 2021/2/20 22:37
 */
@Slf4j
//@Configuration
public class GlobalFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // mediaType
        MediaType mediaType = exchange.getRequest().getHeaders().getContentType();
        String method = exchange.getRequest().getMethodValue();
        // read & modify body
        if (HttpMethod.POST.name().equals(method)) {
            Flux<DataBuffer> body = request.getBody();

            ServerHttpResponse response = exchange.getResponse();
            //WebSession session  = exchange.getSession().block();
            //缓存读取的request body信息
            AtomicReference<String> bodyRef = new AtomicReference<>();
            body.subscribe(dataBuffer -> {
                CharBuffer charBuffer = StandardCharsets.UTF_8.decode(dataBuffer.asByteBuffer());
                DataBufferUtils.release(dataBuffer);
                bodyRef.set(charBuffer.toString());
            });

            //读取request body到缓存
            //获取request body
            String bodyStr = bodyRef.get();
            log.info("bodyParams:{}", bodyStr);

            DataBuffer bodyDataBuffer = stringBuffer(bodyStr);
            Flux<DataBuffer> bodyFlux = Flux.just(bodyDataBuffer);

            request = new ServerHttpRequestDecorator(request) {
                @Override
                public Flux<DataBuffer> getBody() {
                    return bodyFlux;
                }
            };

            return chain.filter(exchange.mutate().request(request).build());

        }else{

        }


        //封装我们的request
        return chain.filter(exchange);
    }

    private DataBuffer stringBuffer(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);

        NettyDataBufferFactory nettyDataBufferFactory = new NettyDataBufferFactory(ByteBufAllocator.DEFAULT);
        DataBuffer buffer = nettyDataBufferFactory.allocateBuffer(bytes.length);
        buffer.write(bytes);
        return buffer;
    }
}
