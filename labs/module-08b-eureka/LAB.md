# Lab 8b: Service Discovery with Eureka

## Objectives
- Set up a Eureka Server for service discovery
- Register a client application with Eureka
- Use service discovery to call other services by name
- Understand Eureka's self-preservation mode and health checks

## Prerequisites
- Completed Lab 8 (Spring Cloud Config) or understanding of Spring Cloud basics
- Java 17 or higher
- Maven 3.6+

## Duration
45-60 minutes

---

## Part 1: Setting Up the Eureka Server

### Step 1.1: Open the Starter Project

Open the starter project located in:
```
labs/module-08b-eureka/starter/eureka-server/
```

### Step 1.2: Review the Project Structure

The starter project includes:
- `pom.xml` - Maven configuration with Spring Cloud dependencies
- `EurekaServerApplication.java` - Main application class (needs annotation)
- `application.yml` - Configuration file (needs to be completed)

### Step 1.3: Enable Eureka Server

Open `src/main/java/com/example/eurekaserver/EurekaServerApplication.java` and add the `@EnableEurekaServer` annotation:

```java
package com.example.eurekaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

### Step 1.4: Configure Eureka Server

Open `src/main/resources/application.yml` and add the following configuration:

```yaml
server:
  port: 8761

spring:
  application:
    name: eureka-server

eureka:
  instance:
    hostname: localhost
  client:
    register-with-eureka: false
    fetch-registry: false
    service-url:
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
  server:
    enable-self-preservation: false
    eviction-interval-timer-in-ms: 5000
```

**Configuration Explained:**
- `port: 8761` - Standard Eureka server port
- `register-with-eureka: false` - Server doesn't register with itself
- `fetch-registry: false` - Server doesn't need to fetch registry
- `enable-self-preservation: false` - Disabled for development (enable in production)
- `eviction-interval-timer-in-ms: 5000` - How often to check for expired instances

### Step 1.5: Start the Eureka Server

Run the application:
```bash
cd starter/eureka-server
mvn spring-boot:run
```

Open your browser and navigate to `http://localhost:8761`. You should see the Eureka Dashboard showing no registered instances yet.

---

## Part 2: Creating a Eureka Client

### Step 2.1: Open the Client Starter Project

Open the starter project located in:
```
labs/module-08b-eureka/starter/eureka-client/
```

### Step 2.2: Review Dependencies

The `pom.xml` already includes the necessary dependencies:
- `spring-cloud-starter-netflix-eureka-client` - Eureka client support
- `spring-boot-starter-web` - Web application support
- `spring-boot-starter-actuator` - Health endpoints for Eureka

### Step 2.3: Configure the Eureka Client

Open `src/main/resources/application.yml` and add:

```yaml
server:
  port: 8080

spring:
  application:
    name: eureka-client

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    registry-fetch-interval-seconds: 5
  instance:
    prefer-ip-address: true
    lease-renewal-interval-in-seconds: 10
    lease-expiration-duration-in-seconds: 30

management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: always
```

**Configuration Explained:**
- `defaultZone` - URL of the Eureka server
- `registry-fetch-interval-seconds` - How often to fetch the registry
- `prefer-ip-address: true` - Register with IP instead of hostname
- `lease-renewal-interval-in-seconds` - Heartbeat interval
- `lease-expiration-duration-in-seconds` - Time before instance is considered down

### Step 2.4: Create a Service Info Controller

Create `src/main/java/com/example/eurekaclient/controller/ServiceController.java`:

```java
package com.example.eurekaclient.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ServiceController {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${server.port}")
    private String serverPort;

    @GetMapping("/info")
    public Map<String, String> getServiceInfo() {
        Map<String, String> info = new HashMap<>();
        info.put("serviceName", applicationName);
        info.put("port", serverPort);
        info.put("status", "UP");
        return info;
    }

    @GetMapping("/hello")
    public Map<String, String> sayHello() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Hello from " + applicationName + " on port " + serverPort);
        return response;
    }
}
```

### Step 2.5: Create a Discovery Controller

Create `src/main/java/com/example/eurekaclient/controller/DiscoveryController.java`:

