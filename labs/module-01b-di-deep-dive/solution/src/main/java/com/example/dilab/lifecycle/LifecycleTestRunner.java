package com.example.dilab.lifecycle;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(4)
public class LifecycleTestRunner implements CommandLineRunner {

    private final DatabaseConnection dbConnection;
    private final CacheManager cacheManager;
    private final ExternalService externalService;

    public LifecycleTestRunner(
            DatabaseConnection dbConnection,
            CacheManager cacheManager,
            ExternalService externalService) {
        this.dbConnection = dbConnection;
        this.cacheManager = cacheManager;
        this.externalService = externalService;
    }

    @Override
    public void run(String... args) {
        System.out.println("\n=== Lifecycle Demo ===");
        System.out.println("All beans initialized!");
        System.out.println("  DatabaseConnection connected: " + dbConnection.isConnected());
        System.out.println("  CacheManager initialized: " + cacheManager.isInitialized());
        System.out.println("  ExternalService running: " + externalService.isRunning());

        // Use the beans
        dbConnection.executeQuery("SELECT * FROM users");
        System.out.println("  Cache timeout: " + cacheManager.get("config.timeout"));

        System.out.println("\n(Shutdown hooks will run @PreDestroy methods on application exit)");
    }
}
