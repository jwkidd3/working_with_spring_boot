# Lab 4: Building RESTful Web Services with Spring Boot

## Objectives

By the end of this lab, you will be able to:
- Add HATEOAS links to API responses
- Configure and use Spring Boot Actuator
- Create custom health indicators and metrics
- Implement advanced filtering
- Document your API with OpenAPI/Swagger

## Prerequisites

- Completed Labs 1-3
- Understanding of REST principles

## Duration

60-75 minutes

---

## Scenario

You will enhance the Task Management API with production-ready features including hypermedia links, monitoring capabilities, and API documentation.

---

## Part 1: Project Setup

### Step 1.1: Create or Update Project

Add these dependencies to your `pom.xml`:

```xml
<!-- HATEOAS -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-hateoas</artifactId>
</dependency>

<!-- Actuator -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

<!-- OpenAPI Documentation -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>

<!-- Micrometer for metrics -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

### Step 1.2: Update Application Properties

Update `src/main/resources/application.properties`:

```properties
# Application
spring.application.name=task-api-advanced

# HSQLDB In-Memory Database
spring.datasource.url=jdbc:hsqldb:mem:taskdb
spring.datasource.driver-class-name=org.hsqldb.jdbc.JDBCDriver
spring.datasource.username=sa
spring.datasource.password=

# JPA
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# Actuator Configuration
management.endpoints.web.exposure.include=health,info,metrics,prometheus,env,loggers
management.endpoint.health.show-details=always
management.info.env.enabled=true

# Application Info
info.app.name=Task Management API
info.app.description=Advanced REST API for task management
info.app.version=2.0.0
info.app.author=Workshop Participant

# OpenAPI Configuration
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.operationsSorter=method
```

---

## Part 2: Implement HATEOAS

### Step 2.1: Create Task Response DTO with Links Support

Create `src/main/java/com/example/taskapi/dto/TaskResponse.java`:

```java
package com.example.taskapi.dto;

import com.example.taskapi.entity.Task;
import com.example.taskapi.entity.TaskPriority;
import com.example.taskapi.entity.TaskStatus;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.time.LocalDateTime;

@Relation(collectionRelation = "tasks", itemRelation = "task")
public class TaskResponse extends RepresentationModel<TaskResponse> {

    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TaskResponse from(Task task) {
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setStatus(task.getStatus());
        response.setPriority(task.getPriority());
        response.setDueDate(task.getDueDate());
        response.setCreatedAt(task.getCreatedAt());
        response.setUpdatedAt(task.getUpdatedAt());
        return response;
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

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
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
}
```

### Step 2.2: Create Task Model Assembler

Create `src/main/java/com/example/taskapi/assembler/TaskModelAssembler.java`:

```java
package com.example.taskapi.assembler;

import com.example.taskapi.controller.TaskController;
import com.example.taskapi.dto.TaskResponse;
import com.example.taskapi.entity.Task;
import com.example.taskapi.entity.TaskStatus;
import org.springframework.hateoas.CollectionModel;
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
        TaskResponse response = TaskResponse.from(task);

        // Self link
        response.add(linkTo(methodOn(TaskController.class).getTask(task.getId())).withSelfRel());

        // Collection link
        response.add(linkTo(methodOn(TaskController.class).getAllTasks(0, 10, "createdAt", "desc"))
            .withRel("tasks"));

        // Action links based on status
        if (task.getStatus() != TaskStatus.COMPLETED) {
            response.add(linkTo(methodOn(TaskController.class).updateTask(task.getId(), null))
                .withRel("update"));

            response.add(linkTo(methodOn(TaskController.class)
                .completeTask(task.getId()))
                .withRel("complete"));
        }

        if (task.getStatus() == TaskStatus.TODO) {
            response.add(linkTo(methodOn(TaskController.class)
                .startTask(task.getId()))
                .withRel("start"));
        }

        response.add(linkTo(methodOn(TaskController.class).deleteTask(task.getId())).withRel("delete"));

        return response;
    }

    @Override
    public CollectionModel<TaskResponse> toCollectionModel(Iterable<? extends Task> tasks) {
        CollectionModel<TaskResponse> collection = super.toCollectionModel(tasks);

        collection.add(linkTo(methodOn(TaskController.class)
            .getAllTasks(0, 10, "createdAt", "desc")).withSelfRel());

        collection.add(linkTo(methodOn(TaskController.class)
            .createTask(null)).withRel("create"));

        collection.add(linkTo(methodOn(TaskController.class)
            .getOverdueTasks()).withRel("overdue"));

        collection.add(linkTo(methodOn(TaskController.class)
            .getStats()).withRel("stats"));

        return collection;
    }
}
```

### Step 2.3: Update Task Controller with HATEOAS

Update `src/main/java/com/example/taskapi/controller/TaskController.java`:

```java
package com.example.taskapi.controller;

