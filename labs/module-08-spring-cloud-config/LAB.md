# Lab 8: Spring Cloud Config

## Objectives

By the end of this lab, you will be able to:
- Set up a Spring Cloud Config Server
- Configure a Config Client to fetch configuration from the server
- Store configuration in a file system backend
- Refresh configuration at runtime without restarting the application
- Use `@RefreshScope` and `@ConfigurationProperties` for dynamic configuration

## Prerequisites

- Completed previous labs
- Understanding of Spring Boot configuration and profiles

## Duration

60-75 minutes

---

## Scenario

You are building a microservices application where multiple services need centralized configuration management. Instead of embedding configuration in each service, you'll create a central Config Server that serves configuration to all client applications. This allows you to:
- Manage configuration in one place
- Change configuration without rebuilding applications
- Support environment-specific configurations (dev, staging, prod)

---

## Part 1: Project Setup

### Step 1.1: Open the Starter Projects

1. Navigate to the `starter` folder for this lab
2. You'll find two projects:
   - `config-server` - The centralized configuration server
   - `config-client` - A client application that consumes configuration
3. Open both projects in your IDE

### Step 1.2: Review the Project Structure

**Config Server:**
```
config-server/
├── src/main/java/com/example/configserver/
│   └── ConfigServerApplication.java
├── src/main/resources/
│   └── application.yml
└── pom.xml
```

**Config Client:**
```
config-client/
├── src/main/java/com/example/configclient/
│   └── ConfigClientApplication.java
├── src/main/resources/
│   └── application.yml
└── pom.xml
```

---

## Part 2: Setting Up the Config Server

### Step 2.1: Review Config Server Dependencies

Open `config-server/pom.xml` and review the dependencies:

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-config-server</artifactId>
</dependency>
```

### Step 2.2: Enable Config Server

Update `ConfigServerApplication.java`:

```java
package com.example.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
```

### Step 2.3: Configure the Config Server

Update `config-server/src/main/resources/application.yml`:

```yaml
server:
  port: 8888

spring:
  application:
    name: config-server
  profiles:
    active: native
  cloud:
    config:
      server:
        native:
          search-locations: file:${user.dir}/../config-repo
```

This configures the server to:
- Run on port 8888 (the default Config Server port)
- Use the `native` profile for file system-based configuration
- Look for configuration files in the `config-repo` folder

### Step 2.4: Create the Configuration Repository

Create configuration files in the `config-repo` folder:

**config-repo/application.yml** (default configuration for all applications):

```yaml
# Default configuration for all applications
app:
  default-message: "Hello from Config Server!"

logging:
  level:
    com.example: INFO
```

**config-repo/config-client.yml** (configuration specific to config-client):

```yaml
# Configuration for config-client application
app:
  name: Task Management Service
  description: A service for managing tasks
  version: 1.0.0

feature:
  new-dashboard: false
  dark-mode: true
```

**config-repo/config-client-dev.yml** (dev profile for config-client):

```yaml
# Development profile configuration
app:
  name: Task Management Service (DEV)
  description: Development instance of task management

feature:
  new-dashboard: true
  dark-mode: true
  debug-mode: true

logging:
  level:
    com.example: DEBUG
```

**config-repo/config-client-prod.yml** (prod profile for config-client):

```yaml
# Production profile configuration
app:
  name: Task Management Service
  description: Production instance of task management

feature:
  new-dashboard: false
  dark-mode: true
  debug-mode: false

logging:
  level:
    com.example: WARN
```

### Step 2.5: Start and Test the Config Server

1. Start the Config Server:
   ```bash
   cd config-server
   mvn spring-boot:run
   ```

2. Test the configuration endpoints:

   ```bash
   # Get default configuration for config-client
   curl http://localhost:8888/config-client/default | jq

   # Get dev profile configuration
   curl http://localhost:8888/config-client/dev | jq

   # Get prod profile configuration
   curl http://localhost:8888/config-client/prod | jq
   ```

   You should see JSON responses containing the merged configuration.

---

## Part 3: Setting Up the Config Client

### Step 3.1: Review Config Client Dependencies

Open `config-client/pom.xml` and review the dependencies:

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-config</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

### Step 3.2: Configure the Config Client

Update `config-client/src/main/resources/application.yml`:

```yaml
server:
  port: 8080

spring:
  application:
    name: config-client
  config:
    import: "configserver:http://localhost:8888"
  cloud:
    config:
      fail-fast: true

management:
  endpoints:
    web:
      exposure:
        include: health,info,refresh
  endpoint:
    health:
      show-details: always
```

### Step 3.3: Create Configuration Properties Class

Create `config-client/src/main/java/com/example/configclient/config/AppProperties.java`:

```java
package com.example.configclient.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String name;
    private String description;
    private String version;
    private String defaultMessage;

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    public void setDefaultMessage(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }
}
```

### Step 3.4: Create Feature Flags Properties Class

Create `config-client/src/main/java/com/example/configclient/config/FeatureProperties.java`:

```java
package com.example.configclient.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "feature")
public class FeatureProperties {

    private boolean newDashboard;
    private boolean darkMode;
    private boolean debugMode;

    // Getters and Setters
    public boolean isNewDashboard() {
        return newDashboard;
    }

    public void setNewDashboard(boolean newDashboard) {
        this.newDashboard = newDashboard;
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    public void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }
}
```

### Step 3.5: Create a Controller to Display Configuration

Create `config-client/src/main/java/com/example/configclient/controller/ConfigController.java`:

```java
package com.example.configclient.controller;

import com.example.configclient.config.AppProperties;
import com.example.configclient.config.FeatureProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/config")
@RefreshScope
public class ConfigController {

