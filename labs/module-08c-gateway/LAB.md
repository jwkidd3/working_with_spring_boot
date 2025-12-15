# Lab 8c: API Gateway with Spring Cloud Gateway

## Objectives
- Set up Spring Cloud Gateway as an API Gateway
- Configure routes to backend services
- Implement path-based and header-based routing
- Add filters for request/response modification
- Integrate with Eureka for dynamic routing
- Implement rate limiting and circuit breakers

## Prerequisites
- Completed Lab 8b (Eureka) or understanding of service discovery
- Java 17 or higher
- Maven 3.6+

## Duration
45-60 minutes

---

## Part 1: Setting Up the API Gateway

### Step 1.1: Open the Starter Project

Open the starter project located in:
```
labs/module-08c-gateway/starter/api-gateway/
```

### Step 1.2: Review the Project Structure

The starter project includes:
- `pom.xml` - Maven configuration with Spring Cloud Gateway dependencies
- `GatewayApplication.java` - Main application class
- `application.yml` - Configuration file (needs routes to be configured)

### Step 1.3: Configure Basic Routes

Open `src/main/resources/application.yml` and add route configuration:

```yaml
server:
  port: 8080

spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: http://localhost:8081
          predicates:
            - Path=/api/users/**
        - id: order-service
          uri: http://localhost:8082
          predicates:
            - Path=/api/orders/**

management:
  endpoints:
    web:
      exposure:
        include: health,info,gateway
  endpoint:
    gateway:
      enabled: true
```

**Configuration Explained:**
- `routes` - List of route definitions
- `id` - Unique identifier for the route
- `uri` - Target service URL
- `predicates` - Conditions that must match for the route to be used

---

## Part 2: Creating Backend Services

### Step 2.1: Set Up User Service

Open `starter/user-service/` and configure the application.

Create `src/main/resources/application.yml`:

```yaml
server:
  port: 8081

spring:
  application:
    name: user-service
```

Create `src/main/java/com/example/userservice/controller/UserController.java`:

```java
package com.example.userservice.controller;

import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final Map<Long, Map<String, Object>> users = new HashMap<>();
    private long nextId = 1;

    public UserController() {
        // Add sample data
        addUser("John Doe", "john@example.com");
        addUser("Jane Smith", "jane@example.com");
    }

    private void addUser(String name, String email) {
        Map<String, Object> user = new HashMap<>();
        user.put("id", nextId);
        user.put("name", name);
        user.put("email", email);
        users.put(nextId++, user);
    }

    @GetMapping
    public List<Map<String, Object>> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @GetMapping("/{id}")
    public Map<String, Object> getUser(@PathVariable Long id) {
        Map<String, Object> user = users.get(id);
        if (user == null) {
            throw new RuntimeException("User not found: " + id);
        }
        return user;
    }

    @PostMapping
    public Map<String, Object> createUser(@RequestBody Map<String, String> request) {
        Map<String, Object> user = new HashMap<>();
        user.put("id", nextId);
        user.put("name", request.get("name"));
        user.put("email", request.get("email"));
        users.put(nextId++, user);
        return user;
    }
}
```

### Step 2.2: Set Up Order Service

Open `starter/order-service/` and configure the application.

Create `src/main/resources/application.yml`:

```yaml
server:
  port: 8082

spring:
  application:
    name: order-service
```

Create `src/main/java/com/example/orderservice/controller/OrderController.java`:

