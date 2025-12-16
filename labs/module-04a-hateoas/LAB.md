# Lab 4a: Implementing HATEOAS

## Objectives

By the end of this lab, you will be able to:
- Understand HATEOAS principles and benefits
- Add hypermedia links to REST responses
- Create a `RepresentationModelAssembler`
- Implement pagination with HATEOAS links
- Build discoverable APIs

## Prerequisites

- Completed Labs 1-4
- Understanding of REST principles
- Working Task API with Spring Data JPA

## Duration

45-60 minutes

---

## Scenario

You will enhance the Task Management API to follow HATEOAS principles, making the API self-documenting and discoverable. Clients will be able to navigate the API by following links in responses.

---

## Part 1: Understanding HATEOAS

### What is HATEOAS?

**Hypermedia as the Engine of Application State**

- REST architectural constraint
- Responses include links to related resources and actions
- Clients navigate the API through links, not hardcoded URLs
- Makes APIs self-documenting and evolvable

### Before HATEOAS:
```json
{
  "id": 1,
  "title": "Complete report",
  "status": "TODO"
}
```

### After HATEOAS:
```json
{
  "id": 1,
  "title": "Complete report",
  "status": "TODO",
  "_links": {
    "self": { "href": "http://localhost:8080/api/tasks/1" },
    "update": { "href": "http://localhost:8080/api/tasks/1" },
    "delete": { "href": "http://localhost:8080/api/tasks/1" },
    "start": { "href": "http://localhost:8080/api/tasks/1/start" },
    "collection": { "href": "http://localhost:8080/api/tasks" }
  }
}
```

---

## Part 2: Project Setup

### Step 2.1: Add HATEOAS Dependency

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-hateoas</artifactId>
</dependency>
```

### Step 2.2: Verify Existing Task Entity

Ensure your `Task` entity is set up:

```java
@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    private TaskStatus status = TaskStatus.TODO;

    @Enumerated(EnumType.STRING)
    private TaskPriority priority = TaskPriority.MEDIUM;

    private LocalDateTime dueDate;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and setters...
}
```

---

## Part 3: Create HATEOAS Response DTO

### Step 3.1: Create TaskResponse with RepresentationModel

Create `src/main/java/com/example/taskapi/dto/TaskResponse.java`:

```java
package com.example.taskapi.dto;

import com.example.taskapi.entity.Task;
import com.example.taskapi.entity.TaskPriority;
import com.example.taskapi.entity.TaskStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.time.LocalDateTime;

@Relation(collectionRelation = "tasks", itemRelation = "task")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskResponse extends RepresentationModel<TaskResponse> {

    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean overdue;

    // Static factory method
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

        // Calculate if overdue
        if (task.getDueDate() != null &&
            task.getStatus() != TaskStatus.COMPLETED &&
            task.getDueDate().isBefore(LocalDateTime.now())) {
            response.setOverdue(true);
        }

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

    public boolean isOverdue() {
        return overdue;
    }

    public void setOverdue(boolean overdue) {
        this.overdue = overdue;
    }
}
```

---

## Part 4: Create Model Assembler

### Step 4.1: Create TaskModelAssembler

Create `src/main/java/com/example/taskapi/assembler/TaskModelAssembler.java`:

```java
package com.example.taskapi.assembler;

import com.example.taskapi.controller.TaskController;
import com.example.taskapi.dto.TaskResponse;
import com.example.taskapi.entity.Task;
import com.example.taskapi.entity.TaskStatus;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class TaskModelAssembler
        extends RepresentationModelAssemblerSupport<Task, TaskResponse> {

    public TaskModelAssembler() {
        super(TaskController.class, TaskResponse.class);
    }

    @Override
    public TaskResponse toModel(Task task) {
        TaskResponse response = TaskResponse.from(task);

        // Self link - always present
        response.add(linkTo(methodOn(TaskController.class)
            .getTask(task.getId()))
            .withSelfRel());

        // Collection link
        response.add(linkTo(methodOn(TaskController.class)
            .getAllTasks(0, 10))
            .withRel("tasks"));

        // Conditional links based on status
        addStatusBasedLinks(response, task);

        // Update link (if not completed)
        if (task.getStatus() != TaskStatus.COMPLETED) {
            response.add(linkTo(methodOn(TaskController.class)
                .updateTask(task.getId(), null))
                .withRel("update"));
        }

        // Delete link - always available
        response.add(linkTo(methodOn(TaskController.class)
            .deleteTask(task.getId()))
            .withRel("delete"));

        return response;
    }

    private void addStatusBasedLinks(TaskResponse response, Task task) {
        switch (task.getStatus()) {
            case TODO:
                // Can start or complete directly
                response.add(linkTo(methodOn(TaskController.class)
                    .startTask(task.getId()))
                    .withRel("start"));
                response.add(linkTo(methodOn(TaskController.class)
                    .completeTask(task.getId()))
                    .withRel("complete"));
                break;

            case IN_PROGRESS:
                // Can complete or cancel
                response.add(linkTo(methodOn(TaskController.class)
                    .completeTask(task.getId()))
                    .withRel("complete"));
                response.add(linkTo(methodOn(TaskController.class)
                    .cancelTask(task.getId()))
                    .withRel("cancel"));
                break;

            case COMPLETED:
                // Can reopen
                response.add(linkTo(methodOn(TaskController.class)
                    .reopenTask(task.getId()))
                    .withRel("reopen"));
                break;

            case CANCELLED:
                // Can reopen
                response.add(linkTo(methodOn(TaskController.class)
                    .reopenTask(task.getId()))
                    .withRel("reopen"));
                break;
        }
    }
}
```

---

## Part 5: Update Controller with HATEOAS

### Step 5.1: Update TaskController

Update `src/main/java/com/example/taskapi/controller/TaskController.java`:

```java
package com.example.taskapi.controller;

