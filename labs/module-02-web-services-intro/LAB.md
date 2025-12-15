# Lab 2: Introduction to Web Services - Building a REST API

## Objectives

By the end of this lab, you will be able to:
- Create a REST API with Spring Boot
- Implement CRUD operations (Create, Read, Update, Delete)
- Handle exceptions using `@RestControllerAdvice`
- Validate request data using Bean Validation
- Return appropriate HTTP status codes

## Prerequisites

- Completed Lab 1
- Understanding of HTTP methods and status codes

## Duration

45-60 minutes

---

## Scenario

You are building a **Task Management API** for a productivity application. The API should allow users to:
- Create tasks
- List all tasks
- Get a specific task
- Update a task
- Delete a task

---

## Part 1: Project Setup

### Step 1.1: Open the Starter Project

1. Navigate to the `starter` folder for this lab
2. Open the project in your IDE (IntelliJ IDEA recommended)
3. Wait for Maven to download dependencies

### Step 1.2: Verify Project Structure

```
task-api/
├── src/main/java/com/example/taskapi/
│   └── TaskApiApplication.java
├── src/main/resources/
│   └── application.properties
└── pom.xml
```

---

## Part 2: Create the Domain Model

### Step 2.1: Create the Task Entity

Create `src/main/java/com/example/taskapi/model/Task.java`:

```java
package com.example.taskapi.model;

import java.time.LocalDateTime;

public class Task {

    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime dueDate;

    public Task() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = TaskStatus.TODO;
        this.priority = TaskPriority.MEDIUM;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }
}
```

### Step 2.2: Create Enums

Create `src/main/java/com/example/taskapi/model/TaskStatus.java`:

```java
package com.example.taskapi.model;

public enum TaskStatus {
    TODO,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}
```

Create `src/main/java/com/example/taskapi/model/TaskPriority.java`:

```java
package com.example.taskapi.model;

public enum TaskPriority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}
```

---

## Part 3: Create Request/Response DTOs

### Step 3.1: Create Task Request DTO with Validation

Create `src/main/java/com/example/taskapi/dto/CreateTaskRequest.java`:

```java
package com.example.taskapi.dto;

import com.example.taskapi.model.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class CreateTaskRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    private TaskPriority priority;

    private LocalDateTime dueDate;

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }
}
```

### Step 3.2: Create Update Task Request DTO

Create `src/main/java/com/example/taskapi/dto/UpdateTaskRequest.java`:

```java
package com.example.taskapi.dto;

import com.example.taskapi.model.TaskPriority;
import com.example.taskapi.model.TaskStatus;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class UpdateTaskRequest {

    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    private TaskStatus status;

    private TaskPriority priority;

    private LocalDateTime dueDate;

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }
}
```

---

## Part 4: Create Exception Handling

### Step 4.1: Create Custom Exception

Create `src/main/java/com/example/taskapi/exception/TaskNotFoundException.java`:

```java
package com.example.taskapi.exception;

public class TaskNotFoundException extends RuntimeException {

    private final Long taskId;

    public TaskNotFoundException(Long taskId) {
        super("Task not found with id: " + taskId);
        this.taskId = taskId;
    }

    public Long getTaskId() {
        return taskId;
    }
}
```

### Step 4.2: Create Error Response DTO

Create `src/main/java/com/example/taskapi/dto/ErrorResponse.java`:

```java
package com.example.taskapi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private List<FieldError> fieldErrors;

    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public static ErrorResponse of(int status, String error, String message, String path) {
        ErrorResponse response = new ErrorResponse();
        response.setStatus(status);
        response.setError(error);
        response.setMessage(message);
        response.setPath(path);
        return response;
    }

    // Inner class for field-level validation errors
    public static class FieldError {
        private String field;
        private String message;
        private Object rejectedValue;

        public FieldError(String field, String message, Object rejectedValue) {
            this.field = field;
            this.message = message;
            this.rejectedValue = rejectedValue;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Object getRejectedValue() {
            return rejectedValue;
        }

        public void setRejectedValue(Object rejectedValue) {
            this.rejectedValue = rejectedValue;
        }
    }

    // Getters and Setters
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<FieldError> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(List<FieldError> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }
}
```

### Step 4.3: Create Global Exception Handler

Create `src/main/java/com/example/taskapi/exception/GlobalExceptionHandler.java`:

```java
package com.example.taskapi.exception;

import com.example.taskapi.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TaskNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleTaskNotFound(TaskNotFoundException ex,
                                            HttpServletRequest request) {
        return ErrorResponse.of(
            HttpStatus.NOT_FOUND.value(),
            "Not Found",
            ex.getMessage(),
            request.getRequestURI()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationErrors(MethodArgumentNotValidException ex,
                                                HttpServletRequest request) {
        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> new ErrorResponse.FieldError(
                error.getField(),
                error.getDefaultMessage(),
                error.getRejectedValue()
            ))
            .toList();

        ErrorResponse response = ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "Validation Failed",
            "Request validation failed",
            request.getRequestURI()
        );
        response.setFieldErrors(fieldErrors);
        return response;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgument(IllegalArgumentException ex,
                                               HttpServletRequest request) {
        return ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            ex.getMessage(),
            request.getRequestURI()
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGenericException(Exception ex,
                                                HttpServletRequest request) {
        // Log the full exception for debugging
        ex.printStackTrace();

        return ErrorResponse.of(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "An unexpected error occurred",
            request.getRequestURI()
        );
    }
}
```

---

## Part 5: Create the Service Layer

### Step 5.1: Create Task Service

Create `src/main/java/com/example/taskapi/service/TaskService.java`:

