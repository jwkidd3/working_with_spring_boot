# Lab 6: Service Orchestration

## Objectives

By the end of this lab, you will be able to:
- Build multiple Spring Boot microservices
- Implement REST-based service communication
- Set up asynchronous messaging with Spring Events (in-memory)
- Handle events between services
- Optionally integrate with RabbitMQ for production

## Prerequisites

- Completed Labs 1-5
- Understanding of microservices concepts

## Duration

60-75 minutes

---

## Scenario

You will build a multi-service application consisting of:
1. **Task Service** - Manages tasks and publishes events
2. **Notification Service** - Listens for events and sends notifications
3. **User Service** - Provides user information

For development, we'll use Spring's built-in `ApplicationEventPublisher` for in-memory event handling. This eliminates the need for external message brokers during development.

---

## Part 1: Project Setup

### Step 1.1: Create Project Structure

Create a parent directory for all services:

```
microservices-lab/
├── task-service/
├── notification-service/
├── user-service/
└── pom.xml (parent - optional)
```

> **Note:** For this lab, we use Spring's in-memory event system. No external services required!

---

## Part 2: Task Service

### Step 2.1: Create Task Service Project

Create `task-service/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.1</version>
        <relativePath/>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>task-service</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>task-service</name>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <dependency>
            <groupId>org.hsqldb</groupId>
            <artifactId>hsqldb</artifactId>
            <scope>runtime</scope>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

### Step 2.2: Task Service Application Properties

Create `task-service/src/main/resources/application.properties`:

```properties
server.port=8081
spring.application.name=task-service

# HSQLDB In-Memory Database
spring.datasource.url=jdbc:hsqldb:mem:taskdb
spring.datasource.driver-class-name=org.hsqldb.jdbc.JDBCDriver
spring.jpa.hibernate.ddl-auto=create-drop

# User Service URL (for REST calls)
user.service.url=http://localhost:8082

# Async event processing
spring.main.allow-bean-definition-overriding=true
```

### Step 2.3: Create Event DTOs

Create `task-service/src/main/java/com/example/taskservice/event/TaskEvent.java`:

```java
package com.example.taskservice.event;

import org.springframework.context.ApplicationEvent;
import java.time.Instant;

public class TaskEvent extends ApplicationEvent {

    private final String eventId;
    private final String eventType;
    private final Long taskId;
    private final String taskTitle;
    private final Long assigneeId;
    private final Instant timestamp;

    public TaskEvent(Object source, String eventType, Long taskId, String taskTitle, Long assigneeId) {
        super(source);
        this.eventId = java.util.UUID.randomUUID().toString();
        this.eventType = eventType;
        this.taskId = taskId;
        this.taskTitle = taskTitle;
        this.assigneeId = assigneeId;
        this.timestamp = Instant.now();
    }

