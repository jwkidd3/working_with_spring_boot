package com.example.dilab.qualifier;

import org.springframework.stereotype.Service;

@Service("pushNotifier")  // Custom bean name
public class PushNotificationService implements NotificationService {

    @Override
    public void send(String message) {
        System.out.println("PUSH: " + message);
    }

    @Override
    public String getType() {
        return "Push";
    }
}
