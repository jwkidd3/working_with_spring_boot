# Module 05b - Actuator & Monitoring Lab
## Project Summary

## Overview
Complete implementation of Lab 5b (Spring Boot Actuator & Monitoring) with both starter and solution projects.

## Location
`/Users/jwkidd3/classes_in_development/working_with_spring_boot/labs/module-05b-actuator/`

## Project Structure

```
module-05b-actuator/
├── LAB.md                          # Existing lab guide (comprehensive)
├── INSTRUCTOR_NOTES.md             # Teaching guide and key points
├── PROJECT_SUMMARY.md              # This file
│
├── starter/                        # Starting point for students
│   ├── README.md                   # Student instructions
│   ├── pom.xml                     # Basic dependencies (NO actuator)
│   └── src/main/
│       ├── java/com/example/taskapi/
│       │   ├── TaskApiApplication.java
│       │   ├── entity/
│       │   │   ├── Task.java
│       │   │   ├── TaskStatus.java
│       │   │   └── TaskPriority.java
│       │   └── repository/
│       │       └── TaskRepository.java
│       └── resources/
│           └── application.properties
│
└── solution/                       # Complete implementation
    ├── README.md                   # Solution documentation
    ├── pom.xml                     # Includes actuator & micrometer
    └── src/main/
        ├── java/com/example/taskapi/
        │   ├── TaskApiApplication.java
        │   ├── config/
        │   │   └── DataInitializer.java
        │   ├── controller/
        │   │   └── TaskController.java
        │   ├── dto/
        │   │   ├── CreateTaskRequest.java
        │   │   └── UpdateTaskRequest.java
        │   ├── entity/
        │   │   ├── Task.java
        │   │   ├── TaskStatus.java
        │   │   └── TaskPriority.java
        │   ├── exception/
        │   │   ├── TaskNotFoundException.java
        │   │   └── GlobalExceptionHandler.java
        │   ├── health/
        │   │   └── TaskServiceHealthIndicator.java
        │   ├── repository/
        │   │   └── TaskRepository.java
        │   └── service/
        │       └── TaskService.java
        └── resources/
            └── application.properties
```

## Files Created

### Starter Project (8 files)
1. **pom.xml** - Spring Boot 3.2.1, Web, JPA, Validation, HSQLDB (no actuator)
2. **TaskApiApplication.java** - Main application class
3. **Task.java** - Task entity with full field set
4. **TaskStatus.java** - Enum (TODO, IN_PROGRESS, COMPLETED, CANCELLED)
5. **TaskPriority.java** - Enum (LOW, MEDIUM, HIGH, CRITICAL)
6. **TaskRepository.java** - JPA repository with countByStatus()
7. **application.properties** - HSQLDB config only
8. **README.md** - Instructions for students

### Solution Project (18 files)
1. **pom.xml** - Adds spring-boot-starter-actuator & micrometer-registry-prometheus
2. **TaskApiApplication.java** - Main application class
3. **Task.java** - Task entity (same as starter)
4. **TaskStatus.java** - Status enum (same as starter)
5. **TaskPriority.java** - Priority enum (same as starter)
6. **TaskRepository.java** - Enhanced with findOverdueTasks()
7. **TaskServiceHealthIndicator.java** - Custom health indicator
8. **TaskService.java** - Service with Micrometer metrics
9. **TaskController.java** - REST controller with CRUD
10. **CreateTaskRequest.java** - DTO for creating tasks
11. **UpdateTaskRequest.java** - DTO for updating tasks
12. **TaskNotFoundException.java** - Custom exception
13. **GlobalExceptionHandler.java** - Exception handling
14. **DataInitializer.java** - Sample data with overdue tasks
15. **application.properties** - Full actuator configuration
16. **README.md** - Complete solution documentation

## Key Features Implemented

### 1. Custom Health Indicator
**File:** `health/TaskServiceHealthIndicator.java`

```java
@Component
public class TaskServiceHealthIndicator implements HealthIndicator
```

**Features:**
- Shows task counts by status (TODO, IN_PROGRESS, COMPLETED, CANCELLED)
- Displays overdue task count
- Warning message when overdue tasks exist
- DEGRADED status when >10 overdue tasks
- DOWN status on database errors

