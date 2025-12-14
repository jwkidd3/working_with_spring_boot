package com.example.dilab.qualifier;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class QualifierTestRunner implements CommandLineRunner {

    private final NotificationManager manager;

    public QualifierTestRunner(NotificationManager manager) {
        this.manager = manager;
    }

    @Override
    public void run(String... args) {
        System.out.println("\n=== Qualifier Demo ===");
        manager.sendViaDefault("Hello via default!");
        System.out.println();
        manager.sendViaEmail("Hello via email!");
        System.out.println();
        manager.sendViaPush("Hello via push!");
        System.out.println();
        manager.sendToAll("Broadcast message!");
    }
}
