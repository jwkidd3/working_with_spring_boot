package com.example.dilab.lifecycle;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Demonstrates InitializingBean and DisposableBean interfaces.
 */
@Component
public class CacheManager implements InitializingBean, DisposableBean {

    private final Map<String, Object> cache = new HashMap<>();
    private boolean initialized = false;

    public CacheManager() {
        System.out.println("1. CacheManager: Constructor called");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("2. CacheManager: afterPropertiesSet - Warming up cache...");
        // Pre-populate cache with default values
        cache.put("config.timeout", 30000);
        cache.put("config.maxRetries", 3);
        initialized = true;
        System.out.println("   Cache warmed up with " + cache.size() + " entries");
    }

    @Override
    public void destroy() throws Exception {
        System.out.println("CacheManager: destroy - Clearing cache...");
        cache.clear();
        initialized = false;
        System.out.println("   Cache cleared!");
    }

    public void put(String key, Object value) {
        cache.put(key, value);
    }

    public Object get(String key) {
        return cache.get(key);
    }

    public boolean isInitialized() {
        return initialized;
    }
}
