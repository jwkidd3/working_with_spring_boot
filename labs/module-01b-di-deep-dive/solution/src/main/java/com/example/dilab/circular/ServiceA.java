package com.example.dilab.circular;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * Demonstrates circular dependency resolution using @Lazy.
 * ServiceA depends on ServiceB, and ServiceB depends on ServiceA.
 */
@Service
public class ServiceA {

    private final ServiceB serviceB;

    // @Lazy breaks the cycle by creating a proxy
    public ServiceA(@Lazy ServiceB serviceB) {
        this.serviceB = serviceB;
    }

    public String getName() {
        return "ServiceA";
    }

    public String callB() {
        return "A calling -> " + serviceB.getName();
    }
}