import com.example.taskapi.assembler.TaskModelAssembler;
import com.example.taskapi.dto.CreateTaskRequest;
import com.example.taskapi.dto.TaskResponse;
import com.example.taskapi.dto.UpdateTaskRequest;
import com.example.taskapi.entity.Task;
import com.example.taskapi.entity.TaskStatus;
import com.example.taskapi.service.TaskService;
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
public class TaskController {

    private final TaskService taskService;
    private final TaskModelAssembler assembler;

    public TaskController(TaskService taskService, TaskModelAssembler assembler) {
        this.taskService = taskService;
        this.assembler = assembler;
    }

    @GetMapping
    public PagedModel<TaskResponse> getAllTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
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

        // Add navigation links
        pagedModel.add(linkTo(methodOn(TaskController.class)
            .getAllTasks(page, size)).withSelfRel());

        if (taskPage.hasNext()) {
            pagedModel.add(linkTo(methodOn(TaskController.class)
                .getAllTasks(page + 1, size)).withRel("next"));
        }

        if (taskPage.hasPrevious()) {
            pagedModel.add(linkTo(methodOn(TaskController.class)
                .getAllTasks(page - 1, size)).withRel("prev"));
        }

        pagedModel.add(linkTo(methodOn(TaskController.class)
            .getAllTasks(0, size)).withRel("first"));

        if (taskPage.getTotalPages() > 0) {
            pagedModel.add(linkTo(methodOn(TaskController.class)
                .getAllTasks(taskPage.getTotalPages() - 1, size)).withRel("last"));
        }

        // Add action links
        pagedModel.add(linkTo(methodOn(TaskController.class)
            .createTask(null)).withRel("create"));

        return pagedModel;
    }

    @GetMapping("/{id}")
    public TaskResponse getTask(@PathVariable Long id) {
        Task task = taskService.findById(id);
        return assembler.toModel(task);
    }

    @GetMapping("/status/{status}")
    public CollectionModel<TaskResponse> getTasksByStatus(@PathVariable TaskStatus status) {
        List<Task> tasks = taskService.findByStatus(status);
        List<TaskResponse> responses = tasks.stream()
            .map(assembler::toModel)
            .toList();

        CollectionModel<TaskResponse> collection = CollectionModel.of(responses);

        collection.add(linkTo(methodOn(TaskController.class)
            .getTasksByStatus(status)).withSelfRel());
        collection.add(linkTo(methodOn(TaskController.class)
            .getAllTasks(0, 10)).withRel("all"));

        return collection;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody CreateTaskRequest request) {
        Task task = taskService.create(request);
        TaskResponse response = assembler.toModel(task);

        return ResponseEntity
            .created(linkTo(methodOn(TaskController.class)
                .getTask(task.getId())).toUri())
            .body(response);
    }

    @PutMapping("/{id}")
    public TaskResponse updateTask(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskRequest request) {
        Task task = taskService.update(id, request);
        return assembler.toModel(task);
    }

    @PostMapping("/{id}/start")
    public TaskResponse startTask(@PathVariable Long id) {
        Task task = taskService.updateStatus(id, TaskStatus.IN_PROGRESS);
        return assembler.toModel(task);
    }

    @PostMapping("/{id}/complete")
    public TaskResponse completeTask(@PathVariable Long id) {
        Task task = taskService.updateStatus(id, TaskStatus.COMPLETED);
        return assembler.toModel(task);
    }

    @PostMapping("/{id}/cancel")
    public TaskResponse cancelTask(@PathVariable Long id) {
        Task task = taskService.updateStatus(id, TaskStatus.CANCELLED);
        return assembler.toModel(task);
    }

    @PostMapping("/{id}/reopen")
    public TaskResponse reopenTask(@PathVariable Long id) {
        Task task = taskService.updateStatus(id, TaskStatus.TODO);
        return assembler.toModel(task);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable Long id) {
        taskService.delete(id);
    }
}
```

---

## Part 6: Add Root API Endpoint

### Step 6.1: Create API Root Controller

Create `src/main/java/com/example/taskapi/controller/RootController.java`:

```java
package com.example.taskapi.controller;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api")
public class RootController {

