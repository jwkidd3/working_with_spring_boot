# Lab 5b - Testing Guide
## Quick Reference for Testing Actuator Implementation

## Running the Application

### Starter Project
```bash
cd /Users/jwkidd3/classes_in_development/working_with_spring_boot/labs/module-05b-actuator/starter
mvn spring-boot:run
```

### Solution Project
```bash
cd /Users/jwkidd3/classes_in_development/working_with_spring_boot/labs/module-05b-actuator/solution
mvn spring-boot:run
```

## Testing Actuator Endpoints

### 1. List All Endpoints
```bash
curl http://localhost:8080/actuator | jq
```

Expected: List of available endpoints

### 2. Health Check
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
      "details": {
        "database": "HSQL Database Engine",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP"
    },
    "ping": {
      "status": "UP"
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

### 3. Health Groups

**Liveness Probe:**
```bash
curl http://localhost:8080/actuator/health/liveness | jq
```

**Readiness Probe:**
```bash
curl http://localhost:8080/actuator/health/readiness | jq
```

### 4. Application Info
```bash
curl http://localhost:8080/actuator/info | jq
```

**Expected:**
```json
{
  "app": {
    "name": "Task Management API",
    "description": "Spring Boot Actuator and Monitoring Lab",
    "version": "1.0.0"
  }
}
```

### 5. List All Metrics
```bash
curl http://localhost:8080/actuator/metrics | jq
```

**Look for custom metrics:**
- tasks.created
- tasks.completed
- tasks.deleted
- tasks.creation.time
- tasks.active
- tasks.overdue

### 6. Check Specific Metrics

**Tasks Created Counter:**
```bash
curl http://localhost:8080/actuator/metrics/tasks.created | jq
```

**Expected:**
```json
{
  "name": "tasks.created",
  "description": "Total number of tasks created",
  "measurements": [
    {
      "statistic": "COUNT",
      "value": 10.0
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

**Tasks Completed Counter:**
```bash
curl http://localhost:8080/actuator/metrics/tasks.completed | jq
```

**Active Tasks Gauge:**
```bash
curl http://localhost:8080/actuator/metrics/tasks.active | jq
```

**Overdue Tasks Gauge:**
```bash
curl http://localhost:8080/actuator/metrics/tasks.overdue | jq
```

**Task Creation Timer:**
```bash
curl http://localhost:8080/actuator/metrics/tasks.creation.time | jq
```

**Expected Timer Response:**
```json
{
  "name": "tasks.creation.time",
  "description": "Time taken to create a task",
  "baseUnit": "seconds",
  "measurements": [
    {
      "statistic": "COUNT",
      "value": 10.0
    },
    {
      "statistic": "TOTAL_TIME",
      "value": 0.123
    },
    {
      "statistic": "MAX",
      "value": 0.023
    }
  ]
}
```

### 7. Prometheus Metrics
```bash
curl http://localhost:8080/actuator/prometheus
```

**Filter for task metrics:**
```bash
curl http://localhost:8080/actuator/prometheus | grep tasks_
```

**Expected Output:**
```
# HELP tasks_created_total Total number of tasks created
# TYPE tasks_created_total counter
tasks_created_total{service="task-api",} 10.0

# HELP tasks_completed_total Total number of tasks completed
# TYPE tasks_completed_total counter
tasks_completed_total{service="task-api",} 2.0

# HELP tasks_active Current active tasks (TODO + IN_PROGRESS)
# TYPE tasks_active gauge
tasks_active 6.0

# HELP tasks_overdue Current overdue tasks
# TYPE tasks_overdue gauge
tasks_overdue 2.0

# HELP tasks_creation_time_seconds Time taken to create a task
# TYPE tasks_creation_time_seconds summary
tasks_creation_time_seconds_count 10.0
tasks_creation_time_seconds_sum 0.123456789
```

## Testing REST API Endpoints

### 1. Get All Tasks
```bash
curl http://localhost:8080/api/tasks | jq
```

### 2. Get Task by ID
```bash
curl http://localhost:8080/api/tasks/1 | jq
```

### 3. Create a New Task
```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Actuator Metrics",
    "description": "Create a task to test metrics",
    "status": "TODO",
    "priority": "HIGH",
    "dueDate": "2025-12-20"
  }' | jq
