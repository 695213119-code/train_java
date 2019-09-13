package com.train.commonservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * 此服务为公共服务
 *
 * @author zhangLei
 * @serial 2019/09/13
 */
@SpringBootApplication
@EnableEurekaClient
public class CommonServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommonServiceApplication.class, args);
    }

}