```java
package com.example.orderservice.controller;

import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final Map<Long, Map<String, Object>> orders = new HashMap<>();
    private long nextId = 1;

    public OrderController() {
        // Add sample data
        addOrder(1L, "Laptop", 999.99);
        addOrder(2L, "Phone", 599.99);
    }

    private void addOrder(Long userId, String product, Double amount) {
        Map<String, Object> order = new HashMap<>();
        order.put("id", nextId);
        order.put("userId", userId);
        order.put("product", product);
        order.put("amount", amount);
        order.put("status", "PENDING");
        orders.put(nextId++, order);
    }

    @GetMapping
    public List<Map<String, Object>> getAllOrders() {
        return new ArrayList<>(orders.values());
    }

    @GetMapping("/{id}")
    public Map<String, Object> getOrder(@PathVariable Long id) {
        Map<String, Object> order = orders.get(id);
        if (order == null) {
            throw new RuntimeException("Order not found: " + id);
        }
        return order;
    }

    @GetMapping("/user/{userId}")
    public List<Map<String, Object>> getOrdersByUser(@PathVariable Long userId) {
        return orders.values().stream()
                .filter(o -> userId.equals(o.get("userId")))
                .toList();
    }

    @PostMapping
    public Map<String, Object> createOrder(@RequestBody Map<String, Object> request) {
        Map<String, Object> order = new HashMap<>();
        order.put("id", nextId);
        order.put("userId", request.get("userId"));
        order.put("product", request.get("product"));
        order.put("amount", request.get("amount"));
        order.put("status", "PENDING");
        orders.put(nextId++, order);
        return order;
    }
}
```

### Step 2.3: Start All Services

Start the services in separate terminals:

```bash
# Terminal 1 - User Service
cd starter/user-service
mvn spring-boot:run

# Terminal 2 - Order Service
cd starter/order-service
mvn spring-boot:run

# Terminal 3 - API Gateway
cd starter/api-gateway
mvn spring-boot:run
```

### Step 2.4: Test the Gateway

Test routing through the gateway:

```bash
# Access users through gateway
curl http://localhost:8080/api/users

# Access orders through gateway
curl http://localhost:8080/api/orders

# Access specific user
curl http://localhost:8080/api/users/1

# Access specific order
curl http://localhost:8080/api/orders/1
```

---

## Part 3: Adding Filters

### Step 3.1: Add Built-in Filters