```

**Then verify metrics incremented:**
```bash
curl http://localhost:8080/actuator/metrics/tasks.created | jq '.measurements[0].value'
curl http://localhost:8080/actuator/metrics/tasks.active | jq '.measurements[0].value'
```

### 4. Update a Task
```bash
curl -X PUT http://localhost:8080/api/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{
    "status": "COMPLETED"
  }' | jq
```

**Then verify completion counter:**
```bash
curl http://localhost:8080/actuator/metrics/tasks.completed | jq '.measurements[0].value'
```

### 5. Delete a Task
```bash
curl -X DELETE http://localhost:8080/api/tasks/1
```

**Then verify delete counter:**
```bash
curl http://localhost:8080/actuator/metrics/tasks.deleted | jq '.measurements[0].value'
```

### 6. Get Overdue Tasks
```bash
curl http://localhost:8080/api/tasks/overdue | jq
```

## Testing Metrics in Action

### Workflow Test
```bash
# 1. Check initial metrics
echo "Initial metrics:"
curl -s http://localhost:8080/actuator/metrics/tasks.created | jq '.measurements[0].value'
curl -s http://localhost:8080/actuator/metrics/tasks.active | jq '.measurements[0].value'

# 2. Create a task
echo "\nCreating task..."
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Metrics Test Task",
    "status": "TODO",
    "priority": "MEDIUM",
    "dueDate": "2025-12-25"
  }' | jq '.id'

# 3. Check metrics after creation
echo "\nMetrics after creation:"
curl -s http://localhost:8080/actuator/metrics/tasks.created | jq '.measurements[0].value'
curl -s http://localhost:8080/actuator/metrics/tasks.active | jq '.measurements[0].value'

# 4. Complete the task (use ID from step 2)
echo "\nCompleting task..."
curl -X PUT http://localhost:8080/api/tasks/11 \
  -H "Content-Type: application/json" \
  -d '{"status": "COMPLETED"}' | jq

# 5. Check metrics after completion
echo "\nMetrics after completion:"
curl -s http://localhost:8080/actuator/metrics/tasks.completed | jq '.measurements[0].value'
curl -s http://localhost:8080/actuator/metrics/tasks.active | jq '.measurements[0].value'
```

## Testing Health Indicator States

### 1. Check Normal State (UP)
```bash
curl http://localhost:8080/actuator/health/taskServiceHealthIndicator | jq
```

Should show UP with task statistics.

### 2. Trigger Warning State
Create many overdue tasks to trigger DEGRADED status (>10 overdue):

```bash
for i in {1..12}; do
  curl -X POST http://localhost:8080/api/tasks \
    -H "Content-Type: application/json" \
    -d "{
      \"title\": \"Overdue Task $i\",
      \"status\": \"TODO\",
      \"priority\": \"HIGH\",
      \"dueDate\": \"2025-12-01\"
    }"
