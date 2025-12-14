package com.example.springfundamentals.service;

import com.example.springfundamentals.model.Greeting;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("fancy")
public class FancyGreetingService implements GreetingService {

    private static final String[] GREETINGS = {
        "Greetings and salutations",
        "A most cordial welcome",
        "Delighted to make your acquaintance",
        "How wonderful to see you"
    };

    @Override
    public Greeting createGreeting(String name) {
        String randomGreeting = GREETINGS[(int) (Math.random() * GREETINGS.length)];
        String message = randomGreeting + ", dear " + name + "!";
        return new Greeting(message, "FancyGreetingService");
    }

    @Override
    public String getServiceName() {
        return "Fancy Greeting Service (Premium Edition)";
    }
}
