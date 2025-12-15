package com.example.feignclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// TODO: Add @EnableFeignClients annotation

@SpringBootApplication
public class FeignClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(FeignClientApplication.class, args);
    }
}