    @GetMapping
    public RepresentationModel<?> root() {
        RepresentationModel<?> model = new RepresentationModel<>();

        model.add(linkTo(methodOn(RootController.class).root()).withSelfRel());
        model.add(linkTo(methodOn(TaskController.class).getAllTasks(0, 10)).withRel("tasks"));
        model.add(linkTo(methodOn(TaskController.class).createTask(null)).withRel("create-task"));

        return model;
    }
}
```

---

## Part 7: Testing HATEOAS

### Step 7.1: Run the Application

```bash
./mvnw spring-boot:run
```

### Step 7.2: Test API Root

```bash
curl http://localhost:8080/api | jq
```

Expected response:
```json
{
  "_links": {
    "self": { "href": "http://localhost:8080/api" },
    "tasks": { "href": "http://localhost:8080/api/tasks?page=0&size=10" },
    "create-task": { "href": "http://localhost:8080/api/tasks" }
  }
}
```

### Step 7.3: Test Task List with Pagination Links

```bash
curl http://localhost:8080/api/tasks | jq
```

Expected response:
```json
{
  "_embedded": {
    "tasks": [
      {
        "id": 1,
        "title": "Learn Spring Boot",
        "status": "TODO",
        "_links": {
          "self": { "href": "http://localhost:8080/api/tasks/1" },
          "tasks": { "href": "http://localhost:8080/api/tasks?page=0&size=10" },
          "start": { "href": "http://localhost:8080/api/tasks/1/start" },
          "complete": { "href": "http://localhost:8080/api/tasks/1/complete" },
          "update": { "href": "http://localhost:8080/api/tasks/1" },
          "delete": { "href": "http://localhost:8080/api/tasks/1" }
        }
      }
    ]
  },
  "_links": {
    "self": { "href": "http://localhost:8080/api/tasks?page=0&size=10" },
    "first": { "href": "http://localhost:8080/api/tasks?page=0&size=10" },
    "last": { "href": "http://localhost:8080/api/tasks?page=0&size=10" },
    "create": { "href": "http://localhost:8080/api/tasks" }
  },
  "page": {
    "size": 10,
    "totalElements": 1,
    "totalPages": 1,
    "number": 0
  }
}
```

### Step 7.4: Test Single Task with Status-Based Links

```bash
# Get a TODO task
curl http://localhost:8080/api/tasks/1 | jq

# Start the task
curl -X POST http://localhost:8080/api/tasks/1/start | jq

# Now the links change - complete and cancel available, start removed
curl http://localhost:8080/api/tasks/1 | jq

# Complete the task
curl -X POST http://localhost:8080/api/tasks/1/complete | jq

# Now only reopen link available
curl http://localhost:8080/api/tasks/1 | jq
```

### Step 7.5: Create Task and Follow Links

```bash
# Create a task
RESPONSE=$(curl -s -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title": "Follow the links", "description": "Navigate via HATEOAS"}')

echo $RESPONSE | jq

# Extract the self link and follow it
SELF_LINK=$(echo $RESPONSE | jq -r '._links.self.href')
curl $SELF_LINK | jq
```

---

## Part 8: Challenge Exercises

### Challenge 1: Add Filtering Links

Add links to filter tasks:
- `/api/tasks?status=TODO`
- `/api/tasks?priority=HIGH`
- Add these as links in the collection response

### Challenge 2: Add Link Titles

Enhance links with titles for better documentation:
```java
response.add(Link.of("/api/tasks/1/start")
    .withRel("start")
    .withTitle("Start working on this task"));
```

### Challenge 3: Affordances

Add affordances to indicate what HTTP methods are available:
```java
response.add(linkTo(methodOn(TaskController.class)
    .updateTask(task.getId(), null))
    .withRel("update")
    .andAffordance(afford(methodOn(TaskController.class)
        .updateTask(task.getId(), null))));
```

### Challenge 4: HAL Explorer

Add HAL Explorer for interactive API exploration:
```xml
<dependency>
    <groupId>org.springframework.data</groupId>
    <artifactId>spring-data-rest-hal-explorer</artifactId>
</dependency>
```

Access at: http://localhost:8080/explorer

---

## Summary

In this lab, you learned:

1. **HATEOAS Principles**: Making APIs discoverable through hypermedia
2. **RepresentationModel**: Base class for HATEOAS responses
3. **ModelAssembler**: Centralizing link creation logic
4. **Conditional Links**: Showing different actions based on resource state
5. **Pagination Links**: Navigation through large collections
6. **API Root**: Entry point for API discovery

## Key Classes

- `RepresentationModel<T>` - Base for resources with links
- `CollectionModel<T>` - Wrapper for collections
- `PagedModel<T>` - Wrapper for paginated results
- `RepresentationModelAssemblerSupport<E, D>` - Helper for converting entities
- `WebMvcLinkBuilder` - Building links from controller methods

## Next Steps

In Lab 4b, you'll add production monitoring with Spring Boot Actuator.
