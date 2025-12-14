# Lab 1b: Dependency Injection Deep Dive

## Objectives

By the end of this lab, you will be able to:
- Compare constructor, setter, and field injection
- Use @Qualifier and @Primary to resolve ambiguous dependencies
- Implement custom bean scopes
- Use lifecycle callbacks with @PostConstruct and @PreDestroy
- Detect and resolve circular dependencies

## Prerequisites

- Completed Lab 1: Spring Fundamentals
- Understanding of Spring IoC container basics

## Duration

45-60 minutes

---

## Part 1: Injection Types Comparison

### Step 1.1: Create Project Structure

Create a new Spring Boot project or continue with your Lab 1 project.

Create the following package structure:
```
src/main/java/com/example/dilab/
├── DiLabApplication.java
├── injection/
├── qualifier/
├── scope/
└── lifecycle/
```

### Step 1.2: Field Injection (Not Recommended)

Create `injection/FieldInjectionExample.java`:

```java
package com.example.dilab.injection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FieldInjectionExample {

    // Field injection - NOT recommended
    @Autowired
    private MessageService messageService;

    public String getMessage() {
        return messageService.getMessage();
    }
}
```

**Problems with Field Injection:**
- Cannot create immutable objects (no `final` fields)
- Hard to test without Spring context
- Hides dependencies (not visible in constructor)
- Can lead to NullPointerException if used outside Spring

### Step 1.3: Setter Injection

Create `injection/SetterInjectionExample.java`:

```java
package com.example.dilab.injection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SetterInjectionExample {

    private MessageService messageService;

    // Setter injection - OK for optional dependencies
    @Autowired
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    public String getMessage() {
        return messageService != null ? messageService.getMessage() : "No service";
    }
}
```

**When to use Setter Injection:**
- Optional dependencies
- Dependencies that can change at runtime
- Circular dependency resolution (use sparingly)

### Step 1.4: Constructor Injection (Recommended)

Create `injection/ConstructorInjectionExample.java`:

```java
package com.example.dilab.injection;

import org.springframework.stereotype.Service;

@Service
public class ConstructorInjectionExample {

    private final MessageService messageService;

    // Constructor injection - RECOMMENDED
    // @Autowired is optional for single constructor (Spring 4.3+)
    public ConstructorInjectionExample(MessageService messageService) {
        this.messageService = messageService;
    }

    public String getMessage() {
        return messageService.getMessage();
    }
}
```

**Benefits of Constructor Injection:**
- Immutable dependencies (`final` fields)
- Required dependencies are explicit
- Easy to test (just pass mocks to constructor)
- Fails fast if dependency is missing

### Step 1.5: Create the MessageService

Create `injection/MessageService.java`:

```java
package com.example.dilab.injection;

import org.springframework.stereotype.Service;

@Service
public class MessageService {

    public String getMessage() {
        return "Hello from MessageService!";
    }
}
```

### Step 1.6: Test Injection Types

Create `injection/InjectionTestRunner.java`:

```java
package com.example.dilab.injection;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class InjectionTestRunner implements CommandLineRunner {

    private final FieldInjectionExample fieldExample;
    private final SetterInjectionExample setterExample;
    private final ConstructorInjectionExample constructorExample;

    public InjectionTestRunner(
            FieldInjectionExample fieldExample,
            SetterInjectionExample setterExample,
            ConstructorInjectionExample constructorExample) {
        this.fieldExample = fieldExample;
        this.setterExample = setterExample;
        this.constructorExample = constructorExample;
    }

    @Override
    public void run(String... args) {
        System.out.println("=== Injection Types Demo ===");
        System.out.println("Field:       " + fieldExample.getMessage());
        System.out.println("Setter:      " + setterExample.getMessage());
        System.out.println("Constructor: " + constructorExample.getMessage());
    }
}
```

---

## Part 2: @Qualifier and @Primary

### Step 2.1: Create Multiple Implementations

Create `qualifier/NotificationService.java`:

```java
package com.example.dilab.qualifier;

public interface NotificationService {
    void send(String message);
    String getType();
}
```

Create `qualifier/EmailNotificationService.java`:

```java
package com.example.dilab.qualifier;

import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService implements NotificationService {

    @Override
    public void send(String message) {
        System.out.println("EMAIL: " + message);
    }

    @Override
    public String getType() {
        return "Email";
    }
}
```

Create `qualifier/SmsNotificationService.java`:

```java
package com.example.dilab.qualifier;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary  // This will be selected by default when no qualifier is specified
public class SmsNotificationService implements NotificationService {

    @Override
    public void send(String message) {
        System.out.println("SMS: " + message);
    }

    @Override
    public String getType() {
        return "SMS";
    }
}
```

Create `qualifier/PushNotificationService.java`:

