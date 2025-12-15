# Lab 5b - Spring Boot Actuator & Monitoring (Starter)

## Overview
This is the starter project for the Spring Boot Actuator and Monitoring lab. The basic Task API application is provided without actuator functionality.

## Current Implementation

### Entities
- `Task.java` - Task entity with title, description, status, priority, and due date
- `TaskStatus.java` - Enum for task status (TODO, IN_PROGRESS, COMPLETED, CANCELLED)
- `TaskPriority.java` - Enum for task priority (LOW, MEDIUM, HIGH, CRITICAL)

### Repository
- `TaskRepository.java` - JPA repository with `countByStatus()` method

### Configuration
- HSQLDB in-memory database
- Spring Data JPA
- Validation support

## Your Tasks

### 1. Add Actuator Dependencies
Add the following dependencies to `pom.xml`:
```xml
<!-- Spring Boot Actuator -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

<!-- Micrometer Prometheus Registry -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

### 2. Configure Actuator Endpoints
Update `application.properties` to:
- Expose health, info, metrics, and prometheus endpoints
- Show detailed health information
- Configure liveness and readiness probes
- Add custom application info

### 3. Create Custom Health Indicator
Create `health/TaskServiceHealthIndicator.java`:
- Implement `HealthIndicator` interface
- Show task counts by status
- Display overdue task warning
- Mark as DEGRADED if too many overdue tasks (>10)

### 4. Create Service Layer with Metrics
Create `service/TaskService.java`:
- Add `Counter` metrics for:
  - Tasks created
  - Tasks completed
  - Tasks deleted
- Add `Timer` metric for task creation time
- Add `Gauge` metrics for:
  - Active tasks (TODO + IN_PROGRESS)
  - Overdue tasks

### 5. Create REST Controller
Create `controller/TaskController.java` with CRUD endpoints:
- `GET /api/tasks` - Get all tasks
- `GET /api/tasks/{id}` - Get task by ID
- `POST /api/tasks` - Create task
- `PUT /api/tasks/{id}` - Update task
- `DELETE /api/tasks/{id}` - Delete task
- `GET /api/tasks/overdue` - Get overdue tasks

### 6. Create DTOs
- `dto/CreateTaskRequest.java` - DTO for creating tasks
- `dto/UpdateTaskRequest.java` - DTO for updating tasks

### 7. Create Exception Handling
- `exception/TaskNotFoundException.java` - Custom exception
- `exception/GlobalExceptionHandler.java` - Global exception handler with `@RestControllerAdvice`

### 8. Create Data Initializer
Create `config/DataInitializer.java`:
- Implement `CommandLineRunner`
- Create sample tasks including some overdue tasks
- This helps test the health indicator and metrics

## Running the Application

```bash
mvn spring-boot:run
```

## Testing Actuator Endpoints

After implementation, test these endpoints:

### Health Endpoint
```bash
curl http://localhost:8080/actuator/health
```

### Custom Health Groups
```bash
# Liveness probe
curl http://localhost:8080/actuator/health/liveness

# Readiness probe
curl http://localhost:8080/actuator/health/readiness
```

### Metrics
```bash
# All metrics
curl http://localhost:8080/actuator/metrics

# Custom task metrics
curl http://localhost:8080/actuator/metrics/tasks.created
curl http://localhost:8080/actuator/metrics/tasks.completed
curl http://localhost:8080/actuator/metrics/tasks.active
curl http://localhost:8080/actuator/metrics/tasks.overdue
```

### Prometheus Metrics
```bash
curl http://localhost:8080/actuator/prometheus
```

### Info Endpoint
```bash
curl http://localhost:8080/actuator/info
```

## Expected Results

1. Health endpoint should show:
   - Overall status: UP
   - Custom TaskServiceHealthIndicator with task counts
   - Warning if there are overdue tasks
   - DEGRADED status if >10 overdue tasks

2. Metrics should track:
   - Number of tasks created/completed/deleted
   - Time taken to create tasks
   - Current count of active tasks
   - Current count of overdue tasks

3. All CRUD operations should work and update metrics accordingly

## Tips

- Use `@Component` for the health indicator
- Inject `MeterRegistry` into the service to create metrics
- Use constructor injection for dependencies
- Test the health indicator by creating tasks with past due dates
- Watch metrics change as you create, update, and delete tasks
