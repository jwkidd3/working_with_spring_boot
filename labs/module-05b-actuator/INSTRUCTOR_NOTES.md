# Module 05b - Actuator & Monitoring Lab
## Instructor Notes

## Lab Overview
This lab teaches students how to implement Spring Boot Actuator for application monitoring and observability using a Task Management API as the example application.

## Learning Objectives
1. Add and configure Spring Boot Actuator
2. Implement custom health indicators
3. Create custom Micrometer metrics (Counters, Gauges, Timers)
4. Configure Prometheus metrics export
5. Set up health groups for Kubernetes-style probes
6. Monitor application health and performance

## Lab Structure

### Starter Project
**Location:** `/labs/module-05b-actuator/starter/`

**What's Provided:**
- Basic Spring Boot application with Web, JPA, and Validation
- Entity layer: Task, TaskStatus, TaskPriority
- Repository layer: TaskRepository with `countByStatus()` method
- HSQLDB database configuration
- **NO** Actuator dependencies or implementation

**What Students Need to Build:**
1. Add Actuator and Micrometer dependencies
2. Configure actuator endpoints in application.properties
3. Create custom health indicator
4. Create service layer with Micrometer metrics
5. Create REST controller
6. Create DTOs and exception handling
7. Create data initializer

**Estimated Time:** 60-90 minutes

### Solution Project
**Location:** `/labs/module-05b-actuator/solution/`

**Complete Implementation Including:**

#### 1. Dependencies (pom.xml)
- `spring-boot-starter-actuator`
- `micrometer-registry-prometheus`

#### 2. Actuator Configuration (application.properties)
```properties
# Exposed endpoints: health, info, metrics, prometheus
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=always

# Health groups for Kubernetes
management.endpoint.health.group.liveness.include=ping
management.endpoint.health.group.readiness.include=db,taskServiceHealthIndicator

# Application info
info.app.name=Task Management API
info.app.description=Spring Boot Actuator and Monitoring Lab
info.app.version=1.0.0
```

#### 3. Custom Health Indicator
**File:** `health/TaskServiceHealthIndicator.java`

**Features:**
- Implements `HealthIndicator` interface
- Returns task counts by status
- Displays overdue task count
- Warning when overdue tasks exist
- DEGRADED status when >10 overdue tasks

**Teaching Points:**
- How to create custom health checks
- Health status levels (UP, DOWN, DEGRADED)
- Adding custom details to health response
- Use cases for health indicators in production

#### 4. Micrometer Metrics
**File:** `service/TaskService.java`

**Metrics Implemented:**

| Metric Name | Type | Description |
|-------------|------|-------------|
| `tasks.created` | Counter | Total tasks created |
| `tasks.completed` | Counter | Total tasks completed |
| `tasks.deleted` | Counter | Total tasks deleted |
| `tasks.creation.time` | Timer | Time to create a task |
| `tasks.active` | Gauge | Current active tasks (TODO + IN_PROGRESS) |
| `tasks.overdue` | Gauge | Current overdue tasks |

**Teaching Points:**
- Difference between Counter, Gauge, and Timer
- When to use each metric type
- Adding tags to metrics for filtering
- Injecting MeterRegistry
- Using Timer.record() for timing operations

#### 5. REST API
**File:** `controller/TaskController.java`

**Endpoints:**
- `GET /api/tasks` - Get all tasks
- `GET /api/tasks/{id}` - Get task by ID
- `POST /api/tasks` - Create task (triggers metrics)
- `PUT /api/tasks/{id}` - Update task (triggers completion counter)
- `DELETE /api/tasks/{id}` - Delete task (triggers delete counter)
- `GET /api/tasks/overdue` - Get overdue tasks

#### 6. Sample Data
**File:** `config/DataInitializer.java`

**Provides:**
- 10 sample tasks with various statuses
- 2 overdue tasks (for testing health indicator)
- Mix of priorities and due dates
- Demonstrates health indicator warnings

## Teaching Strategy

### Part 1: Introduction (15 min)
1. Explain Actuator's role in production monitoring
2. Discuss observability: metrics, health checks, info
3. Show Actuator endpoints in a running application
4. Explain Micrometer as the metrics facade

### Part 2: Basic Setup (20 min)
1. Add dependencies
2. Configure application.properties
3. Run application and explore default endpoints
4. Explain built-in health indicators

### Part 3: Custom Health Indicator (25 min)
1. Explain HealthIndicator interface
2. Create TaskServiceHealthIndicator
3. Implement health() method
4. Test and show different health statuses
5. Discuss health groups for Kubernetes

### Part 4: Custom Metrics (30 min)
1. Explain metric types (Counter, Gauge, Timer)
2. Create TaskService with MeterRegistry
3. Implement counters for operations
4. Add gauge for current state
5. Add timer for operation duration
6. Test metrics through actuator endpoints

