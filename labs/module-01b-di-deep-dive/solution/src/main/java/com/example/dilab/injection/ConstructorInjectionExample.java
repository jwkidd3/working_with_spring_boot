package com.example.dilab.injection;

import org.springframework.stereotype.Service;

/**
 * Demonstrates constructor injection - RECOMMENDED approach.
 *
 * Benefits of constructor injection:
 * - Immutable dependencies (final fields)
 * - Required dependencies are explicit
 * - Easy to test (just pass mocks to constructor)
 * - Fails fast if dependency is missing
 *
 * Note: @Autowired is optional for single constructor (Spring 4.3+)
 */
@Service
public class ConstructorInjectionExample {

    private final MessageService messageService;

    public ConstructorInjectionExample(MessageService messageService) {
        this.messageService = messageService;
    }

    public String getMessage() {
        return messageService.getMessage();
    }
}
