# Module 5a: HATEOAS with Spring Boot

## Lab Objectives
By the end of this lab, you will be able to:
- Understand HATEOAS principles and Richardson Maturity Model Level 3
- Implement hypermedia-driven REST APIs using Spring HATEOAS
- Create RepresentationModel-based DTOs
- Build Model Assemblers to generate hypermedia links
- Add conditional links based on resource state
- Implement pagination with navigation links
- Create an API root endpoint for discoverability
- Use RFC 7807 Problem Details for error responses

## Lab Duration
Approximately 90-120 minutes

## Prerequisites
- Java 17 or higher installed
- Maven 3.6 or higher installed
- IDE (IntelliJ IDEA, Eclipse, or VS Code with Java extensions)
- Basic understanding of REST principles
- Familiarity with Spring Boot and JPA
- Understanding of HTTP methods and status codes

## Lab Scenario
You are building a Task Management API that needs to be self-documenting and navigable. Instead of requiring clients to construct URLs or know all endpoints upfront, you'll implement HATEOAS to allow clients to discover and navigate the API through hypermedia links.

The API will manage tasks with different states (CREATED, IN_PROGRESS, COMPLETED, CANCELLED) and provide different actions based on each task's current state.

## Part 1: Understanding HATEOAS

### What is HATEOAS?
HATEOAS (Hypermedia as the Engine of Application State) is a constraint of REST architecture where:
- Clients interact with applications entirely through hypermedia provided dynamically by servers
- Clients don't need to know the API structure beforehand
- The server tells the client what actions are available through links

### Richardson Maturity Model
- **Level 0**: Single URI, single HTTP method (like SOAP)
- **Level 1**: Multiple URIs, single HTTP method
- **Level 2**: Multiple URIs, multiple HTTP methods
- **Level 3**: Level 2 + Hypermedia Controls (HATEOAS)

### Benefits of HATEOAS
1. **Self-documenting**: API responses indicate what's possible
2. **Decoupling**: Clients don't hardcode URLs
3. **Evolvability**: Server can change URLs without breaking clients
4. **State-driven**: Available actions reflect current resource state

## Part 2: Project Setup

### Step 1: Examine the Starter Project
Navigate to the starter folder and examine:
- `pom.xml` - Note the dependencies (Web, JPA, Validation, HSQLDB)
- `Task.java` - The entity with status and priority
- `TaskRepository.java` - JPA repository with query methods
- `application.properties` - HSQLDB configuration

### Step 2: Add HATEOAS Dependency
Open `pom.xml` and add the Spring HATEOAS starter after the existing dependencies:

```xml
<!-- Spring Boot HATEOAS Starter -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-hateoas</artifactId>
</dependency>
```

### Step 3: Configure HATEOAS
Add to `application.properties`:
```properties
# HATEOAS Configuration
spring.hateoas.use-hal-as-default-json-media-type=true
```

## Part 3: Create DTOs

### Step 1: Create TaskResponse DTO
Create `dto/TaskResponse.java`:

```java
package com.example.taskapi.dto;

import com.example.taskapi.entity.TaskPriority;
import com.example.taskapi.entity.TaskStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Relation(collectionRelation = "tasks", itemRelation = "task")
public class TaskResponse extends RepresentationModel<TaskResponse> {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime dueDate;

    // Add constructors, getters, and setters
}
```

**Key Points**:
- Extends `RepresentationModel<TaskResponse>` to support links
- `@Relation` defines how collections and items are named in HAL format
- `@JsonInclude` excludes null fields from JSON response

### Step 2: Create Request DTOs
Create `dto/CreateTaskRequest.java` for creating tasks:
```java
package com.example.taskapi.dto;

import com.example.taskapi.entity.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateTaskRequest {
    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 100)
    private String title;

    @Size(max = 500)
    private String description;

    @NotNull(message = "Priority is required")
    private TaskPriority priority;

    private LocalDateTime dueDate;

    // Add constructors, getters, and setters
}
```

Create `dto/UpdateTaskRequest.java` for updates (all fields optional).

## Part 4: Build Model Assembler

### Step 1: Create TaskModelAssembler
Create `assembler/TaskModelAssembler.java`:

```java
package com.example.taskapi.assembler;

import com.example.taskapi.controller.TaskController;
import com.example.taskapi.dto.TaskResponse;
import com.example.taskapi.entity.Task;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class TaskModelAssembler extends RepresentationModelAssemblerSupport<Task, TaskResponse> {

    public TaskModelAssembler() {
        super(TaskController.class, TaskResponse.class);
    }

    @Override
    public TaskResponse toModel(Task task) {
        TaskResponse response = new TaskResponse(/* map fields */);

        // Add self link
        response.add(linkTo(methodOn(TaskController.class)
            .getTaskById(task.getId()))
            .withSelfRel());

        // Add collection link
        response.add(linkTo(methodOn(TaskController.class)
            .getAllTasks(null, null))
            .withRel("tasks"));

        // Add status-based conditional links
        addStatusBasedLinks(response, task);

        return response;
    }

    private void addStatusBasedLinks(TaskResponse response, Task task) {
        // Add different links based on task status
        // Example: CREATED tasks can start or cancel
        // IN_PROGRESS tasks can complete or cancel
        // COMPLETED tasks can reopen
        // etc.
    }
}
```

