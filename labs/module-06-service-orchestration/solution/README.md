# Lab 7: Service Orchestration - Solution

This directory contains the complete solution for the Service Orchestration lab, demonstrating a multi-service architecture using Spring Events.

## Architecture Overview

The solution consists of two microservices:

### 1. Task Service (Port 8081)
A task management service that:
- Manages tasks with full CRUD operations
- Publishes events when tasks are created, assigned, or completed
- Communicates with User Service to fetch user details
- Uses asynchronous event processing

### 2. User Service (Port 8082)
A simple user management service that:
- Provides user information via REST API
- Maintains an in-memory user database
- Supports user lookup by ID

## Key Components

### Task Service Components

#### Domain Layer
- **Task.java**: Entity with JPA annotations, lifecycle callbacks
- **TaskStatus.java**: Enum for task states (TODO, IN_PROGRESS, COMPLETED)
- **TaskRepository.java**: JPA repository with custom queries

#### Event Layer
- **TaskEvent.java**: Application event with event metadata
- **TaskEventPublisher.java**: Publishes events for task lifecycle changes
- **TaskEventListener.java**: Listens and processes events asynchronously

#### Service Layer
- **TaskService.java**: Business logic with event publication
- **AsyncConfig.java**: Async processing configuration

#### Controller Layer
- **TaskController.java**: REST endpoints with request/response records

### User Service Components

- **User.java**: Record for user data
- **UserController.java**: REST API with in-memory storage

## Running the Solution

### Start User Service

```bash
cd solution/user-service
mvn spring-boot:run
```

The User Service will start on port 8082.

### Start Task Service

Open a new terminal:

```bash
cd solution/task-service
mvn spring-boot:run
```

The Task Service will start on port 8081.

## Testing the Solution

### 1. View Available Users

```bash
curl http://localhost:8082/api/users
```

Expected response:
```json
[
  {"id": 1, "name": "John Doe", "email": "john.doe@example.com"},
  {"id": 2, "name": "Jane Smith", "email": "jane.smith@example.com"},
  {"id": 3, "name": "Bob Johnson", "email": "bob.johnson@example.com"},
  {"id": 4, "name": "Alice Williams", "email": "alice.williams@example.com"},
  {"id": 5, "name": "Charlie Brown", "email": "charlie.brown@example.com"}
]
```

### 2. Create a Task

```bash
curl -X POST http://localhost:8081/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Implement user authentication",
    "description": "Add OAuth2 support to the application"
  }'
```

Expected response:
```json
{
  "id": 1,
  "title": "Implement user authentication",
  "description": "Add OAuth2 support to the application",
  "status": "TODO",
  "assigneeId": null,
  "createdAt": "2025-12-14T10:30:00",
  "updatedAt": "2025-12-14T10:30:00"
}
```

Check the Task Service console for event logs:
```
TaskEventPublisher: Publishing event: TASK_CREATED for task: Implement user authentication
TaskEventListener: Processing event: TASK_CREATED for task: Implement user authentication
```

### 3. Assign Task to User

```bash
curl -X PUT "http://localhost:8081/api/tasks/1/assign?assigneeId=1"
```

Expected response:
```json
{
  "id": 1,
  "title": "Implement user authentication",
  "description": "Add OAuth2 support to the application",
  "status": "IN_PROGRESS",
  "assigneeId": 1,
  "createdAt": "2025-12-14T10:30:00",
  "updatedAt": "2025-12-14T10:31:00"
}
```

Check console logs:
```
TaskEventPublisher: Publishing event: TASK_ASSIGNED for task: Implement user authentication to assignee: 1
TaskEventListener: Processing event: TASK_ASSIGNED for task: Implement user authentication
TaskEventListener: Fetched user details: {id=1, name=John Doe, email=john.doe@example.com}
TaskEventListener: Notification would be sent to: john.doe@example.com
```

### 4. Complete Task

```bash
curl -X PUT http://localhost:8081/api/tasks/1/complete
```

Expected response:
```json
{
  "id": 1,
  "title": "Implement user authentication",
  "description": "Add OAuth2 support to the application",
  "status": "COMPLETED",
  "assigneeId": 1,
  "createdAt": "2025-12-14T10:30:00",
  "updatedAt": "2025-12-14T10:32:00"
}
```

Check console logs:
```
TaskEventPublisher: Publishing event: TASK_COMPLETED for task: Implement user authentication by assignee: 1
TaskEventListener: Processing event: TASK_COMPLETED for task: Implement user authentication
TaskEventListener: Task completed by: John Doe
TaskEventListener: Completion notification would be sent to: john.doe@example.com
```

### 5. List All Tasks

```bash
curl http://localhost:8081/api/tasks
```

### 6. Filter Tasks by Status

```bash
curl "http://localhost:8081/api/tasks?status=IN_PROGRESS"
```

### 7. Filter Tasks by Assignee

