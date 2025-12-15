package com.example.configlab.controller;

import com.example.configlab.config.NotificationProperties;
import com.example.configlab.service.AppInfoService;
import com.example.configlab.service.EmailSender;
import com.example.configlab.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

    private final AppInfoService appInfoService;
    private final NotificationService notificationService;
    private final NotificationProperties notificationProperties;
    private final EmailSender emailSender;

    public ConfigController(AppInfoService appInfoService,
                           NotificationService notificationService,
                           NotificationProperties notificationProperties,
                           EmailSender emailSender) {
        this.appInfoService = appInfoService;
        this.notificationService = notificationService;
        this.notificationProperties = notificationProperties;
        this.emailSender = emailSender;
    }

    @GetMapping("/info")
    public Map<String, Object> getInfo() {
        return Map.of(
            "application", appInfoService.getAppInfo(),
            "notificationEnabled", appInfoService.isNotificationEnabled(),
            "maxRetries", appInfoService.getMaxRetries(),
            "defaultSender", appInfoService.getDefaultSender()
        );
    }

    @GetMapping("/notification")
    public Map<String, Object> getNotificationConfig() {
        return Map.of(
            "enabled", notificationProperties.isEnabled(),
            "sender", notificationProperties.getSender(),
            "timeout", notificationProperties.getTimeout().toString(),
            "maxRetries", notificationProperties.getMaxRetries(),
            "emailHost", notificationProperties.getEmail().getHost(),
            "emailPort", notificationProperties.getEmail().getPort(),
            "smsEnabled", notificationProperties.getSms().isEnabled(),
            "smsProvider", notificationProperties.getSms().getProvider()
        );
    }

    @PostMapping("/notify")
    public Map<String, String> sendNotification(@RequestParam String to,
                                                 @RequestParam String message) {
        String result = notificationService.sendNotification(to, message);
        return Map.of("result", result);
    }

    @GetMapping("/email/provider")
    public Map<String, String> getEmailProvider() {
        return Map.of("provider", emailSender.getProviderName());
    }

    @PostMapping("/email/send")
    public Map<String, String> sendEmail(@RequestParam String to,
                                          @RequestParam String subject,
                                          @RequestParam String body) {
        String result = emailSender.send(to, subject, body);
        return Map.of("result", result);
    }
}
