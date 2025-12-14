package com.example.dilab.qualifier;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary  // This will be selected by default when no qualifier is specified
public class SmsNotificationService implements NotificationService {

    @Override
    public void send(String message) {
        System.out.println("SMS: " + message);
    }

    @Override
    public String getType() {
        return "SMS";
    }
}
