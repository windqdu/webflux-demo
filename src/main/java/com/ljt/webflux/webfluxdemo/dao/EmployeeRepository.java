package com.ljt.webflux.webfluxdemo.dao;

import com.ljt.webflux.webfluxdemo.entity.Employee;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

/**
 * @author lijuntao1
 * @date 2020/12/26 10:05
 */
public interface EmployeeRepository extends R2dbcRepository<Employee, Long> {

//    @Query("select * from employee where customer_id = $1")
    Mono<Employee> findEmployeeByCustomerId(String cusId);

}
