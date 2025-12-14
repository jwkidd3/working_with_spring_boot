# Lab 1: Spring Framework Fundamentals

## Objectives

By the end of this lab, you will be able to:
- Create a Spring Boot application from scratch
- Define and configure Spring beans
- Implement dependency injection using constructor injection
- Work with Spring profiles for environment-specific configuration
- Use lifecycle callbacks (@PostConstruct and @PreDestroy)

## Prerequisites

- JDK 17 or higher installed
- Maven 3.6+ or Gradle 7+ installed
- IDE of your choice (IntelliJ IDEA recommended)

## Duration

45-60 minutes

---

## Part 1: Creating Your First Spring Boot Application

### Step 1.1: Generate the Project

1. Go to [Spring Initializr](https://start.spring.io/)
2. Configure the project:
   - **Project:** Maven
   - **Language:** Java
   - **Spring Boot:** 3.2.x (latest stable)
   - **Group:** com.example
   - **Artifact:** spring-fundamentals-lab
   - **Name:** spring-fundamentals-lab
   - **Package name:** com.example.springfundamentals
   - **Packaging:** Jar
   - **Java:** 17

3. Add Dependencies:
   - Spring Web

4. Click **Generate** to download the project

5. Extract and open in your IDE

### Step 1.2: Verify the Project Structure

Your project should have this structure:
```
spring-fundamentals-lab/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/springfundamentals/
│   │   │       └── SpringFundamentalsLabApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/
│           └── com/example/springfundamentals/
│               └── SpringFundamentalsLabApplicationTests.java
├── pom.xml
└── mvnw
```

### Step 1.3: Run the Application

```bash
./mvnw spring-boot:run
```

You should see Spring Boot start up successfully. Press `Ctrl+C` to stop.

---

## Part 2: Creating Services with Dependency Injection

### Step 2.1: Create a Model Class

Create a new file `src/main/java/com/example/springfundamentals/model/Greeting.java`:

```java
package com.example.springfundamentals.model;

public class Greeting {
    private String message;
    private String sender;
    private long timestamp;

    public Greeting() {
        this.timestamp = System.currentTimeMillis();
    }

    public Greeting(String message, String sender) {
        this();
        this.message = message;
        this.sender = sender;
    }

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Greeting{" +
                "message='" + message + '\'' +
                ", sender='" + sender + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
```

### Step 2.2: Create a Greeting Service Interface

Create `src/main/java/com/example/springfundamentals/service/GreetingService.java`:

```java
package com.example.springfundamentals.service;

import com.example.springfundamentals.model.Greeting;

public interface GreetingService {
    Greeting createGreeting(String name);
    String getServiceName();
}
```

### Step 2.3: Create the Default Implementation

Create `src/main/java/com/example/springfundamentals/service/SimpleGreetingService.java`:

```java
package com.example.springfundamentals.service;

import com.example.springfundamentals.model.Greeting;
import org.springframework.stereotype.Service;

@Service
public class SimpleGreetingService implements GreetingService {

    @Override
    public Greeting createGreeting(String name) {
        String message = "Hello, " + name + "! Welcome to Spring Boot.";
        return new Greeting(message, "SimpleGreetingService");
    }

    @Override
    public String getServiceName() {
        return "Simple Greeting Service";
    }
}
```

### Step 2.4: Create a Formatting Service

Create `src/main/java/com/example/springfundamentals/service/FormattingService.java`:

```java
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
```

### Step 2.5: Create an Application Service (Demonstrating DI)

Create `src/main/java/com/example/springfundamentals/service/GreetingApplicationService.java`:

```java
package com.example.springfundamentals.service;

import com.example.springfundamentals.model.Greeting;
import org.springframework.stereotype.Service;

@Service
public class GreetingApplicationService {

    private final GreetingService greetingService;
    private final FormattingService formattingService;

    // Constructor Injection - Spring will automatically inject dependencies
    public GreetingApplicationService(GreetingService greetingService,
                                       FormattingService formattingService) {
        this.greetingService = greetingService;
        this.formattingService = formattingService;
    }

    public String getFormattedGreeting(String name) {
        Greeting greeting = greetingService.createGreeting(name);

        String formattedMessage = formattingService.formatGreeting(greeting.getMessage());
        String formattedTime = formattingService.formatTimestamp(greeting.getTimestamp());

        return String.format("%s%n[From: %s at %s]",
                formattedMessage,
                greeting.getSender(),
                formattedTime);
    }

    public String getServiceInfo() {
        return "Using: " + greetingService.getServiceName();
    }
}
```

### Step 2.6: Create a REST Controller

Create `src/main/java/com/example/springfundamentals/controller/GreetingController.java`:

```java
package com.example.springfundamentals.controller;

import com.example.springfundamentals.service.GreetingApplicationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingController {

    private final GreetingApplicationService applicationService;

    public GreetingController(GreetingApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping("/greet")
    public String greet(@RequestParam(defaultValue = "World") String name) {
        return applicationService.getFormattedGreeting(name);
    }

    @GetMapping("/info")
    public String info() {
        return applicationService.getServiceInfo();
    }
}
```

### Step 2.7: Test Your Application

1. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

2. Test the endpoints:
   ```bash
   curl http://localhost:8080/greet
   curl http://localhost:8080/greet?name=John
   curl http://localhost:8080/info
   ```

---

## Part 3: Working with Profiles

### Step 3.1: Create a Fancy Greeting Service

Create `src/main/java/com/example/springfundamentals/service/FancyGreetingService.java`:

```java
package com.example.springfundamentals.service;

import com.example.springfundamentals.model.Greeting;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("fancy")
public class FancyGreetingService implements GreetingService {

    private static final String[] GREETINGS = {
        "Greetings and salutations",
        "A most cordial welcome",
        "Delighted to make your acquaintance",
        "How wonderful to see you"
    };

    @Override
    public Greeting createGreeting(String name) {
        String randomGreeting = GREETINGS[(int) (Math.random() * GREETINGS.length)];
        String message = randomGreeting + ", dear " + name + "!";
        return new Greeting(message, "FancyGreetingService");
    }

    @Override
    public String getServiceName() {
        return "Fancy Greeting Service (Premium Edition)";
    }
}
```

### Step 3.2: Update SimpleGreetingService with Profile

Update `SimpleGreetingService.java` to add a profile:

```java
package com.example.springfundamentals.service;

import com.example.springfundamentals.model.Greeting;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("!fancy")  // Active when 'fancy' profile is NOT active
public class SimpleGreetingService implements GreetingService {

    @Override
    public Greeting createGreeting(String name) {
        String message = "Hello, " + name + "! Welcome to Spring Boot.";
        return new Greeting(message, "SimpleGreetingService");
    }

    @Override
    public String getServiceName() {
        return "Simple Greeting Service";
    }
}
```

### Step 3.3: Create Profile-Specific Properties

Create `src/main/resources/application-fancy.properties`:

```properties
# Fancy profile configuration
server.port=8081
app.greeting.prefix=Most Distinguished
```

Update `src/main/resources/application.properties`:

```properties
# Default configuration
spring.application.name=spring-fundamentals-lab
app.greeting.prefix=Hello
```

### Step 3.4: Test with Different Profiles

1. Run with default profile:
   ```bash
   ./mvnw spring-boot:run
   curl http://localhost:8080/info
   # Output: Using: Simple Greeting Service
   ```

2. Run with fancy profile:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=fancy
   curl http://localhost:8081/info
   # Output: Using: Fancy Greeting Service (Premium Edition)
   ```

---

## Part 4: Lifecycle Callbacks and Bean Configuration

### Step 4.1: Create a Cache Service with Lifecycle Callbacks

Create `src/main/java/com/example/springfundamentals/service/CacheService.java`:

```java
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

        // Pre-populate with some data
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
```

### Step 4.2: Create a Configuration Class with @Bean

Create `src/main/java/com/example/springfundamentals/config/AppConfig.java`:

```java
package com.example.springfundamentals.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class AppConfig {

    @Value("${app.timezone:UTC}")
    private String timezone;

    @Bean
    public Clock applicationClock() {
        System.out.println("Creating Clock bean with timezone: " + timezone);
        return Clock.system(ZoneId.of(timezone));
    }

    @Bean
    public AppInfo appInfo(@Value("${spring.application.name:unknown}") String appName,
                           @Value("${app.version:1.0.0}") String version) {
        return new AppInfo(appName, version);
    }

    // Inner class to hold application info
    public static class AppInfo {
        private final String name;
        private final String version;

        public AppInfo(String name, String version) {
            this.name = name;
            this.version = version;
        }

        public String getName() {
            return name;
        }

        public String getVersion() {
            return version;
        }

        @Override
        public String toString() {
            return name + " v" + version;
        }
    }
}
```

### Step 4.3: Update the Controller to Use New Services

Update `GreetingController.java`:

```java
package com.example.springfundamentals.controller;

import com.example.springfundamentals.config.AppConfig;
import com.example.springfundamentals.service.CacheService;
import com.example.springfundamentals.service.GreetingApplicationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.LocalDateTime;

@RestController
public class GreetingController {

    private final GreetingApplicationService applicationService;
    private final CacheService cacheService;
    private final Clock clock;
    private final AppConfig.AppInfo appInfo;

    public GreetingController(GreetingApplicationService applicationService,
                              CacheService cacheService,
                              Clock clock,
                              AppConfig.AppInfo appInfo) {
        this.applicationService = applicationService;
        this.cacheService = cacheService;
        this.clock = clock;
        this.appInfo = appInfo;
    }

    @GetMapping("/greet")
    public String greet(@RequestParam(defaultValue = "World") String name) {
        // Cache the greeting
        String cacheKey = "greeting_" + name;
        if (cacheService.contains(cacheKey)) {
            return "CACHED: " + cacheService.get(cacheKey);
        }

        String greeting = applicationService.getFormattedGreeting(name);
        cacheService.put(cacheKey, greeting);
        return greeting;
    }

    @GetMapping("/info")
    public String info() {
        return String.format("%s%nService: %s%nCache entries: %d%nServer time: %s",
                appInfo,
                applicationService.getServiceInfo(),
                cacheService.size(),
                LocalDateTime.now(clock));
    }

    @GetMapping("/cache/stats")
    public String cacheStats() {
        return "Cache contains " + cacheService.size() + " entries";
    }
}
```

### Step 4.4: Update Properties

Update `application.properties`:

```properties
# Application configuration
spring.application.name=spring-fundamentals-lab
app.version=1.0.0
app.timezone=America/New_York
app.greeting.prefix=Hello
```

### Step 4.5: Test Lifecycle and Configuration

1. Run the application and observe the console output for lifecycle messages
2. Test all endpoints:
   ```bash
   curl http://localhost:8080/info
   curl http://localhost:8080/greet?name=Alice
   curl http://localhost:8080/greet?name=Alice  # Should show CACHED
   curl http://localhost:8080/cache/stats
   ```
3. Stop the application (Ctrl+C) and observe the cleanup messages

---

## Part 5: Challenge Exercises

### Challenge 1: Add a New Greeting Service

Create a `HolidayGreetingService` that:
- Is active only with the "holiday" profile
- Returns holiday-themed greetings
- Uses the current date to pick appropriate holiday greetings

### Challenge 2: Implement Conditional Beans

Create a configuration that:
- Conditionally creates a `MetricsService` bean only if `app.metrics.enabled=true`
- Hint: Use `@ConditionalOnProperty`

### Challenge 3: Bean Scopes

Modify the `CacheService` to:
- Create a `@Scope("prototype")` variant called `SessionCache`
- Demonstrate the difference in behavior between singleton and prototype scopes

---

## Summary

In this lab, you learned:

1. **Project Setup**: How to create a Spring Boot application using Spring Initializr
2. **Dependency Injection**: How to use constructor injection to wire services together
3. **Stereotype Annotations**: Using @Service, @RestController, and @Configuration
4. **Profiles**: Creating environment-specific configurations using @Profile
5. **Lifecycle Callbacks**: Using @PostConstruct and @PreDestroy
6. **Java Configuration**: Creating beans with @Bean and @Configuration

## Next Steps

In Module 2, you'll learn about web services fundamentals including HTTP methods, REST principles, and how Spring Boot simplifies building web services.