```java
package com.example.dilab.qualifier;

import org.springframework.stereotype.Service;

@Service("pushNotifier")  // Custom bean name
public class PushNotificationService implements NotificationService {

    @Override
    public void send(String message) {
        System.out.println("PUSH: " + message);
    }

    @Override
    public String getType() {
        return "Push";
    }
}
```

### Step 2.2: Use @Qualifier to Select Implementation

Create `qualifier/NotificationManager.java`:

```java
package com.example.dilab.qualifier;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationManager {

    private final NotificationService primaryService;      // Gets @Primary (SMS)
    private final NotificationService emailService;        // Gets specific impl
    private final NotificationService pushService;         // Gets by bean name
    private final List<NotificationService> allServices;   // Gets ALL implementations

    public NotificationManager(
            NotificationService primaryService,
            @Qualifier("emailNotificationService") NotificationService emailService,
            @Qualifier("pushNotifier") NotificationService pushService,
            List<NotificationService> allServices) {
        this.primaryService = primaryService;
        this.emailService = emailService;
        this.pushService = pushService;
        this.allServices = allServices;
    }

    public void sendViaDefault(String message) {
        System.out.println("Using default (@Primary): " + primaryService.getType());
        primaryService.send(message);
    }

    public void sendViaEmail(String message) {
        System.out.println("Using @Qualifier for email:");
        emailService.send(message);
    }

    public void sendViaPush(String message) {
        System.out.println("Using @Qualifier with bean name:");
        pushService.send(message);
    }

    public void sendToAll(String message) {
        System.out.println("Sending to ALL implementations:");
        allServices.forEach(service -> service.send(message));
    }
}
```

### Step 2.3: Create Custom Qualifier Annotation

Create `qualifier/NotificationType.java`:

```java
package com.example.dilab.qualifier;

import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE})
public @interface NotificationType {
    String value();
}
```

Update `EmailNotificationService.java` to use custom qualifier:

```java
package com.example.dilab.qualifier;

import org.springframework.stereotype.Service;

@Service
@NotificationType("email")  // Custom qualifier
public class EmailNotificationService implements NotificationService {
    // ... same as before
}
```

### Step 2.4: Test Qualifier Resolution

Create `qualifier/QualifierTestRunner.java`:

```java
package com.example.dilab.qualifier;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class QualifierTestRunner implements CommandLineRunner {

    private final NotificationManager manager;

    public QualifierTestRunner(NotificationManager manager) {
        this.manager = manager;
    }

    @Override
    public void run(String... args) {
        System.out.println("\n=== Qualifier Demo ===");
        manager.sendViaDefault("Hello via default!");
        System.out.println();
        manager.sendViaEmail("Hello via email!");
        System.out.println();
        manager.sendViaPush("Hello via push!");
        System.out.println();
        manager.sendToAll("Broadcast message!");
    }
}
```

---

## Part 3: Bean Scopes

### Step 3.1: Singleton Scope (Default)

Create `scope/SingletonCounter.java`:

```java
package com.example.dilab.scope;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component  // Default scope is singleton
public class SingletonCounter {

    private final AtomicInteger count = new AtomicInteger(0);
    private final String instanceId;

    public SingletonCounter() {
        this.instanceId = "Singleton-" + System.identityHashCode(this);
        System.out.println("SingletonCounter created: " + instanceId);
    }

    public int increment() {
        return count.incrementAndGet();
    }

    public String getInstanceId() {
        return instanceId;
    }
}
```

### Step 3.2: Prototype Scope

Create `scope/PrototypeCounter.java`:

```java
package com.example.dilab.scope;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
@Scope("prototype")  // New instance for each injection
public class PrototypeCounter {

    private final AtomicInteger count = new AtomicInteger(0);
    private final String instanceId;

    public PrototypeCounter() {
        this.instanceId = "Prototype-" + System.identityHashCode(this);
        System.out.println("PrototypeCounter created: " + instanceId);
    }

    public int increment() {
        return count.incrementAndGet();
    }

    public String getInstanceId() {
        return instanceId;
    }
}
```

### Step 3.3: Request Scope (Web Applications)

Create `scope/RequestScopedBean.java`:

```java
package com.example.dilab.scope;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RequestScopedBean {

    private final String requestId;
    private final LocalDateTime createdAt;

    public RequestScopedBean() {
        this.requestId = "Request-" + System.identityHashCode(this);
        this.createdAt = LocalDateTime.now();
        System.out.println("RequestScopedBean created: " + requestId);
    }

    public String getRequestId() {
        return requestId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
```

### Step 3.4: Custom Scope (Thread Scope Example)

Create `scope/ThreadScopeConfig.java`:

