# HATEOAS Quick Reference Guide

## Essential Annotations

### @Relation
Defines collection and item names in HAL format
```java
@Relation(collectionRelation = "tasks", itemRelation = "task")
public class TaskResponse extends RepresentationModel<TaskResponse> {
    // fields
}
```

### @RestController
```java
@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    // endpoints
}
```

## Core Classes

### RepresentationModel
Base class for resources with links
```java
public class TaskResponse extends RepresentationModel<TaskResponse> {
    private Long id;
    private String title;
    // getters/setters
}
```

### RepresentationModelAssemblerSupport
Converts entities to models with links
```java
@Component
public class TaskModelAssembler
    extends RepresentationModelAssemblerSupport<Task, TaskResponse> {

    public TaskModelAssembler() {
        super(TaskController.class, TaskResponse.class);
    }

    @Override
    public TaskResponse toModel(Task task) {
        TaskResponse response = new TaskResponse(/* map fields */);

        // Add links
        response.add(linkTo(methodOn(TaskController.class)
            .getTaskById(task.getId()))
            .withSelfRel());

        return response;
    }
}
```

## Creating Links

### Self Link
```java
response.add(linkTo(methodOn(TaskController.class)
    .getTaskById(id))
    .withSelfRel());
```

### Custom Relation
```java
response.add(linkTo(methodOn(TaskController.class)
    .getAllTasks(0, 10))
    .withRel("tasks"));
```

### With Title
```java
response.add(linkTo(methodOn(TaskController.class)
    .getTasksByStatus(TaskStatus.COMPLETED, 0, 10))
    .withRel("tasks-completed")
    .withTitle("Completed tasks"));
```

### Collection Links
```java
CollectionModel<TaskResponse> resources =
    assembler.toCollectionModel(tasks);

resources.add(linkTo(methodOn(TaskController.class)
    .getAllTasks(0, 10))
    .withSelfRel());
```

## Pagination

### Controller Setup
```java
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;
    private final TaskModelAssembler assembler;
    private final PagedResourcesAssembler<Task> pagedAssembler;

    @GetMapping
    public ResponseEntity<PagedModel<TaskResponse>> getAllTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Task> taskPage = taskService.getAllTasks(pageable);

        PagedModel<TaskResponse> pagedModel =
            pagedAssembler.toModel(taskPage, assembler);

        return ResponseEntity.ok(pagedModel);
    }
}
```

### Service Layer
```java
@Service
public class TaskService {
    private final TaskRepository repository;

    public Page<Task> getAllTasks(Pageable pageable) {
        return repository.findAll(pageable);
    }
}
```

## HTTP Status Codes

### Success
```java
// 200 OK - Successful GET, PUT, PATCH
return ResponseEntity.ok(taskResponse);

// 201 Created - Successful POST
return ResponseEntity
    .created(linkTo(methodOn(TaskController.class)
        .getTaskById(task.getId()))
        .toUri())
    .body(taskResponse);

// 204 No Content - Successful DELETE
return ResponseEntity.noContent().build();
```

## Error Handling

### Custom Exception
```java
public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(String message) {
        super(message);
    }
}
```

### Exception Handler
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<Problem> handleTaskNotFound(
            TaskNotFoundException ex) {

        Problem problem = Problem.create()
            .withTitle("Task Not Found")
            .withStatus(HttpStatus.NOT_FOUND)
            .withDetail(ex.getMessage());

        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .contentType(MediaTypes.HTTP_PROBLEM_DETAILS_JSON)
            .body(problem);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Problem> handleIllegalState(
            IllegalStateException ex) {

        Problem problem = Problem.create()
            .withTitle("Invalid State Transition")
            .withStatus(HttpStatus.CONFLICT)
            .withDetail(ex.getMessage());

        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .contentType(MediaTypes.HTTP_PROBLEM_DETAILS_JSON)
            .body(problem);
    }
}
```

## Validation

### Request DTO
```java
public class CreateTaskRequest {
    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 100)
    private String title;

    @NotNull(message = "Priority is required")
    private TaskPriority priority;

    // getters/setters
}
```

### Controller
```java
@PostMapping
public ResponseEntity<TaskResponse> createTask(
        @Valid @RequestBody CreateTaskRequest request) {

    Task task = taskService.createTask(request);
    TaskResponse response = assembler.toModel(task);

    return ResponseEntity
        .created(linkTo(methodOn(TaskController.class)
            .getTaskById(task.getId()))
            .toUri())
        .body(response);
}
```

### Validation Error Handler
```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<Problem> handleValidation(
        MethodArgumentNotValidException ex) {

    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach(error -> {
        String field = ((FieldError) error).getField();
        String message = error.getDefaultMessage();
        errors.put(field, message);
    });

    Problem problem = Problem.create()
        .withTitle("Validation Failed")
        .withStatus(HttpStatus.BAD_REQUEST)
        .withDetail("Invalid request parameters")
        .withProperties(map -> map.put("errors", errors));

    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(problem);
}
```

## Configuration

### application.properties
```properties
# HATEOAS Configuration
spring.hateoas.use-hal-as-default-json-media-type=true