**Response Example:**
```json
{
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
```

### 2. Micrometer Metrics
**File:** `service/TaskService.java`

**Counters:**
- `tasks.created` - Total tasks created
- `tasks.completed` - Total tasks completed
- `tasks.deleted` - Total tasks deleted

**Timer:**
- `tasks.creation.time` - Duration of task creation operations

**Gauges:**
- `tasks.active` - Current count of active tasks (TODO + IN_PROGRESS)
- `tasks.overdue` - Current count of overdue tasks

**Implementation:**
```java
// Counter
this.tasksCreatedCounter = Counter.builder("tasks.created")
    .description("Total number of tasks created")
    .tag("service", "task-api")
    .register(meterRegistry);

// Gauge
meterRegistry.gauge("tasks.active", this, service -> {
    long todo = taskRepository.countByStatus(TaskStatus.TODO);
    long inProgress = taskRepository.countByStatus(TaskStatus.IN_PROGRESS);
    return todo + inProgress;
});

// Timer usage
return taskCreationTimer.record(() -> {
    // task creation logic
});
```

### 3. Actuator Configuration
**File:** `application.properties`

```properties
# Expose endpoints
management.endpoints.web.exposure.include=health,info,metrics,prometheus

# Show detailed health information
management.endpoint.health.show-details=always
management.endpoint.health.show-components=always

# Health groups for Kubernetes
management.endpoint.health.group.liveness.include=ping
management.endpoint.health.group.liveness.show-details=always

management.endpoint.health.group.readiness.include=db,taskServiceHealthIndicator
management.endpoint.health.group.readiness.show-details=always

# Application info
info.app.name=Task Management API
info.app.description=Spring Boot Actuator and Monitoring Lab
info.app.version=1.0.0
```

### 4. REST API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/tasks` | Get all tasks |
| GET | `/api/tasks/{id}` | Get task by ID |
| POST | `/api/tasks` | Create new task |
| PUT | `/api/tasks/{id}` | Update task |
| DELETE | `/api/tasks/{id}` | Delete task |
| GET | `/api/tasks/overdue` | Get overdue tasks |

### 5. Actuator Endpoints

| Endpoint | Description |
|----------|-------------|
| `/actuator` | List all endpoints |
| `/actuator/health` | Overall health status |
| `/actuator/health/liveness` | Liveness probe |
| `/actuator/health/readiness` | Readiness probe |
| `/actuator/info` | Application information |
| `/actuator/metrics` | All available metrics |
| `/actuator/metrics/{name}` | Specific metric |
| `/actuator/prometheus` | Prometheus-formatted metrics |

### 6. Sample Data
**File:** `config/DataInitializer.java`

Creates 10 sample tasks including:
- 4 TODO tasks
- 2 IN_PROGRESS tasks
- 2 COMPLETED tasks
- 1 CANCELLED task
- 2 Overdue tasks (for testing health indicator)

## Technology Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Spring Boot | 3.2.1 | Framework |
| Java | 17 | Language |
| Spring Boot Actuator | 3.2.1 | Monitoring |
| Micrometer | (via Boot) | Metrics |
| Micrometer Prometheus | (via Boot) | Prometheus export |
| Spring Data JPA | 3.2.1 | Data access |
| Spring Validation | 3.2.1 | Request validation |
| HSQLDB | (latest) | In-memory database |

## Testing the Implementation

### Build and Run
```bash
cd /Users/jwkidd3/classes_in_development/working_with_spring_boot/labs/module-05b-actuator/solution
mvn clean install
mvn spring-boot:run
```

### Test Health Endpoint
```bash
curl http://localhost:8080/actuator/health | jq
```

### Test Custom Metrics
```bash
# List all metrics
curl http://localhost:8080/actuator/metrics | jq

# Check tasks created
curl http://localhost:8080/actuator/metrics/tasks.created | jq

# Check active tasks
curl http://localhost:8080/actuator/metrics/tasks.active | jq
```

### Create Task and Watch Metrics
```bash
# Create a task
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Metrics",
    "description": "Testing actuator metrics",
    "status": "TODO",
    "priority": "HIGH",
    "dueDate": "2025-12-20"
  }'

# Check counter incremented
curl http://localhost:8080/actuator/metrics/tasks.created | jq
```

