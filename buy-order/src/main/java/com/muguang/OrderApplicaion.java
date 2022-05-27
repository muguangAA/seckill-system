package com.muguang;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class OrderApplicaion {
    public static void main(String[] args) {
        SpringApplication.run(OrderApplicaion.class, args);
    }
}
