# Lab 8d: Circuit Breaker with Resilience4j

## Objectives
- Understand the Circuit Breaker pattern and its importance
- Implement Circuit Breaker using Resilience4j
- Configure fallback methods for graceful degradation
- Implement retry and rate limiter patterns
- Monitor circuit breaker state with Actuator

## Prerequisites
- Completed previous Spring Cloud labs or understanding of microservices
- Java 17 or higher
- Maven 3.6+

## Duration
45-60 minutes

---

## Part 1: Understanding the Circuit Breaker Pattern

### Why Circuit Breakers?

In a microservices architecture, services depend on each other. When a downstream service fails or becomes slow:
- Requests pile up, consuming resources
- Failures cascade to other services
- The entire system can become unresponsive

The Circuit Breaker pattern prevents this by:
- **Closed State**: Normal operation, requests flow through
- **Open State**: Failures detected, requests fail fast without calling the service
- **Half-Open State**: After a timeout, allows test requests to check if service recovered

---

## Part 2: Setting Up the Project

### Step 2.1: Open the Starter Project

Open the starter project located in:
```
labs/module-08d-circuit-breaker/starter/resilience-demo/
```

### Step 2.2: Review Dependencies

The `pom.xml` includes:
- `spring-cloud-starter-circuitbreaker-resilience4j` - Circuit breaker implementation
- `spring-boot-starter-aop` - Required for annotation-based circuit breakers
- `spring-boot-starter-actuator` - For monitoring

### Step 2.3: Configure Resilience4j

Create/update `src/main/resources/application.yml`:

```yaml
server:
  port: 8080

spring:
  application:
    name: resilience-demo

resilience4j:
  circuitbreaker:
    instances:
      inventoryService:
        register-health-indicator: true
        sliding-window-size: 10
        minimum-number-of-calls: 5
        permitted-number-of-calls-in-half-open-state: 3
        wait-duration-in-open-state: 10s
        failure-rate-threshold: 50
        automatic-transition-from-open-to-half-open-enabled: true
  retry:
    instances:
      inventoryService:
        max-attempts: 3
        wait-duration: 1s
        retry-exceptions:
          - java.io.IOException
          - java.util.concurrent.TimeoutException
  timelimiter:
    instances:
      inventoryService:
        timeout-duration: 3s
  ratelimiter:
    instances:
      inventoryService:
        limit-for-period: 10
        limit-refresh-period: 1s
        timeout-duration: 0s

management:
  endpoints:
    web:
      exposure:
        include: health,circuitbreakers,circuitbreakerevents
  health:
    circuitbreakers:
      enabled: true
  endpoint:
    health:
      show-details: always
```

**Configuration Explained:**
- `sliding-window-size: 10` - Number of calls to track
- `failure-rate-threshold: 50` - Open circuit when 50% of calls fail
- `wait-duration-in-open-state: 10s` - Time before trying half-open
- `permitted-number-of-calls-in-half-open-state: 3` - Test calls in half-open

---

## Part 3: Creating the Inventory Service (Simulated External Service)

### Step 3.1: Open Inventory Service Starter

Open `starter/inventory-service/`

### Step 3.2: Create the Inventory Controller

Create `src/main/java/com/example/inventoryservice/controller/InventoryController.java`:

```java
package com.example.inventoryservice.controller;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final Random random = new Random();
    private boolean simulateFailure = false;
    private boolean simulateSlow = false;

    @GetMapping("/{productId}")
    public Map<String, Object> getInventory(@PathVariable String productId) throws InterruptedException {
        // Simulate failures for testing
        if (simulateFailure) {
            throw new RuntimeException("Inventory service is down!");
        }

        // Simulate slow responses
        if (simulateSlow) {
            Thread.sleep(5000);
        }

        Map<String, Object> inventory = new HashMap<>();
        inventory.put("productId", productId);
        inventory.put("quantity", random.nextInt(100));
        inventory.put("warehouse", "WAREHOUSE-A");
        inventory.put("status", "AVAILABLE");
        return inventory;
    }

    @PostMapping("/simulate/failure/{enable}")
    public Map<String, String> toggleFailure(@PathVariable boolean enable) {
        this.simulateFailure = enable;
        Map<String, String> response = new HashMap<>();
        response.put("failureSimulation", enable ? "enabled" : "disabled");
        return response;
    }

    @PostMapping("/simulate/slow/{enable}")
    public Map<String, String> toggleSlow(@PathVariable boolean enable) {
        this.simulateSlow = enable;
        Map<String, String> response = new HashMap<>();
        response.put("slowSimulation", enable ? "enabled" : "disabled");
        return response;
    }
}
```

### Step 3.3: Configure Inventory Service

Create `src/main/resources/application.yml`:

```yaml
server:
  port: 8081

spring:
  application:
    name: inventory-service
```

---

## Part 4: Implementing Circuit Breaker

### Step 4.1: Create Inventory Service Client

In the resilience-demo project, create `src/main/java/com/example/resiliencedemo/service/InventoryService.java`:

