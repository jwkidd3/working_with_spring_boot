package com.example.springfundamentals.service;

import com.example.springfundamentals.model.Greeting;

public interface GreetingService {
    Greeting createGreeting(String name);
    String getServiceName();
}
