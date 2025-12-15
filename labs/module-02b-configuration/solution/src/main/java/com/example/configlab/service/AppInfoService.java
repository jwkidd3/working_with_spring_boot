package com.example.configlab.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AppInfoService {

    @Value("${spring.application.name}")
    private String appName;

    @Value("${app.version}")
    private String version;

    @Value("${app.description}")
    private String description;

    @Value("${app.notification.enabled}")
    private boolean notificationEnabled;

    @Value("${app.notification.max-retries}")
    private int maxRetries;

    @Value("${app.notification.sender:noreply@example.com}")
    private String defaultSender;

    public String getAppInfo() {
        return String.format("%s v%s - %s", appName, version, description);
    }

    public boolean isNotificationEnabled() {
        return notificationEnabled;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public String getDefaultSender() {
        return defaultSender;
    }
}
