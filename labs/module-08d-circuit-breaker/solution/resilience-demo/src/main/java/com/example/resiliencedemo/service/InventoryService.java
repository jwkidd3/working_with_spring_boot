package com.example.resiliencedemo.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class InventoryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);
    private final RestTemplate restTemplate;

    public InventoryService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @CircuitBreaker(name = "inventoryService", fallbackMethod = "getInventoryFallback")
    @Retry(name = "inventoryService")
    @SuppressWarnings("unchecked")
    public Map<String, Object> getInventory(String productId) {
        logger.info("Calling inventory service for product: {}", productId);
        String url = "http://localhost:8081/api/inventory/" + productId;
        return restTemplate.getForObject(url, Map.class);
    }

    @CircuitBreaker(name = "inventoryService", fallbackMethod = "getInventoryAsyncFallback")
    @TimeLimiter(name = "inventoryService")
    @SuppressWarnings("unchecked")
    public CompletableFuture<Map<String, Object>> getInventoryAsync(String productId) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Async call to inventory service for product: {}", productId);
            String url = "http://localhost:8081/api/inventory/" + productId;
            return restTemplate.getForObject(url, Map.class);
        });
    }

    @RateLimiter(name = "inventoryService", fallbackMethod = "getInventoryRateLimitFallback")
    @SuppressWarnings("unchecked")
    public Map<String, Object> getInventoryRateLimited(String productId) {
        logger.info("Rate limited call for product: {}", productId);
        String url = "http://localhost:8081/api/inventory/" + productId;
        return restTemplate.getForObject(url, Map.class);
    }

    public Map<String, Object> getInventoryFallback(String productId, Throwable t) {
        logger.warn("Fallback triggered for product: {}. Reason: {}", productId, t.getMessage());
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("productId", productId);
        fallback.put("quantity", -1);
        fallback.put("warehouse", "UNKNOWN");
        fallback.put("status", "SERVICE_UNAVAILABLE");
        fallback.put("fallback", true);
        fallback.put("error", t.getMessage());
        return fallback;
    }

    public CompletableFuture<Map<String, Object>> getInventoryAsyncFallback(String productId, Throwable t) {
        return CompletableFuture.completedFuture(getInventoryFallback(productId, t));
    }

    public Map<String, Object> getInventoryRateLimitFallback(String productId, Throwable t) {
        logger.warn("Rate limit exceeded for product: {}", productId);
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("productId", productId);
        fallback.put("status", "RATE_LIMITED");
        fallback.put("message", "Too many requests. Please try again later.");
        return fallback;
    }
}
