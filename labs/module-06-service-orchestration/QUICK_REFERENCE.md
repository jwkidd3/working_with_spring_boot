# Service Orchestration Lab - Quick Reference

## Quick Start Commands

### Starting Services

```bash
# Terminal 1 - User Service
cd solution/user-service
mvn spring-boot:run

# Terminal 2 - Task Service
cd solution/task-service
mvn spring-boot:run
```

### Running Test Script

```bash
cd solution
./test-workflow.sh
```

## API Endpoints

### User Service (Port 8082)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/users | Get all users |
| GET | /api/users/{id} | Get user by ID |
| POST | /api/users | Create new user |

### Task Service (Port 8081)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/tasks | Create new task |
| GET | /api/tasks | Get all tasks |
| GET | /api/tasks?status={status} | Filter by status |
| GET | /api/tasks?assigneeId={id} | Filter by assignee |
| GET | /api/tasks/{id} | Get task by ID |
| PUT | /api/tasks/{id}/assign?assigneeId={id} | Assign task |
| PUT | /api/tasks/{id}/complete | Complete task |
| PUT | /api/tasks/{id} | Update task |
| DELETE | /api/tasks/{id} | Delete task |

## Sample cURL Commands

### User Service Examples

```bash
# Get all users
curl http://localhost:8082/api/users

# Get specific user
curl http://localhost:8082/api/users/1

# Create user
curl -X POST http://localhost:8082/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"New User","email":"new.user@example.com"}'
```

### Task Service Examples

```bash
# Create task
curl -X POST http://localhost:8081/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"My Task","description":"Task description"}'

# Get all tasks
curl http://localhost:8081/api/tasks

# Get task by ID
curl http://localhost:8081/api/tasks/1

# Assign task
curl -X PUT "http://localhost:8081/api/tasks/1/assign?assigneeId=1"

# Complete task
curl -X PUT http://localhost:8081/api/tasks/1/complete

# Filter by status
curl "http://localhost:8081/api/tasks?status=IN_PROGRESS"

# Filter by assignee
curl "http://localhost:8081/api/tasks?assigneeId=1"
```

## Key Code Patterns

### Publishing Events

```java
@Component
public class TaskEventPublisher {
    private final ApplicationEventPublisher eventPublisher;

    public void publishTaskCreated(Long taskId, String taskTitle) {
        TaskEvent event = new TaskEvent(this, EventType.TASK_CREATED, taskId, taskTitle, null);
        eventPublisher.publishEvent(event);
    }
}
```

### Listening to Events

```java
@Component
public class TaskEventListener {
    @Async("taskExecutor")
    @EventListener
    public void handleTaskEvent(TaskEvent event) {
        // Process event asynchronously
    }
}
```

### Async Configuration

```java
@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-event-");
        executor.initialize();
        return executor;
    }
}
```

### Service-to-Service Communication

```java
@Component
public class TaskEventListener {
    private final RestTemplate restTemplate;

    @Value("${user.service.url}")
    private String userServiceUrl;

    private void handleTaskAssigned(TaskEvent event) {
        String url = userServiceUrl + "/api/users/" + event.getAssigneeId();
        Map<String, Object> user = restTemplate.getForObject(url, Map.class);
        // Use user details
    }
}
```

## Event Flow

### Task Created
1. Task saved to database
2. TASK_CREATED event published
3. Event listener processes asynchronously
4. Event details logged

### Task Assigned
1. Task updated with assignee ID
2. Status changed to IN_PROGRESS
3. TASK_ASSIGNED event published
4. Event listener fetches user details from User Service
5. Logs notification details

### Task Completed
1. Task status changed to COMPLETED
2. TASK_COMPLETED event published
3. Event listener fetches user details
4. Logs completion notification

## Configuration Properties

### Task Service (application.properties)

```properties
server.port=8081
spring.application.name=task-service
spring.datasource.url=jdbc:hsqldb:mem:taskdb
spring.jpa.hibernate.ddl-auto=create-drop
user.service.url=http://localhost:8082
```

### User Service (application.properties)

```properties
server.port=8082
spring.application.name=user-service
```

## Sample Users (Pre-loaded in User Service)

| ID | Name | Email |
|----|------|-------|
| 1 | John Doe | john.doe@example.com |
| 2 | Jane Smith | jane.smith@example.com |
| 3 | Bob Johnson | bob.johnson@example.com |
| 4 | Alice Williams | alice.williams@example.com |
| 5 | Charlie Brown | charlie.brown@example.com |

## Task Status Values

- `TODO` - Initial state
- `IN_PROGRESS` - Task is being worked on
- `COMPLETED` - Task is finished

## Event Types

- `TASK_CREATED` - Triggered when a new task is created
- `TASK_ASSIGNED` - Triggered when a task is assigned to a user
- `TASK_COMPLETED` - Triggered when a task is marked complete

## Troubleshooting Quick Fixes

| Problem | Solution |
|---------|----------|
| Port already in use | Kill process: `lsof -ti:8081 \| xargs kill -9` |
| Services can't communicate | Check both services are running |
| Events not firing | Verify @EnableAsync is configured |
| Database errors | Check HSQLDB dependency in pom.xml |
| RestTemplate not found | Add RestTemplate @Bean in main class |

## File Locations

### Task Service Key Files
- Main: `src/main/java/com/example/taskservice/TaskServiceApplication.java`
- Config: `src/main/java/com/example/taskservice/config/AsyncConfig.java`
- Events: `src/main/java/com/example/taskservice/event/`
- Entity: `src/main/java/com/example/taskservice/entity/Task.java`
- Service: `src/main/java/com/example/taskservice/service/TaskService.java`
- Controller: `src/main/java/com/example/taskservice/controller/TaskController.java`

### User Service Key Files
- Main: `src/main/java/com/example/userservice/UserServiceApplication.java`
- Model: `src/main/java/com/example/userservice/model/User.java`
- Controller: `src/main/java/com/example/userservice/controller/UserController.java`

## Maven Commands

```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Package as JAR
mvn package

# Run application
mvn spring-boot:run

# Skip tests
mvn clean install -DskipTests
```

## Viewing Logs

Watch for these log patterns:

```
# Event publication
TaskEventPublisher: Publishing event: TASK_CREATED for task: ...

# Event processing
TaskEventListener: Processing event: TASK_CREATED for task: ...

# Service communication
TaskEventListener: Fetched user details: ...

# Async execution
async-event-1: Processing event...
```

## Testing Checklist

- [ ] User Service starts on port 8082
- [ ] Task Service starts on port 8081
- [ ] Can fetch users from User Service
- [ ] Can create tasks in Task Service
- [ ] TASK_CREATED events appear in logs
- [ ] Can assign tasks to users
- [ ] TASK_ASSIGNED events fetch user details
- [ ] Can complete tasks
- [ ] TASK_COMPLETED events appear in logs
- [ ] Async processing visible in logs (separate threads)

## Next Steps

After mastering this lab:

1. Add error handling and retry logic
2. Implement event persistence
3. Create a notification service
4. Add circuit breaker pattern
5. Integrate message queue (RabbitMQ/Kafka)
6. Add monitoring with Actuator
7. Write integration tests

---

**Quick Tip**: Keep both service terminals visible to watch event flow in real-time!
