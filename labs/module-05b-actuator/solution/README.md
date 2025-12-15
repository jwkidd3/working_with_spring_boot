# Lab 5b - Spring Boot Actuator & Monitoring (Solution)

## Overview
This is the complete solution for the Spring Boot Actuator and Monitoring lab, demonstrating how to monitor and manage a Spring Boot application using Actuator endpoints and Micrometer metrics.

## Implementation Details

### Dependencies
- `spring-boot-starter-actuator` - Actuator endpoints for monitoring
- `micrometer-registry-prometheus` - Prometheus metrics export
- `spring-boot-starter-web` - REST API support
- `spring-boot-starter-data-jpa` - Database access
- `spring-boot-starter-validation` - Request validation
- `hsqldb` - In-memory database

### Project Structure

```
src/main/java/com/example/taskapi/
├── TaskApiApplication.java          # Main application class
├── config/
│   └── DataInitializer.java         # Sample data initialization
├── controller/
│   └── TaskController.java          # REST endpoints
├── dto/
│   ├── CreateTaskRequest.java       # Request DTO for creating tasks
│   └── UpdateTaskRequest.java       # Request DTO for updating tasks
├── entity/
│   ├── Task.java                    # Task entity
│   ├── TaskStatus.java              # Status enumeration
│   └── TaskPriority.java            # Priority enumeration
├── exception/
│   ├── TaskNotFoundException.java   # Custom exception
│   └── GlobalExceptionHandler.java  # Exception handling
├── health/
│   └── TaskServiceHealthIndicator.java  # Custom health indicator
├── repository/
│   └── TaskRepository.java          # Data access
└── service/
    └── TaskService.java             # Business logic with metrics
```

### Actuator Features

#### 1. Custom Health Indicator
**File:** `health/TaskServiceHealthIndicator.java`

- Monitors task service health
- Reports task counts by status
- Warns about overdue tasks
- Changes status to DEGRADED when >10 overdue tasks

**Implementation highlights:**
```java
@Component
public class TaskServiceHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        // Check task counts and overdue tasks
        // Return UP, DEGRADED, or DOWN status
    }
}
```

#### 2. Micrometer Metrics
**File:** `service/TaskService.java`

**Counters:**
- `tasks.created` - Total tasks created
- `tasks.completed` - Total tasks completed
- `tasks.deleted` - Total tasks deleted

**Timer:**
- `tasks.creation.time` - Time to create a task

**Gauges:**
- `tasks.active` - Current active tasks (TODO + IN_PROGRESS)
- `tasks.overdue` - Current overdue tasks

**Implementation highlights:**
```java
// Counters
private final Counter tasksCreatedCounter;
private final Counter tasksCompletedCounter;
private final Counter tasksDeletedCounter;

// Timer
private final Timer taskCreationTimer;

// Constructor initializes metrics
public TaskService(TaskRepository taskRepository, MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;

    // Register counters
    this.tasksCreatedCounter = Counter.builder("tasks.created")
        .description("Total number of tasks created")
        .tag("service", "task-api")
        .register(meterRegistry);

    // Register gauges
    meterRegistry.gauge("tasks.active", this, service -> {
        long todo = taskRepository.countByStatus(TaskStatus.TODO);
        long inProgress = taskRepository.countByStatus(TaskStatus.IN_PROGRESS);
        return todo + inProgress;
    });
}
```

#### 3. Actuator Configuration
**File:** `application.properties`

```properties
# Expose endpoints
management.endpoints.web.exposure.include=health,info,metrics,prometheus

# Health details
management.endpoint.health.show-details=always

# Health groups for Kubernetes
management.endpoint.health.group.liveness.include=ping
management.endpoint.health.group.readiness.include=db,taskServiceHealthIndicator

# Application info
info.app.name=Task Management API
info.app.description=Spring Boot Actuator and Monitoring Lab
info.app.version=1.0.0
```

### REST API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/tasks` | Get all tasks |
| GET | `/api/tasks/{id}` | Get task by ID |
| POST | `/api/tasks` | Create new task |
| PUT | `/api/tasks/{id}` | Update existing task |
| DELETE | `/api/tasks/{id}` | Delete task |
| GET | `/api/tasks/overdue` | Get overdue tasks |

### Actuator Endpoints

| Endpoint | Description |
|----------|-------------|
| `/actuator` | List all available endpoints |
| `/actuator/health` | Application health status |
| `/actuator/health/liveness` | Liveness probe (for Kubernetes) |
| `/actuator/health/readiness` | Readiness probe (for Kubernetes) |
| `/actuator/info` | Application information |
| `/actuator/metrics` | List all available metrics |
| `/actuator/metrics/{metric}` | Specific metric value |
| `/actuator/prometheus` | Prometheus-formatted metrics |