**Key Points**:
- `RepresentationModelAssemblerSupport` provides base functionality
- `linkTo(methodOn(...))` creates type-safe links
- Conditional links guide clients on available actions

### Step 2: Implement Status-Based Links
Complete the `addStatusBasedLinks` method:

```java
private void addStatusBasedLinks(TaskResponse response, Task task) {
    switch (task.getStatus()) {
        case CREATED:
            response.add(linkTo(methodOn(TaskController.class)
                .startTask(task.getId())).withRel("start"));
            response.add(linkTo(methodOn(TaskController.class)
                .cancelTask(task.getId())).withRel("cancel"));
            break;
        case IN_PROGRESS:
            response.add(linkTo(methodOn(TaskController.class)
                .completeTask(task.getId())).withRel("complete"));
            response.add(linkTo(methodOn(TaskController.class)
                .cancelTask(task.getId())).withRel("cancel"));
            break;
        case COMPLETED:
            response.add(linkTo(methodOn(TaskController.class)
                .reopenTask(task.getId())).withRel("reopen"));
            break;
        case CANCELLED:
            response.add(linkTo(methodOn(TaskController.class)
                .reopenTask(task.getId())).withRel("reopen"));
            break;
    }

    // Always available
    response.add(linkTo(methodOn(TaskController.class)
        .updateTask(task.getId(), null)).withRel("update"));
    response.add(linkTo(methodOn(TaskController.class)
        .deleteTask(task.getId())).withRel("delete"));
}
```

## Part 5: Implement Service Layer

### Create TaskService
Create `service/TaskService.java` with:
- CRUD operations (create, read, update, delete)
- Query methods (by status, by priority)
- State transition methods (start, complete, cancel, reopen)

**Important**: Validate state transitions:
- `startTask`: Only from CREATED
- `completeTask`: Only from IN_PROGRESS
- `cancelTask`: From CREATED or IN_PROGRESS (not COMPLETED)
- `reopenTask`: From COMPLETED or CANCELLED

Throw `IllegalStateException` for invalid transitions.

## Part 6: Create Controllers

### Step 1: Create TaskController
Create `controller/TaskController.java`:

```java
package com.example.taskapi.controller;

import com.example.taskapi.assembler.TaskModelAssembler;
import com.example.taskapi.dto.TaskResponse;
import com.example.taskapi.service.TaskService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;
    private final TaskModelAssembler assembler;
    private final PagedResourcesAssembler<Task> pagedAssembler;

    // Implement endpoints:
    // GET /api/tasks - getAllTasks with pagination
    // GET /api/tasks/{id} - getTaskById
    // GET /api/tasks/status/{status} - getTasksByStatus
    // GET /api/tasks/priority/{priority} - getTasksByPriority
    // POST /api/tasks - createTask
    // PUT /api/tasks/{id} - updateTask
    // DELETE /api/tasks/{id} - deleteTask
    // PATCH /api/tasks/{id}/start - startTask
    // PATCH /api/tasks/{id}/complete - completeTask
    // PATCH /api/tasks/{id}/cancel - cancelTask
    // PATCH /api/tasks/{id}/reopen - reopenTask
}
```

**Key Points**:
- Use `PagedResourcesAssembler` for automatic pagination links
- Return `PagedModel<TaskResponse>` for collections
- Return `TaskResponse` for single resources
- Use `ResponseEntity.created()` with Location header for POST

### Step 2: Create RootController
Create `controller/RootController.java` for API discovery:

```java
package com.example.taskapi.controller;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api")
public class RootController {

    @GetMapping
    public ResponseEntity<RepresentationModel<?>> root() {
        RepresentationModel<?> root = new RepresentationModel<>();

        // Add links to all main resources
        root.add(linkTo(methodOn(TaskController.class)
            .getAllTasks(0, 10)).withRel("tasks"));

        // Add links for common filters
        // tasks-created, tasks-in-progress, tasks-completed, etc.

        return ResponseEntity.ok(root);
    }
}
```

## Part 7: Exception Handling

### Step 1: Create Custom Exception
Create `exception/TaskNotFoundException.java`:

```java
package com.example.taskapi.exception;

public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(String message) {
        super(message);
    }
}
```

### Step 2: Create Global Exception Handler
Create `exception/GlobalExceptionHandler.java`:

```java
package com.example.taskapi.exception;

import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<Problem> handleTaskNotFound(TaskNotFoundException ex) {
        Problem problem = Problem.create()
            .withTitle("Task Not Found")
            .withStatus(HttpStatus.NOT_FOUND)
            .withDetail(ex.getMessage());

        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(problem);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Problem> handleIllegalState(IllegalStateException ex) {
        Problem problem = Problem.create()
            .withTitle("Invalid State Transition")
            .withStatus(HttpStatus.CONFLICT)
            .withDetail(ex.getMessage());

        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(problem);
    }

    // Add handlers for validation errors and generic exceptions
}
```

## Part 8: Data Initialization

Create `config/DataInitializer.java` to populate sample data:
- Implement `CommandLineRunner`
- Create 10+ sample tasks in various states
- Include different priorities and due dates

## Part 9: Testing

### Test 1: API Discovery
```bash
curl http://localhost:8080/api
```
Expected: Links to all main resources

### Test 2: Get All Tasks
```bash
curl http://localhost:8080/api/tasks
```
Expected: Paginated list with navigation links

### Test 3: Get Single Task
```bash
curl http://localhost:8080/api/tasks/1
```
Expected: Task with status-appropriate links

### Test 4: Create Task
```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Learn HATEOAS",
    "description": "Complete lab exercise",
    "priority": "HIGH"
  }'
```
Expected: 201 Created with Location header

### Test 5: State Transitions
```bash
# Start a CREATED task
curl -X PATCH http://localhost:8080/api/tasks/3/start

# Complete an IN_PROGRESS task
curl -X PATCH http://localhost:8080/api/tasks/2/complete

# Try invalid transition (should fail)
curl -X PATCH http://localhost:8080/api/tasks/1/complete
```

### Test 6: Pagination
```bash
curl "http://localhost:8080/api/tasks?page=0&size=5"
```
Expected: First 5 tasks with next/last links

## Part 10: Verification Checklist

Verify your implementation includes:
- [ ] RepresentationModel-based DTOs
- [ ] Model assembler with link generation
- [ ] Self link on every resource
- [ ] Collection link on single resources
- [ ] Status-based conditional links
- [ ] Pagination links (first, last, next, prev)
- [ ] API root with discovery links
- [ ] RFC 7807 Problem Details for errors
- [ ] Proper HTTP status codes
- [ ] HAL media type in responses
- [ ] Location header on POST requests

## Understanding the Response

### HAL Format
```json
{
  "id": 1,
  "title": "Task Title",
  "_links": {
    "self": { "href": "..." },
    "tasks": { "href": "..." },
    "start": { "href": "..." }
  }
}
```

### Pagination
```json
{
  "_embedded": { "tasks": [...] },
  "_links": {
    "first": { "href": "..." },
    "self": { "href": "..." },
    "next": { "href": "..." },
    "last": { "href": "..." }
  },
  "page": {
    "size": 10,
    "totalElements": 50,
    "totalPages": 5,
    "number": 0
  }
}
```

## Common Issues and Solutions

### Issue 1: Links not appearing
- Ensure DTO extends RepresentationModel
- Check assembler is being used
- Verify HATEOAS dependency is included

### Issue 2: Wrong media type
- Add `spring.hateoas.use-hal-as-default-json-media-type=true`
- Check Accept header in requests

### Issue 3: Pagination links missing
- Inject `PagedResourcesAssembler<Task>`
- Use `toModel()` method on Page results

### Issue 4: Circular references
- Use DTOs instead of entities in responses
- Don't return entities directly

## Bonus Challenges

1. **HAL Browser**: Add HAL Browser dependency for interactive exploration
2. **Search**: Implement search by title with links
3. **Filtering**: Add due date range filtering
4. **Sorting**: Support multiple sort fields
5. **ALPS**: Add ALPS (Application-Level Profile Semantics) metadata
6. **Affordances**: Use Spring HATEOAS affordances for documenting available operations

## Review Questions

1. What is the difference between Level 2 and Level 3 REST APIs?
2. Why do we use DTOs instead of entities in HATEOAS responses?
3. How do conditional links improve API usability?
4. What is the purpose of the @Relation annotation?
5. Why is API discoverability important?
6. What are the advantages of HAL format?
7. How does HATEOAS support API evolution?

## Summary

In this lab, you learned:
- HATEOAS principles and benefits
- How to use Spring HATEOAS framework
- Creating RepresentationModel-based DTOs
- Building model assemblers
- Adding conditional links based on state
- Implementing pagination with navigation
- Creating discoverable APIs
- Using RFC 7807 Problem Details

## Next Steps
- Explore Spring Data REST for automatic HATEOAS
- Learn about other hypermedia formats (JSON-LD, Siren)
- Implement a hypermedia-driven client
- Study API versioning with HATEOAS

## Resources
- [Spring HATEOAS Reference](https://docs.spring.io/spring-hateoas/docs/current/reference/html/)
- [HAL Specification](http://stateless.co/hal_specification.html)
- [RFC 7807](https://tools.ietf.org/html/rfc7807)
- [Richardson Maturity Model](https://martinfowler.com/articles/richardsonMaturityModel.html)