    // Getters
    public String getEventId() { return eventId; }
    public String getEventType() { return eventType; }
    public Long getTaskId() { return taskId; }
    public String getTaskTitle() { return taskTitle; }
    public Long getAssigneeId() { return assigneeId; }
    public Instant getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return "TaskEvent{" +
            "eventId='" + eventId + '\'' +
            ", eventType='" + eventType + '\'' +
            ", taskId=" + taskId +
            ", taskTitle='" + taskTitle + '\'' +
            ", timestamp=" + timestamp +
            '}';
    }
}
```

### Step 2.4: Enable Async Event Processing

Create `task-service/src/main/java/com/example/taskservice/config/AsyncConfig.java`:

```java
package com.example.taskservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfig {
    // Enables async event processing
    // Events will be processed in separate threads
}
```

### Step 2.5: Event Publisher

Create `task-service/src/main/java/com/example/taskservice/event/TaskEventPublisher.java`:

```java
package com.example.taskservice.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class TaskEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(TaskEventPublisher.class);
    private final ApplicationEventPublisher eventPublisher;

    public TaskEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void publishTaskCreated(Long taskId, String title, Long assigneeId) {
        TaskEvent event = new TaskEvent(this, "TASK_CREATED", taskId, title, assigneeId);
        publish(event);
    }

    public void publishTaskAssigned(Long taskId, String title, Long assigneeId) {
        TaskEvent event = new TaskEvent(this, "TASK_ASSIGNED", taskId, title, assigneeId);
        publish(event);
    }

    public void publishTaskCompleted(Long taskId, String title, Long assigneeId) {
        TaskEvent event = new TaskEvent(this, "TASK_COMPLETED", taskId, title, assigneeId);
        publish(event);
    }

    private void publish(TaskEvent event) {
        log.info("Publishing event: {}", event);
        eventPublisher.publishEvent(event);
    }
}
```

### Step 2.6: Event Listener (In-Memory)

Create `task-service/src/main/java/com/example/taskservice/event/TaskEventListener.java`:

```java
package com.example.taskservice.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class TaskEventListener {

    private static final Logger log = LoggerFactory.getLogger(TaskEventListener.class);

    @Async
    @EventListener
    public void handleTaskEvent(TaskEvent event) {
        log.info("Received event: {}", event);

        // Simulate notification processing
        switch (event.getEventType()) {
            case "TASK_CREATED" -> notifyTaskCreated(event);
            case "TASK_ASSIGNED" -> notifyTaskAssigned(event);
            case "TASK_COMPLETED" -> notifyTaskCompleted(event);
            default -> log.warn("Unknown event type: {}", event.getEventType());
        }
    }

    private void notifyTaskCreated(TaskEvent event) {
        log.info("NOTIFICATION: New task created - '{}'", event.getTaskTitle());
        // In a real app: send email, push notification, etc.
    }

    private void notifyTaskAssigned(TaskEvent event) {
        log.info("NOTIFICATION: Task '{}' assigned to user {}",
            event.getTaskTitle(), event.getAssigneeId());
        // In a real app: notify the assignee
    }

    private void notifyTaskCompleted(TaskEvent event) {
        log.info("NOTIFICATION: Task '{}' has been completed!", event.getTaskTitle());
        // In a real app: notify stakeholders
    }
}
```

### Step 2.7: Task Entity and Repository

Create `task-service/src/main/java/com/example/taskservice/entity/Task.java`:

```java
package com.example.taskservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    private TaskStatus status = TaskStatus.TODO;

    private Long assigneeId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
    public Long getAssigneeId() { return assigneeId; }
    public void setAssigneeId(Long assigneeId) { this.assigneeId = assigneeId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
```

Create `task-service/src/main/java/com/example/taskservice/entity/TaskStatus.java`:

```java
package com.example.taskservice.entity;

public enum TaskStatus {
    TODO, IN_PROGRESS, COMPLETED
}
```

Create `task-service/src/main/java/com/example/taskservice/repository/TaskRepository.java`:

```java
package com.example.taskservice.repository;

import com.example.taskservice.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
}
```

### Step 2.8: Task Service

Create `task-service/src/main/java/com/example/taskservice/service/TaskService.java`:

```java
package com.example.taskservice.service;

import com.example.taskservice.entity.Task;
import com.example.taskservice.entity.TaskStatus;
import com.example.taskservice.event.TaskEventPublisher;
import com.example.taskservice.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskEventPublisher eventPublisher;

    public TaskService(TaskRepository taskRepository, TaskEventPublisher eventPublisher) {
        this.taskRepository = taskRepository;
        this.eventPublisher = eventPublisher;
    }

    public List<Task> findAll() {
        return taskRepository.findAll();
    }

    public Task findById(Long id) {
        return taskRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Task not found: " + id));
    }

    public Task create(String title, String description, Long assigneeId) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setAssigneeId(assigneeId);

        Task saved = taskRepository.save(task);

        // Publish event (in-memory)
        eventPublisher.publishTaskCreated(saved.getId(), saved.getTitle(), assigneeId);

        return saved;
    }

    public Task assign(Long taskId, Long assigneeId) {
        Task task = findById(taskId);
        task.setAssigneeId(assigneeId);

        Task saved = taskRepository.save(task);

        // Publish event
        eventPublisher.publishTaskAssigned(saved.getId(), saved.getTitle(), assigneeId);

        return saved;
    }

    public Task complete(Long taskId) {
        Task task = findById(taskId);
        task.setStatus(TaskStatus.COMPLETED);

        Task saved = taskRepository.save(task);

        // Publish event
        eventPublisher.publishTaskCompleted(saved.getId(), saved.getTitle(), saved.getAssigneeId());

        return saved;
    }
}
```

### Step 2.9: Task Controller

Create `task-service/src/main/java/com/example/taskservice/controller/TaskController.java`:

```java
package com.example.taskservice.controller;

