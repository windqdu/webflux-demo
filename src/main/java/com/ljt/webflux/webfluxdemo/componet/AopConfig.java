package com.ljt.webflux.webfluxdemo.componet;

import com.ljt.webflux.webfluxdemo.model.ResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;

/**
 * https://stackoom.com/question/3axOo/%E5%A6%82%E4%BD%95%E4%BD%BF%E7%94%A8Spring-AOP%E5%92%8CWebFlux%E4%BB%8EjoinPoint-proceed-%E8%BF%94%E5%9B%9E%E7%9A%84%E5%AF%B9%E8%B1%A1
 *
 * @author lijuntao1
 * @date 2020/12/27 21:14
 */
@Aspect
@Slf4j
@Component
public class AopConfig {

    @Pointcut("execution(public * com.ljt.webflux.webfluxdemo.controller.*Controller.*(..))")
    public void pointcut() {
    }

    @Before("pointcut()")
    public void doBefore(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().toString();
        String params = Arrays.toString(joinPoint.getArgs());
        log.info("methodName:{} params:{}", methodName, params);
    }

    @Around("pointcut()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) {
        final long start = System.currentTimeMillis();

        String methodName = joinPoint.getSignature().toString();
        String params = Arrays.toString(joinPoint.getArgs());
        log.info("methodName:{} params:{}", methodName, params);

        Object result = null;
        try {
            result = joinPoint.proceed();

            if (result instanceof Mono) {
                Mono process = (Mono) result;
                //此处需要紧接着方法执行的Mono往下处理，否则响应式链路会断开
                return process.map(v -> handlerResult(v)).doOnNext(resp -> log.info("resp:{}", resp)).
                    doFinally(response -> calcCostTime(response, start, joinPoint));
            } else if (result instanceof Flux) {
                Flux process = (Flux) result;
                return process.map(v -> handlerResult(v))
                    .doOnNext(resp -> log.info("resp:{}", resp))
                    .doFinally(response -> calcCostTime(response, start, joinPoint));
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } catch (Throwable throwable) {
            log.error(throwable.getMessage(), throwable);
        }
        return Mono.just(handlerResult(result)).doOnNext(resp -> log.info("resp:{}", resp))
            .doFinally(response -> calcCostTime(response, start, joinPoint));
    }

    private Object handlerResult(Object v) {
        if (v instanceof ResponseVO) {
            return v;
        } else if (v instanceof Exception) {
            Exception exception = (Exception) v;
            return ResponseVO.fail(exception.getLocalizedMessage());
        } else if (v instanceof String) {
            return v;
        } else {
            return ResponseVO.success(v);
        }
    }

    private void calcCostTime(Object response, long start, ProceedingJoinPoint joinPoint) {
        final long executionTime = System.currentTimeMillis() - start;
        // here you can access the response object and do your actions
        log.info("response:{}", response);
        log.info("{} executed in ：{} ms", joinPoint.getSignature(), executionTime);
    }

    private Mono validToken(String token) {
        if ("1".equals(token)) {
            return Mono.just("token");
        } else {
            return Mono.empty();

        }
    }
}