import com.example.taskapi.assembler.TaskModelAssembler;
import com.example.taskapi.dto.CreateTaskRequest;
import com.example.taskapi.dto.TaskResponse;
import com.example.taskapi.dto.UpdateTaskRequest;
import com.example.taskapi.entity.Task;
import com.example.taskapi.entity.TaskPriority;
import com.example.taskapi.entity.TaskStatus;
import com.example.taskapi.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tasks", description = "Task management endpoints")
public class TaskController {

    private final TaskService taskService;
    private final TaskModelAssembler assembler;

    public TaskController(TaskService taskService, TaskModelAssembler assembler) {
        this.taskService = taskService;
        this.assembler = assembler;
    }

    @GetMapping
    @Operation(summary = "Get all tasks", description = "Retrieve all tasks with pagination")
    public PagedModel<TaskResponse> getAllTasks(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
            ? Sort.by(sortBy).ascending()
            : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Task> taskPage = taskService.findAll(pageable);

        List<TaskResponse> taskResponses = taskPage.getContent().stream()
            .map(assembler::toModel)
            .toList();

        PagedModel.PageMetadata metadata = new PagedModel.PageMetadata(
            taskPage.getSize(),
            taskPage.getNumber(),
            taskPage.getTotalElements(),
            taskPage.getTotalPages()
        );

        PagedModel<TaskResponse> pagedModel = PagedModel.of(taskResponses, metadata);

        pagedModel.add(linkTo(methodOn(TaskController.class)
            .getAllTasks(page, size, sortBy, sortDir)).withSelfRel());

        if (taskPage.hasNext()) {
            pagedModel.add(linkTo(methodOn(TaskController.class)
                .getAllTasks(page + 1, size, sortBy, sortDir)).withRel("next"));
        }

        if (taskPage.hasPrevious()) {
            pagedModel.add(linkTo(methodOn(TaskController.class)
                .getAllTasks(page - 1, size, sortBy, sortDir)).withRel("prev"));
        }

        return pagedModel;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID", description = "Retrieve a specific task by its ID")
    @ApiResponse(responseCode = "200", description = "Task found")
    @ApiResponse(responseCode = "404", description = "Task not found")
    public TaskResponse getTask(@PathVariable Long id) {
        Task task = taskService.findById(id);
        return assembler.toModel(task);
    }

    @GetMapping("/search")
    @Operation(summary = "Search tasks", description = "Search tasks with filters")
    public CollectionModel<TaskResponse> searchTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) String keyword) {

        List<Task> tasks = taskService.search(status, priority, keyword);
        return assembler.toCollectionModel(tasks);
    }

    @GetMapping("/overdue")
    @Operation(summary = "Get overdue tasks", description = "Retrieve all tasks that are past their due date")
    public CollectionModel<TaskResponse> getOverdueTasks() {
        List<Task> tasks = taskService.findOverdueTasks();
        CollectionModel<TaskResponse> collection = assembler.toCollectionModel(tasks);
        collection.add(linkTo(methodOn(TaskController.class).getOverdueTasks()).withSelfRel());
        return collection;
    }

    @PostMapping
    @Operation(summary = "Create task", description = "Create a new task")
    @ApiResponse(responseCode = "201", description = "Task created")
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody CreateTaskRequest request) {
        Task task = taskService.create(request);
        TaskResponse response = assembler.toModel(task);
        return ResponseEntity
            .created(linkTo(methodOn(TaskController.class).getTask(task.getId())).toUri())
            .body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update task", description = "Update an existing task")
    public TaskResponse updateTask(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskRequest request) {
        Task task = taskService.update(id, request);
        return assembler.toModel(task);
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "Start task", description = "Change task status to IN_PROGRESS")
    public TaskResponse startTask(@PathVariable Long id) {
        Task task = taskService.updateStatus(id, TaskStatus.IN_PROGRESS);
        return assembler.toModel(task);
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Complete task", description = "Change task status to COMPLETED")
    public TaskResponse completeTask(@PathVariable Long id) {
        Task task = taskService.updateStatus(id, TaskStatus.COMPLETED);
        return assembler.toModel(task);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete task", description = "Delete a task by ID")
    public void deleteTask(@PathVariable Long id) {
        taskService.delete(id);
    }

    @GetMapping("/stats")
    @Operation(summary = "Get task statistics", description = "Get counts by status")
    public TaskStats getStats() {
        return new TaskStats(
            taskService.countByStatus(TaskStatus.TODO),
            taskService.countByStatus(TaskStatus.IN_PROGRESS),
            taskService.countByStatus(TaskStatus.COMPLETED),
            taskService.countByStatus(TaskStatus.CANCELLED)
        );
    }

    public record TaskStats(long todo, long inProgress, long completed, long cancelled) {}
}
```

### Step 2.4: Update Task Service

Add the `updateStatus` and `search` methods to your `TaskService`:

```java
public Task updateStatus(Long id, TaskStatus status) {
    Task task = findById(id);
    task.setStatus(status);
    return taskRepository.save(task);
}

@Transactional(readOnly = true)
public List<Task> search(TaskStatus status, TaskPriority priority, String keyword) {
    if (status == null && priority == null && keyword == null) {
        return taskRepository.findAll();
    }

    // Use specification or custom query
    return taskRepository.findAll().stream()
        .filter(t -> status == null || t.getStatus() == status)
        .filter(t -> priority == null || t.getPriority() == priority)
        .filter(t -> keyword == null || t.getTitle().toLowerCase().contains(keyword.toLowerCase()))
        .toList();
}
```

---

## Part 3: Custom Health Indicator

### Step 3.1: Create Database Health Indicator

Create `src/main/java/com/example/taskapi/health/TaskServiceHealthIndicator.java`:

```java
package com.example.taskapi.health;

import com.example.taskapi.repository.TaskRepository;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class TaskServiceHealthIndicator implements HealthIndicator {

    private final TaskRepository taskRepository;

    public TaskServiceHealthIndicator(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public Health health() {
        try {
            long taskCount = taskRepository.count();
            long overdueCount = taskRepository.findOverdueTasks(java.time.LocalDateTime.now()).size();

            Health.Builder builder = Health.up()
                .withDetail("totalTasks", taskCount)
                .withDetail("overdueTasks", overdueCount);

            // Warn if too many overdue tasks
            if (overdueCount > 10) {
                builder = Health.status("WARNING")
                    .withDetail("totalTasks", taskCount)
                    .withDetail("overdueTasks", overdueCount)
                    .withDetail("message", "High number of overdue tasks!");
            }

            return builder.build();

        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

---

## Part 4: Custom Metrics

### Step 4.1: Add Metrics to Task Service

Update `src/main/java/com/example/taskapi/service/TaskService.java`:

```java
package com.example.taskapi.service;

import com.example.taskapi.dto.CreateTaskRequest;
import com.example.taskapi.dto.UpdateTaskRequest;
import com.example.taskapi.entity.Task;
import com.example.taskapi.entity.TaskPriority;
import com.example.taskapi.entity.TaskStatus;
import com.example.taskapi.exception.TaskNotFoundException;
import com.example.taskapi.repository.TaskRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final Counter tasksCreatedCounter;
    private final Counter tasksCompletedCounter;
    private final Timer taskSearchTimer;

    public TaskService(TaskRepository taskRepository, MeterRegistry registry) {
        this.taskRepository = taskRepository;

        // Custom metrics
        this.tasksCreatedCounter = Counter.builder("tasks.created.total")
            .description("Total number of tasks created")
            .register(registry);

        this.tasksCompletedCounter = Counter.builder("tasks.completed.total")
            .description("Total number of tasks completed")
            .register(registry);

        this.taskSearchTimer = Timer.builder("tasks.search.time")
            .description("Time taken to search tasks")
            .register(registry);

        // Gauge for current task counts
        registry.gauge("tasks.active.count", taskRepository,
            repo -> repo.countByStatus(TaskStatus.TODO) + repo.countByStatus(TaskStatus.IN_PROGRESS));
    }

    @Transactional(readOnly = true)
    public List<Task> findAll() {
        return taskRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Task> findAll(Pageable pageable) {
        return taskRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Task findById(Long id) {
        return taskRepository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Task> findByStatus(TaskStatus status) {
        return taskRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Task> search(TaskStatus status, TaskPriority priority, String keyword) {
        return taskSearchTimer.record(() -> {
            if (status == null && priority == null && keyword == null) {
                return taskRepository.findAll();
            }

            return taskRepository.findAll().stream()
                .filter(t -> status == null || t.getStatus() == status)
                .filter(t -> priority == null || t.getPriority() == priority)
                .filter(t -> keyword == null ||
                    t.getTitle().toLowerCase().contains(keyword.toLowerCase()))
                .toList();
        });
    }

    @Transactional(readOnly = true)
    public List<Task> findOverdueTasks() {
        return taskRepository.findOverdueTasks(LocalDateTime.now());
    }

    public Task create(CreateTaskRequest request) {
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());

        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }

        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }

        Task saved = taskRepository.save(task);
        tasksCreatedCounter.increment();
        return saved;
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

        return taskRepository.save(task);
    }

    public Task updateStatus(Long id, TaskStatus status) {
        Task task = findById(id);
        TaskStatus previousStatus = task.getStatus();
        task.setStatus(status);

        Task saved = taskRepository.save(task);

        // Track completed tasks
        if (status == TaskStatus.COMPLETED && previousStatus != TaskStatus.COMPLETED) {
            tasksCompletedCounter.increment();
        }

        return saved;
    }

    public void delete(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException(id);
        }
        taskRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public long countByStatus(TaskStatus status) {
        return taskRepository.countByStatus(status);
    }
}
```

---

## Part 5: API Documentation

### Step 5.1: Create OpenAPI Configuration

Create `src/main/java/com/example/taskapi/config/OpenApiConfig.java`:

```java
package com.example.taskapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Task Management API")
                .version("2.0.0")
                .description("RESTful API for managing tasks with HATEOAS support")
                .contact(new Contact()
                    .name("Workshop Team")
                    .email("workshop@example.com"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0")))
            .servers(List.of(
                new Server().url("http://localhost:8080").description("Development server")
            ));
    }
}
```

---

## Part 6: Testing

### Step 6.1: Run the Application

```bash
./mvnw spring-boot:run
```

### Step 6.2: Test HATEOAS Responses

**Get all tasks with links:**
```bash
curl http://localhost:8080/api/tasks | jq
```

Expected response with `_links`:
```json
{
  "_embedded": {
    "tasks": [
      {
        "id": 1,
        "title": "Learn Spring Boot",
        "_links": {
          "self": {"href": "http://localhost:8080/api/tasks/1"},
          "tasks": {"href": "http://localhost:8080/api/tasks"},
          "update": {"href": "http://localhost:8080/api/tasks/1"},
          "complete": {"href": "http://localhost:8080/api/tasks/1/complete"},
          "start": {"href": "http://localhost:8080/api/tasks/1/start"},
          "delete": {"href": "http://localhost:8080/api/tasks/1"}
        }
      }
    ]
  },
  "_links": {
    "self": {"href": "http://localhost:8080/api/tasks"},
    "create": {"href": "http://localhost:8080/api/tasks"},
    "overdue": {"href": "http://localhost:8080/api/tasks/overdue"},
    "stats": {"href": "http://localhost:8080/api/tasks/stats"}
  },
  "page": {
    "size": 10,
    "totalElements": 6,
    "totalPages": 1,
    "number": 0
  }
}
```

### Step 6.3: Test Actuator Endpoints

```bash
# Health check
curl http://localhost:8080/actuator/health | jq

# Application info
curl http://localhost:8080/actuator/info | jq

# Metrics
curl http://localhost:8080/actuator/metrics | jq

# Custom metrics
curl http://localhost:8080/actuator/metrics/tasks.created.total | jq
curl http://localhost:8080/actuator/metrics/tasks.active.count | jq
```

### Step 6.4: Access Swagger UI

Open in browser: http://localhost:8080/swagger-ui.html

You'll see interactive API documentation with:
- All endpoints listed
- Request/response schemas
- Try it out functionality

---

## Part 7: Challenge Exercises

### Challenge 1: Add Rate Limiting Info

Add a custom actuator endpoint that shows:
- Total API requests in the last hour
- Average response time
- Error rate

### Challenge 2: Conditional Links

Modify the assembler to:
- Only show "start" link if status is TODO
- Only show "complete" link if status is IN_PROGRESS
- Add a "reopen" link for COMPLETED tasks

### Challenge 3: Prometheus Integration

Configure the application to expose metrics for Prometheus:
- Access `/actuator/prometheus`
- Create a Grafana dashboard (optional)

### Challenge 4: API Versioning

Implement header-based API versioning:
- V1: Current response format
- V2: Add new fields (e.g., `timeEstimate`, `assignee`)

---

## Summary

In this lab, you learned:

1. **HATEOAS**: Adding hypermedia links to REST responses
2. **Actuator**: Production monitoring with health checks and metrics
3. **Custom Health Indicators**: Application-specific health monitoring
4. **Custom Metrics**: Business metrics with Micrometer
5. **OpenAPI/Swagger**: Interactive API documentation

## Next Steps

In Module 5, you'll add security to your API with Spring Security.
