package com.example.dilab.scope;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Singleton scope (default) - one instance per Spring container.
 */
@Component
public class SingletonCounter {

    private final AtomicInteger count = new AtomicInteger(0);
    private final String instanceId;

    public SingletonCounter() {
        this.instanceId = "Singleton-" + System.identityHashCode(this);
        System.out.println("SingletonCounter created: " + instanceId);
    }

    public int increment() {
        return count.incrementAndGet();
    }

    public String getInstanceId() {
        return instanceId;
    }
}
