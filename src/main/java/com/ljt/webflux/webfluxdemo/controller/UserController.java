package com.ljt.webflux.webfluxdemo.controller;

import com.google.gson.Gson;
import com.ljt.webflux.webfluxdemo.dao.EmployeeRepository;
import com.ljt.webflux.webfluxdemo.entity.Employee;
import com.ljt.webflux.webfluxdemo.model.ResponseVO;
import com.ljt.webflux.webfluxdemo.model.SaveUserReq;
import com.ljt.webflux.webfluxdemo.service.UserService;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * https://zhuanlan.zhihu.com/p/45351651
 * https://gitee.com/ffzs/WebFlux_r2dbc
 *
 * @author lijuntao1
 * @date 2020/12/25 16:06
 */
@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private Gson gson = new Gson();

    @Autowired
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserService userService;

    @GetMapping("/hello")
    public Mono<String> hello() {
        return Mono.just("Hello, Reactive");
    }

    @GetMapping("/getUser")
    public Mono<ResponseVO> getUser() {

        return reactiveRedisTemplate.opsForValue().get("hedu:employeeAuth:1131735461").map(ResponseVO::success);
    }

    @GetMapping("/getEmployee")
    public Mono<ResponseVO> getEmployee(@RequestParam("id") Long id) {

        return employeeRepository.findById(id).map(ResponseVO::success);
    }

    @GetMapping("/getEmployeeNyCusId")
    public Mono getEmployee(@RequestParam("cusId") String cusId) {

        return employeeRepository.findEmployeeByCustomerId(cusId);
    }

    /**
     * https://www.baeldung.com/java-stream-reduce
     * 大多数项目返回给调用非的数据结构都是统一的，为了实现这个，可将查询的数据列出Flux转换成Mono，然后统一处理后返给调用非
     * 注：Flux方式返回数据如果是多条，在调用非看来，相当于调用了多次单条查询
     *
     * @param ids
     * @return
     */
    @GetMapping("/getEmployees")
    public Mono getEmployee(@RequestParam("ids") List<Long> ids) {
        List<Employee> lst = new ArrayList<>();
        return employeeRepository.findAllById((Iterable<Long>) ids).reduce(lst, (v1, v2) -> {
            v1.add(v2);
            return v1;
        });
    }

    @GetMapping("/updateEmployee")
    public Mono<ResponseVO> updateEmployee(@RequestParam("id") Long id, @RequestParam("name") String name) {
        Mono<String> db = employeeRepository.findById(id)
            .map(e -> e.withName(name))
            .map(employeeRepository::save)
            .flatMap(v -> v)
            .map(e -> e.toString())
            .doOnSuccess(v -> {
                log.info("save success");
            });

        //数据库查询不到会返回Mono.empty()，如果db没有查询到任何数据，则执行后面的
        return db.switchIfEmpty(Mono.just("no find id:" + id)).map(ResponseVO::success);
    }

    @GetMapping("/getAuth")
    public Mono<ResponseVO> getAuth(@RequestParam("cusId") String cusId) {
        Mono<String> db = employeeRepository.findById(2L).map(Employee::toString);
        Mono<String> redis = reactiveRedisTemplate.opsForValue().get("hedu:employeeAuth:" + cusId);

        return Mono.zip(redis, db, (v1, v2) -> v1 + v2).map(ResponseVO::success);
    }

    @GetMapping("/getAuth1")
    public Mono getAuth1(@RequestParam("cusId") String cusId) {

        Mono<JSONObject> db = employeeRepository.findById(2L).map(JSONObject::fromObject);

        //此处redis查询结果不存在，会返回一个"null"字符串，而不是Mono.empty(),而数据库查询不到会返回Mono.empty()
        Mono<JSONObject> redis = reactiveRedisTemplate.opsForValue()
            .get("hedu:employeeAuth:" + cusId)
            .filter(v -> StringUtils.hasLength(v) && !"null".equalsIgnoreCase(v))
            .map(JSONObject::fromObject);
        //此处过滤掉null值，则redisMono返回empty，此时switchIfEmpty才会起作用。

        return redis.switchIfEmpty(db);
    }

    @GetMapping("/saveEmployee")
    public Mono saveEmployee(@RequestParam("id") Long id, @RequestParam("name") String name) {

        return userService.save(id, name);

    }

    @PostMapping("/saveUserJson")
    public Mono saveUserJson(ServerWebExchange serverWebExchange, @RequestBody SaveUserReq saveUserReq) {

        return userService.save(saveUserReq.getId(), saveUserReq.getName());

    }

    @PostMapping("/saveUser")
    public Mono saveUser(ServerWebExchange serverWebExchange, SaveUserReq saveUserReq) {

        return userService.save(saveUserReq.getId(), saveUserReq.getName());

    }

    @GetMapping("/flux")
    public Flux<Object> getMUser(ServerWebExchange serverWebExchange, @RequestParam("num") Integer num) {
        Object[] arrStr = IntStream.range(0, num).boxed().toArray();
        return Flux.fromArray(arrStr);

    }

    @GetMapping("/mono")
    public Mono getMoUser(ServerWebExchange serverWebExchange, @RequestParam("num") Integer num) {
        Object[] arrStr = IntStream.range(0, num).boxed().toArray();
        return Flux.fromArray(arrStr).buffer().single();

    }

    @GetMapping(value ="/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Object> getSseUser(ServerWebExchange serverWebExchange, @RequestParam("num") Integer num) {
        Object[] arrStr = IntStream.range(0, num).mapToObj(String::valueOf).toArray();
        return Flux.fromArray(arrStr);

    }
}
