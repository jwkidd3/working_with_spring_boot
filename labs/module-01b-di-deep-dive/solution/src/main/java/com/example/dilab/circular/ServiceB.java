package com.example.dilab.circular;

import org.springframework.stereotype.Service;

/**
 * Part of the circular dependency example with ServiceA.
 */
@Service
public class ServiceB {

    private final ServiceA serviceA;

    public ServiceB(ServiceA serviceA) {
        this.serviceA = serviceA;
    }

    public String getName() {
        return "ServiceB";
    }

    public String callA() {
        return "B calling -> " + serviceA.getName();
    }
}
