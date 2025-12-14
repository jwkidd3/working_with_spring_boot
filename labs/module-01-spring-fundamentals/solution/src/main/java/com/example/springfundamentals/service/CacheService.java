package com.example.springfundamentals.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CacheService {

    private Map<String, Object> cache;
    private boolean initialized = false;

    @PostConstruct
    public void init() {
        System.out.println("=== CacheService: Initializing cache... ===");
        cache = new ConcurrentHashMap<>();

        cache.put("welcome_message", "Welcome to our application!");
        cache.put("version", "1.0.0");

        initialized = true;
        System.out.println("=== CacheService: Cache initialized with " + cache.size() + " entries ===");
    }

    @PreDestroy
    public void cleanup() {
        System.out.println("=== CacheService: Cleaning up cache... ===");
        if (cache != null) {
            System.out.println("=== CacheService: Clearing " + cache.size() + " entries ===");
            cache.clear();
        }
        System.out.println("=== CacheService: Cleanup complete ===");
    }

    public void put(String key, Object value) {
        validateInitialized();
        cache.put(key, value);
    }

    public Object get(String key) {
        validateInitialized();
        return cache.get(key);
    }

    public boolean contains(String key) {
        validateInitialized();
        return cache.containsKey(key);
    }

    public int size() {
        validateInitialized();
        return cache.size();
    }

    private void validateInitialized() {
        if (!initialized) {
            throw new IllegalStateException("CacheService not initialized!");
        }
    }
}