```java
package com.example.dilab.scope;

import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.SimpleThreadScope;

@Configuration
public class ThreadScopeConfig {

    @Bean
    public static CustomScopeConfigurer customScopeConfigurer() {
        CustomScopeConfigurer configurer = new CustomScopeConfigurer();
        configurer.addScope("thread", new SimpleThreadScope());
        return configurer;
    }
}
```

Create `scope/ThreadScopedBean.java`:

```java
package com.example.dilab.scope;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("thread")
public class ThreadScopedBean {

    private final String threadId;

    public ThreadScopedBean() {
        this.threadId = "Thread-" + Thread.currentThread().getName() +
                        "-" + System.identityHashCode(this);
        System.out.println("ThreadScopedBean created: " + threadId);
    }

    public String getThreadId() {
        return threadId;
    }
}
```

### Step 3.5: Test Scopes

Create `scope/ScopeTestRunner.java`:

```java
package com.example.dilab.scope;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
public class ScopeTestRunner implements CommandLineRunner {

    private final SingletonCounter singleton1;
    private final SingletonCounter singleton2;
    private final ObjectFactory<PrototypeCounter> prototypeFactory;

    public ScopeTestRunner(
            SingletonCounter singleton1,
            SingletonCounter singleton2,
            ObjectFactory<PrototypeCounter> prototypeFactory) {
        this.singleton1 = singleton1;
        this.singleton2 = singleton2;
        this.prototypeFactory = prototypeFactory;
    }

    @Override
    public void run(String... args) {
        System.out.println("\n=== Scope Demo ===");

        // Singleton - same instance
        System.out.println("\nSingleton scope:");
        System.out.println("  singleton1: " + singleton1.getInstanceId() +
                          " count=" + singleton1.increment());
        System.out.println("  singleton2: " + singleton2.getInstanceId() +
                          " count=" + singleton2.increment());
        System.out.println("  Same instance? " + (singleton1 == singleton2));

        // Prototype - new instance each time
        System.out.println("\nPrototype scope:");
        PrototypeCounter proto1 = prototypeFactory.getObject();
        PrototypeCounter proto2 = prototypeFactory.getObject();
        System.out.println("  proto1: " + proto1.getInstanceId() +
                          " count=" + proto1.increment());
        System.out.println("  proto2: " + proto2.getInstanceId() +
                          " count=" + proto2.increment());
        System.out.println("  Same instance? " + (proto1 == proto2));
    }
}
```

---

## Part 4: Lifecycle Callbacks

### Step 4.1: Using @PostConstruct and @PreDestroy

Create `lifecycle/DatabaseConnection.java`:

```java
package com.example.dilab.lifecycle;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

@Component
public class DatabaseConnection {

    private boolean connected = false;

    public DatabaseConnection() {
        System.out.println("1. DatabaseConnection: Constructor called");
    }

    @PostConstruct
    public void init() {
        System.out.println("2. DatabaseConnection: @PostConstruct - Opening connection...");
        // Simulate connection setup
        this.connected = true;
        System.out.println("   Connection established!");
    }

    @PreDestroy
    public void cleanup() {
        System.out.println("DatabaseConnection: @PreDestroy - Closing connection...");
        // Simulate connection cleanup
        this.connected = false;
        System.out.println("   Connection closed!");
    }

    public boolean isConnected() {
        return connected;
    }

    public void executeQuery(String query) {
        if (!connected) {
            throw new IllegalStateException("Not connected!");
        }
        System.out.println("Executing query: " + query);
    }
}
```

### Step 4.2: Using InitializingBean and DisposableBean

Create `lifecycle/CacheManager.java`:

```java
package com.example.dilab.lifecycle;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

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
```

### Step 4.3: Using @Bean initMethod and destroyMethod

Create `lifecycle/ExternalService.java`:

```java
package com.example.dilab.lifecycle;

public class ExternalService {

    private boolean running = false;

    public ExternalService() {
        System.out.println("1. ExternalService: Constructor called");
    }

    // Custom init method - no annotations needed
    public void start() {
        System.out.println("2. ExternalService: start() - Starting service...");
        running = true;
        System.out.println("   Service started!");
    }

    // Custom destroy method - no annotations needed
    public void stop() {
        System.out.println("ExternalService: stop() - Stopping service...");
        running = false;
        System.out.println("   Service stopped!");
    }

    public boolean isRunning() {
        return running;
    }
}
```

Create `lifecycle/LifecycleConfig.java`:

```java
package com.example.dilab.lifecycle;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LifecycleConfig {

    @Bean(initMethod = "start", destroyMethod = "stop")
    public ExternalService externalService() {
        return new ExternalService();
    }
}
```

### Step 4.4: Test Lifecycle Callbacks

Create `lifecycle/LifecycleTestRunner.java`:

```java
package com.example.dilab.lifecycle;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(4)
public class LifecycleTestRunner implements CommandLineRunner {

    private final DatabaseConnection dbConnection;
    private final CacheManager cacheManager;
    private final ExternalService externalService;

    public LifecycleTestRunner(
            DatabaseConnection dbConnection,
            CacheManager cacheManager,
            ExternalService externalService) {
        this.dbConnection = dbConnection;
        this.cacheManager = cacheManager;
        this.externalService = externalService;
    }

    @Override
    public void run(String... args) {
        System.out.println("\n=== Lifecycle Demo ===");
        System.out.println("All beans initialized!");
        System.out.println("  DatabaseConnection connected: " + dbConnection.isConnected());
        System.out.println("  CacheManager initialized: " + cacheManager.isInitialized());
        System.out.println("  ExternalService running: " + externalService.isRunning());

        // Use the beans
        dbConnection.executeQuery("SELECT * FROM users");
        System.out.println("  Cache timeout: " + cacheManager.get("config.timeout"));

        System.out.println("\n(Shutdown hooks will run @PreDestroy methods on application exit)");
    }
}
```

---

## Part 5: Circular Dependencies

### Step 5.1: Create a Circular Dependency Problem

Create `circular/ServiceA.java`:

```java
package com.example.dilab.circular;

import org.springframework.stereotype.Service;

@Service
public class ServiceA {

    private final ServiceB serviceB;

    // This creates a circular dependency: A -> B -> A
    public ServiceA(ServiceB serviceB) {
        this.serviceB = serviceB;
    }

    public String getName() {
        return "ServiceA";
    }

    public String callB() {
        return "A calling -> " + serviceB.getName();
    }
}
```

Create `circular/ServiceB.java`:

```java
package com.example.dilab.circular;

import org.springframework.stereotype.Service;

@Service
public class ServiceB {

    private final ServiceA serviceA;

    // Circular: B depends on A, but A depends on B
    public ServiceB(ServiceA serviceA) {
        this.serviceA = serviceA;
    }

    public String getName() {
        return "ServiceB";
    }

    public String callA() {
        return "B calling -> " + serviceA.getName();
    }
}
```

### Step 5.2: Run and See the Error

Run the application - you'll see:
```
***************************
APPLICATION FAILED TO START
***************************

The dependencies of some of the beans in the application context form a cycle:

┌─────┐
|  serviceA defined in file [...]
↑     ↓
|  serviceB defined in file [...]
└─────┘
```

### Step 5.3: Solution 1 - Use @Lazy

Update `circular/ServiceA.java`:

```java
package com.example.dilab.circular;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class ServiceA {

    private final ServiceB serviceB;

    // @Lazy breaks the cycle by creating a proxy
    public ServiceA(@Lazy ServiceB serviceB) {
        this.serviceB = serviceB;
    }

    public String getName() {
        return "ServiceA";
    }

    public String callB() {
        return "A calling -> " + serviceB.getName();
    }
}
```

### Step 5.4: Solution 2 - Use Setter Injection

Alternative approach - update `circular/ServiceB.java`:

```java
package com.example.dilab.circular;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ServiceB {

    private ServiceA serviceA;

    // Setter injection allows the bean to be created first,
    // then dependency injected after
    @Autowired
    public void setServiceA(ServiceA serviceA) {
        this.serviceA = serviceA;
    }

    public String getName() {
        return "ServiceB";
    }

    public String callA() {
        return "B calling -> " + serviceA.getName();
    }
}
```

### Step 5.5: Solution 3 - Refactor to Remove Cycle (Best)

The best solution is to refactor your code to eliminate the cycle:

```java
// Extract common functionality to a third service
@Service
public class CommonService {
    // Shared logic here
}

@Service
public class ServiceA {
    private final CommonService common;
    // A uses Common, not B
}

@Service
public class ServiceB {
    private final CommonService common;
    // B uses Common, not A
}
```

---

## Summary

In this lab, you learned:

1. **Injection Types**: Constructor injection is preferred for required dependencies
2. **@Qualifier/@Primary**: Resolve ambiguous dependencies with multiple implementations
3. **Bean Scopes**: Singleton (default), Prototype, Request, Session, and custom scopes
4. **Lifecycle Callbacks**: @PostConstruct, @PreDestroy, InitializingBean, DisposableBean
5. **Circular Dependencies**: Detection, causes, and resolution strategies

## Best Practices

| Topic | Recommendation |
|-------|----------------|
| Injection | Use constructor injection for required dependencies |
| Multiple Impls | Use @Primary for default, @Qualifier for specific |
| Scope | Use singleton unless you need instance-per-request |
| Lifecycle | Use @PostConstruct/@PreDestroy for simple cases |
| Circular Deps | Refactor code to eliminate cycles |

## Next Steps

Continue to Lab 2 to learn about building REST APIs with Spring MVC.
