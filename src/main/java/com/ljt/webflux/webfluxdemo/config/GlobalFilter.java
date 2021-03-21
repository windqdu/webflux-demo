package com.ljt.webflux.webfluxdemo.config;

import io.netty.buffer.ByteBufAllocator;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;

/**
 * @author lijuntao1
 * @date 2021/2/20 22:37
 */
@Slf4j
@Configuration
public class GlobalFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        String method = exchange.getRequest().getMethodValue();
        if (HttpMethod.POST.name().equals(method)) {
            return parsePostBody(exchange, chain).flatMap(v -> validReq(exchange, chain));
        }

        return validReq(exchange, chain);
    }

    private Mono<Void> validReq(ServerWebExchange exchange, WebFilterChain chain) {
        return chain.filter(exchange);
    }

    /**
     * 解析post方法的body
     *
     * @param exchange
     * @param chain
     * @return
     */
    private Mono<ServerWebExchange> parsePostBody(ServerWebExchange exchange, WebFilterChain chain) {
        //使用filter解析post的body数据，会导致application/x-www-form-urlencoded类型的接口绑定参数失败，
        //所以此处只解析了application/json接口的body数据
        MediaType mediaType = exchange.getRequest().getHeaders().getContentType();
        ServerHttpRequest request = exchange.getRequest();

        if (mediaType.isCompatibleWith(MediaType.APPLICATION_JSON)) {
            Flux<DataBuffer> body = request.getBody();

            return body.map(dataBuffer -> {
                CharBuffer charBuffer = StandardCharsets.UTF_8.decode(dataBuffer.asByteBuffer());
                DataBufferUtils.release(dataBuffer);

                return charBuffer.toString();
            }).reduce(new ArrayList<String>(), (v1, v2) -> {
                v1.add(v2);

                return v1;
            }).map(v -> {
                //读取request body到缓存
                String bodyStr = Joiner.on("").join(v);
                log.info("bodyParams:{}", bodyStr);

                exchange.getAttributes().put("bodyParams", bodyStr);

                DataBuffer bodyDataBuffer = stringBuffer(bodyStr);
                Flux<DataBuffer> bodyFlux = Flux.just(bodyDataBuffer);

                ServerHttpRequest req = new ServerHttpRequestDecorator(request) {
                    @Override
                    public Flux<DataBuffer> getBody() {
                        return bodyFlux;
                    }
                };

                return exchange.mutate().request(req).build();
            });
        } else if (mediaType.isCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED)) {
            return exchange.getFormData().map(formData -> {
                StringBuilder sb = new StringBuilder();
                formData.forEach((k, v) -> {
                    sb.append(k).append("=").append(v != null && v.size() > 0 ? v.get(0) : "").append("&");
                });

                sb.deleteCharAt(sb.length() - 1);

                log.info("bodyParams:{}", sb.toString());

                exchange.getAttributes().put("bodyParams", sb.toString());
                exchange.getAttributes().put("bodyMap", urlEncodeBody2Map(sb.toString()));

                return exchange.mutate().request(request).build();
            });
        }

        return Mono.just(exchange);
    }

    /**
     * 将application/x-www-form-urlencoded 请求的body参数转换成map
     *
     * @param body
     * @return
     */
    private Map<String, Object> urlEncodeBody2Map(String body) {
        if (StringUtils.isBlank(body)) {
            return Maps.newHashMap();
        }

        return Splitter.on("&").splitToList(body).stream().map(v -> {
            Map<String, Object> map = Maps.newHashMap();
            List<String> lst = Splitter.on("=").splitToList(v);
            if (lst.size() > 1) {
                map.put(lst.get(0), lst.get(1));
            } else if (lst.size() > 0) {
                map.put(lst.get(0), "");
            }

            return map;
        }).reduce(Maps.newHashMap(), (m1, m2) -> {
            m1.putAll(m2);

            return m1;
        });
    }

    private DataBuffer stringBuffer(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);

        NettyDataBufferFactory nettyDataBufferFactory = new NettyDataBufferFactory(ByteBufAllocator.DEFAULT);
        DataBuffer buffer = nettyDataBufferFactory.allocateBuffer(bytes.length);
        buffer.write(bytes);
        return buffer;
    }
}