```java
package com.example.taskapi.service;

import com.example.taskapi.dto.CreateTaskRequest;
import com.example.taskapi.dto.UpdateTaskRequest;
import com.example.taskapi.exception.TaskNotFoundException;
import com.example.taskapi.model.Task;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TaskService {

    private final Map<Long, Task> taskStore = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public TaskService() {
        // Initialize with some sample data
        createSampleTasks();
    }

    private void createSampleTasks() {
        CreateTaskRequest task1 = new CreateTaskRequest();
        task1.setTitle("Learn Spring Boot");
        task1.setDescription("Complete the Spring Boot workshop");
        create(task1);

        CreateTaskRequest task2 = new CreateTaskRequest();
        task2.setTitle("Build REST API");
        task2.setDescription("Implement CRUD operations");
        create(task2);
    }

    public List<Task> findAll() {
        return new ArrayList<>(taskStore.values());
    }

    public Task findById(Long id) {
        return Optional.ofNullable(taskStore.get(id))
            .orElseThrow(() -> new TaskNotFoundException(id));
    }

    public Task create(CreateTaskRequest request) {
        Task task = new Task();
        task.setId(idGenerator.getAndIncrement());
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());

        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }

        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }

        taskStore.put(task.getId(), task);
        return task;
    }

    public Task update(Long id, UpdateTaskRequest request) {
        Task task = findById(id);

        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }

        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }

        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }

        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }

        task.setUpdatedAt(LocalDateTime.now());
        taskStore.put(id, task);
        return task;
    }

    public void delete(Long id) {
        if (!taskStore.containsKey(id)) {
            throw new TaskNotFoundException(id);
        }
        taskStore.remove(id);
    }
}
```

---

## Part 6: Create the REST Controller

### Step 6.1: Create Task Controller

Create `src/main/java/com/example/taskapi/controller/TaskController.java`:

```java
package com.example.taskapi.controller;

import com.example.taskapi.dto.CreateTaskRequest;
import com.example.taskapi.dto.UpdateTaskRequest;
import com.example.taskapi.model.Task;
import com.example.taskapi.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // GET /api/tasks - List all tasks
    @GetMapping
    public List<Task> getAllTasks() {
        return taskService.findAll();
    }

    // GET /api/tasks/{id} - Get a specific task
    @GetMapping("/{id}")
    public Task getTask(@PathVariable Long id) {
        return taskService.findById(id);
    }

    // POST /api/tasks - Create a new task
    @PostMapping
    public ResponseEntity<Task> createTask(@Valid @RequestBody CreateTaskRequest request) {
        Task task = taskService.create(request);
        URI location = URI.create("/api/tasks/" + task.getId());
        return ResponseEntity.created(location).body(task);
    }

    // PUT /api/tasks/{id} - Update a task
    @PutMapping("/{id}")
    public Task updateTask(@PathVariable Long id,
                           @Valid @RequestBody UpdateTaskRequest request) {
        return taskService.update(id, request);
    }

    // DELETE /api/tasks/{id} - Delete a task
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable Long id) {
        taskService.delete(id);
    }
}
```

---

## Part 7: Testing the API

### Step 7.1: Run the Application

```bash
./mvnw spring-boot:run
```

### Step 7.2: Test with cURL

**List all tasks:**
```bash
curl -X GET http://localhost:8080/api/tasks
```

**Get a specific task:**
```bash
curl -X GET http://localhost:8080/api/tasks/1
```

**Create a new task:**
```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Complete Lab 2",
    "description": "Build a REST API with Spring Boot",
    "priority": "HIGH"
  }'
```

**Update a task:**
```bash
curl -X PUT http://localhost:8080/api/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{
    "status": "IN_PROGRESS"
  }'
```

**Delete a task:**
```bash
curl -X DELETE http://localhost:8080/api/tasks/1
```

### Step 7.3: Test Error Handling

**Task not found:**
```bash
curl -X GET http://localhost:8080/api/tasks/999
```

Expected response:
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Task not found with id: 999",
  "path": "/api/tasks/999"
}
```

**Validation error:**
```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "ab"
  }'
```

Expected response:
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Request validation failed",
  "path": "/api/tasks",
  "fieldErrors": [
    {
      "field": "title",
      "message": "Title must be between 3 and 100 characters",
      "rejectedValue": "ab"
    }
  ]
}
```

---

## Part 8: Challenge Exercises

### Challenge 1: Add Search Functionality

Add a search endpoint that filters tasks:
- `GET /api/tasks/search?status=TODO` - Filter by status
- `GET /api/tasks/search?priority=HIGH` - Filter by priority
- `GET /api/tasks/search?status=TODO&priority=HIGH` - Combined filter

### Challenge 2: Add Pagination

Modify the `GET /api/tasks` endpoint to support pagination:
- `GET /api/tasks?page=0&size=10` - First page with 10 items
- Return total count in a custom header `X-Total-Count`

### Challenge 3: Add PATCH Support

Add a `PATCH` endpoint that performs partial updates differently from `PUT`:
- `PATCH /api/tasks/{id}` - Only update provided fields
- Validate that at least one field is provided

### Challenge 4: Add Due Date Validation

Create a custom validator that ensures:
- Due date must be in the future when creating a task
- Due date cannot be set to the past when updating

---

## Summary

In this lab, you learned:

1. **REST API Design**: How to design RESTful endpoints with proper HTTP methods
2. **DTOs and Validation**: Using request DTOs with Jakarta Bean Validation
3. **Exception Handling**: Creating global exception handlers with `@RestControllerAdvice`
4. **HTTP Status Codes**: Returning appropriate status codes for different scenarios
5. **ResponseEntity**: Full control over HTTP responses

## Next Steps

In Module 3, you'll learn how to connect your REST API to a database using Spring Data JPA.
