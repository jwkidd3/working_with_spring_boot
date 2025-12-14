package com.example.springfundamentals.service;

import com.example.springfundamentals.model.Greeting;
import org.springframework.stereotype.Service;

@Service
public class GreetingApplicationService {

    private final GreetingService greetingService;
    private final FormattingService formattingService;

    public GreetingApplicationService(GreetingService greetingService,
                                       FormattingService formattingService) {
        this.greetingService = greetingService;
        this.formattingService = formattingService;
    }

    public String getFormattedGreeting(String name) {
        Greeting greeting = greetingService.createGreeting(name);

        String formattedMessage = formattingService.formatGreeting(greeting.getMessage());
        String formattedTime = formattingService.formatTimestamp(greeting.getTimestamp());

        return String.format("%s%n[From: %s at %s]",
                formattedMessage,
                greeting.getSender(),
                formattedTime);
    }

    public String getServiceInfo() {
        return "Using: " + greetingService.getServiceName();
    }
}
