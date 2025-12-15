package com.example.configlab.service;

import com.example.configlab.config.NotificationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("prod | staging | default")
public class ProdEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(ProdEmailSender.class);
    private final NotificationProperties properties;

    public ProdEmailSender(NotificationProperties properties) {
        this.properties = properties;
    }

    @Override
    public String send(String to, String subject, String body) {
        NotificationProperties.Email email = properties.getEmail();
        log.info("Sending email via {}:{} to {}", email.getHost(), email.getPort(), to);
        return String.format("Email sent to %s via %s", to, email.getHost());
    }

    @Override
    public String getProviderName() {
        return "SMTP: " + properties.getEmail().getHost();
    }
}