```bash
curl "http://localhost:8081/api/tasks?assigneeId=1"
```

## Key Learning Points

### 1. Spring Application Events

The solution demonstrates the publish-subscribe pattern:

```java
// Publishing events
@Component
public class TaskEventPublisher {
    private final ApplicationEventPublisher eventPublisher;

    public void publishTaskCreated(Long taskId, String taskTitle) {
        TaskEvent event = new TaskEvent(this, EventType.TASK_CREATED, taskId, taskTitle, null);
        eventPublisher.publishEvent(event);
    }
}

// Listening to events
@Component
public class TaskEventListener {
    @Async("taskExecutor")
    @EventListener
    public void handleTaskEvent(TaskEvent event) {
        // Process event asynchronously
    }
}
```

### 2. Asynchronous Processing

Events are processed asynchronously using Spring's @Async:

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

### 3. Service-to-Service Communication

RestTemplate is used for synchronous HTTP communication:

```java
@Component
public class TaskEventListener {
    private final RestTemplate restTemplate;

    private void handleTaskAssigned(TaskEvent event) {
        String url = userServiceUrl + "/api/users/" + event.getAssigneeId();
        Map<String, Object> user = restTemplate.getForObject(url, Map.class);
        // Process user details
    }
}
```

### 4. Event-Driven Architecture Benefits

- **Loose Coupling**: Services don't directly depend on each other
- **Scalability**: Async processing allows better resource utilization
- **Extensibility**: New event listeners can be added without changing publishers
- **Resilience**: Event processing failures don't block the main flow

### 5. Transaction Management

The TaskService uses @Transactional to ensure data consistency:

```java
@Service
@Transactional
public class TaskService {
    public Task createTask(String title, String description) {
        Task task = taskRepository.save(new Task(title, description));
        eventPublisher.publishTaskCreated(task.getId(), task.getTitle());
        return task;
    }
}
```

## Architecture Patterns Demonstrated

1. **Microservices Architecture**: Independent, deployable services
2. **Event-Driven Architecture**: Decoupled communication via events
3. **Repository Pattern**: Data access abstraction with JPA
4. **Service Layer Pattern**: Business logic separation
5. **DTO Pattern**: Request/response records for API contracts

## Configuration Highlights

### Task Service (application.properties)
```properties
server.port=8081
spring.jpa.hibernate.ddl-auto=create-drop
user.service.url=http://localhost:8082
```

### User Service (application.properties)
```properties
server.port=8082
```

## Extension Ideas

1. **Add Event Persistence**: Store events in a database for audit trail
2. **Implement Retry Logic**: Handle User Service failures gracefully
3. **Add Circuit Breaker**: Use Resilience4j for fault tolerance
4. **Message Queue Integration**: Replace in-memory events with RabbitMQ/Kafka
5. **Service Discovery**: Use Eureka for dynamic service registration
6. **API Gateway**: Add Spring Cloud Gateway for routing
7. **Distributed Tracing**: Implement Sleuth and Zipkin

## Common Issues and Solutions

### Issue: Events not firing asynchronously
**Solution**: Ensure @EnableAsync is present and events are published from Spring-managed beans

### Issue: User Service connection refused
**Solution**: Verify User Service is running on port 8082 and check firewall settings

### Issue: Database errors on startup
**Solution**: Check HSQLDB dependency and DDL auto configuration

### Issue: RestTemplate bean not found
**Solution**: Ensure RestTemplate bean is configured in main application class

## Testing with Multiple Tasks

Create and manage multiple tasks to see the event orchestration in action:

```bash
# Create multiple tasks
for i in {1..5}; do
  curl -X POST http://localhost:8081/api/tasks \
    -H "Content-Type: application/json" \
    -d "{\"title\":\"Task $i\",\"description\":\"Description for task $i\"}"
  sleep 1
done

# Assign them to different users
curl -X PUT "http://localhost:8081/api/tasks/1/assign?assigneeId=1"
curl -X PUT "http://localhost:8081/api/tasks/2/assign?assigneeId=2"
curl -X PUT "http://localhost:8081/api/tasks/3/assign?assigneeId=1"

# Complete some tasks
curl -X PUT http://localhost:8081/api/tasks/1/complete
curl -X PUT http://localhost:8081/api/tasks/2/complete

# View tasks by assignee
curl "http://localhost:8081/api/tasks?assigneeId=1"
```

## Performance Observations

Watch the console logs to observe:
- Asynchronous event processing happening in separate threads
- Event processing not blocking the main request flow
- Parallel processing of multiple events
- Service-to-service communication timing

## Conclusion

This solution demonstrates a production-ready approach to service orchestration using:
- Spring Boot for rapid development
- Spring Events for decoupled communication
- Async processing for scalability
- RESTful APIs for service integration
- JPA for data persistence

The architecture can be extended to handle more complex scenarios like saga patterns, compensating transactions, and distributed workflows.
