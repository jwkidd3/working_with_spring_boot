# Lab 7b: API Documentation with OpenAPI

## Objectives

By the end of this lab, you will be able to:
- Integrate SpringDoc OpenAPI with Spring Boot
- Access and customize Swagger UI
- Document API endpoints with annotations
- Customize schemas and examples
- Group APIs and add security documentation

## Prerequisites

- Completed Labs 1-8
- Understanding of REST API concepts

## Duration

45-60 minutes

---

## Part 1: Adding SpringDoc OpenAPI

### Step 1.1: Add Dependencies

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

### Step 1.2: Basic Configuration

Create `src/main/resources/application.properties` entries:

```properties
# OpenAPI Configuration
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.operationsSorter=method
springdoc.swagger-ui.tagsSorter=alpha
springdoc.swagger-ui.tryItOutEnabled=true

# Show actuator endpoints in docs (optional)
springdoc.show-actuator=false

# Packages to scan
springdoc.packagesToScan=com.example.taskapi.controller
```

### Step 1.3: Start and Access Documentation

```bash
./mvnw spring-boot:run
```

Access the documentation:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs
- **OpenAPI YAML**: http://localhost:8080/api-docs.yaml

---

## Part 2: Configuring OpenAPI Metadata

### Step 2.1: Create OpenAPI Configuration

Create `config/OpenApiConfig.java`:

```java
package com.example.taskapi.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name:Task API}")
    private String applicationName;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title(applicationName)
                .version("1.0.0")
                .description("""
                    Task Management API for managing tasks, assignments, and workflows.

                    ## Features
                    - Create, read, update, and delete tasks
                    - Assign tasks to users
                    - Track task status and priority
                    - Search and filter tasks

                    ## Authentication
                    This API uses JWT Bearer tokens for authentication.
                    """)
                .contact(new Contact()
                    .name("API Support")
                    .email("support@example.com")
                    .url("https://example.com/support"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8080")
                    .description("Development Server"),
                new Server()
                    .url("https://api.example.com")
                    .description("Production Server")
            ));
    }
}
```

### Step 2.2: Refresh and View

Restart the application and refresh Swagger UI to see the updated metadata.

---

## Part 3: Documenting Controllers

### Step 3.1: Add Controller-Level Documentation

Update your `TaskController.java`:

```java
package com.example.taskapi.controller;

import com.example.taskapi.dto.CreateTaskRequest;
import com.example.taskapi.dto.TaskResponse;
import com.example.taskapi.dto.UpdateTaskRequest;
import com.example.taskapi.entity.Task;
import com.example.taskapi.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tasks", description = "Task management endpoints")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @Operation(
        summary = "Get all tasks",
        description = "Retrieves a paginated list of all tasks. Supports sorting and filtering."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved task list"
        )
    })
    @GetMapping
    public Page<TaskResponse> getAllTasks(
            @Parameter(description = "Pagination and sorting parameters")
            Pageable pageable) {
        return taskService.findAll(pageable).map(TaskResponse::from);
    }

    @Operation(
        summary = "Get task by ID",
        description = "Retrieves a single task by its unique identifier"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Task found",
            content = @Content(schema = @Schema(implementation = TaskResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Task not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @GetMapping("/{id}")
    public TaskResponse getTask(
            @Parameter(description = "Task ID", required = true, example = "1")
            @PathVariable Long id) {
        return TaskResponse.from(taskService.findById(id));
    }

    @Operation(
        summary = "Create a new task",
        description = "Creates a new task with the provided details"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Task created successfully"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data"
        )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse createTask(
            @Parameter(description = "Task details", required = true)
            @Valid @RequestBody CreateTaskRequest request) {
        Task task = taskService.create(request);
        return TaskResponse.from(task);
    }

    @Operation(
        summary = "Update an existing task",
        description = "Updates a task with the provided details. All fields are optional."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Task updated successfully"),
        @ApiResponse(responseCode = "404", description = "Task not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PutMapping("/{id}")
    public TaskResponse updateTask(
            @Parameter(description = "Task ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskRequest request) {
        Task task = taskService.update(id, request);
        return TaskResponse.from(task);
    }

    @Operation(
        summary = "Delete a task",
        description = "Permanently deletes a task. This action cannot be undone."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(
            @Parameter(description = "Task ID", required = true)
            @PathVariable Long id) {
        taskService.delete(id);
    }
}
```

