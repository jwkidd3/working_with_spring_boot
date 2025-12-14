package com.example.dilab.lifecycle;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

/**
 * Demonstrates @PostConstruct and @PreDestroy lifecycle callbacks.
 */
@Component
public class DatabaseConnection {

    private boolean connected = false;

    public DatabaseConnection() {
        System.out.println("1. DatabaseConnection: Constructor called");
    }

    @PostConstruct
    public void init() {
        System.out.println("2. DatabaseConnection: @PostConstruct - Opening connection...");
        // Simulate connection setup
        this.connected = true;
        System.out.println("   Connection established!");
    }

    @PreDestroy
    public void cleanup() {
        System.out.println("DatabaseConnection: @PreDestroy - Closing connection...");
        // Simulate connection cleanup
        this.connected = false;
        System.out.println("   Connection closed!");
    }

    public boolean isConnected() {
        return connected;
    }

    public void executeQuery(String query) {
        if (!connected) {
            throw new IllegalStateException("Not connected!");
        }
        System.out.println("Executing query: " + query);
    }
}
