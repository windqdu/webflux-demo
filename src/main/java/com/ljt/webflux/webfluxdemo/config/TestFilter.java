package com.ljt.webflux.webfluxdemo.config;

import com.ljt.webflux.webfluxdemo.componet.RequestValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.function.BiPredicate;

/**
 * @author lijuntao1
 * @date 2021/2/9 17:46
 */
@Slf4j
@Configuration
public class TestFilter implements WebFilter {

    @Autowired
    private RequestValidationService requestValidationService;

    @Override
    public Mono<Void> filter(ServerWebExchange serverWebExchange, WebFilterChain webFilterChain) {
        return requestValidationService.validate(serverWebExchange, new BiPredicate<ServerHttpRequest, String>() {
            @Override
            public boolean test(ServerHttpRequest serverHttpRequest, String bodyStr) {
                /** application logic can go here. few points:
                 1. I used a BiPredicate because I just need a true or false if the request should be passed to the controller.
                 2. If you want todo other mutations you could swap the predicate to a normal function and return
                 a mutated ServerWebExchange.
                 3. I pass body separately here to ensure safety of accessing the request body and not having to rewrap
                 the ServerWebExchange. A side affect of this though is any mutations to the String body
                 do not affect downstream.
                 **/

                log.info("bodyStr:{}", bodyStr);

                return true;
            }
        }).flatMap((ServerWebExchange r) -> webFilterChain.filter(r));

    }
}