import com.example.taskservice.entity.Task;
import com.example.taskservice.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<Task> getAllTasks() {
        return taskService.findAll();
    }

    @GetMapping("/{id}")
    public Task getTask(@PathVariable Long id) {
        return taskService.findById(id);
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody CreateTaskRequest request) {
        Task task = taskService.create(
            request.title(),
            request.description(),
            request.assigneeId()
        );
        return ResponseEntity.status(201).body(task);
    }

    @PostMapping("/{id}/assign")
    public Task assignTask(@PathVariable Long id, @RequestParam Long assigneeId) {
        return taskService.assign(id, assigneeId);
    }

    @PostMapping("/{id}/complete")
    public Task completeTask(@PathVariable Long id) {
        return taskService.complete(id);
    }

    public record CreateTaskRequest(String title, String description, Long assigneeId) {}
}
```

### Step 2.10: Main Application

Create `task-service/src/main/java/com/example/taskservice/TaskServiceApplication.java`:

```java
package com.example.taskservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TaskServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TaskServiceApplication.class, args);
    }
}
```

---

## Part 3: Testing the In-Memory Event System

### Step 3.1: Start the Application

```bash
cd task-service
./mvnw spring-boot:run
```

### Step 3.2: Test Event Flow

**Create a task:**
```bash
curl -X POST http://localhost:8081/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Complete Lab 6",
    "description": "Build microservices",
    "assigneeId": 1
  }'
```

Check the console logs - you should see:
```
Publishing event: TaskEvent{eventType='TASK_CREATED'...}
Received event: TaskEvent{eventType='TASK_CREATED'...}
NOTIFICATION: New task created - 'Complete Lab 6'
```

**Complete a task:**
```bash
curl -X POST http://localhost:8081/api/tasks/1/complete
```

Check console logs:
```
Publishing event: TaskEvent{eventType='TASK_COMPLETED'...}
Received event: TaskEvent{eventType='TASK_COMPLETED'...}
NOTIFICATION: Task 'Complete Lab 6' has been completed!
```

---

## Part 4: REST-Based Service Communication

### Step 4.1: Create User Service

Create `user-service/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.1</version>
        <relativePath/>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>user-service</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>user-service</name>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

### Step 4.2: User Service Properties

Create `user-service/src/main/resources/application.properties`:

```properties
server.port=8082
spring.application.name=user-service
```

### Step 4.3: User Model and Controller

Create `user-service/src/main/java/com/example/userservice/model/User.java`:

```java
package com.example.userservice.model;

public record User(Long id, String name, String email) {}
```

Create `user-service/src/main/java/com/example/userservice/controller/UserController.java`:

```java
package com.example.userservice.controller;

import com.example.userservice.model.User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final Map<Long, User> users = new ConcurrentHashMap<>();

    public UserController() {
        // Initialize with sample data
        users.put(1L, new User(1L, "John Doe", "john@example.com"));
        users.put(2L, new User(2L, "Jane Smith", "jane@example.com"));
        users.put(3L, new User(3L, "Bob Wilson", "bob@example.com"));
    }

    @GetMapping
    public java.util.Collection<User> getAllUsers() {
        return users.values();
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        User user = users.get(id);
        if (user == null) {
            throw new RuntimeException("User not found: " + id);
        }
        return user;
    }
}
```

