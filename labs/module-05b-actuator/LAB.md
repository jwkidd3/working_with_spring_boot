# Lab 5b: Spring Boot Actuator & Monitoring

## Objectives

By the end of this lab, you will be able to:
- Configure Spring Boot Actuator endpoints
- Create custom health indicators
- Add custom metrics with Micrometer
- Monitor application performance
- Integrate with Prometheus (optional)

## Prerequisites

- Completed Labs 1-5a
- Running Task API application

## Duration

45-60 minutes

---

## Scenario

You need to make your Task Management API production-ready by adding monitoring capabilities. Operations teams need to check application health, track business metrics, and monitor performance.

---

## Part 1: Adding Actuator

### Step 1.1: Add Dependencies

Add to your `pom.xml`:

```xml
<!-- Actuator for monitoring -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

<!-- Micrometer for metrics -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

### Step 1.2: Basic Actuator Configuration

Update `src/main/resources/application.properties`:

```properties
# Actuator Configuration
management.endpoints.web.exposure.include=health,info,metrics,prometheus,env,loggers,beans,mappings
management.endpoint.health.show-details=always
management.endpoint.health.show-components=always

# Application Info
management.info.env.enabled=true
management.info.java.enabled=true
management.info.os.enabled=true

info.app.name=Task Management API
info.app.description=RESTful API for task management
info.app.version=1.0.0
info.app.encoding=UTF-8
info.app.java.version=${java.version}
```

### Step 1.3: Test Default Endpoints

```bash
./mvnw spring-boot:run

# List all actuator endpoints
curl http://localhost:8080/actuator | jq

# Health check
curl http://localhost:8080/actuator/health | jq

# Application info
curl http://localhost:8080/actuator/info | jq

# All metrics
curl http://localhost:8080/actuator/metrics | jq
```

---

## Part 2: Understanding Health Endpoints

### Step 2.1: Health Check Response

```bash
curl http://localhost:8080/actuator/health | jq
```

Expected response:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "HSQLDB",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 499963174912,
        "free": 234567890123,
        "threshold": 10485760,
        "path": "/path/to/app",
        "exists": true
      }
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

### Step 2.2: Health Status Values

| Status | Description |
|--------|-------------|
| `UP` | Component is functioning normally |
| `DOWN` | Component is not working |
| `OUT_OF_SERVICE` | Component is temporarily unavailable |
| `UNKNOWN` | Status cannot be determined |

---

## Part 3: Custom Health Indicators

### Step 3.1: Create Task Service Health Indicator

Create `src/main/java/com/example/taskapi/health/TaskServiceHealthIndicator.java`:

```java
package com.example.taskapi.health;

