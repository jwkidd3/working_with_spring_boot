# Module 06: Service Orchestration with Spring Events

## Lab Overview

This lab introduces service orchestration patterns using Spring's event-driven programming model. You will build a multi-service architecture where services communicate through events and REST APIs.

## What You Will Learn

- Spring Application Events and the Observer pattern
- Asynchronous event processing with @Async
- Service-to-service communication using RestTemplate
- Event-driven architecture principles
- Multi-service coordination and orchestration

## Lab Structure

```
module-06-service-orchestration/
├── starter/
│   ├── task-service/        # Basic structure to build upon
│   └── README.md            # Detailed lab instructions
└── solution/
    ├── task-service/        # Complete Task Service implementation
    ├── user-service/        # Complete User Service implementation
    └── README.md            # Solution guide with testing instructions
```

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Your favorite IDE (IntelliJ IDEA, Eclipse, or VS Code)
- Basic understanding of:
  - Spring Boot
  - REST APIs
  - JPA/Hibernate
  - Asynchronous programming concepts

## Quick Start

### Option 1: Start with the Starter Project

Navigate to the `starter/` directory and follow the instructions in the README.md to build the solution from scratch.

```bash
cd starter
cat README.md
```

### Option 2: Run the Complete Solution

Navigate to the `solution/` directory to run the fully implemented services.

```bash
# Terminal 1 - Start User Service
cd solution/user-service
mvn spring-boot:run

# Terminal 2 - Start Task Service
cd solution/task-service
mvn spring-boot:run
```

## Architecture

The lab implements a microservices architecture with two services:

```
┌─────────────────┐         ┌─────────────────┐
│   Task Service  │         │   User Service  │
│   (Port 8081)   │────────▶│   (Port 8082)   │
└─────────────────┘         └─────────────────┘
       │                            │
       │                            │
   Spring Events              In-Memory Store
   JPA/HSQLDB                 REST API
   Async Processing
```

### Task Service
- Manages tasks with CRUD operations
- Publishes events for task lifecycle changes
- Processes events asynchronously
- Communicates with User Service via REST

### User Service
- Provides user information
- Simple REST API
- In-memory data storage

## Key Concepts Covered

### 1. Spring Application Events

Learn how to implement the Observer pattern using Spring's event infrastructure:

```java
// Event Publisher
eventPublisher.publishEvent(new TaskEvent(...));

// Event Listener
@EventListener
public void handleTaskEvent(TaskEvent event) {
    // Process event
}
```

### 2. Asynchronous Processing

Configure and use async event processing:

```java
@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean
    public Executor taskExecutor() {
        // Configure thread pool
    }
}

@Async("taskExecutor")
@EventListener
public void handleTaskEvent(TaskEvent event) {
    // Processed asynchronously
}
```

### 3. Service Orchestration

Coordinate multiple services to complete business processes:

- Task created → Event published → Notification logged
- Task assigned → Event published → User details fetched → Notification sent
- Task completed → Event published → Completion notification sent

### 4. RESTful Communication

Use RestTemplate for service-to-service communication:

```java
User user = restTemplate.getForObject(
    "http://localhost:8082/api/users/{id}",
    User.class,
    userId
);
```

## Lab Exercises

The lab is divided into progressive parts:

1. **Part 1**: Create the domain model (Task entity, repository)
2. **Part 2**: Implement event infrastructure (events, publishers, listeners)
3. **Part 3**: Build the Task Service with event integration
4. **Part 4**: Create the User Service
5. **Part 5**: Configure inter-service communication

Each part builds upon the previous one, gradually implementing the complete solution.

## Testing Workflow

Once both services are running, test the complete workflow:

```bash
# 1. Create a task
curl -X POST http://localhost:8081/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"Test Task","description":"Test Description"}'

# 2. Assign to user
curl -X PUT "http://localhost:8081/api/tasks/1/assign?assigneeId=1"

# 3. Complete the task
curl -X PUT http://localhost:8081/api/tasks/1/complete

# 4. View all tasks
curl http://localhost:8081/api/tasks
```

## Expected Learning Outcomes

After completing this lab, you will be able to:

1. Design event-driven architectures using Spring Events
2. Implement asynchronous event processing with @Async
3. Coordinate multiple microservices
4. Build RESTful APIs for service communication
5. Handle cross-service transactions and workflows
6. Apply best practices for service orchestration

## Bonus Challenges

Once you complete the basic lab, try these extensions:

1. **Error Handling**: Add retry logic for User Service failures
2. **Event History**: Store events in a database for audit trail
3. **Notification Service**: Create a third service for email notifications
4. **Circuit Breaker**: Implement Resilience4j for fault tolerance
5. **Message Queue**: Replace in-memory events with RabbitMQ or Kafka
6. **Monitoring**: Add Spring Boot Actuator and metrics
7. **Testing**: Write integration tests for the event flow

## Troubleshooting

### Services won't start
- Check that ports 8081 and 8082 are available
- Verify Maven dependencies are downloaded
- Ensure Java 17 is installed

### Events not firing
- Verify @EnableAsync is configured
- Check that events are published from Spring-managed beans
- Review async thread pool configuration

### User Service communication fails
- Ensure User Service is running on port 8082
- Verify the user.service.url property
- Check RestTemplate bean configuration

## Additional Resources

- [Spring Events Documentation](https://docs.spring.io/spring-framework/reference/core/beans/context-introduction.html#context-functionality-events)
- [Spring @Async Guide](https://spring.io/guides/gs/async-method/)
- [RestTemplate Documentation](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html#rest-resttemplate)
- [Microservices Patterns](https://microservices.io/patterns/index.html)

## Time Estimate

- **Starter Project**: 2-3 hours to complete all parts
- **Solution Review**: 30-45 minutes to understand implementation
- **Bonus Challenges**: 1-2 hours each

## Next Steps

After completing this lab, consider exploring:

- Module 07: Additional Topics (Message Queues, Caching, Scheduling)
- Spring Cloud for advanced microservices features
- Kubernetes deployment for production environments
- Distributed tracing with Spring Cloud Sleuth

---

**Happy Coding!**

For questions or issues with this lab, please refer to the solution README or consult the course instructor.
