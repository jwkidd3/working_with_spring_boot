package com.example.springfundamentals.service;

import com.example.springfundamentals.model.Greeting;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("!fancy")
public class SimpleGreetingService implements GreetingService {

    @Override
    public Greeting createGreeting(String name) {
        String message = "Hello, " + name + "! Welcome to Spring Boot.";
        return new Greeting(message, "SimpleGreetingService");
    }

    @Override
    public String getServiceName() {
        return "Simple Greeting Service";
    }
}
