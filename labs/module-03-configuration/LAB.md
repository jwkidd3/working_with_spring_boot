# Lab 3: Spring Boot Configuration & Profiles

## Objectives

By the end of this lab, you will be able to:
- Use `@ConfigurationProperties` for type-safe configuration
- Create and use Spring profiles for different environments
- Externalize configuration with environment variables
- Implement conditional bean creation
- Use `@Value` annotations effectively

## Prerequisites

- Completed Labs 1-2
- Understanding of Spring beans and dependency injection

## Duration

45-60 minutes

---

## Scenario

You are building a notification service that needs to work differently in development, staging, and production environments. You'll configure email settings, feature flags, and service endpoints using Spring Boot's configuration capabilities.

---

## Part 1: Project Setup

### Step 1.1: Create the Project

1. Go to [Spring Initializr](https://start.spring.io/)
2. Configure:
   - **Group:** com.example
   - **Artifact:** config-lab
   - **Name:** config-lab
   - **Package name:** com.example.configlab
   - **Java:** 17

3. Add Dependencies:
   - Spring Web
   - Validation

4. Generate and extract the project

### Step 1.2: Project Structure

```
config-lab/
├── src/main/java/com/example/configlab/
│   └── ConfigLabApplication.java
├── src/main/resources/
│   └── application.properties
└── pom.xml
```

---

## Part 2: Basic Configuration with @Value

### Step 2.1: Create Application Properties

Update `src/main/resources/application.properties`:

```properties
# Application Info
spring.application.name=config-lab
app.version=1.0.0
app.description=Configuration Lab Application

# Server Configuration
server.port=8080

# Custom Properties
app.notification.enabled=true
app.notification.max-retries=3
app.notification.timeout-seconds=30
```

### Step 2.2: Create a Configuration Reader Service

Create `src/main/java/com/example/configlab/service/AppInfoService.java`:

```java
package com.example.configlab.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AppInfoService {

    @Value("${spring.application.name}")
    private String appName;

    @Value("${app.version}")
    private String version;

    @Value("${app.description}")
    private String description;

    @Value("${app.notification.enabled}")
    private boolean notificationEnabled;

    @Value("${app.notification.max-retries}")
    private int maxRetries;

    // Default value if property not found
    @Value("${app.notification.sender:noreply@example.com}")
    private String defaultSender;

    public String getAppInfo() {
        return String.format("%s v%s - %s", appName, version, description);
    }

    public boolean isNotificationEnabled() {
        return notificationEnabled;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public String getDefaultSender() {
        return defaultSender;
    }
}
```

### Step 2.3: Create a Controller to Test

Create `src/main/java/com/example/configlab/controller/ConfigController.java`:

```java
package com.example.configlab.controller;

import com.example.configlab.service.AppInfoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

    private final AppInfoService appInfoService;

    public ConfigController(AppInfoService appInfoService) {
        this.appInfoService = appInfoService;
    }

    @GetMapping("/info")
    public Map<String, Object> getInfo() {
        return Map.of(
            "application", appInfoService.getAppInfo(),
            "notificationEnabled", appInfoService.isNotificationEnabled(),
            "maxRetries", appInfoService.getMaxRetries(),
            "defaultSender", appInfoService.getDefaultSender()
        );
    }
}
```

### Step 2.4: Test Basic Configuration

```bash
./mvnw spring-boot:run

curl http://localhost:8080/api/config/info | jq
```

Expected response:
```json
{
  "application": "config-lab v1.0.0 - Configuration Lab Application",
  "notificationEnabled": true,
  "maxRetries": 3,
  "defaultSender": "noreply@example.com"
}
```

---

## Part 3: Type-Safe Configuration with @ConfigurationProperties

### Step 3.1: Create Notification Properties Class

Create `src/main/java/com/example/configlab/config/NotificationProperties.java`:

```java
package com.example.configlab.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "app.notification")
@Validated
public class NotificationProperties {

    /**
     * Whether notifications are enabled
     */
    private boolean enabled = true;

    /**
     * Maximum retry attempts
     */
    @Min(1)
    @Max(10)
    private int maxRetries = 3;

    /**
     * Timeout duration for sending notifications
     */
    private Duration timeout = Duration.ofSeconds(30);

    /**
     * Default sender email
     */
    @NotBlank
    private String sender = "noreply@example.com";

    /**
     * Email configuration
     */
    private final Email email = new Email();

    /**
     * SMS configuration
     */
    private final Sms sms = new Sms();

    /**
     * List of admin emails to notify on errors
     */
    private List<String> adminEmails = new ArrayList<>();

    // Getters and Setters
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public Email getEmail() {
        return email;
    }

    public Sms getSms() {
        return sms;
    }

    public List<String> getAdminEmails() {
        return adminEmails;
    }

    public void setAdminEmails(List<String> adminEmails) {
        this.adminEmails = adminEmails;
    }

    // Nested class for email settings
    public static class Email {
        private String host = "localhost";
        private int port = 25;
        private String username;
        private String password;
        private boolean starttls = false;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public boolean isStarttls() {
            return starttls;
        }

        public void setStarttls(boolean starttls) {
            this.starttls = starttls;
        }
    }

    // Nested class for SMS settings
    public static class Sms {
        private boolean enabled = false;
        private String provider = "twilio";
        private String apiKey;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
    }
}
```

### Step 3.2: Enable Configuration Properties

Create `src/main/java/com/example/configlab/config/AppConfig.java`:

```java
package com.example.configlab.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(NotificationProperties.class)
public class AppConfig {
}
```

### Step 3.3: Update Application Properties

Update `src/main/resources/application.properties`:

```properties
# Application Info
spring.application.name=config-lab
app.version=1.0.0
app.description=Configuration Lab Application

# Notification Configuration
app.notification.enabled=true
app.notification.max-retries=3
app.notification.timeout=30s
app.notification.sender=notifications@myapp.com
app.notification.admin-emails[0]=admin@myapp.com
app.notification.admin-emails[1]=devops@myapp.com

# Email Settings
app.notification.email.host=smtp.example.com
app.notification.email.port=587
app.notification.email.username=smtp-user
app.notification.email.starttls=true

# SMS Settings
app.notification.sms.enabled=false
app.notification.sms.provider=twilio
```

### Step 3.4: Create Notification Service

Create `src/main/java/com/example/configlab/service/NotificationService.java`:

```java
package com.example.configlab.service;

import com.example.configlab.config.NotificationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final NotificationProperties properties;

    public NotificationService(NotificationProperties properties) {
        this.properties = properties;
        log.info("NotificationService initialized with sender: {}", properties.getSender());
        log.info("Email host: {}, port: {}",
            properties.getEmail().getHost(),
            properties.getEmail().getPort());
    }

    public String sendNotification(String to, String message) {
        if (!properties.isEnabled()) {
            return "Notifications are disabled";
        }

        log.info("Sending notification to {} from {} (timeout: {})",
            to, properties.getSender(), properties.getTimeout());

        // Simulate sending with retry logic
        for (int attempt = 1; attempt <= properties.getMaxRetries(); attempt++) {
            log.info("Attempt {} of {}", attempt, properties.getMaxRetries());
            // Simulated success
            return String.format("Notification sent to %s (attempt %d)", to, attempt);
        }

        return "Failed to send notification";
    }

    public NotificationProperties.Email getEmailConfig() {
        return properties.getEmail();
    }

    public NotificationProperties.Sms getSmsConfig() {
        return properties.getSms();
    }

    public boolean isSmsEnabled() {
        return properties.getSms().isEnabled();
    }
}
```

### Step 3.5: Update Controller

Update `ConfigController.java`:

```java
package com.example.configlab.controller;

import com.example.configlab.config.NotificationProperties;
import com.example.configlab.service.AppInfoService;
import com.example.configlab.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

    private final AppInfoService appInfoService;
    private final NotificationService notificationService;
    private final NotificationProperties notificationProperties;

    public ConfigController(AppInfoService appInfoService,
                           NotificationService notificationService,
                           NotificationProperties notificationProperties) {
        this.appInfoService = appInfoService;
        this.notificationService = notificationService;
        this.notificationProperties = notificationProperties;
    }

    @GetMapping("/info")
    public Map<String, Object> getInfo() {
        return Map.of(
            "application", appInfoService.getAppInfo(),
            "notificationEnabled", appInfoService.isNotificationEnabled(),
            "maxRetries", appInfoService.getMaxRetries(),
            "defaultSender", appInfoService.getDefaultSender()
        );
    }

    @GetMapping("/notification")
    public Map<String, Object> getNotificationConfig() {
        return Map.of(
            "enabled", notificationProperties.isEnabled(),
            "sender", notificationProperties.getSender(),
            "timeout", notificationProperties.getTimeout().toString(),
            "maxRetries", notificationProperties.getMaxRetries(),
            "emailHost", notificationProperties.getEmail().getHost(),
            "emailPort", notificationProperties.getEmail().getPort(),
            "smsEnabled", notificationProperties.getSms().isEnabled(),
            "smsProvider", notificationProperties.getSms().getProvider()
        );
    }

    @PostMapping("/notify")
    public Map<String, String> sendNotification(@RequestParam String to,
                                                 @RequestParam String message) {
        String result = notificationService.sendNotification(to, message);
        return Map.of("result", result);
    }
}
```

### Step 3.6: Test Configuration Properties

```bash
./mvnw spring-boot:run

# Get notification config
curl http://localhost:8080/api/config/notification | jq

# Send a test notification
curl -X POST "http://localhost:8080/api/config/notify?to=user@example.com&message=Hello" | jq
```

---

## Part 4: Spring Profiles

### Step 4.1: Create Development Profile

Create `src/main/resources/application-dev.properties`:

```properties
# Development Profile
spring.application.name=config-lab-dev

# Notification - Development Settings
app.notification.enabled=true
app.notification.sender=dev-notifications@localhost
app.notification.timeout=5s
app.notification.max-retries=1

# Email - Use local mailhog or similar
app.notification.email.host=localhost
app.notification.email.port=1025
app.notification.email.starttls=false

# SMS - Disabled in dev
app.notification.sms.enabled=false

# Logging
logging.level.com.example=DEBUG
```

### Step 4.2: Create Production Profile

Create `src/main/resources/application-prod.properties`:

```properties
# Production Profile
spring.application.name=config-lab-prod

# Notification - Production Settings
app.notification.enabled=true
app.notification.sender=notifications@mycompany.com
app.notification.timeout=30s
app.notification.max-retries=5

# Email - Production SMTP
app.notification.email.host=smtp.sendgrid.net
app.notification.email.port=587
app.notification.email.starttls=true

# SMS - Enabled in production
app.notification.sms.enabled=true
app.notification.sms.provider=twilio

# Logging
logging.level.com.example=INFO
logging.level.root=WARN
```

### Step 4.3: Create Staging Profile (YAML format)

Create `src/main/resources/application-staging.yml`:

```yaml
spring:
  application:
    name: config-lab-staging

app:
  notification:
    enabled: true
    sender: staging-notifications@mycompany.com
    timeout: 15s
    max-retries: 3
    email:
      host: smtp-staging.mycompany.com
      port: 587
      starttls: true
    sms:
      enabled: true
      provider: twilio-sandbox

logging:
  level:
    com.example: DEBUG
    root: INFO
```

### Step 4.4: Create Profile-Specific Beans

Create `src/main/java/com/example/configlab/service/EmailSender.java`:

```java
package com.example.configlab.service;

public interface EmailSender {
    String send(String to, String subject, String body);
    String getProviderName();
}
```

Create `src/main/java/com/example/configlab/service/DevEmailSender.java`:

```java
package com.example.configlab.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("dev")
public class DevEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(DevEmailSender.class);

    @Override
    public String send(String to, String subject, String body) {
        log.info("DEV MODE: Would send email to {} with subject: {}", to, subject);
        log.debug("Email body: {}", body);
        return "Email logged (dev mode - not actually sent)";
    }

    @Override
    public String getProviderName() {
        return "Development Console Logger";
    }
}
```

Create `src/main/java/com/example/configlab/service/ProdEmailSender.java`:

```java
package com.example.configlab.service;

import com.example.configlab.config.NotificationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("prod | staging")
public class ProdEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(ProdEmailSender.class);
    private final NotificationProperties properties;

    public ProdEmailSender(NotificationProperties properties) {
        this.properties = properties;
    }

    @Override
    public String send(String to, String subject, String body) {
        NotificationProperties.Email email = properties.getEmail();
        log.info("Sending email via {}:{} to {}", email.getHost(), email.getPort(), to);

        // In real app: use JavaMailSender to send
        return String.format("Email sent to %s via %s", to, email.getHost());
    }

    @Override
    public String getProviderName() {
        return "SMTP: " + properties.getEmail().getHost();
    }
}
```

### Step 4.5: Add Email Endpoint to Controller

Add to `ConfigController.java`:

```java
private final EmailSender emailSender;

// Update constructor to include EmailSender
public ConfigController(AppInfoService appInfoService,
                       NotificationService notificationService,
                       NotificationProperties notificationProperties,
                       EmailSender emailSender) {
    this.appInfoService = appInfoService;
    this.notificationService = notificationService;
    this.notificationProperties = notificationProperties;
    this.emailSender = emailSender;
}

@GetMapping("/email/provider")
public Map<String, String> getEmailProvider() {
    return Map.of("provider", emailSender.getProviderName());
}

@PostMapping("/email/send")
public Map<String, String> sendEmail(@RequestParam String to,
                                      @RequestParam String subject,
                                      @RequestParam String body) {
    String result = emailSender.send(to, subject, body);
    return Map.of("result", result);
}
```

### Step 4.6: Test with Different Profiles

```bash
# Run with dev profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

curl http://localhost:8080/api/config/email/provider
# Response: {"provider": "Development Console Logger"}

# Run with prod profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod

curl http://localhost:8080/api/config/email/provider
# Response: {"provider": "SMTP: smtp.sendgrid.net"}
```

---

## Part 5: Conditional Bean Configuration

### Step 5.1: Create Feature Flag Properties

Add to `NotificationProperties.java` or create new class:

```java
package com.example.configlab.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.features")
public class FeatureProperties {

    private boolean betaFeatures = false;
    private boolean analytics = true;
    private boolean darkMode = false;

    public boolean isBetaFeatures() {
        return betaFeatures;
    }

    public void setBetaFeatures(boolean betaFeatures) {
        this.betaFeatures = betaFeatures;
    }

    public boolean isAnalytics() {
        return analytics;
    }

    public void setAnalytics(boolean analytics) {
        this.analytics = analytics;
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    public void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
    }
}
```

### Step 5.2: Create Conditional Services

Create `src/main/java/com/example/configlab/service/AnalyticsService.java`:

```java
package com.example.configlab.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.features.analytics", havingValue = "true", matchIfMissing = true)
public class AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    public void trackEvent(String event, String userId) {
        log.info("Analytics: {} by user {}", event, userId);
    }
}
```

Create `src/main/java/com/example/configlab/service/BetaFeatureService.java`:

```java
package com.example.configlab.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.features.beta-features", havingValue = "true")
public class BetaFeatureService {

    private static final Logger log = LoggerFactory.getLogger(BetaFeatureService.class);

    public String getExperimentalFeature() {
        log.info("Accessing experimental feature");
        return "This is a beta feature!";
    }
}
```

### Step 5.3: Update Properties

Add to `application.properties`:

```properties
# Feature Flags
app.features.analytics=true
app.features.beta-features=false
app.features.dark-mode=false
```

Add to `application-dev.properties`:

```properties
# Enable beta features in dev
app.features.beta-features=true
```

### Step 5.4: Create Features Endpoint

Add to controller or create new one:

```java
@RestController
@RequestMapping("/api/features")
public class FeatureController {

    private final Optional<AnalyticsService> analyticsService;
    private final Optional<BetaFeatureService> betaFeatureService;

    public FeatureController(
            @Autowired(required = false) AnalyticsService analyticsService,
            @Autowired(required = false) BetaFeatureService betaFeatureService) {
        this.analyticsService = Optional.ofNullable(analyticsService);
        this.betaFeatureService = Optional.ofNullable(betaFeatureService);
    }

    @GetMapping("/status")
    public Map<String, Boolean> getFeatureStatus() {
        return Map.of(
            "analytics", analyticsService.isPresent(),
            "betaFeatures", betaFeatureService.isPresent()
        );
    }

    @GetMapping("/beta")
    public Map<String, String> getBetaFeature() {
        return betaFeatureService
            .map(service -> Map.of("feature", service.getExperimentalFeature()))
            .orElse(Map.of("error", "Beta features not enabled"));
    }

    @PostMapping("/track")
    public Map<String, String> trackEvent(@RequestParam String event,
                                           @RequestParam String userId) {
        analyticsService.ifPresent(service -> service.trackEvent(event, userId));
        return Map.of("tracked", String.valueOf(analyticsService.isPresent()));
    }
}
```

---

## Part 6: Environment Variables Override

### Step 6.1: Test Environment Variable Override

```bash
# Override properties with environment variables
APP_NOTIFICATION_SENDER=env-override@example.com \
APP_NOTIFICATION_MAX_RETRIES=10 \
./mvnw spring-boot:run

curl http://localhost:8080/api/config/notification | jq
```

### Step 6.2: Command Line Arguments

```bash
# Override with command line arguments
./mvnw spring-boot:run \
  -Dspring-boot.run.arguments="--app.notification.sender=cli-override@example.com --server.port=9090"
```

---

## Part 7: Testing

### Step 7.1: Run Application with Different Profiles

```bash
# Default
./mvnw spring-boot:run
curl http://localhost:8080/api/config/notification | jq

# Development
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
curl http://localhost:8080/api/config/notification | jq
curl http://localhost:8080/api/features/status | jq
curl http://localhost:8080/api/features/beta | jq

# Production
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
curl http://localhost:8080/api/config/notification | jq
```

---

## Part 8: Challenge Exercises

### Challenge 1: Secret Management

Create a configuration for sensitive data:
- Database credentials that should not be in properties files
- Use environment variables or external config
- Add a `/api/config/secrets-check` endpoint that shows which secrets are configured (without showing values)

### Challenge 2: Dynamic Configuration

Implement configuration that can be changed at runtime:
- Create a `@RefreshScope` bean
- Add Spring Cloud Config client dependency
- Demonstrate property refresh without restart

### Challenge 3: Configuration Validation

Enhance `NotificationProperties` with:
- Custom validation for email format
- Cross-field validation (if SMS enabled, API key required)
- Fail-fast validation on startup

### Challenge 4: Multiple Configuration Sources

Configure the application to read from:
- Default properties
- Profile-specific properties
- External file location
- Environment variables
- Show precedence order

---

## Summary

In this lab, you learned:

1. **@Value Annotation**: Reading individual properties with defaults
2. **@ConfigurationProperties**: Type-safe configuration binding with nested objects
3. **Spring Profiles**: Environment-specific configuration (dev, staging, prod)
4. **Profile-Specific Beans**: Using @Profile for conditional bean creation
5. **@ConditionalOnProperty**: Feature flags and conditional services
6. **Configuration Precedence**: Environment variables and command-line overrides

## Next Steps

In Module 4, you'll learn about database integration with Spring Data JPA.
