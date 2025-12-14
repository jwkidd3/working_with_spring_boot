package com.example.dilab.qualifier;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationManager {

    private final NotificationService primaryService;      // Gets @Primary (SMS)
    private final NotificationService emailService;        // Gets specific impl
    private final NotificationService pushService;         // Gets by bean name
    private final List<NotificationService> allServices;   // Gets ALL implementations

    public NotificationManager(
            NotificationService primaryService,
            @NotificationType("email") NotificationService emailService,
            @Qualifier("pushNotifier") NotificationService pushService,
            List<NotificationService> allServices) {
        this.primaryService = primaryService;
        this.emailService = emailService;
        this.pushService = pushService;
        this.allServices = allServices;
    }

    public void sendViaDefault(String message) {
        System.out.println("Using default (@Primary): " + primaryService.getType());
        primaryService.send(message);
    }

    public void sendViaEmail(String message) {
        System.out.println("Using @NotificationType(\"email\"):");
        emailService.send(message);
    }

    public void sendViaPush(String message) {
        System.out.println("Using @Qualifier with bean name:");
        pushService.send(message);
    }

    public void sendToAll(String message) {
        System.out.println("Sending to ALL implementations:");
        allServices.forEach(service -> service.send(message));
    }
}
