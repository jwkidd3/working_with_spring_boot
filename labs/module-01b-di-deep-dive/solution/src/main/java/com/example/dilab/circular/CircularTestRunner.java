package com.example.dilab.circular;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(5)
public class CircularTestRunner implements CommandLineRunner {

    private final ServiceA serviceA;
    private final ServiceB serviceB;

    public CircularTestRunner(ServiceA serviceA, ServiceB serviceB) {
        this.serviceA = serviceA;
        this.serviceB = serviceB;
    }

    @Override
    public void run(String... args) {
        System.out.println("\n=== Circular Dependency Demo ===");
        System.out.println("Circular dependency resolved with @Lazy!");
        System.out.println("  " + serviceA.callB());
        System.out.println("  " + serviceB.callA());
    }
}
