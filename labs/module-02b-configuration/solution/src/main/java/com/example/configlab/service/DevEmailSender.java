package com.example.configlab.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("dev")
public class DevEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(DevEmailSender.class);

    @Override
    public String send(String to, String subject, String body) {
        log.info("DEV MODE: Would send email to {} with subject: {}", to, subject);
        log.debug("Email body: {}", body);
        return "Email logged (dev mode - not actually sent)";
    }

    @Override
    public String getProviderName() {
        return "Development Console Logger";
    }
}
