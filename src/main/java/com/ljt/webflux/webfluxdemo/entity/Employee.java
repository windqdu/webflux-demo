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
    private String uid;
    private String deptNumber;
    private String mobile;
    private String employeeNumber;
    private String mail;
    private String telephoneNumber;
    private String gender;
    private String idNumber;
    private String birthday;
    private Long titleLevelId;
    private String entryTime;
    private Long expiredTime;
    private String school;
    private String education;
    private Short accountType;
    private String directManagerId;
    private String mentorId;
    private Short lockFlag;
    private Integer creditHours;
    private Integer credit;
    private Integer points;
    private Long createTime;
    private Long updateTime;
    private String picUrl;
    private Integer designateCredit;
    private String shrPostCode;
    private String shrPostName;
    private Long postInfoId;
    private String hashCodeMd5;
    private String cisCode;
    private String userType;
    private String shopCisCode;

}