### Part 5: Integration & Testing (20 min)
1. Complete REST controller
2. Test CRUD operations
3. Watch metrics change in real-time
4. Demonstrate Prometheus format
5. Discuss production monitoring setup

## Key Demonstration Points

### 1. Health Checks
```bash
# Show overall health
curl http://localhost:8080/actuator/health

# Show custom health indicator
curl http://localhost:8080/actuator/health | jq '.components.taskServiceHealthIndicator'

# Kubernetes-style probes
curl http://localhost:8080/actuator/health/liveness
curl http://localhost:8080/actuator/health/readiness
```

### 2. Metrics in Action
```bash
# Create a task
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Demo Task",
    "status": "TODO",
    "priority": "HIGH"
  }'

# Watch counter increment
curl http://localhost:8080/actuator/metrics/tasks.created

# Check active tasks gauge
curl http://localhost:8080/actuator/metrics/tasks.active
```

### 3. Prometheus Integration
```bash
# Show Prometheus format
curl http://localhost:8080/actuator/prometheus | grep tasks_
```

## Common Student Issues

### Issue 1: Metrics Not Showing
**Symptom:** Custom metrics don't appear in /actuator/metrics
**Solution:**
- Ensure metrics are registered in constructor, not in methods
- Verify MeterRegistry is properly injected
- Check that service methods are actually being called

### Issue 2: Health Indicator Not Visible
**Symptom:** Custom health indicator doesn't show
**Solution:**
- Confirm @Component annotation is present
- Verify HealthIndicator interface is implemented
- Check show-details configuration

### Issue 3: Gauge Not Updating
**Symptom:** Gauge shows stale values
**Solution:**
- Gauges are evaluated on-demand, not cached
- Lambda should query current state, not capture at registration time
- Ensure repository methods return current counts

### Issue 4: Timer Not Recording
**Symptom:** Timer shows 0 or no data
**Solution:**
- Use Timer.record() or Timer.recordCallable()
- Don't manually call timer.record(duration) - use functional approach
- Ensure method actually executes (not just registered)

## Extension Activities

For advanced students or extra time:

1. **Custom Actuator Endpoint**
   - Create custom endpoint showing task statistics
   - Implement @Endpoint, @ReadOperation

2. **Additional Metrics**
   - Add Distribution Summary for task completion times
   - Add Counter with tags for different priorities
   - Track tasks by status as separate gauges

3. **Prometheus + Grafana**
   - Set up Prometheus to scrape metrics
   - Create Grafana dashboard
   - Set up alerting rules

4. **Security**
   - Add Spring Security
   - Secure actuator endpoints
   - Use separate management port

5. **Distributed Tracing**
   - Add Micrometer Tracing
   - Integrate with Zipkin or Jaeger
   - Track request flows

## Testing Checklist

Students should verify:

- [ ] Application starts successfully
- [ ] /actuator endpoint lists available endpoints
- [ ] /actuator/health shows UP status
- [ ] Custom health indicator appears in health response
- [ ] Health shows task counts and overdue warning
- [ ] /actuator/metrics lists custom metrics
- [ ] tasks.created counter increments when creating tasks
- [ ] tasks.completed counter increments when completing tasks
- [ ] tasks.active gauge shows correct count
- [ ] tasks.overdue gauge shows correct count
- [ ] /actuator/prometheus returns metrics in Prometheus format
- [ ] Liveness and readiness probes work
- [ ] /actuator/info shows application details

## Sample Output

### Health Endpoint
```json
{
  "status": "UP",
  "components": {
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

### Custom Metric
```json
{
  "name": "tasks.created",
  "description": "Total number of tasks created",
  "baseUnit": null,
  "measurements": [
    {
      "statistic": "COUNT",
      "value": 15.0
    }
  ],
  "availableTags": [
    {
      "tag": "service",
      "values": ["task-api"]
    }
  ]
}
```

## Additional Resources

- Spring Boot Actuator Reference: https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html
- Micrometer Documentation: https://micrometer.io/docs
- Prometheus Integration: https://micrometer.io/docs/registry/prometheus
- Production-Ready Features Guide: https://spring.io/guides/gs/actuator-service/

## Time Estimates

- **Lecture/Demo:** 45-60 minutes
- **Lab Exercise:** 60-90 minutes
- **Review/Discussion:** 20-30 minutes
- **Total:** 2-3 hours

## Prerequisites

Students should understand:
- Spring Boot basics
- REST API development
- Spring Data JPA
- Dependency injection
- Basic monitoring concepts

## Next Steps

After this lab, students should be able to:
1. Set up production monitoring for Spring Boot apps
2. Create meaningful health checks
3. Track business metrics with Micrometer
4. Integrate with Prometheus/Grafana
5. Implement observability best practices
