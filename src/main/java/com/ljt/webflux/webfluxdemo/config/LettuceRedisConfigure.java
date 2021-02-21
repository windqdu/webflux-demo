package com.ljt.webflux.webfluxdemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;

/**
 * @author lijuntao1
 * @date 2020/12/25 17:11
 */
@Configuration
public class LettuceRedisConfigure {
    @Bean
    public ReactiveRedisTemplate<String, String> reactiveStringRedisTemplate(ReactiveRedisConnectionFactory factory) {
        ReactiveRedisTemplate<String, String> reactiveRedisTemplate = new ReactiveRedisTemplate<>(factory,
            RedisSerializationContext.string());
        return reactiveRedisTemplate;
    }

}
