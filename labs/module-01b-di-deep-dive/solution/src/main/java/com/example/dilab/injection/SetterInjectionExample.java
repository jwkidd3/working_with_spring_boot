package com.example.dilab.injection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Demonstrates setter injection - OK for optional dependencies.
 *
 * When to use setter injection:
 * - Optional dependencies
 * - Dependencies that can change at runtime
 * - Circular dependency resolution (use sparingly)
 */
@Service
public class SetterInjectionExample {

    private MessageService messageService;

    @Autowired
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    public String getMessage() {
        return messageService != null ? messageService.getMessage() : "No service";
    }
}