### Step 3.2: Create Error Response Schema

Create `dto/ErrorResponse.java`:

```java
package com.example.taskapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Standard error response")
public record ErrorResponse(

    @Schema(description = "HTTP status code", example = "404")
    int status,

    @Schema(description = "Error type", example = "Not Found")
    String error,

    @Schema(description = "Detailed error message", example = "Task not found with id: 123")
    String message,

    @Schema(description = "Request path", example = "/api/tasks/123")
    String path,

    @Schema(description = "Timestamp of the error")
    LocalDateTime timestamp,

    @Schema(description = "Field-specific validation errors", nullable = true)
    List<FieldError> fieldErrors
) {
    @Schema(description = "Field validation error details")
    public record FieldError(
        @Schema(description = "Field name", example = "title")
        String field,

        @Schema(description = "Error message", example = "must not be blank")
        String message
    ) {}
}
```

---

## Part 4: Documenting DTOs and Schemas

### Step 4.1: Document Request DTOs

Create/Update `dto/CreateTaskRequest.java`:

```java
package com.example.taskapi.dto;

import com.example.taskapi.entity.TaskPriority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Request body for creating a new task")
public class CreateTaskRequest {

    @Schema(
        description = "Title of the task",
        example = "Complete project documentation",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minLength = 1,
        maxLength = 200
    )
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must be less than 200 characters")
    private String title;

    @Schema(
        description = "Detailed description of the task",
        example = "Write comprehensive API documentation including examples and error codes",
        maxLength = 2000
    )
    @Size(max = 2000, message = "Description must be less than 2000 characters")
    private String description;

    @Schema(
        description = "Task priority level",
        example = "HIGH",
        defaultValue = "MEDIUM"
    )
    private TaskPriority priority = TaskPriority.MEDIUM;

    @Schema(
        description = "Username of the assignee",
        example = "john.doe"
    )
    private String assignee;

    @Schema(
        description = "Due date for the task",
        example = "2024-12-31",
        format = "date"
    )
    private LocalDate dueDate;

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public TaskPriority getPriority() { return priority; }
    public void setPriority(TaskPriority priority) { this.priority = priority; }
    public String getAssignee() { return assignee; }
    public void setAssignee(String assignee) { this.assignee = assignee; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
}
```

### Step 4.2: Document Response DTOs

Create/Update `dto/TaskResponse.java`:

```java
package com.example.taskapi.dto;

import com.example.taskapi.entity.Task;
import com.example.taskapi.entity.TaskPriority;
import com.example.taskapi.entity.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Task response containing all task details")
public record TaskResponse(

    @Schema(description = "Unique task identifier", example = "1")
    Long id,

    @Schema(description = "Task title", example = "Complete project documentation")
    String title,

    @Schema(description = "Task description", example = "Write comprehensive API docs")
    String description,

    @Schema(description = "Current status of the task", example = "IN_PROGRESS")
    TaskStatus status,

    @Schema(description = "Priority level", example = "HIGH")
    TaskPriority priority,

    @Schema(description = "Assigned user", example = "john.doe")
    String assignee,

    @Schema(description = "Due date", example = "2024-12-31")
    LocalDate dueDate,

    @Schema(description = "Creation timestamp")
    LocalDateTime createdAt,

    @Schema(description = "Last update timestamp")
    LocalDateTime updatedAt,

    @Schema(description = "Version for optimistic locking", example = "0")
    Long version

) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(
            task.getId(),
            task.getTitle(),
            task.getDescription(),
            task.getStatus(),
            task.getPriority(),
            task.getAssignee(),
            task.getDueDate(),
            task.getCreatedAt(),
            task.getUpdatedAt(),
            task.getVersion()
        );
    }
}
```

### Step 4.3: Document Enums

Update `entity/TaskStatus.java`:

```java
package com.example.taskapi.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Task status values")
public enum TaskStatus {

    @Schema(description = "Task is pending and not started")
    TODO,

    @Schema(description = "Task is currently being worked on")
    IN_PROGRESS,

    @Schema(description = "Task has been completed")
    COMPLETED,

    @Schema(description = "Task has been cancelled")
    CANCELLED
}
```

Update `entity/TaskPriority.java`:

```java
package com.example.taskapi.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Task priority levels")
public enum TaskPriority {

    @Schema(description = "Low priority - can be done when time permits")
    LOW,

    @Schema(description = "Medium priority - standard tasks")
    MEDIUM,

    @Schema(description = "High priority - should be done soon")
    HIGH,

    @Schema(description = "Urgent - requires immediate attention")
    URGENT
}
```

---

## Part 5: Adding Examples

### Step 5.1: Add Request/Response Examples

Update controller methods with examples:

```java
@Operation(
    summary = "Create a new task",
    description = "Creates a new task with the provided details"
)
@io.swagger.v3.oas.annotations.parameters.RequestBody(
    description = "Task creation request",
    required = true,
    content = @Content(
        mediaType = "application/json",
        schema = @Schema(implementation = CreateTaskRequest.class),
        examples = {
            @io.swagger.v3.oas.annotations.media.ExampleObject(
                name = "Simple Task",
                summary = "Basic task with minimal fields",
                value = """
                    {
                        "title": "Review pull request",
                        "description": "Review and approve PR #123"
                    }
                    """
            ),
            @io.swagger.v3.oas.annotations.media.ExampleObject(
                name = "Urgent Task",
                summary = "High priority task with due date",
                value = """
                    {
                        "title": "Fix production bug",
                        "description": "Users cannot login - investigate and fix",
                        "priority": "URGENT",
                        "assignee": "senior.dev",
                        "dueDate": "2024-01-20"
                    }
                    """
            )
        }
    )
)
@PostMapping
@ResponseStatus(HttpStatus.CREATED)
public TaskResponse createTask(@Valid @RequestBody CreateTaskRequest request) {
    // implementation
}
```

### Step 5.2: Document Query Parameters

```java
@Operation(summary = "Search tasks with filters")
@GetMapping("/search")
public Page<TaskResponse> searchTasks(

    @Parameter(
        description = "Search keyword (searches in title and description)",
        example = "documentation"
    )
    @RequestParam(required = false) String keyword,

    @Parameter(
        description = "Filter by status",
        schema = @Schema(implementation = TaskStatus.class)
    )
    @RequestParam(required = false) TaskStatus status,

    @Parameter(
        description = "Filter by priority",
        schema = @Schema(implementation = TaskPriority.class)
    )
    @RequestParam(required = false) TaskPriority priority,

    @Parameter(
        description = "Filter by assignee username",
        example = "john.doe"
    )
    @RequestParam(required = false) String assignee,

    @Parameter(
        description = "Filter tasks due before this date",
        example = "2024-12-31"
    )
    @RequestParam(required = false) LocalDate dueBefore,

    @Parameter(hidden = true)  // Hide from docs
    Pageable pageable
) {
    // implementation
}
```

---

## Part 6: Security Documentation

### Step 6.1: Add Security Scheme

Update `OpenApiConfig.java`:

```java
package com.example.taskapi.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
            .info(new Info()
                .title("Task API")
                .version("1.0.0")
                .description("Task Management API"))
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
            .components(new Components()
                .addSecuritySchemes(securitySchemeName,
                    new SecurityScheme()
                        .name(securitySchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Enter JWT token")
                )
            );
    }
}
```

### Step 6.2: Mark Public Endpoints

For endpoints that don't require authentication:

```java
@Operation(
    summary = "Health check",
    security = {}  // Empty = no security required
)
@GetMapping("/health")
public Map<String, String> health() {
    return Map.of("status", "UP");
}
```

### Step 6.3: Document Role Requirements

```java
@Operation(
    summary = "Delete a task",
    description = "Permanently deletes a task. **Requires ADMIN role.**"
)
@ApiResponses({
    @ApiResponse(responseCode = "204", description = "Task deleted"),
    @ApiResponse(responseCode = "401", description = "Not authenticated"),
    @ApiResponse(responseCode = "403", description = "Not authorized - requires ADMIN role"),
    @ApiResponse(responseCode = "404", description = "Task not found")
})
@DeleteMapping("/{id}")
@ResponseStatus(HttpStatus.NO_CONTENT)
public void deleteTask(@PathVariable Long id) {
    taskService.delete(id);
}
```

---

## Part 7: API Grouping

### Step 7.1: Group APIs by Tag

Create multiple controller tags:

```java
// TaskController.java
@Tag(name = "Tasks", description = "Task CRUD operations")
@RestController
@RequestMapping("/api/tasks")
public class TaskController { }

// TaskSearchController.java
@Tag(name = "Search", description = "Task search and filtering")
@RestController
@RequestMapping("/api/tasks/search")
public class TaskSearchController { }

// TaskStatisticsController.java
@Tag(name = "Statistics", description = "Task analytics and reports")
@RestController
@RequestMapping("/api/tasks/stats")
public class TaskStatisticsController { }
```

### Step 7.2: Configure Tag Order

Update `OpenApiConfig.java`:

```java
import io.swagger.v3.oas.models.tags.Tag;

@Bean
public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(new Info().title("Task API").version("1.0.0"))
        .tags(List.of(
            new Tag().name("Tasks").description("Core task operations"),
            new Tag().name("Search").description("Search and filter tasks"),
            new Tag().name("Statistics").description("Analytics and reports")
        ));
}
```

### Step 7.3: Multiple API Groups (Optional)

For larger applications, create separate API groups:

```properties
# Public API
springdoc.group-configs[0].group=public
springdoc.group-configs[0].paths-to-match=/api/public/**

# Admin API
springdoc.group-configs[1].group=admin
springdoc.group-configs[1].paths-to-match=/api/admin/**

# Internal API
springdoc.group-configs[2].group=internal
springdoc.group-configs[2].paths-to-match=/api/internal/**
```

Access different groups:
- http://localhost:8080/swagger-ui.html?urls.primaryName=public
- http://localhost:8080/swagger-ui.html?urls.primaryName=admin

---

## Part 8: Customizing Swagger UI

### Step 8.1: Customize Appearance

Add to `application.properties`:

```properties
# Swagger UI Customization
springdoc.swagger-ui.display-request-duration=true
springdoc.swagger-ui.filter=true
springdoc.swagger-ui.show-extensions=true
springdoc.swagger-ui.show-common-extensions=true
springdoc.swagger-ui.doc-expansion=none
springdoc.swagger-ui.default-models-expand-depth=2
springdoc.swagger-ui.default-model-expand-depth=2

# Syntax highlighting
springdoc.swagger-ui.syntax-highlight.theme=monokai

# Disable "Try it out" for specific operations
# springdoc.swagger-ui.supported-submit-methods=get,post
```

### Step 8.2: Custom CSS (Advanced)

Create `src/main/resources/static/swagger-ui/custom.css`:

```css
/* Custom Swagger UI styles */
.swagger-ui .topbar {
    background-color: #1a1a2e;
}

.swagger-ui .info .title {
    color: #16213e;
}
```

Configure in `application.properties`:

```properties
springdoc.swagger-ui.css=swagger-ui/custom.css
```

---

## Part 9: Testing the Documentation

### Step 9.1: Verify All Endpoints

1. Open http://localhost:8080/swagger-ui.html
2. Expand each tag and verify descriptions
3. Check request/response schemas
4. Test "Try it out" functionality

### Step 9.2: Export OpenAPI Spec

```bash
# Download JSON spec
curl http://localhost:8080/api-docs -o openapi.json

# Download YAML spec
curl http://localhost:8080/api-docs.yaml -o openapi.yaml
```

### Step 9.3: Validate Spec

Use online validators or CLI tools:

```bash
# Using npm swagger-cli
npx @apidevtools/swagger-cli validate openapi.yaml
```

---

## Summary

In this lab, you learned:

1. **SpringDoc Setup**: Adding OpenAPI documentation to Spring Boot
2. **Metadata Configuration**: API info, contact, license, servers
3. **Endpoint Documentation**: @Operation, @ApiResponses, @Parameter
4. **Schema Documentation**: @Schema with examples and descriptions
5. **Security Documentation**: JWT Bearer token configuration
6. **API Grouping**: Organizing endpoints with tags

## Key Annotations

| Annotation | Purpose |
|------------|---------|
| `@Tag` | Group endpoints by category |
| `@Operation` | Describe an endpoint |
| `@ApiResponse` | Document response codes |
| `@Parameter` | Describe path/query parameters |
| `@Schema` | Document DTOs and fields |
| `@ExampleObject` | Provide request/response examples |

## Best Practices

1. **Be Descriptive**: Write clear summaries and descriptions
2. **Use Examples**: Provide realistic example values
3. **Document Errors**: List all possible error responses
4. **Keep Updated**: Documentation should match implementation
5. **Test Regularly**: Verify docs are accurate and complete

## Course Completion

Congratulations! You've completed all labs in the Spring Boot Workshop!
