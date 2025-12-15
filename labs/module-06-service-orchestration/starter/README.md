# Lab 7: Service Orchestration with Spring Events

## Overview
In this lab, you will implement a multi-service architecture using Spring's event-driven programming model to orchestrate interactions between a Task Service and a User Service.

## Learning Objectives
- Understand service orchestration patterns
- Implement Spring Application Events
- Use asynchronous event processing with @Async
- Coordinate multiple services using REST communication
- Apply event-driven architecture principles

## Prerequisites
- Completed previous labs (Spring fundamentals, REST services, JPA)
- Understanding of RESTful web services
- Basic knowledge of asynchronous processing

## Architecture

You will build two services:

1. **Task Service** (Port 8081)
   - Manages tasks with CRUD operations
   - Publishes events when tasks are created, assigned, or completed
   - Communicates with User Service to fetch user details

2. **User Service** (Port 8082)
   - Manages user information
   - Provides user lookup endpoints

## Starter Code

The starter project includes:
- Basic Task Service structure with Spring Boot configuration
- Maven dependencies configured
- Application properties with HSQLDB setup

## Your Tasks

### Part 1: Create the Domain Model

1. Create `TaskStatus` enum with values: TODO, IN_PROGRESS, COMPLETED

2. Create `Task` entity with:
   - id (Long, auto-generated)
   - title (String, required)
   - description (String)
   - status (TaskStatus)
   - assigneeId (Long)
   - createdAt (LocalDateTime)
   - updatedAt (LocalDateTime)

3. Create `TaskRepository` interface extending JpaRepository

### Part 2: Implement Event Infrastructure

1. Create `TaskEvent` class:
   - Extends ApplicationEvent
   - Fields: eventId, eventType, taskId, taskTitle, assigneeId, timestamp
   - EventType enum: TASK_CREATED, TASK_ASSIGNED, TASK_COMPLETED

2. Create `TaskEventPublisher`:
   - Inject ApplicationEventPublisher
   - Methods to publish different event types

3. Create `AsyncConfig`:
   - Enable async processing with @EnableAsync
   - Configure ThreadPoolTaskExecutor

4. Create `TaskEventListener`:
   - Listen for TaskEvent with @EventListener and @Async
   - Log event details
   - Call User Service to fetch assignee details

### Part 3: Implement Task Service

1. Create `TaskService`:
   - CRUD operations
   - Publish events when tasks are created, assigned, completed
   - Use TaskEventPublisher

2. Create `TaskController`:
   - REST endpoints for task operations
   - POST /api/tasks - create task
   - GET /api/tasks - list all tasks
   - GET /api/tasks/{id} - get task by id
   - PUT /api/tasks/{id}/assign - assign task
   - PUT /api/tasks/{id}/complete - complete task

3. Create `CreateTaskRequest` record for request validation

### Part 4: Create User Service

1. Create User Service project with:
   - UserServiceApplication
   - User record (id, name, email)
   - UserController with in-memory user storage
   - GET /api/users - list all users
   - GET /api/users/{id} - get user by id

2. Configure to run on port 8082

### Part 5: Configure Service Communication

1. In Task Service, add RestTemplate configuration
2. Configure user.service.url property
3. Update TaskEventListener to call User Service

## Testing Your Implementation

1. Start User Service:
   ```bash
   cd user-service
   mvn spring-boot:run
   ```

2. Start Task Service:
   ```bash
   cd task-service
   mvn spring-boot:run
   ```

3. Test the workflow:

   a. Get users:
   ```bash
   curl http://localhost:8082/api/users
   ```

   b. Create a task:
   ```bash
   curl -X POST http://localhost:8081/api/tasks \
     -H "Content-Type: application/json" \
     -d '{"title":"Implement feature X","description":"Add new feature"}'
   ```

   c. Assign task to user:
   ```bash
   curl -X PUT http://localhost:8081/api/tasks/1/assign?assigneeId=1
   ```

   d. Complete task:
   ```bash
   curl -X PUT http://localhost:8081/api/tasks/1/complete
   ```

4. Check the console logs to see:
   - Event publication
   - Asynchronous event processing
   - User Service API calls
   - Event details logged

## Expected Output

When you create and assign a task, you should see logs like:
```
TaskEventPublisher: Publishing event: TASK_CREATED for task: Implement feature X
TaskEventListener: Processing event: TASK_CREATED for task: Implement feature X
TaskEventPublisher: Publishing event: TASK_ASSIGNED for task: Implement feature X
TaskEventListener: Processing event: TASK_ASSIGNED for task: Implement feature X
TaskEventListener: Fetched user details: User[id=1, name=John Doe, email=john.doe@example.com]
```

## Key Concepts

### Spring Application Events
- Decoupled communication between components
- Publisher publishes events, listeners react
- Synchronous by default, can be made async

### @Async Processing
- Enables asynchronous method execution
- Requires @EnableAsync configuration
- Uses separate thread pool

### Service Orchestration
- Coordinating multiple services to complete a business process
- Event-driven approach for loose coupling
- REST communication for service-to-service interaction

## Bonus Challenges

1. Add error handling for User Service failures
2. Implement retry logic for service communication
3. Add event history tracking
4. Create a notification endpoint in User Service
5. Implement circuit breaker pattern for resilience

## Solution

A complete solution is provided in the `solution` directory for reference.

## Common Issues

1. **Events not firing**: Ensure @EnableAsync is configured
2. **User Service not found**: Check port configuration and service URLs
3. **Async not working**: Events must be published from Spring-managed beans
4. **Database errors**: Verify HSQLDB dependency and configuration

## Additional Resources

- [Spring Events Documentation](https://docs.spring.io/spring-framework/reference/core/beans/context-introduction.html#context-functionality-events)
- [Spring @Async Documentation](https://docs.spring.io/spring-framework/reference/integration/scheduling.html#scheduling-annotation-support-async)
- [RestTemplate Documentation](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html#rest-resttemplate)
