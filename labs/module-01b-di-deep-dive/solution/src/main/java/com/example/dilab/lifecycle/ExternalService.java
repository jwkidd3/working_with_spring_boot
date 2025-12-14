package com.example.dilab.lifecycle;

/**
 * External service class without Spring annotations.
 * Lifecycle is managed via @Bean initMethod/destroyMethod.
 */
public class ExternalService {

    private boolean running = false;

    public ExternalService() {
        System.out.println("1. ExternalService: Constructor called");
    }

    // Custom init method - no annotations needed
    public void start() {
        System.out.println("2. ExternalService: start() - Starting service...");
        running = true;
        System.out.println("   Service started!");
    }

    // Custom destroy method - no annotations needed
    public void stop() {
        System.out.println("ExternalService: stop() - Stopping service...");
        running = false;
        System.out.println("   Service stopped!");
    }

    public boolean isRunning() {
        return running;
    }
}