import com.example.taskapi.entity.TaskStatus;
import com.example.taskapi.repository.TaskRepository;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TaskServiceHealthIndicator implements HealthIndicator {

    private final TaskRepository taskRepository;

    public TaskServiceHealthIndicator(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public Health health() {
        try {
            long totalTasks = taskRepository.count();
            long todoTasks = taskRepository.countByStatus(TaskStatus.TODO);
            long inProgressTasks = taskRepository.countByStatus(TaskStatus.IN_PROGRESS);
            long completedTasks = taskRepository.countByStatus(TaskStatus.COMPLETED);
            long overdueTasks = countOverdueTasks();

            Health.Builder builder = Health.up()
                .withDetail("totalTasks", totalTasks)
                .withDetail("todoTasks", todoTasks)
                .withDetail("inProgressTasks", inProgressTasks)
                .withDetail("completedTasks", completedTasks)
                .withDetail("overdueTasks", overdueTasks);

            // Warning if too many overdue tasks
            if (overdueTasks > 10) {
                return Health.status("WARNING")
                    .withDetail("totalTasks", totalTasks)
                    .withDetail("overdueTasks", overdueTasks)
                    .withDetail("message", "High number of overdue tasks!")
                    .build();
            }

            // Down if database query fails implicitly (caught below)
            return builder.build();

        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .withException(e)
                .build();
        }
    }

    private long countOverdueTasks() {
        return taskRepository.findAll().stream()
            .filter(task -> task.getDueDate() != null)
            .filter(task -> task.getStatus() != TaskStatus.COMPLETED)
            .filter(task -> task.getDueDate().isBefore(LocalDateTime.now()))
            .count();
    }
}
```

### Step 3.2: Create External Service Health Indicator

Create `src/main/java/com/example/taskapi/health/ExternalApiHealthIndicator.java`:

```java
package com.example.taskapi.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ExternalApiHealthIndicator implements HealthIndicator {

    private final String externalServiceUrl = "https://httpstat.us/200";

    @Override
    public Health health() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(externalServiceUrl, String.class);

            return Health.up()
                .withDetail("service", "external-api")
                .withDetail("url", externalServiceUrl)
                .withDetail("status", "reachable")
                .build();

        } catch (Exception e) {
            return Health.down()
                .withDetail("service", "external-api")
                .withDetail("url", externalServiceUrl)
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

### Step 3.3: Test Custom Health Indicators

```bash
curl http://localhost:8080/actuator/health | jq
```

Expected response:
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "diskSpace": { "status": "UP" },
    "externalApi": {
      "status": "UP",
      "details": {
        "service": "external-api",
        "url": "https://httpstat.us/200",
        "status": "reachable"
      }
    },
    "ping": { "status": "UP" },
    "taskService": {
      "status": "UP",
      "details": {
        "totalTasks": 5,
        "todoTasks": 2,
        "inProgressTasks": 1,
        "completedTasks": 2,
        "overdueTasks": 0
      }
    }
  }
}
```

---

## Part 4: Health Groups

### Step 4.1: Configure Health Groups

Update `application.properties`:

```properties
# Health Groups
management.endpoint.health.group.liveness.include=ping
management.endpoint.health.group.readiness.include=db,taskService
management.endpoint.health.group.external.include=externalApi

# Show details for groups
management.endpoint.health.group.liveness.show-details=always
management.endpoint.health.group.readiness.show-details=always
management.endpoint.health.group.external.show-details=always
```

### Step 4.2: Test Health Groups

```bash
# Liveness probe (is the app running?)
curl http://localhost:8080/actuator/health/liveness | jq

# Readiness probe (is the app ready to serve traffic?)
curl http://localhost:8080/actuator/health/readiness | jq

# External dependencies
curl http://localhost:8080/actuator/health/external | jq
```

---

## Part 5: Custom Metrics with Micrometer

### Step 5.1: Add Metrics to Task Service

Update `src/main/java/com/example/taskapi/service/TaskService.java`:

```java
package com.example.taskapi.service;

