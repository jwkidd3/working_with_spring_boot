package com.example.dilab.scope;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Prototype scope - new instance for each injection/request.
 */
@Component
@Scope("prototype")
public class PrototypeCounter {

    private final AtomicInteger count = new AtomicInteger(0);
    private final String instanceId;

    public PrototypeCounter() {
        this.instanceId = "Prototype-" + System.identityHashCode(this);
        System.out.println("PrototypeCounter created: " + instanceId);
    }

    public int increment() {
        return count.incrementAndGet();
    }

    public String getInstanceId() {
        return instanceId;
    }
}
