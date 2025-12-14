package com.example.dilab.scope;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
public class ScopeTestRunner implements CommandLineRunner {

    private final SingletonCounter singleton1;
    private final SingletonCounter singleton2;
    private final ObjectFactory<PrototypeCounter> prototypeFactory;

    public ScopeTestRunner(
            SingletonCounter singleton1,
            SingletonCounter singleton2,
            ObjectFactory<PrototypeCounter> prototypeFactory) {
        this.singleton1 = singleton1;
        this.singleton2 = singleton2;
        this.prototypeFactory = prototypeFactory;
    }

    @Override
    public void run(String... args) {
        System.out.println("\n=== Scope Demo ===");

        // Singleton - same instance
        System.out.println("\nSingleton scope:");
        System.out.println("  singleton1: " + singleton1.getInstanceId() +
                          " count=" + singleton1.increment());
        System.out.println("  singleton2: " + singleton2.getInstanceId() +
                          " count=" + singleton2.increment());
        System.out.println("  Same instance? " + (singleton1 == singleton2));

        // Prototype - new instance each time
        System.out.println("\nPrototype scope:");
        PrototypeCounter proto1 = prototypeFactory.getObject();
        PrototypeCounter proto2 = prototypeFactory.getObject();
        System.out.println("  proto1: " + proto1.getInstanceId() +
                          " count=" + proto1.increment());
        System.out.println("  proto2: " + proto2.getInstanceId() +
                          " count=" + proto2.increment());
        System.out.println("  Same instance? " + (proto1 == proto2));
    }
}
