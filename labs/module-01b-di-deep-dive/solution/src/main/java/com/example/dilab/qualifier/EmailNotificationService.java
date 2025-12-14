package com.example.dilab.qualifier;

import org.springframework.stereotype.Service;

@Service
@NotificationType("email")
public class EmailNotificationService implements NotificationService {

    @Override
    public void send(String message) {
        System.out.println("EMAIL: " + message);
    }

    @Override
    public String getType() {
        return "Email";
    }
}
