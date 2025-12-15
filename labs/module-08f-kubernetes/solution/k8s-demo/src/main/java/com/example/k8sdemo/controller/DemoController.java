package com.example.k8sdemo.controller;

import com.example.k8sdemo.config.AppProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DemoController {

    private final AppProperties appProperties;

    @Value("${app.secret.api-key:not-set}")
    private String apiKey;

    @Value("${app.secret.db-password:not-set}")
    private String dbPassword;

    public DemoController(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @GetMapping("/hello")
    public Map<String, Object> hello() throws UnknownHostException {
        Map<String, Object> response = new HashMap<>();
        response.put("message", appProperties.getGreeting());
        response.put("hostname", InetAddress.getLocalHost().getHostName());
        response.put("featureEnabled", appProperties.getFeature().isEnabled());
        return response;
    }

    @GetMapping("/config")
    public Map<String, Object> getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("greeting", appProperties.getGreeting());
        config.put("featureEnabled", appProperties.getFeature().isEnabled());
        config.put("apiKeyConfigured", !apiKey.equals("not-set"));
        config.put("dbPasswordConfigured", !dbPassword.equals("not-set"));
        return config;
    }

    @GetMapping("/health/custom")
    public Map<String, String> customHealth() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("application", "k8s-demo");
        return health;
    }
}
