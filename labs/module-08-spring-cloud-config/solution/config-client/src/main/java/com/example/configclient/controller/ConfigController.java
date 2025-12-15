package com.example.configclient.controller;

import com.example.configclient.config.AppProperties;
import com.example.configclient.config.FeatureProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/config")
@RefreshScope
public class ConfigController {

    private final AppProperties appProperties;
    private final FeatureProperties featureProperties;

    @Value("${app.default-message:No message configured}")
    private String defaultMessage;

    public ConfigController(AppProperties appProperties, FeatureProperties featureProperties) {
        this.appProperties = appProperties;
        this.featureProperties = featureProperties;
    }

    @GetMapping("/app")
    public AppProperties getAppConfig() {
        return appProperties;
    }

    @GetMapping("/features")
    public FeatureProperties getFeatures() {
        return featureProperties;
    }

    @GetMapping("/message")
    public Map<String, String> getMessage() {
        Map<String, String> response = new HashMap<>();
        response.put("message", defaultMessage);
        return response;
    }

    @GetMapping("/all")
    public Map<String, Object> getAllConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("app", appProperties);
        config.put("features", featureProperties);
        config.put("defaultMessage", defaultMessage);
        return config;
    }
}