Create `user-service/src/main/java/com/example/userservice/UserServiceApplication.java`:

```java
package com.example.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
```

### Step 4.4: Add REST Client to Task Service

Add to Task Service's event listener to call User Service:

Update `TaskEventListener.java`:

```java
package com.example.taskservice.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class TaskEventListener {

    private static final Logger log = LoggerFactory.getLogger(TaskEventListener.class);
    private final RestTemplate restTemplate;

    @Value("${user.service.url}")
    private String userServiceUrl;

    public TaskEventListener() {
        this.restTemplate = new RestTemplate();
    }

    @Async
    @EventListener
    public void handleTaskEvent(TaskEvent event) {
        log.info("Received event: {}", event);

        switch (event.getEventType()) {
            case "TASK_CREATED" -> notifyTaskCreated(event);
            case "TASK_ASSIGNED" -> notifyTaskAssigned(event);
            case "TASK_COMPLETED" -> notifyTaskCompleted(event);
            default -> log.warn("Unknown event type: {}", event.getEventType());
        }
    }

    private void notifyTaskCreated(TaskEvent event) {
        log.info("NOTIFICATION: New task created - '{}'", event.getTaskTitle());
    }

    private void notifyTaskAssigned(TaskEvent event) {
        // Call User Service to get user details
        try {
            String userEmail = getUserEmail(event.getAssigneeId());
            log.info("NOTIFICATION: Task '{}' assigned to {} ({})",
                event.getTaskTitle(), event.getAssigneeId(), userEmail);
        } catch (Exception e) {
            log.warn("Could not fetch user details: {}", e.getMessage());
            log.info("NOTIFICATION: Task '{}' assigned to user {}",
                event.getTaskTitle(), event.getAssigneeId());
        }
    }

    private void notifyTaskCompleted(TaskEvent event) {
        log.info("NOTIFICATION: Task '{}' has been completed!", event.getTaskTitle());
    }

    private String getUserEmail(Long userId) {
        if (userId == null) return "unassigned";

        String url = userServiceUrl + "/api/users/" + userId;
        var response = restTemplate.getForObject(url, UserResponse.class);
        return response != null ? response.email() : "unknown";
    }

    record UserResponse(Long id, String name, String email) {}
}
```

### Step 4.5: Test Multi-Service Communication

Start both services:

```bash
# Terminal 1: User Service
cd user-service
./mvnw spring-boot:run

# Terminal 2: Task Service
cd task-service
./mvnw spring-boot:run
```

Test:
```bash
# Assign task to user 1
curl -X POST "http://localhost:8081/api/tasks/1/assign?assigneeId=1"
```

You should see in Task Service logs:
```
NOTIFICATION: Task 'Complete Lab 6' assigned to 1 (john@example.com)
```

---

## Part 5: Challenge Exercises

### Challenge 1: Add Separate Notification Service

Create a standalone Notification Service that:
- Runs on port 8083
- Listens for events via REST webhook or shared event queue
- Sends email notifications (simulated)

### Challenge 2: Circuit Breaker Pattern

Implement resilience patterns for REST calls:
- Add Spring Cloud Circuit Breaker
- Handle User Service failures gracefully
- Implement fallback responses

### Challenge 3: Event Sourcing

Implement event sourcing:
- Store all events in an event store
- Rebuild task state from events
- Add event replay capability

### Challenge 4: WebSocket Notifications

Implement real-time notifications:
- Add WebSocket support
- Push events to connected clients
- Create a simple web UI to display notifications

---

## Summary

In this lab, you learned:

1. **Spring Events**: Using `ApplicationEventPublisher` for in-memory messaging
2. **Async Processing**: Enabling async event handling with `@Async`
3. **REST Communication**: Calling other services via `RestTemplate`
4. **Event-Driven Architecture**: Decoupling services with events

## Next Steps

In Module 7, you'll learn about additional topics including testing strategies and deployment.
