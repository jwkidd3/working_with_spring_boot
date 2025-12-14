# Lab 3: Using Spring Boot with Databases

## Objectives

By the end of this lab, you will be able to:
- Configure a database with Spring Boot
- Create JPA entities with proper mappings
- Use Spring Data JPA repositories
- Implement custom queries
- Add pagination and sorting

## Prerequisites

- Completed Labs 1 and 2
- Understanding of SQL basics

## Duration

60-75 minutes

---

## Scenario

You will enhance the Task Management API from Lab 2 to persist data in a database. We'll use HSQLDB for development (in-memory database) and create a proper data access layer.

---

## Part 1: Project Setup

### Step 1.1: Create New Project or Modify Existing

Create a new project from Spring Initializr or add dependencies to your Lab 2 project.

**Dependencies:**
- Spring Web
- Spring Data JPA
- Validation
- HSQLDB Database

**Add to pom.xml:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<dependency>
    <groupId>org.hsqldb</groupId>
    <artifactId>hsqldb</artifactId>
    <scope>runtime</scope>
</dependency>
```

### Step 1.2: Configure Database

Update `src/main/resources/application.properties`:

```properties
# Application
spring.application.name=task-api

# HSQLDB In-Memory Database Configuration
spring.datasource.url=jdbc:hsqldb:mem:taskdb
spring.datasource.driver-class-name=org.hsqldb.jdbc.JDBCDriver
spring.datasource.username=sa
spring.datasource.password=