    private final AppProperties appProperties;
    private final FeatureProperties featureProperties;

    @Value("${app.default-message:No message configured}")
    private String defaultMessage;

    public ConfigController(AppProperties appProperties, FeatureProperties featureProperties) {
        this.appProperties = appProperties;
        this.featureProperties = featureProperties;
    }

    @GetMapping("/app")
    public AppProperties getAppConfig() {
        return appProperties;
    }

    @GetMapping("/features")
    public FeatureProperties getFeatures() {
        return featureProperties;
    }

    @GetMapping("/message")
    public Map<String, String> getMessage() {
        Map<String, String> response = new HashMap<>();
        response.put("message", defaultMessage);
        return response;
    }

    @GetMapping("/all")
    public Map<String, Object> getAllConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("app", appProperties);
        config.put("features", featureProperties);
        config.put("defaultMessage", defaultMessage);
        return config;
    }
}
```

### Step 3.6: Start and Test the Config Client

1. Make sure the Config Server is running on port 8888

2. Start the Config Client:
   ```bash
   cd config-client
   mvn spring-boot:run
   ```

3. Test the configuration endpoints:

   ```bash
   # Get app configuration
   curl http://localhost:8080/api/config/app | jq

   # Get feature flags
   curl http://localhost:8080/api/config/features | jq

   # Get all configuration
   curl http://localhost:8080/api/config/all | jq
   ```

---

## Part 4: Runtime Configuration Refresh

### Step 4.1: Understand Refresh Behavior

Spring Cloud Config supports refreshing configuration at runtime:

- **@ConfigurationProperties beans**: Automatically refresh without any additional annotations
- **@Value injected fields**: Require `@RefreshScope` on the bean to refresh

### Step 4.2: Test Configuration Refresh

1. With both servers running, verify current configuration:
   ```bash
   curl http://localhost:8080/api/config/app | jq
   ```

2. Modify `config-repo/config-client.yml` - change the description:
   ```yaml
   app:
     name: Task Management Service
     description: An UPDATED service for managing tasks  # Changed!
     version: 1.0.0
   ```

3. Trigger a refresh on the client:
   ```bash
   curl -X POST http://localhost:8080/actuator/refresh -H "Content-Type: application/json"
   ```

4. Verify the configuration has been updated:
   ```bash
   curl http://localhost:8080/api/config/app | jq
   ```

   You should see the updated description!

### Step 4.3: Test Feature Flag Updates

1. Check current feature flags:
   ```bash
   curl http://localhost:8080/api/config/features | jq
   ```

2. Modify `config-repo/config-client.yml` - enable new-dashboard:
   ```yaml
   feature:
     new-dashboard: true  # Changed from false!
     dark-mode: true
   ```

3. Refresh and verify:
   ```bash
   curl -X POST http://localhost:8080/actuator/refresh -H "Content-Type: application/json"
   curl http://localhost:8080/api/config/features | jq
   ```

---

## Part 5: Using Profiles with Config Server

### Step 5.1: Run Client with Dev Profile

1. Stop the config-client if running

2. Start with the dev profile:
   ```bash
   cd config-client
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

3. Check the configuration:
   ```bash
   curl http://localhost:8080/api/config/all | jq
   ```

   You should see the dev-specific configuration (debug-mode: true, etc.)

### Step 5.2: Run Client with Prod Profile

1. Stop the config-client

2. Start with the prod profile:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=prod
   ```

3. Check the configuration:
   ```bash
   curl http://localhost:8080/api/config/all | jq
   ```

   You should see the prod-specific configuration.

---

## Part 6: Challenge Exercises

### Challenge 1: Add a New Microservice Configuration

1. Create a new configuration file `config-repo/order-service.yml`
2. Add properties for an order service (order limits, retry counts, etc.)
3. Create a simple Spring Boot client that reads this configuration

### Challenge 2: Encrypt Sensitive Configuration

1. Research Spring Cloud Config encryption
2. Add an encryption key to the Config Server
3. Encrypt a database password in the configuration
4. Verify the client receives the decrypted value

### Challenge 3: Health Check Integration

1. Add a health check that verifies Config Server connectivity
2. Implement graceful degradation when Config Server is unavailable
3. Use cached configuration as a fallback

### Challenge 4: Git Backend

1. Create a Git repository for configuration
2. Reconfigure the Config Server to use Git backend
3. Test configuration updates via Git commits

---

## Summary

In this lab, you learned:

1. **Config Server Setup**: How to create a centralized configuration server with Spring Cloud Config
2. **Native File Backend**: Using file system-based configuration storage
3. **Config Client**: Connecting client applications to fetch configuration from the server
4. **@ConfigurationProperties**: Type-safe configuration binding that auto-refreshes
5. **@RefreshScope**: Making @Value-injected beans refreshable at runtime
6. **Runtime Refresh**: Updating configuration without restarting applications
7. **Profile Support**: Environment-specific configuration (dev, prod, etc.)

## Key Concepts

| Concept | Description |
|---------|-------------|
| Config Server | Centralized server that serves configuration to clients |
| Config Client | Application that fetches configuration from Config Server |
| Native Backend | File system-based configuration storage |
| @RefreshScope | Enables runtime refresh for @Value-injected beans |
| @ConfigurationProperties | Type-safe configuration that auto-refreshes |
| /actuator/refresh | Endpoint to trigger configuration refresh |

## Next Steps

- Explore Spring Cloud Bus for broadcasting configuration changes to multiple clients
- Implement Git-based configuration for version control
- Add security to protect sensitive configuration
- Integrate with service discovery (Eureka) for dynamic Config Server location
