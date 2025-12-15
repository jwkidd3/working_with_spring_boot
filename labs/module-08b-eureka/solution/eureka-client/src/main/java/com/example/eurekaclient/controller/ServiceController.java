package com.example.eurekaclient.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ServiceController {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${server.port}")
    private String serverPort;

    @GetMapping("/info")
    public Map<String, String> getServiceInfo() {
        Map<String, String> info = new HashMap<>();
        info.put("serviceName", applicationName);
        info.put("port", serverPort);
        info.put("status", "UP");
        return info;
    }

    @GetMapping("/hello")
    public Map<String, String> sayHello() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Hello from " + applicationName + " on port " + serverPort);
        return response;
    }
}
