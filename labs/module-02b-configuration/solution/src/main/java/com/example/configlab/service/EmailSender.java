package com.example.configlab.service;

public interface EmailSender {
    String send(String to, String subject, String body);
    String getProviderName();
}