import com.example.taskapi.dto.CreateTaskRequest;
import com.example.taskapi.dto.UpdateTaskRequest;
import com.example.taskapi.entity.Task;
import com.example.taskapi.entity.TaskPriority;
import com.example.taskapi.entity.TaskStatus;
import com.example.taskapi.exception.TaskNotFoundException;
import com.example.taskapi.repository.TaskRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Transactional
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;

    // Counters
    private final Counter tasksCreatedCounter;
    private final Counter tasksCompletedCounter;
    private final Counter tasksDeletedCounter;

    // Timer
    private final Timer taskCreationTimer;

    // For gauges (needs to be held by the service)
    private final AtomicInteger activeTasks = new AtomicInteger(0);

    public TaskService(TaskRepository taskRepository, MeterRegistry registry) {
        this.taskRepository = taskRepository;

        // Counter - tracks total number of events
        this.tasksCreatedCounter = Counter.builder("tasks.created.total")
            .description("Total number of tasks created")
            .tag("type", "task")
            .register(registry);

        this.tasksCompletedCounter = Counter.builder("tasks.completed.total")
            .description("Total number of tasks completed")
            .tag("type", "task")
            .register(registry);

        this.tasksDeletedCounter = Counter.builder("tasks.deleted.total")
            .description("Total number of tasks deleted")
            .tag("type", "task")
            .register(registry);

        // Timer - tracks duration of operations
        this.taskCreationTimer = Timer.builder("tasks.creation.time")
            .description("Time taken to create a task")
            .register(registry);

        // Gauge - tracks current value
        Gauge.builder("tasks.active.count", this, TaskService::getActiveTaskCount)
            .description("Current number of active (non-completed) tasks")
            .register(registry);

        Gauge.builder("tasks.overdue.count", this, TaskService::getOverdueTaskCount)
            .description("Current number of overdue tasks")
            .register(registry);

        // Counter with tags for different priorities
        for (TaskPriority priority : TaskPriority.values()) {
            Counter.builder("tasks.by.priority")
                .description("Tasks created by priority")
                .tag("priority", priority.name())
                .register(registry);
        }
    }

    private double getActiveTaskCount() {
        try {
            return taskRepository.countByStatus(TaskStatus.TODO) +
                   taskRepository.countByStatus(TaskStatus.IN_PROGRESS);
        } catch (Exception e) {
            return 0;
        }
    }

    private double getOverdueTaskCount() {
        try {
            return taskRepository.findAll().stream()
                .filter(task -> task.getDueDate() != null)
                .filter(task -> task.getStatus() != TaskStatus.COMPLETED)
                .filter(task -> task.getDueDate().isBefore(LocalDateTime.now()))
                .count();
        } catch (Exception e) {
            return 0;
        }
    }

    @Transactional(readOnly = true)
    public Page<Task> findAll(Pageable pageable) {
        return taskRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Task findById(Long id) {
        return taskRepository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Task> findByStatus(TaskStatus status) {
        return taskRepository.findByStatus(status);
    }

    public Task create(CreateTaskRequest request) {
        return taskCreationTimer.record(() -> {
            Task task = new Task();
            task.setTitle(request.getTitle());
            task.setDescription(request.getDescription());

            if (request.getPriority() != null) {
                task.setPriority(request.getPriority());
            }

            if (request.getDueDate() != null) {
                task.setDueDate(request.getDueDate());
            }

            Task saved = taskRepository.save(task);

            // Increment counter
            tasksCreatedCounter.increment();

            log.info("Task created: {} (ID: {})", saved.getTitle(), saved.getId());
            return saved;
        });
    }

    public Task update(Long id, UpdateTaskRequest request) {
        Task task = findById(id);

        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }

        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }

        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }

        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }

        return taskRepository.save(task);
    }

    public Task updateStatus(Long id, TaskStatus newStatus) {
        Task task = findById(id);
        TaskStatus oldStatus = task.getStatus();
        task.setStatus(newStatus);

        Task saved = taskRepository.save(task);

        // Track completions
        if (newStatus == TaskStatus.COMPLETED && oldStatus != TaskStatus.COMPLETED) {
            tasksCompletedCounter.increment();
            log.info("Task completed: {} (ID: {})", saved.getTitle(), saved.getId());
        }

        return saved;
    }

    public void delete(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException(id);
        }
        taskRepository.deleteById(id);
        tasksDeletedCounter.increment();
        log.info("Task deleted: ID {}", id);
    }

    @Transactional(readOnly = true)
    public long countByStatus(TaskStatus status) {
        return taskRepository.countByStatus(status);
    }
}
```

### Step 5.2: Add Repository Methods

Update `TaskRepository.java`:

```java
package com.example.taskapi.repository;

import com.example.taskapi.entity.Task;
import com.example.taskapi.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByStatus(TaskStatus status);

    long countByStatus(TaskStatus status);
}
```

### Step 5.3: Test Custom Metrics

```bash
# List all metrics
curl http://localhost:8080/actuator/metrics | jq

# Get specific metric
curl http://localhost:8080/actuator/metrics/tasks.created.total | jq

# Get active task count
curl http://localhost:8080/actuator/metrics/tasks.active.count | jq

# Get task creation time
curl http://localhost:8080/actuator/metrics/tasks.creation.time | jq

