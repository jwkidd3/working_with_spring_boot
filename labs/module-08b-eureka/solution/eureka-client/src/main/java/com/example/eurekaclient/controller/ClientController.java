package com.example.eurekaclient.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/client")
public class ClientController {

    private final RestTemplate restTemplate;

    public ClientController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/call")
    @SuppressWarnings("unchecked")
    public Map<String, String> callService() {
        // Use service name instead of host:port
        String url = "http://eureka-client/api/hello";
        return restTemplate.getForObject(url, Map.class);
    }
}