# JPA Configuration
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Better error messages
server.error.include-message=always
```

---

## Part 2: Create JPA Entities

### Step 2.1: Create the Task Entity

Replace the Task model with a JPA entity.

Create `src/main/java/com/example/taskapi/entity/Task.java`:

```java
package com.example.taskapi.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.TODO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskPriority priority = TaskPriority.MEDIUM;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructors
    public Task() {
    }

    public Task(String title, String description) {
        this.title = title;
        this.description = description;
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

### Step 2.2: Move Enums to Entity Package

Create `src/main/java/com/example/taskapi/entity/TaskStatus.java`:

```java
package com.example.taskapi.entity;

public enum TaskStatus {
    TODO,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}
```

Create `src/main/java/com/example/taskapi/entity/TaskPriority.java`:

```java
package com.example.taskapi.entity;

public enum TaskPriority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}
```

---

## Part 3: Create the Repository

### Step 3.1: Create Task Repository

Create `src/main/java/com/example/taskapi/repository/TaskRepository.java`:

```java
package com.example.taskapi.repository;

import com.example.taskapi.entity.Task;
import com.example.taskapi.entity.TaskPriority;
import com.example.taskapi.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // Derived query methods
    List<Task> findByStatus(TaskStatus status);

    List<Task> findByPriority(TaskPriority priority);

    List<Task> findByStatusAndPriority(TaskStatus status, TaskPriority priority);

    // Find tasks due before a certain date
    List<Task> findByDueDateBefore(LocalDateTime date);

    // Find overdue tasks (due date passed and not completed)
    @Query("SELECT t FROM Task t WHERE t.dueDate < :now AND t.status != 'COMPLETED'")
    List<Task> findOverdueTasks(@Param("now") LocalDateTime now);

    // Search by title (case-insensitive)
    List<Task> findByTitleContainingIgnoreCase(String keyword);

    // Count tasks by status
    long countByStatus(TaskStatus status);

    // Pagination support
    Page<Task> findByStatus(TaskStatus status, Pageable pageable);

    // Custom query with multiple filters
    @Query("SELECT t FROM Task t WHERE " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:priority IS NULL OR t.priority = :priority) AND " +
           "(:keyword IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Task> findWithFilters(
        @Param("status") TaskStatus status,
        @Param("priority") TaskPriority priority,
        @Param("keyword") String keyword,
        Pageable pageable);
}
```

---

## Part 4: Update DTOs

### Step 4.1: Update Request DTOs

Update `src/main/java/com/example/taskapi/dto/CreateTaskRequest.java`:

```java
package com.example.taskapi.dto;

import com.example.taskapi.entity.TaskPriority;
import jakarta.validation.constraints.Future;
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

    @Future(message = "Due date must be in the future")
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

Update `src/main/java/com/example/taskapi/dto/UpdateTaskRequest.java`:

```java
package com.example.taskapi.dto;

import com.example.taskapi.entity.TaskPriority;
import com.example.taskapi.entity.TaskStatus;
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

### Step 4.2: Create Page Response DTO

Create `src/main/java/com/example/taskapi/dto/PageResponse.java`:

```java
package com.example.taskapi.dto;

import org.springframework.data.domain.Page;
import java.util.List;

public class PageResponse<T> {

    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;

    public PageResponse(Page<T> page) {
        this.content = page.getContent();
        this.pageNumber = page.getNumber();
        this.pageSize = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.first = page.isFirst();
        this.last = page.isLast();
    }

    // Getters
    public List<T> getContent() {
        return content;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean isFirst() {
        return first;
    }

    public boolean isLast() {
        return last;
    }
}
```

---

## Part 5: Update the Service Layer

### Step 5.1: Update Task Service

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

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
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
    public List<Task> findByPriority(TaskPriority priority) {
        return taskRepository.findByPriority(priority);
    }

    @Transactional(readOnly = true)
    public Page<Task> search(TaskStatus status, TaskPriority priority,
                             String keyword, Pageable pageable) {
        return taskRepository.findWithFilters(status, priority, keyword, pageable);
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

        return taskRepository.save(task);
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

## Part 6: Update the Controller

### Step 6.1: Update Task Controller

Update `src/main/java/com/example/taskapi/controller/TaskController.java`:

```java
package com.example.taskapi.controller;

import com.example.taskapi.dto.CreateTaskRequest;
import com.example.taskapi.dto.PageResponse;
import com.example.taskapi.dto.UpdateTaskRequest;
import com.example.taskapi.entity.Task;
import com.example.taskapi.entity.TaskPriority;
import com.example.taskapi.entity.TaskStatus;
import com.example.taskapi.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    // GET /api/tasks - List all tasks with pagination
    @GetMapping
    public PageResponse<Task> getAllTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
            ? Sort.by(sortBy).ascending()
            : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Task> taskPage = taskService.findAll(pageable);

        return new PageResponse<>(taskPage);
    }

    // GET /api/tasks/{id} - Get a specific task
    @GetMapping("/{id}")
    public Task getTask(@PathVariable Long id) {
        return taskService.findById(id);
    }

    // GET /api/tasks/search - Search with filters
    @GetMapping("/search")
    public PageResponse<Task> searchTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Task> taskPage = taskService.search(status, priority, keyword, pageable);

        return new PageResponse<>(taskPage);
    }

    // GET /api/tasks/status/{status} - Get tasks by status
    @GetMapping("/status/{status}")
    public List<Task> getTasksByStatus(@PathVariable TaskStatus status) {
        return taskService.findByStatus(status);
    }

    // GET /api/tasks/overdue - Get overdue tasks
    @GetMapping("/overdue")
    public List<Task> getOverdueTasks() {
        return taskService.findOverdueTasks();
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

    // GET /api/tasks/stats - Get task statistics
    @GetMapping("/stats")
    public TaskStats getStats() {
        return new TaskStats(
            taskService.countByStatus(TaskStatus.TODO),
            taskService.countByStatus(TaskStatus.IN_PROGRESS),
            taskService.countByStatus(TaskStatus.COMPLETED),
            taskService.countByStatus(TaskStatus.CANCELLED)
        );
    }

    // Inner class for stats response
    public record TaskStats(long todo, long inProgress, long completed, long cancelled) {}
}
```

---

## Part 7: Add Sample Data

### Step 7.1: Create Data Initializer

Create `src/main/java/com/example/taskapi/config/DataInitializer.java`:

```java
package com.example.taskapi.config;

import com.example.taskapi.entity.Task;
import com.example.taskapi.entity.TaskPriority;
import com.example.taskapi.entity.TaskStatus;
import com.example.taskapi.repository.TaskRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(TaskRepository repository) {
        return args -> {
            // Only initialize if database is empty
            if (repository.count() == 0) {
                Task task1 = new Task("Learn Spring Boot", "Complete the Spring Boot workshop");
                task1.setPriority(TaskPriority.HIGH);
                task1.setDueDate(LocalDateTime.now().plusDays(7));

                Task task2 = new Task("Build REST API", "Implement CRUD operations");
                task2.setStatus(TaskStatus.IN_PROGRESS);
                task2.setPriority(TaskPriority.HIGH);

                Task task3 = new Task("Write unit tests", "Add comprehensive test coverage");
                task3.setPriority(TaskPriority.MEDIUM);
                task3.setDueDate(LocalDateTime.now().plusDays(14));

                Task task4 = new Task("Deploy to production", "Set up CI/CD pipeline");
                task4.setPriority(TaskPriority.LOW);
                task4.setDueDate(LocalDateTime.now().plusDays(30));

                Task task5 = new Task("Review code", "Peer review pending PRs");
                task5.setStatus(TaskStatus.COMPLETED);
                task5.setPriority(TaskPriority.MEDIUM);

                Task task6 = new Task("Overdue task example", "This task is overdue");
                task6.setDueDate(LocalDateTime.now().minusDays(1));
                task6.setPriority(TaskPriority.URGENT);

                repository.save(task1);
                repository.save(task2);
                repository.save(task3);
                repository.save(task4);
                repository.save(task5);
                repository.save(task6);

                System.out.println("Sample data initialized!");
            }
        };
    }
}
```

---

## Part 8: Testing

### Step 8.1: Run the Application

```bash
./mvnw spring-boot:run
```

### Step 8.2: Test the Endpoints

**Get all tasks with pagination:**
```bash
curl "http://localhost:8080/api/tasks?page=0&size=5"
```

**Search tasks:**
```bash
curl "http://localhost:8080/api/tasks/search?status=TODO&priority=HIGH"
curl "http://localhost:8080/api/tasks/search?keyword=Spring"
```

**Get overdue tasks:**
```bash
curl http://localhost:8080/api/tasks/overdue
```

**Get task statistics:**
```bash
curl http://localhost:8080/api/tasks/stats
```

**Create a task:**
```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "New Database Task",
    "description": "Testing JPA persistence",
    "priority": "HIGH"
  }'
```

### Step 8.3: Verify Data Persistence

You can verify data persistence using the API endpoints or by adding a simple database manager tool. For quick testing, use the REST API:

```bash
# List all tasks
curl http://localhost:8080/api/tasks | jq

# Check database via API search
curl "http://localhost:8080/api/tasks/search/status?status=TODO" | jq
```

Alternatively, you can add SQL logging to see queries:
```properties
# Already enabled in application.properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

---

## Part 9: Challenge Exercises

### Challenge 1: Add Category Entity

Create a `Category` entity with a many-to-one relationship:
- Tasks belong to a category
- Categories have a name and description
- Filter tasks by category

### Challenge 2: Add Auditing

Implement JPA Auditing to automatically track:
- Created by
- Last modified by
- Add `@EntityListeners` and `@EnableJpaAuditing`

### Challenge 3: Implement Soft Delete

Instead of actually deleting tasks:
- Add a `deleted` boolean field
- Override delete to set `deleted = true`
- Filter out deleted tasks in queries

### Challenge 4: Add Task History

Create a `TaskHistory` entity that:
- Records every status change
- Includes timestamp and previous/new status
- Accessible via `/api/tasks/{id}/history`

---

## Summary

In this lab, you learned:

1. **Database Configuration**: Setting up H2 and JPA properties
2. **JPA Entities**: Creating entities with proper annotations
3. **Repositories**: Using Spring Data JPA repositories
4. **Query Methods**: Derived queries and @Query annotations
5. **Pagination**: Implementing paginated responses
6. **Transactions**: Using @Transactional for data consistency

## Next Steps

In Module 4, you'll build more advanced REST features including HATEOAS, filtering, and monitoring.
