package com.example.resiliencedemo.controller;

import com.example.resiliencedemo.service.InventoryService;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final InventoryService inventoryService;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public ProductController(InventoryService inventoryService,
                           CircuitBreakerRegistry circuitBreakerRegistry) {
        this.inventoryService = inventoryService;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    @GetMapping("/{productId}/inventory")
    public Map<String, Object> getProductInventory(@PathVariable String productId) {
        return inventoryService.getInventory(productId);
    }

    @GetMapping("/{productId}/inventory/async")
    public CompletableFuture<Map<String, Object>> getProductInventoryAsync(@PathVariable String productId) {
        return inventoryService.getInventoryAsync(productId);
    }

    @GetMapping("/{productId}/inventory/rate-limited")
    public Map<String, Object> getProductInventoryRateLimited(@PathVariable String productId) {
        return inventoryService.getInventoryRateLimited(productId);
    }

    @GetMapping("/circuit-breaker/status")
    public Map<String, Object> getCircuitBreakerStatus() {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("inventoryService");
        CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();

        Map<String, Object> status = new HashMap<>();
        status.put("state", circuitBreaker.getState().toString());
        status.put("failureRate", metrics.getFailureRate());
        status.put("slowCallRate", metrics.getSlowCallRate());
        status.put("numberOfBufferedCalls", metrics.getNumberOfBufferedCalls());
        status.put("numberOfFailedCalls", metrics.getNumberOfFailedCalls());
        status.put("numberOfSuccessfulCalls", metrics.getNumberOfSuccessfulCalls());
        status.put("numberOfSlowCalls", metrics.getNumberOfSlowCalls());
        status.put("numberOfNotPermittedCalls", metrics.getNumberOfNotPermittedCalls());
        return status;
    }

    @PostMapping("/circuit-breaker/reset")
    public Map<String, String> resetCircuitBreaker() {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("inventoryService");
        circuitBreaker.reset();
        Map<String, String> response = new HashMap<>();
        response.put("status", "Circuit breaker reset");
        response.put("newState", circuitBreaker.getState().toString());
        return response;
    }
}
