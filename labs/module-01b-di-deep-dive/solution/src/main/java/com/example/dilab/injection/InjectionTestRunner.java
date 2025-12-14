package com.example.dilab.injection;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class InjectionTestRunner implements CommandLineRunner {

    private final FieldInjectionExample fieldExample;
    private final SetterInjectionExample setterExample;
    private final ConstructorInjectionExample constructorExample;

    public InjectionTestRunner(
            FieldInjectionExample fieldExample,
            SetterInjectionExample setterExample,
            ConstructorInjectionExample constructorExample) {
        this.fieldExample = fieldExample;
        this.setterExample = setterExample;
        this.constructorExample = constructorExample;
    }

    @Override
    public void run(String... args) {
        System.out.println("\n=== Injection Types Demo ===");
        System.out.println("Field:       " + fieldExample.getMessage());
        System.out.println("Setter:      " + setterExample.getMessage());
        System.out.println("Constructor: " + constructorExample.getMessage());
    }
}
