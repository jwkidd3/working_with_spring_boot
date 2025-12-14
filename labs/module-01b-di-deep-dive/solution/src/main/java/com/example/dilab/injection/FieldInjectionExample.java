package com.example.dilab.injection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Demonstrates field injection - NOT recommended.
 *
 * Problems with field injection:
 * - Cannot create immutable objects (no final fields)
 * - Hard to test without Spring context
 * - Hides dependencies (not visible in constructor)
 * - Can lead to NullPointerException if used outside Spring
 */
@Service
public class FieldInjectionExample {

    @Autowired
    private MessageService messageService;

    public String getMessage() {
        return messageService.getMessage();
    }
}