done
```

Then check health:
```bash
curl http://localhost:8080/actuator/health | jq '.components.taskServiceHealthIndicator'
```

Should show DEGRADED status with reason.

## JVM and System Metrics

### Memory Usage
```bash
curl http://localhost:8080/actuator/metrics/jvm.memory.used | jq
curl http://localhost:8080/actuator/metrics/jvm.memory.max | jq
```

### Thread Count
```bash
curl http://localhost:8080/actuator/metrics/jvm.threads.live | jq
curl http://localhost:8080/actuator/metrics/jvm.threads.peak | jq
```

### CPU Usage
```bash
curl http://localhost:8080/actuator/metrics/system.cpu.usage | jq
curl http://localhost:8080/actuator/metrics/process.cpu.usage | jq
```

### HTTP Request Metrics
```bash
curl http://localhost:8080/actuator/metrics/http.server.requests | jq
```

**Filter by endpoint:**
```bash
curl "http://localhost:8080/actuator/metrics/http.server.requests?tag=uri:/api/tasks" | jq
```

## Common Issues and Solutions

### Issue 1: Endpoints Not Exposed
**Symptom:** 404 error on actuator endpoints

**Check:**
```bash
curl http://localhost:8080/actuator | jq '.["_links"] | keys'
```

**Solution:** Verify in `application.properties`:
```properties
management.endpoints.web.exposure.include=health,info,metrics,prometheus
```

### Issue 2: Custom Metrics Missing
**Symptom:** Custom metrics don't appear

**Check:**
```bash
curl http://localhost:8080/actuator/metrics | jq '.names' | grep tasks
```

**Solution:** Ensure metrics are registered in constructor, not in methods.

### Issue 3: Health Details Not Showing
**Symptom:** Health endpoint shows only status

**Check:**
```bash
curl http://localhost:8080/actuator/health | jq
```

**Solution:** Verify in `application.properties`:
```properties
management.endpoint.health.show-details=always
```

### Issue 4: Custom Health Indicator Not Appearing
**Symptom:** taskServiceHealthIndicator missing from health response

**Check:**
```bash
curl http://localhost:8080/actuator/health | jq '.components | keys'
```

**Solution:**
- Ensure `@Component` annotation is present
- Verify `HealthIndicator` interface is implemented
- Check for errors in application logs

## Verification Checklist

- [ ] Application starts on port 8080
- [ ] `/actuator` endpoint lists all endpoints
- [ ] `/actuator/health` shows UP status
- [ ] Custom health indicator appears in health response
- [ ] Health indicator shows task counts
- [ ] Liveness and readiness probes work
- [ ] `/actuator/info` shows application information
- [ ] `/actuator/metrics` lists all metrics
- [ ] Custom metrics appear (tasks.created, tasks.active, etc.)
- [ ] Creating a task increments tasks.created counter
- [ ] Completing a task increments tasks.completed counter
- [ ] Deleting a task increments tasks.deleted counter
- [ ] Active tasks gauge reflects current state
- [ ] Overdue tasks gauge shows correct count
- [ ] `/actuator/prometheus` returns Prometheus format
- [ ] All CRUD endpoints work correctly
- [ ] Exception handling returns proper error responses

## Performance Testing

### Load Test with Multiple Requests
```bash
# Create 100 tasks
for i in {1..100}; do
  curl -X POST http://localhost:8080/api/tasks \
    -H "Content-Type: application/json" \
    -d "{
      \"title\": \"Load Test Task $i\",
      \"status\": \"TODO\",
      \"priority\": \"MEDIUM\"
    }" &
done
wait

# Check metrics
curl http://localhost:8080/actuator/metrics/tasks.created | jq
curl http://localhost:8080/actuator/metrics/tasks.creation.time | jq
```

### Monitor JVM Under Load
```bash
watch -n 1 'curl -s http://localhost:8080/actuator/metrics/jvm.memory.used | jq'
```

## Useful Commands Summary

| Purpose | Command |
|---------|---------|
| List endpoints | `curl http://localhost:8080/actuator \| jq` |
| Check health | `curl http://localhost:8080/actuator/health \| jq` |
| List metrics | `curl http://localhost:8080/actuator/metrics \| jq` |
| Specific metric | `curl http://localhost:8080/actuator/metrics/{name} \| jq` |
| Prometheus format | `curl http://localhost:8080/actuator/prometheus` |
| Create task | `curl -X POST http://localhost:8080/api/tasks -H "Content-Type: application/json" -d '{...}'` |
| Update task | `curl -X PUT http://localhost:8080/api/tasks/{id} -H "Content-Type: application/json" -d '{...}'` |
| Delete task | `curl -X DELETE http://localhost:8080/api/tasks/{id}` |
| Pretty print JSON | Add `\| jq` to any curl command |
| Filter Prometheus | `curl http://localhost:8080/actuator/prometheus \| grep tasks_` |

## Expected Initial Values (After Startup)

| Metric | Initial Value | Description |
|--------|---------------|-------------|
| tasks.created | 10.0 | Sample data creates 10 tasks |
| tasks.completed | 0.0 | No completions yet |
| tasks.deleted | 0.0 | No deletions yet |
| tasks.active | 6.0 | 4 TODO + 2 IN_PROGRESS |
| tasks.overdue | 2.0 | 2 tasks with past due dates |
| totalTasks (health) | 10 | Total in database |
| todoTasks (health) | 4 | Tasks in TODO status |
| inProgressTasks (health) | 2 | Tasks IN_PROGRESS |
| completedTasks (health) | 2 | Completed tasks |
| cancelledTasks (health) | 1 | Cancelled tasks |
| overdueTasks (health) | 2 | Past due date |
