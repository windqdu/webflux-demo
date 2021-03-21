package com.ljt.webflux.webfluxdemo.entity;

import lombok.Builder;
import lombok.Data;
import lombok.With;
import org.springframework.data.annotation.Id;

/**
 * @author lijuntao1
 * @date 2020/12/26 10:03
 */
@Data
@With
@Builder
public class Employee {
    @Id
    private Long id;
    private String customerId;
    private String name;

}