# Create some tasks and check metrics
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title": "Test metrics"}'

curl http://localhost:8080/actuator/metrics/tasks.created.total | jq
```

---

## Part 6: Prometheus Integration

### Step 6.1: Test Prometheus Endpoint

```bash
curl http://localhost:8080/actuator/prometheus
```

Expected output (partial):
```
# HELP tasks_created_total Total number of tasks created
# TYPE tasks_created_total counter
tasks_created_total{type="task",} 5.0

# HELP tasks_active_count Current number of active (non-completed) tasks
# TYPE tasks_active_count gauge
tasks_active_count 3.0

# HELP tasks_creation_time_seconds Time taken to create a task
# TYPE tasks_creation_time_seconds summary
tasks_creation_time_seconds_count 5.0
tasks_creation_time_seconds_sum 0.123456789
```

---

## Part 7: Loggers Endpoint

### Step 7.1: View Logger Levels

```bash
# Get all loggers
curl http://localhost:8080/actuator/loggers | jq

# Get specific logger
curl http://localhost:8080/actuator/loggers/com.example.taskapi | jq
```

### Step 7.2: Change Logger Level at Runtime

```bash
# Set to DEBUG
curl -X POST http://localhost:8080/actuator/loggers/com.example.taskapi \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel": "DEBUG"}'

# Verify change
curl http://localhost:8080/actuator/loggers/com.example.taskapi | jq

# Reset to default
curl -X POST http://localhost:8080/actuator/loggers/com.example.taskapi \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel": null}'
```

---

## Part 8: Securing Actuator Endpoints

### Step 8.1: Configure Security for Actuator

Add to `application.properties`:

```properties
# Actuator Security (when Spring Security is present)
management.endpoints.web.base-path=/manage
management.endpoints.web.exposure.include=health,info,metrics,prometheus

# Separate port for actuator (optional)
# management.server.port=9090
```

### Step 8.2: Security Configuration (if using Spring Security)

```java
@Bean
public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
    http
        .securityMatcher("/manage/**")
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/manage/health/**").permitAll()
            .requestMatchers("/manage/info").permitAll()
            .requestMatchers("/manage/**").hasRole("ADMIN")
        );
    return http.build();
}
```

---

## Part 9: Challenge Exercises

### Challenge 1: Business Metrics Dashboard

Create additional metrics:
- Average task completion time
- Task completion rate (completed/total)
- Tasks per priority breakdown
- Most productive hours (tasks completed by hour)

### Challenge 2: Custom Endpoint

Create a custom actuator endpoint:

```java
@Endpoint(id = "taskstats")
@Component
public class TaskStatsEndpoint {

    @ReadOperation
    public Map<String, Object> taskStats() {
        // Return custom statistics
    }
}
```

### Challenge 3: Alerting Thresholds

Implement health indicator that goes DOWN when:
- Overdue tasks exceed 20
- Task creation rate drops to 0 for 1 hour
- Database connection pool is exhausted

### Challenge 4: Distributed Tracing

Add Spring Cloud Sleuth/Micrometer Tracing:
- Add trace IDs to logs
- Track requests across services
- Export traces to Zipkin

---

## Summary

In this lab, you learned:

1. **Actuator Setup**: Enabling and configuring management endpoints
2. **Health Indicators**: Built-in and custom health checks
3. **Health Groups**: Organizing health checks for Kubernetes probes
4. **Custom Metrics**: Counters, Gauges, and Timers with Micrometer
5. **Prometheus Integration**: Exposing metrics for scraping
6. **Runtime Management**: Changing log levels without restart

## Key Endpoints

| Endpoint | Purpose |
|----------|---------|
| `/actuator/health` | Application health status |
| `/actuator/info` | Application information |
| `/actuator/metrics` | Application metrics |
| `/actuator/prometheus` | Prometheus-format metrics |
| `/actuator/loggers` | Logger configuration |
| `/actuator/env` | Environment properties |

## Next Steps

In Lab 6, you'll secure your API with Spring Security and JWT authentication.