```java
package com.example.eurekaclient.controller;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/discovery")
public class DiscoveryController {

    private final DiscoveryClient discoveryClient;

    public DiscoveryController(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @GetMapping("/services")
    public List<String> getServices() {
        return discoveryClient.getServices();
    }

    @GetMapping("/services/{serviceName}")
    public List<Map<String, Object>> getServiceInstances(@PathVariable String serviceName) {
        return discoveryClient.getInstances(serviceName).stream()
                .map(this::mapInstance)
                .collect(Collectors.toList());
    }

    private Map<String, Object> mapInstance(ServiceInstance instance) {
        Map<String, Object> map = new HashMap<>();
        map.put("serviceId", instance.getServiceId());
        map.put("host", instance.getHost());
        map.put("port", instance.getPort());
        map.put("uri", instance.getUri().toString());
        map.put("metadata", instance.getMetadata());
        return map;
    }
}
```

### Step 2.6: Start the Eureka Client

Make sure the Eureka Server is still running, then start the client:

```bash
cd starter/eureka-client
mvn spring-boot:run
```

### Step 2.7: Verify Registration

1. Go back to the Eureka Dashboard at `http://localhost:8761`
2. You should see `EUREKA-CLIENT` listed under "Instances currently registered with Eureka"
3. Test the client endpoints:
   - `http://localhost:8080/api/info`
   - `http://localhost:8080/api/hello`
   - `http://localhost:8080/api/discovery/services`
   - `http://localhost:8080/api/discovery/services/eureka-client`

---

## Part 3: Running Multiple Instances

### Step 3.1: Start a Second Client Instance

Open a new terminal and run another instance on a different port:

```bash
cd starter/eureka-client
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

### Step 3.2: Verify Multiple Instances

1. Check the Eureka Dashboard - you should see 2 instances of `EUREKA-CLIENT`
2. Call `http://localhost:8080/api/discovery/services/eureka-client` to see both instances listed

---

## Part 4: Service-to-Service Communication (Optional)

### Step 4.1: Add RestTemplate with Load Balancing

Add to `EurekaClientApplication.java`:

```java
package com.example.eurekaclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class EurekaClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaClientApplication.class, args);
    }

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

### Step 4.2: Create a Client Communication Controller

Create `src/main/java/com/example/eurekaclient/controller/ClientController.java`:

```java
package com.example.eurekaclient.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/client")
public class ClientController {

    private final RestTemplate restTemplate;

    public ClientController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/call")
    @SuppressWarnings("unchecked")
    public Map<String, String> callService() {
        // Use service name instead of host:port
        String url = "http://eureka-client/api/hello";
        return restTemplate.getForObject(url, Map.class);
    }
}
```

### Step 4.3: Test Load Balancing

With both instances running (ports 8080 and 8081):

1. Call `http://localhost:8080/api/client/call` multiple times
2. Observe that responses come from different ports (load balancing in action)

---

## Part 5: Understanding Eureka Behavior

### Step 5.1: Instance Deregistration

1. Stop one of the client instances (Ctrl+C)
2. Wait for the eviction interval (5 seconds in our config)
3. Refresh the Eureka Dashboard to see the instance removed

### Step 5.2: Self-Preservation Mode

In production, Eureka enters self-preservation mode when it loses too many instances too quickly. This prevents network partition issues from removing valid instances.

To enable self-preservation (recommended for production):

```yaml
eureka:
  server:
    enable-self-preservation: true
```

---

## Exercises

### Exercise 1: Add Instance Metadata
Add custom metadata to your client registration:

```yaml
eureka:
  instance:
    metadata-map:
      zone: us-east-1
      version: 1.0.0
```

Verify the metadata appears in the discovery endpoint response.

### Exercise 2: Health Check Configuration
Configure Eureka to use actuator health endpoint:

```yaml
eureka:
  instance:
    health-check-url-path: /actuator/health
```

### Exercise 3: Create a Second Service
Create a new service (e.g., `greeting-service`) that:
1. Registers with Eureka
2. Provides a `/greet` endpoint
3. Is called by `eureka-client` using the service name

---

## Summary

In this lab, you learned:
- How to set up a Eureka Server for service discovery
- How to register client applications with Eureka
- How to use DiscoveryClient to find registered services
- How to use @LoadBalanced RestTemplate for service-to-service communication
- How Eureka handles multiple instances and load balancing
- Eureka's self-preservation mode and health checking

## Key Concepts

| Concept | Description |
|---------|-------------|
| Service Registry | Central server that maintains a list of available services |
| Service Discovery | Process of finding service instances by name |
| Heartbeat | Regular signal from client to server indicating it's alive |
| Self-Preservation | Eureka's protection against network partition issues |
| Load Balancing | Distributing requests across multiple service instances |

## Next Steps
- Explore Spring Cloud Gateway for API routing
- Learn about Resilience4j for circuit breaker patterns
- Investigate distributed tracing with Spring Cloud Sleuth
