package com.example.springfundamentals.service;

import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class FormattingService {

    private final DateTimeFormatter formatter = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    public String formatTimestamp(long timestamp) {
        return formatter.format(Instant.ofEpochMilli(timestamp));
    }

    public String formatGreeting(String message) {
        return ">>> " + message.toUpperCase() + " <<<";
    }
}