```java
package com.example.resiliencedemo.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
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
    public Map<String, Object> getInventory(String productId) {
        logger.info("Calling inventory service for product: {}", productId);
        String url = "http://localhost:8081/api/inventory/" + productId;
        return restTemplate.getForObject(url, Map.class);
    }

    @CircuitBreaker(name = "inventoryService", fallbackMethod = "getInventoryAsyncFallback")
    @TimeLimiter(name = "inventoryService")
    public CompletableFuture<Map<String, Object>> getInventoryAsync(String productId) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Async call to inventory service for product: {}", productId);
            String url = "http://localhost:8081/api/inventory/" + productId;
            return restTemplate.getForObject(url, Map.class);
        });
    }

    // Fallback method - must have same parameters plus Throwable
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
}
```

### Step 4.2: Create REST Controller

Create `src/main/java/com/example/resiliencedemo/controller/ProductController.java`:

```java
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
```

### Step 4.3: Configure RestTemplate Bean

Create `src/main/java/com/example/resiliencedemo/config/AppConfig.java`:

```java
package com.example.resiliencedemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

---

## Part 5: Testing the Circuit Breaker

### Step 5.1: Start Both Services

```bash
# Terminal 1 - Inventory Service
cd starter/inventory-service
mvn spring-boot:run

# Terminal 2 - Resilience Demo
cd starter/resilience-demo
mvn spring-boot:run
```

### Step 5.2: Test Normal Operation

```bash
# Should return inventory data
curl http://localhost:8080/api/products/PROD-001/inventory

# Check circuit breaker status
curl http://localhost:8080/api/products/circuit-breaker/status
```

### Step 5.3: Test Circuit Breaker Opening

```bash
# Enable failure simulation
curl -X POST http://localhost:8081/api/inventory/simulate/failure/true

# Make several requests - watch the fallback kick in
for i in {1..10}; do
  curl http://localhost:8080/api/products/PROD-001/inventory
  echo ""
done

# Check circuit breaker - should be OPEN
curl http://localhost:8080/api/products/circuit-breaker/status
```

### Step 5.4: Test Circuit Breaker Recovery

```bash
# Disable failure simulation
curl -X POST http://localhost:8081/api/inventory/simulate/failure/false

# Wait 10 seconds for half-open state, then make requests
sleep 10
curl http://localhost:8080/api/products/PROD-001/inventory

# Check status - should transition back to CLOSED
curl http://localhost:8080/api/products/circuit-breaker/status
```

### Step 5.5: Monitor with Actuator

```bash
# View circuit breaker health
curl http://localhost:8080/actuator/health

# View circuit breaker events
curl http://localhost:8080/actuator/circuitbreakerevents
```

---

## Part 6: Implementing Rate Limiter

### Step 6.1: Add Rate Limited Endpoint

Add to `InventoryService.java`:

```java
@RateLimiter(name = "inventoryService", fallbackMethod = "getInventoryRateLimitFallback")
public Map<String, Object> getInventoryRateLimited(String productId) {
    logger.info("Rate limited call for product: {}", productId);
    String url = "http://localhost:8081/api/inventory/" + productId;
    return restTemplate.getForObject(url, Map.class);
}

public Map<String, Object> getInventoryRateLimitFallback(String productId, Throwable t) {
    logger.warn("Rate limit exceeded for product: {}", productId);
    Map<String, Object> fallback = new HashMap<>();
    fallback.put("productId", productId);
    fallback.put("status", "RATE_LIMITED");
    fallback.put("message", "Too many requests. Please try again later.");
    return fallback;
}
```

Add the import:
```java
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
```

---

## Exercises

### Exercise 1: Custom Circuit Breaker Configuration
Create a second circuit breaker configuration for a "payment service" with:
- Higher failure threshold (70%)
- Longer wait duration (30s)
- Custom fallback

### Exercise 2: Bulkhead Pattern
Implement the Bulkhead pattern to limit concurrent calls:

```yaml
resilience4j:
  bulkhead:
    instances:
      inventoryService:
        max-concurrent-calls: 10
        max-wait-duration: 500ms
```

### Exercise 3: Circuit Breaker Events
Create an event listener to log circuit breaker state changes:

```java
@Component
public class CircuitBreakerEventListener {

    public CircuitBreakerEventListener(CircuitBreakerRegistry registry) {
        registry.circuitBreaker("inventoryService")
            .getEventPublisher()
            .onStateTransition(event ->
                logger.info("State transition: {}", event));
    }
}
```

---

## Summary

In this lab, you learned:
- The Circuit Breaker pattern and why it's essential for microservices
- How to implement circuit breakers with Resilience4j
- How to configure fallback methods for graceful degradation
- How to use retry, time limiter, and rate limiter patterns
- How to monitor circuit breaker state with Actuator

## Key Concepts

| Concept | Description |
|---------|-------------|
| Circuit Breaker | Prevents cascading failures by failing fast |
| Fallback | Alternative response when the circuit is open |
| Sliding Window | Number of recent calls used to calculate failure rate |
| Half-Open | Testing state to check if service recovered |
| Retry | Automatically retry failed operations |
| Rate Limiter | Limit the number of calls in a time period |
| Bulkhead | Isolate failures by limiting concurrent calls |

## Circuit Breaker States

| State | Description |
|-------|-------------|
| CLOSED | Normal operation, all calls pass through |
| OPEN | Failures detected, calls fail immediately |
| HALF_OPEN | Testing if service recovered |
| DISABLED | Circuit breaker is disabled |
| FORCED_OPEN | Manually forced open |

## Next Steps
- Explore Spring Cloud OpenFeign for declarative REST clients
- Learn about distributed tracing with Micrometer
- Investigate Spring Cloud Kubernetes for container orchestration
