package com.example.configlab.service;

import com.example.configlab.config.NotificationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final NotificationProperties properties;

    public NotificationService(NotificationProperties properties) {
        this.properties = properties;
        log.info("NotificationService initialized with sender: {}", properties.getSender());
        log.info("Email host: {}, port: {}",
            properties.getEmail().getHost(),
            properties.getEmail().getPort());
    }

    public String sendNotification(String to, String message) {
        if (!properties.isEnabled()) {
            return "Notifications are disabled";
        }

        log.info("Sending notification to {} from {} (timeout: {})",
            to, properties.getSender(), properties.getTimeout());

        for (int attempt = 1; attempt <= properties.getMaxRetries(); attempt++) {
            log.info("Attempt {} of {}", attempt, properties.getMaxRetries());
            return String.format("Notification sent to %s (attempt %d)", to, attempt);
        }

        return "Failed to send notification";
    }

    public NotificationProperties.Email getEmailConfig() {
        return properties.getEmail();
    }

    public NotificationProperties.Sms getSmsConfig() {
        return properties.getSms();
    }

    public boolean isSmsEnabled() {
        return properties.getSms().isEnabled();
    }
}
