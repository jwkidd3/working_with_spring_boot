package com.example.eurekaclient.controller;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/discovery")
public class DiscoveryController {

    private final DiscoveryClient discoveryClient;

    public DiscoveryController(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @GetMapping("/services")
    public List<String> getServices() {
        return discoveryClient.getServices();
    }

    @GetMapping("/services/{serviceName}")
    public List<Map<String, Object>> getServiceInstances(@PathVariable String serviceName) {
        return discoveryClient.getInstances(serviceName).stream()
                .map(this::mapInstance)
                .collect(Collectors.toList());
    }

    private Map<String, Object> mapInstance(ServiceInstance instance) {
        Map<String, Object> map = new HashMap<>();
        map.put("serviceId", instance.getServiceId());
        map.put("host", instance.getHost());
        map.put("port", instance.getPort());
        map.put("uri", instance.getUri().toString());
        map.put("metadata", instance.getMetadata());
        return map;
    }
}
