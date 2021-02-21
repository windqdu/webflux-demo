package com.ljt.webflux.webfluxdemo.service;

import com.ljt.webflux.webfluxdemo.dao.EmployeeRepository;
import com.ljt.webflux.webfluxdemo.entity.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

/**
 * @author lijuntao1
 * @date 2020/12/29 10:02
 */
@Service
public class UserService {

    @Autowired
    public EmployeeRepository employeeRepository;

    /**
     * r2dbc支持注解事物
     *
     * @param id
     * @param name
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Mono<Employee> save(Long id, String name) {
        return employeeRepository.findById(id)
            .map(v -> v.withName(name))
            .map(v -> employeeRepository.save(v))
            .flatMap(v -> v)
            .map(e -> {
                if (e.getName().startsWith("1234")) {
                    //抛出异常，数据保存会回滚
                    throw new IllegalStateException("save error");
                } else {
                    return e;
                }
            });
    }
}
