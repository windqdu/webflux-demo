package com.ljt.webflux.webfluxdemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;

/**
 * @author lijuntao1
 * @date 2021/2/21 12:32
 */
@Configuration
public class UserRouting {
    @Bean
    public RouterFunction<ServerResponse> userRouter() {

        return RouterFunctions.nest(
            //相当于controller 下的 request mapping
            path("/user"),

            //各个路由节点
            RouterFunctions.route(GET("/getuserid"), this::getUserId)
                .andRoute(GET("/getusername"), this::getUserName));
    }

    /**
     *  获取用户id
     * @param serverRequest
     * @return
     */
    public Mono<ServerResponse> getUserId(ServerRequest serverRequest){
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(Mono.just("14455662442"),String.class);
    }

    /**
     *  获取用户姓名
     * @param serverRequest
     * @return
     */
    public Mono<ServerResponse> getUserName(ServerRequest serverRequest){

        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(Mono.just("crabman"),String.class);
    }
}
