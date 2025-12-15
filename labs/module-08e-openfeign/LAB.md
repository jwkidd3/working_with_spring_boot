# Lab 8e: Declarative REST Clients with Spring Cloud OpenFeign

## Objectives
- Understand declarative REST clients and their benefits
- Implement Feign clients to call external services
- Configure Feign with Eureka for service discovery
- Add error handling and fallbacks with Feign
- Customize Feign configuration (timeouts, logging, interceptors)

## Prerequisites
- Completed previous Spring Cloud labs (especially Eureka)
- Java 17 or higher
- Maven 3.6+

## Duration
45-60 minutes

---

## Part 1: Understanding OpenFeign

### What is OpenFeign?

OpenFeign is a declarative HTTP client that makes writing web service clients easier. Instead of manually creating RestTemplate calls, you define an interface and annotate it:

**Traditional RestTemplate approach:**
```java
String url = "http://product-service/api/products/" + productId;
Product product = restTemplate.getForObject(url, Product.class);
```

**OpenFeign approach:**
```java
@FeignClient(name = "product-service")
public interface ProductClient {
    @GetMapping("/api/products/{id}")
    Product getProduct(@PathVariable Long id);
}
```

### Benefits of OpenFeign
- Declarative, interface-based approach
- Automatic integration with Eureka service discovery
- Built-in load balancing
- Easy integration with Circuit Breaker
- Customizable request/response handling

---

## Part 2: Setting Up the Product Service

### Step 2.1: Open the Product Service Starter

Open `starter/product-service/`

### Step 2.2: Create the Product Model

Create `src/main/java/com/example/productservice/model/Product.java`:

```java
package com.example.productservice.model;

import java.math.BigDecimal;

public class Product {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private Integer stockQuantity;

    public Product() {}

    public Product(Long id, String name, String description,
                   BigDecimal price, String category, Integer stockQuantity) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.stockQuantity = stockQuantity;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
}
```

### Step 2.3: Create the Product Controller

Create `src/main/java/com/example/productservice/controller/ProductController.java`:

```java
package com.example.productservice.controller;

import com.example.productservice.model.Product;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final Map<Long, Product> products = new HashMap<>();

    public ProductController() {
        products.put(1L, new Product(1L, "Laptop", "High-performance laptop",
                new BigDecimal("999.99"), "Electronics", 50));
        products.put(2L, new Product(2L, "Smartphone", "Latest smartphone",
                new BigDecimal("699.99"), "Electronics", 100));
        products.put(3L, new Product(3L, "Headphones", "Wireless headphones",
                new BigDecimal("199.99"), "Electronics", 200));
    }

    @GetMapping
    public List<Product> getAllProducts() {
        return new ArrayList<>(products.values());
    }

    @GetMapping("/{id}")
    public Product getProduct(@PathVariable Long id) {
        Product product = products.get(id);
        if (product == null) {
            throw new RuntimeException("Product not found: " + id);
        }
        return product;
    }

    @GetMapping("/category/{category}")
    public List<Product> getProductsByCategory(@PathVariable String category) {
        return products.values().stream()
                .filter(p -> p.getCategory().equalsIgnoreCase(category))
                .toList();
    }

    @PostMapping
    public Product createProduct(@RequestBody Product product) {
        long newId = products.keySet().stream().max(Long::compare).orElse(0L) + 1;
        product.setId(newId);
        products.put(newId, product);
        return product;
    }

    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable Long id, @RequestBody Product product) {
        if (!products.containsKey(id)) {
            throw new RuntimeException("Product not found: " + id);
        }
        product.setId(id);
        products.put(id, product);
        return product;
    }

    @DeleteMapping("/{id}")
    public Map<String, String> deleteProduct(@PathVariable Long id) {
        products.remove(id);
        Map<String, String> response = new HashMap<>();
        response.put("status", "deleted");
        response.put("productId", id.toString());
        return response;
    }
}
```

### Step 2.4: Configure Product Service

Create `src/main/resources/application.yml`:

```yaml
server:
  port: 8081

spring:
  application:
    name: product-service

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
```

---

## Part 3: Creating the Feign Client Application

### Step 3.1: Open the Feign Client Starter