# JSON Formatting
spring.jackson.serialization.indent-output=true
```

## Common Patterns

### API Root Endpoint
```java
@RestController
@RequestMapping("/api")
public class RootController {

    @GetMapping
    public ResponseEntity<RepresentationModel<?>> root() {
        RepresentationModel<?> root = new RepresentationModel<>();

        root.add(linkTo(methodOn(RootController.class)
            .root())
            .withSelfRel());

        root.add(linkTo(methodOn(TaskController.class)
            .getAllTasks(0, 10))
            .withRel("tasks"));

        return ResponseEntity.ok(root);
    }
}
```

### Conditional Links
```java
private void addStatusBasedLinks(TaskResponse response, Task task) {
    switch (task.getStatus()) {
        case CREATED:
            response.add(linkTo(methodOn(TaskController.class)
                .startTask(task.getId()))
                .withRel("start"));
            break;
        case IN_PROGRESS:
            response.add(linkTo(methodOn(TaskController.class)
                .completeTask(task.getId()))
                .withRel("complete"));
            break;
        // ... more cases
    }
}
```

### State Transitions
```java
@PatchMapping("/{id}/start")
public ResponseEntity<TaskResponse> startTask(@PathVariable Long id) {
    Task task = taskService.startTask(id);
    TaskResponse response = assembler.toModel(task);
    return ResponseEntity.ok(response);
}
```

## Testing with cURL

### GET Request
```bash
curl http://localhost:8080/api/tasks
```

### POST Request
```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Task Title",
    "description": "Description",
    "priority": "HIGH"
  }'
```

### PATCH Request
```bash
curl -X PATCH http://localhost:8080/api/tasks/1/start
```

### DELETE Request
```bash
curl -X DELETE http://localhost:8080/api/tasks/1
```

### With Pretty Print
```bash
curl http://localhost:8080/api/tasks | json_pp
```

## HAL Response Format

### Single Resource
```json
{
  "id": 1,
  "title": "Task",
  "status": "IN_PROGRESS",
  "_links": {
    "self": { "href": "http://localhost:8080/api/tasks/1" },
    "tasks": { "href": "http://localhost:8080/api/tasks" },
    "complete": { "href": "http://localhost:8080/api/tasks/1/complete" }
  }
}
```

### Collection
```json
{
  "_embedded": {
    "tasks": [
      { "id": 1, "_links": {...} },
      { "id": 2, "_links": {...} }
    ]
  },
  "_links": {
    "self": { "href": "http://localhost:8080/api/tasks" }
  }
}
```

### Paginated Collection
```json
{
  "_embedded": { "tasks": [...] },
  "_links": {
    "first": { "href": "...?page=0&size=10" },
    "self": { "href": "...?page=0&size=10" },
    "next": { "href": "...?page=1&size=10" },
    "last": { "href": "...?page=2&size=10" }
  },
  "page": {
    "size": 10,
    "totalElements": 25,
    "totalPages": 3,
    "number": 0
  }
}
```

## Common Link Relations

| Relation | Description |
|----------|-------------|
| `self` | Link to the resource itself |
| `collection` | Link to the parent collection |
| `next` | Next page in pagination |
| `prev` | Previous page in pagination |
| `first` | First page in pagination |
| `last` | Last page in pagination |
| `create` | Create a new resource |
| `update` | Update the resource |
| `delete` | Delete the resource |

## Troubleshooting

### Links not appearing
- Ensure DTO extends `RepresentationModel`
- Check assembler is being used
- Verify HATEOAS dependency is included

### Wrong media type
- Set `spring.hateoas.use-hal-as-default-json-media-type=true`
- Accept header should be `application/hal+json`

### Pagination links missing
- Inject `PagedResourcesAssembler<Entity>`
- Use `toModel()` on Page results

### Circular references
- Use DTOs, not entities in responses
- Add `@JsonInclude(JsonInclude.Include.NON_NULL)`

## Best Practices

1. **Always use DTOs for responses**, not entities
2. **Add self link to every resource**
3. **Use meaningful link relations**
4. **Implement API root for discoverability**
5. **Add conditional links based on state**
6. **Use RFC 7807 for error responses**
7. **Provide pagination for collections**
8. **Use proper HTTP status codes**
9. **Validate all inputs**
10. **Keep link creation in assemblers**

## Dependencies (pom.xml)

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-hateoas</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

## Import Statements

```java
// HATEOAS
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.hateoas.server.core.Relation;
import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.hateoas.MediaTypes;

// Link Building
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

// Pagination
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedResourcesAssembler;

// HTTP
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

// Validation
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
```