Update `application.yml` to add filters:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: http://localhost:8081
          predicates:
            - Path=/api/users/**
          filters:
            - AddRequestHeader=X-Request-Source, API-Gateway
            - AddResponseHeader=X-Response-Time, ${time}
        - id: order-service
          uri: http://localhost:8082
          predicates:
            - Path=/api/orders/**
          filters:
            - AddRequestHeader=X-Request-Source, API-Gateway
            - RewritePath=/api/orders/(?<segment>.*), /api/orders/${segment}
```

### Step 3.2: Create a Custom Filter

Create `src/main/java/com/example/gateway/filter/LoggingFilter.java`:

```java
package com.example.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();
        String method = exchange.getRequest().getMethod().toString();

        logger.info("Incoming request: {} {}", method, path);

        long startTime = System.currentTimeMillis();

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long duration = System.currentTimeMillis() - startTime;
            logger.info("Request completed: {} {} - {} ms", method, path, duration);
        }));
    }

    @Override
    public int getOrder() {
        return -1; // Execute first
    }
}
```

### Step 3.3: Create a Request Timing Filter Factory

Create `src/main/java/com/example/gateway/filter/TimingGatewayFilterFactory.java`:

```java
package com.example.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class TimingGatewayFilterFactory extends AbstractGatewayFilterFactory<TimingGatewayFilterFactory.Config> {

    private static final Logger logger = LoggerFactory.getLogger(TimingGatewayFilterFactory.class);

    public TimingGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            long startTime = System.currentTimeMillis();

            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                long duration = System.currentTimeMillis() - startTime;
                if (config.isEnabled()) {
                    exchange.getResponse().getHeaders()
                            .add("X-Response-Time-Ms", String.valueOf(duration));
                }
            }));
        };
    }

    public static class Config {
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
```

---

## Part 4: Integrating with Eureka

### Step 4.1: Add Eureka Client Dependency

The `pom.xml` already includes the Eureka client dependency.

### Step 4.2: Configure Eureka Integration

Update `application.yml` to use Eureka for service discovery:

```yaml
server:
  port: 8080

spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - AddRequestHeader=X-Request-Source, API-Gateway
        - id: order-service
          uri: lb://order-service
          predicates:
            - Path=/api/orders/**
          filters:
            - AddRequestHeader=X-Request-Source, API-Gateway

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/

management:
  endpoints:
    web:
      exposure:
        include: health,info,gateway
```

**Key Changes:**
- `uri: lb://user-service` - Uses load-balanced URI with service name
- `discovery.locator.enabled: true` - Auto-creates routes for discovered services

### Step 4.3: Update Backend Services for Eureka

Add Eureka client to both user-service and order-service `application.yml`:

```yaml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
```

### Step 4.4: Test with Eureka

1. Start Eureka Server (from Lab 8b)
2. Start user-service and order-service
3. Start api-gateway
4. Verify all services register with Eureka at `http://localhost:8761`
5. Test routing through the gateway

---

## Part 5: Advanced Routing

### Step 5.1: Header-Based Routing

Add a route that matches based on headers:

```yaml
routes:
  - id: user-service-v2
    uri: lb://user-service-v2
    predicates:
      - Path=/api/users/**
      - Header=X-API-Version, v2
    filters:
      - AddRequestHeader=X-Request-Source, API-Gateway
  - id: user-service
    uri: lb://user-service
    predicates:
      - Path=/api/users/**
    filters:
      - AddRequestHeader=X-Request-Source, API-Gateway
```

### Step 5.2: Query Parameter Routing

```yaml
routes:
  - id: order-service-premium
    uri: lb://order-service-premium
    predicates:
      - Path=/api/orders/**
      - Query=tier, premium
```

### Step 5.3: Weight-Based Routing (Canary Deployments)

```yaml
routes:
  - id: user-service-canary
    uri: lb://user-service-v2
    predicates:
      - Path=/api/users/**
      - Weight=group1, 10
  - id: user-service-stable
    uri: lb://user-service
    predicates:
      - Path=/api/users/**
      - Weight=group1, 90
```

---

## Part 6: Rate Limiting (Optional)

### Step 6.1: Add Redis Dependency

Add to `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis-reactive</artifactId>
</dependency>
```

### Step 6.2: Configure Rate Limiter

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10
                redis-rate-limiter.burstCapacity: 20
                key-resolver: "#{@userKeyResolver}"
```

### Step 6.3: Create Key Resolver

```java
package com.example.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimiterConfig {

    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> Mono.just(
            exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
        );
    }
}
```

---

## Exercises

### Exercise 1: Add Circuit Breaker
Configure a circuit breaker for the user-service route:

```yaml
filters:
  - name: CircuitBreaker
    args:
      name: userServiceCircuitBreaker
      fallbackUri: forward:/fallback/users
```

Create a fallback controller that returns a default response.

### Exercise 2: Request Transformation
Create a filter that transforms request bodies before forwarding to backend services.

### Exercise 3: Authentication Filter
Create a global filter that validates JWT tokens before allowing requests through.

---

## Summary

In this lab, you learned:
- How to set up Spring Cloud Gateway as an API Gateway
- How to configure routes with predicates and filters
- How to create custom global and route-specific filters
- How to integrate with Eureka for dynamic service discovery
- How to implement advanced routing patterns (header-based, weight-based)
- How to add rate limiting for API protection

## Key Concepts

| Concept | Description |
|---------|-------------|
| Route | A mapping between a predicate and a target URI |
| Predicate | A condition that must match for the route to apply |
| Filter | A component that modifies requests or responses |
| Global Filter | A filter that applies to all routes |
| Load Balancer | Distributes requests across service instances |
| Rate Limiter | Controls the rate of incoming requests |

## Common Predicates

| Predicate | Example | Description |
|-----------|---------|-------------|
| Path | `Path=/api/**` | Match by path pattern |
| Method | `Method=GET,POST` | Match by HTTP method |
| Header | `Header=X-Request-Id, \d+` | Match by header value |
| Query | `Query=name, value` | Match by query parameter |
| Host | `Host=**.example.com` | Match by host header |
| Weight | `Weight=group, 80` | Route percentage of traffic |

## Common Filters

| Filter | Description |
|--------|-------------|
| AddRequestHeader | Add header to downstream request |
| AddResponseHeader | Add header to response |
| RewritePath | Rewrite the request path |
| StripPrefix | Remove path prefix |
| CircuitBreaker | Add circuit breaker pattern |
| RequestRateLimiter | Limit request rate |
| Retry | Retry failed requests |

## Next Steps
- Explore Spring Cloud Circuit Breaker with Resilience4j
- Learn about distributed tracing with Micrometer Tracing
- Investigate API security with Spring Security and OAuth2