Open `starter/feign-client/`

### Step 3.2: Enable Feign Clients

Update the main application class:

```java
package com.example.feignclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class FeignClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(FeignClientApplication.class, args);
    }
}
```

### Step 3.3: Create the Product Model (Client Side)

Create `src/main/java/com/example/feignclient/model/Product.java`:

```java
package com.example.feignclient.model;

import java.math.BigDecimal;

public class Product {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private Integer stockQuantity;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
}
```

### Step 3.4: Create the Feign Client Interface

Create `src/main/java/com/example/feignclient/client/ProductClient.java`:

```java
package com.example.feignclient.client;

import com.example.feignclient.model.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "product-service", fallback = ProductClientFallback.class)
public interface ProductClient {

    @GetMapping("/api/products")
    List<Product> getAllProducts();

    @GetMapping("/api/products/{id}")
    Product getProduct(@PathVariable("id") Long id);

    @GetMapping("/api/products/category/{category}")
    List<Product> getProductsByCategory(@PathVariable("category") String category);

    @PostMapping("/api/products")
    Product createProduct(@RequestBody Product product);

    @PutMapping("/api/products/{id}")
    Product updateProduct(@PathVariable("id") Long id, @RequestBody Product product);

    @DeleteMapping("/api/products/{id}")
    void deleteProduct(@PathVariable("id") Long id);
}
```

### Step 3.5: Create Fallback Implementation

Create `src/main/java/com/example/feignclient/client/ProductClientFallback.java`:

```java
package com.example.feignclient.client;

import com.example.feignclient.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Component
public class ProductClientFallback implements ProductClient {

    private static final Logger logger = LoggerFactory.getLogger(ProductClientFallback.class);

    @Override
    public List<Product> getAllProducts() {
        logger.warn("Fallback: getAllProducts");
        return Collections.emptyList();
    }

    @Override
    public Product getProduct(Long id) {
        logger.warn("Fallback: getProduct for id {}", id);
        Product fallback = new Product();
        fallback.setId(id);
        fallback.setName("Unknown Product");
        fallback.setDescription("Product service unavailable");
        fallback.setPrice(BigDecimal.ZERO);
        fallback.setCategory("Unknown");
        fallback.setStockQuantity(0);
        return fallback;
    }

    @Override
    public List<Product> getProductsByCategory(String category) {
        logger.warn("Fallback: getProductsByCategory for {}", category);
        return Collections.emptyList();
    }

    @Override
    public Product createProduct(Product product) {
        logger.warn("Fallback: createProduct");
        return null;
    }

    @Override
    public Product updateProduct(Long id, Product product) {
        logger.warn("Fallback: updateProduct for id {}", id);
        return null;
    }

    @Override
    public void deleteProduct(Long id) {
        logger.warn("Fallback: deleteProduct for id {}", id);
    }
}
```

### Step 3.6: Create the Store Controller

Create `src/main/java/com/example/feignclient/controller/StoreController.java`:

```java
package com.example.feignclient.controller;

import com.example.feignclient.client.ProductClient;
import com.example.feignclient.model.Product;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/store")
public class StoreController {

    private final ProductClient productClient;

    public StoreController(ProductClient productClient) {
        this.productClient = productClient;
    }

    @GetMapping("/products")
    public List<Product> getAllProducts() {
        return productClient.getAllProducts();
    }

    @GetMapping("/products/{id}")
    public Product getProduct(@PathVariable Long id) {
        return productClient.getProduct(id);
    }

    @GetMapping("/products/category/{category}")
    public List<Product> getProductsByCategory(@PathVariable String category) {
        return productClient.getProductsByCategory(category);
    }

    @GetMapping("/catalog")
    public Map<String, Object> getCatalog() {
        Map<String, Object> catalog = new HashMap<>();
        List<Product> products = productClient.getAllProducts();
        catalog.put("totalProducts", products.size());
        catalog.put("products", products);
        return catalog;
    }
}
```

### Step 3.7: Configure Feign Client

Create `src/main/resources/application.yml`:

```yaml
server:
  port: 8080

spring:
  application:
    name: feign-client
  cloud:
    openfeign:
      circuitbreaker:
        enabled: true

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true

feign:
  client:
    config:
      default:
        connect-timeout: 5000
        read-timeout: 5000
        logger-level: full
  circuitbreaker:
    enabled: true

logging:
  level:
    com.example.feignclient.client: DEBUG

management:
  endpoints:
    web:
      exposure:
        include: health,info
```

---

## Part 4: Testing the Application

### Step 4.1: Start Eureka Server

Start the Eureka Server from Lab 8b:

```bash
cd ../module-08b-eureka/solution/eureka-server
mvn spring-boot:run
```

### Step 4.2: Start Product Service

```bash
cd starter/product-service
mvn spring-boot:run
```

### Step 4.3: Start Feign Client

```bash
cd starter/feign-client
mvn spring-boot:run
```

### Step 4.4: Test the Endpoints

```bash
# Get all products through Feign client
curl http://localhost:8080/api/store/products

# Get specific product
curl http://localhost:8080/api/store/products/1

# Get products by category
curl http://localhost:8080/api/store/products/category/Electronics

# Get catalog summary
curl http://localhost:8080/api/store/catalog
```

---

## Part 5: Advanced Feign Configuration

### Step 5.1: Custom Configuration Class

Create `src/main/java/com/example/feignclient/config/FeignConfig.java`:

```java
package com.example.feignclient.config;

import feign.Logger;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("X-Request-Source", "feign-client");
            requestTemplate.header("X-Correlation-Id",
                java.util.UUID.randomUUID().toString());
        };
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder();
    }
}
```

### Step 5.2: Custom Error Decoder

Create `src/main/java/com/example/feignclient/config/CustomErrorDecoder.java`:

```java
package com.example.feignclient.config;

import feign.Response;
import feign.codec.ErrorDecoder;

public class CustomErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == 404) {
            return new ProductNotFoundException("Product not found");
        }
        if (response.status() == 503) {
            return new ServiceUnavailableException("Service temporarily unavailable");
        }
        return defaultDecoder.decode(methodKey, response);
    }
}

class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String message) {
        super(message);
    }
}

class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String message) {
        super(message);
    }
}
```

### Step 5.3: Apply Custom Configuration to Client

Update the Feign client to use custom configuration:

```java
@FeignClient(
    name = "product-service",
    fallback = ProductClientFallback.class,
    configuration = FeignConfig.class
)
public interface ProductClient {
    // ... methods
}
```

---

## Exercises

### Exercise 1: Multiple Feign Clients
Create a second Feign client for an "inventory-service" that checks stock levels.

### Exercise 2: Request Interceptor for Authentication
Add a request interceptor that adds a Bearer token to all requests:

```java
@Bean
public RequestInterceptor authInterceptor() {
    return template -> template.header("Authorization", "Bearer " + getToken());
}
```

### Exercise 3: Retry Configuration
Configure Feign to retry failed requests:

```yaml
feign:
  client:
    config:
      product-service:
        retryer: com.example.feignclient.config.CustomRetryer
```

---

## Summary

In this lab, you learned:
- How to create declarative REST clients with OpenFeign
- How to integrate Feign with Eureka service discovery
- How to implement fallback methods for resilience
- How to customize Feign with interceptors and error decoders
- How to configure timeouts and logging

## Key Concepts

| Concept | Description |
|---------|-------------|
| @FeignClient | Declares a REST client interface |
| @EnableFeignClients | Enables Feign client scanning |
| Fallback | Alternative implementation when service fails |
| RequestInterceptor | Modifies outgoing requests |
| ErrorDecoder | Handles error responses |
| Configuration | Customizes Feign behavior |

## Feign vs RestTemplate

| Feature | Feign | RestTemplate |
|---------|-------|--------------|
| Style | Declarative | Imperative |
| Boilerplate | Minimal | More code needed |
| Load Balancing | Built-in | Requires @LoadBalanced |
| Circuit Breaker | Easy integration | Manual setup |
| Testing | Easy to mock | Harder to mock |

## Next Steps
- Explore Spring Cloud Kubernetes for container orchestration
- Learn about distributed tracing integration
- Investigate contract testing with Spring Cloud Contract