## Running the Application

### Build and Run
```bash
mvn clean install
mvn spring-boot:run
```

The application will start on port 8080 with sample data already loaded.

## Testing the Implementation

### 1. Health Checks

**Overall Health:**
```bash
curl http://localhost:8080/actuator/health | jq
```

**Expected Response:**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": { ... }
    },
    "taskServiceHealthIndicator": {
      "status": "UP",
      "details": {
        "totalTasks": 10,
        "todoTasks": 4,
        "inProgressTasks": 2,
        "completedTasks": 2,
        "cancelledTasks": 1,
        "overdueTasks": 2,
        "warning": "There are 2 overdue task(s)"
      }
    }
  }
}
```

**Liveness Probe:**
```bash
curl http://localhost:8080/actuator/health/liveness
```

**Readiness Probe:**
```bash
curl http://localhost:8080/actuator/health/readiness
```

### 2. Application Info

```bash
curl http://localhost:8080/actuator/info | jq
```

### 3. Metrics

**List all metrics:**
```bash
curl http://localhost:8080/actuator/metrics | jq
```

**Custom task metrics:**
```bash
# Tasks created
curl http://localhost:8080/actuator/metrics/tasks.created | jq

# Tasks completed
curl http://localhost:8080/actuator/metrics/tasks.completed | jq

# Active tasks (gauge)
curl http://localhost:8080/actuator/metrics/tasks.active | jq

# Overdue tasks (gauge)
curl http://localhost:8080/actuator/metrics/tasks.overdue | jq

# Task creation time
curl http://localhost:8080/actuator/metrics/tasks.creation.time | jq
```

**JVM metrics:**
```bash
curl http://localhost:8080/actuator/metrics/jvm.memory.used | jq
curl http://localhost:8080/actuator/metrics/jvm.threads.live | jq
```

**HTTP metrics:**
```bash
curl http://localhost:8080/actuator/metrics/http.server.requests | jq
```

### 4. Prometheus Metrics

```bash
curl http://localhost:8080/actuator/prometheus
```

### 5. Create Tasks and Watch Metrics

**Create a task:**
```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Actuator Metrics",
    "description": "Create a task to see metrics in action",
    "status": "TODO",
    "priority": "HIGH",
    "dueDate": "2025-12-20"
  }'
```

**Watch metrics change:**
```bash
# Should increment by 1
curl http://localhost:8080/actuator/metrics/tasks.created | jq '.measurements[0].value'

# Should show increased active tasks
curl http://localhost:8080/actuator/metrics/tasks.active | jq '.measurements[0].value'
```

**Complete a task:**
```bash
curl -X PUT http://localhost:8080/api/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{
    "status": "COMPLETED"
  }'
```

**Check completion metric:**
```bash
curl http://localhost:8080/actuator/metrics/tasks.completed | jq '.measurements[0].value'
```

## Key Learning Points

### 1. Health Indicators
- Custom health indicators provide domain-specific health information
- Can return UP, DOWN, or custom statuses like DEGRADED
- Useful for Kubernetes liveness and readiness probes

### 2. Micrometer Metrics
- **Counters**: Monotonically increasing values (created, completed, deleted)
- **Gauges**: Current snapshot values (active tasks, overdue tasks)
- **Timers**: Measure duration of operations (task creation time)

### 3. Prometheus Integration
- Micrometer automatically converts metrics to Prometheus format
- Metrics include labels/tags for filtering
- Ready for Prometheus scraping and Grafana visualization

### 4. Production Monitoring
- Actuator endpoints should be secured in production
- Consider running on separate management port
- Use health groups for container orchestration
- Monitor custom business metrics alongside technical metrics

## Security Considerations

For production deployments:

1. **Secure actuator endpoints:**
```properties
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=when-authorized
```

2. **Use separate management port:**
```properties
management.server.port=9090
management.server.address=127.0.0.1
```

3. **Add Spring Security:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

## Next Steps

1. Integrate with Prometheus and Grafana for visualization
2. Set up alerting based on custom metrics
3. Add more custom metrics for business KPIs
4. Create custom actuator endpoints
5. Implement distributed tracing with Micrometer Tracing

## Troubleshooting

**Metrics not appearing:**
- Ensure MeterRegistry is properly injected
- Check that metrics are registered in constructor
- Verify actuator dependency is included

**Health indicator not showing:**
- Confirm @Component annotation is present
- Check HealthIndicator interface is implemented
- Verify show-details is set to always

**Prometheus endpoint empty:**
- Ensure micrometer-registry-prometheus dependency is included
- Check prometheus is in exposed endpoints list