### Test Prometheus Endpoint
```bash
curl http://localhost:8080/actuator/prometheus | grep tasks_
```

## Learning Objectives Covered

### 1. Actuator Setup
- [x] Add actuator dependencies
- [x] Configure exposed endpoints
- [x] Configure health details display
- [x] Set up application info

### 2. Health Indicators
- [x] Understand built-in health indicators (db, diskspace, ping)
- [x] Create custom health indicator
- [x] Return different health statuses (UP, DOWN, DEGRADED)
- [x] Add custom details to health response
- [x] Configure health groups for Kubernetes

### 3. Micrometer Metrics
- [x] Inject MeterRegistry
- [x] Create Counter metrics
- [x] Create Gauge metrics
- [x] Create Timer metrics
- [x] Add tags to metrics
- [x] Use metrics in business logic

### 4. Prometheus Integration
- [x] Add Prometheus registry dependency
- [x] Expose Prometheus endpoint
- [x] Understand Prometheus metric format
- [x] Ready for Prometheus scraping

### 5. Production Monitoring
- [x] Health checks for container orchestration
- [x] Custom business metrics
- [x] Performance timing metrics
- [x] Real-time application statistics

## Key Teaching Points

### 1. Metric Types
- **Counter**: Monotonically increasing (created, completed, deleted)
- **Gauge**: Current value snapshot (active tasks, overdue tasks)
- **Timer**: Duration measurements (creation time)

### 2. Health Indicator Best Practices
- Return UP for healthy state
- Use DEGRADED for warning states
- Use DOWN only for critical failures
- Include meaningful details
- Keep checks lightweight

### 3. Production Considerations
- Secure actuator endpoints in production
- Consider separate management port
- Use health groups for orchestration
- Monitor custom business metrics
- Set up alerting on metrics

## Documentation Files

1. **LAB.md** - Comprehensive lab guide with step-by-step instructions
2. **INSTRUCTOR_NOTES.md** - Teaching strategy, common issues, demo points
3. **starter/README.md** - Student instructions and tasks
4. **solution/README.md** - Complete solution documentation
5. **PROJECT_SUMMARY.md** - This file

## Differences Between Starter and Solution

### Starter Has:
- Basic entity layer
- Repository with countByStatus()
- HSQLDB configuration
- No actuator dependencies
- No service layer
- No controller
- No metrics
- No health indicators

### Solution Adds:
- spring-boot-starter-actuator dependency
- micrometer-registry-prometheus dependency
- Custom health indicator
- Service layer with Micrometer metrics
- REST controller with CRUD operations
- DTOs for requests
- Exception handling
- Data initializer with sample data
- Complete actuator configuration

## Estimated Lab Time
- **Setup and introduction:** 15 minutes
- **Basic actuator configuration:** 20 minutes
- **Custom health indicator:** 25 minutes
- **Custom metrics:** 30 minutes
- **Testing and exploration:** 20 minutes
- **Total:** 90-120 minutes

## Next Steps for Students

After completing this lab, students should:
1. Understand Spring Boot Actuator endpoints
2. Know how to create custom health indicators
3. Be able to add custom metrics with Micrometer
4. Understand metric types (Counter, Gauge, Timer)
5. Know how to integrate with Prometheus
6. Be ready to implement monitoring in production applications

## Related Labs
- **Module 05a** - HATEOAS (REST maturity)
- **Module 06** - Service Orchestration
- **Module 05** - Spring Security (for securing actuator endpoints)

## Additional Resources
- Spring Boot Actuator Reference: https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html
- Micrometer Documentation: https://micrometer.io/docs
- Prometheus Integration: https://micrometer.io/docs/registry/prometheus

## Success Criteria

Students successfully complete the lab when:
- [x] Application starts without errors
- [x] All actuator endpoints are accessible
- [x] Custom health indicator appears in health response
- [x] Custom metrics appear in metrics endpoint
- [x] Metrics update when tasks are created/updated/deleted
- [x] Prometheus endpoint returns formatted metrics
- [x] Health groups work for liveness/readiness probes
- [x] All CRUD operations function correctly
