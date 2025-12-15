# Module 5a: HATEOAS with Spring Boot

## Overview
This lab teaches students how to implement HATEOAS (Hypermedia as the Engine of Application State) in Spring Boot REST APIs, achieving Richardson Maturity Model Level 3. Students will build a Task Management API that provides hypermedia-driven responses with status-based conditional links.

## Lab Structure

```
module-05a-hateoas/
├── LAB_INSTRUCTIONS.md          # Complete step-by-step lab guide
├── starter/                      # Starting point for students
│   ├── pom.xml                  # Dependencies: Web, JPA, Validation, HSQLDB
│   ├── README.md                # Starter-specific instructions
│   └── src/main/
│       ├── java/com/example/taskapi/
│       │   ├── TaskApiApplication.java
│       │   ├── entity/
│       │   │   ├── Task.java           # JPA entity
│       │   │   ├── TaskStatus.java     # Enum: CREATED, IN_PROGRESS, COMPLETED, CANCELLED
│       │   │   └── TaskPriority.java   # Enum: LOW, MEDIUM, HIGH, CRITICAL
│       │   └── repository/
│       │       └── TaskRepository.java # JPA repository with query methods
│       └── resources/
│           └── application.properties  # HSQLDB configuration
└── solution/                     # Complete implementation
    ├── pom.xml                  # Includes spring-boot-starter-hateoas
    ├── README.md                # Solution documentation
    └── src/main/
        ├── java/com/example/taskapi/
        │   ├── TaskApiApplication.java
        │   ├── entity/          # Same as starter
        │   ├── repository/      # Same as starter
        │   ├── dto/
        │   │   ├── TaskResponse.java         # Extends RepresentationModel
        │   │   ├── CreateTaskRequest.java    # Create DTO
        │   │   └── UpdateTaskRequest.java    # Update DTO
        │   ├── assembler/
        │   │   └── TaskModelAssembler.java   # Link generation logic
        │   ├── service/
        │   │   └── TaskService.java          # Business logic & state transitions
        │   ├── controller/
        │   │   ├── TaskController.java       # CRUD + state transition endpoints
        │   │   └── RootController.java       # API discovery endpoint
        │   ├── exception/
        │   │   ├── TaskNotFoundException.java
        │   │   └── GlobalExceptionHandler.java # RFC 7807 Problem Details
        │   └── config/
        │       └── DataInitializer.java      # Sample data
        └── resources/
            └── application.properties         # HATEOAS configuration
```

## Learning Objectives
After completing this lab, students will be able to:
1. Understand HATEOAS principles and Richardson Maturity Model Level 3
2. Implement hypermedia-driven REST APIs using Spring HATEOAS
3. Create RepresentationModel-based DTOs with @Relation annotation
4. Build Model Assemblers using RepresentationModelAssemblerSupport
5. Generate type-safe links using WebMvcLinkBuilder
6. Add conditional links based on resource state
7. Implement pagination with PagedModel and navigation links
8. Create API root endpoints for discoverability
9. Use RFC 7807 Problem Details for error responses
10. Work with HAL (Hypertext Application Language) format

## Key Concepts Covered

### 1. HATEOAS Fundamentals
- **What**: Hypermedia as the Engine of Application State
- **Why**: Self-documenting, evolvable, client-decoupled APIs
- **How**: Include hypermedia links in responses

### 2. Richardson Maturity Model
- **Level 0**: Single endpoint, single method
- **Level 1**: Multiple resources
- **Level 2**: HTTP verbs and status codes
- **Level 3**: Hypermedia controls (HATEOAS)

### 3. Spring HATEOAS Components
- **RepresentationModel**: Base class for resources with links
- **Link**: Represents a hypermedia link (rel + href)
- **WebMvcLinkBuilder**: Type-safe link creation
- **RepresentationModelAssembler**: Converts entities to models
- **PagedModel**: Pagination with navigation links

### 4. HAL Format
```json
{
  "id": 1,
  "title": "Task",
  "_links": {
    "self": { "href": "http://localhost:8080/api/tasks/1" },
    "tasks": { "href": "http://localhost:8080/api/tasks" }
  }
}
```

### 5. Status-Based Conditional Links
The API provides different links based on task state:
- **CREATED** → can `start` or `cancel`
- **IN_PROGRESS** → can `complete` or `cancel`
- **COMPLETED** → can `reopen`
- **CANCELLED** → can `reopen`

## Technology Stack
- **Spring Boot**: 3.2.1
- **Java**: 17
- **Spring HATEOAS**: Included in Spring Boot 3.2.1
- **Spring Data JPA**: Data access
- **Bean Validation**: Request validation
- **HSQLDB**: In-memory database
- **Maven**: Build tool

## Sample API Responses

### API Root
```bash
GET http://localhost:8080/api
```
```json
{
  "_links": {
    "self": { "href": "http://localhost:8080/api" },
    "tasks": { "href": "http://localhost:8080/api/tasks" },
    "tasks-created": { "href": "http://localhost:8080/api/tasks/status/CREATED" },
    "tasks-high-priority": { "href": "http://localhost:8080/api/tasks/priority/HIGH" }
  }
}
```

### Single Task (IN_PROGRESS)
```bash
GET http://localhost:8080/api/tasks/2
```
```json
{
  "id": 2,
  "title": "Implement HATEOAS Support",
  "status": "IN_PROGRESS",
  "priority": "HIGH",
  "_links": {
    "self": { "href": "http://localhost:8080/api/tasks/2" },
    "tasks": { "href": "http://localhost:8080/api/tasks" },
    "complete": { "href": "http://localhost:8080/api/tasks/2/complete" },
    "cancel": { "href": "http://localhost:8080/api/tasks/2/cancel" },
    "update": { "href": "http://localhost:8080/api/tasks/2" },
    "delete": { "href": "http://localhost:8080/api/tasks/2" }
  }
}
```

### Paginated Collection
```bash
GET http://localhost:8080/api/tasks?page=0&size=5
```
```json
{
  "_embedded": {
    "tasks": [ /* array of tasks */ ]
  },
  "_links": {
    "first": { "href": "http://localhost:8080/api/tasks?page=0&size=5" },
    "self": { "href": "http://localhost:8080/api/tasks?page=0&size=5" },
    "next": { "href": "http://localhost:8080/api/tasks?page=1&size=5" },
    "last": { "href": "http://localhost:8080/api/tasks?page=1&size=5" }
  },
  "page": {
    "size": 5,
    "totalElements": 10,
    "totalPages": 2,
    "number": 0
  }
}
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api` | API root with discovery links |
| GET | `/api/tasks` | Get all tasks (paginated) |
| GET | `/api/tasks/{id}` | Get task by ID |
| GET | `/api/tasks/status/{status}` | Get tasks by status |
| GET | `/api/tasks/priority/{priority}` | Get tasks by priority |
| POST | `/api/tasks` | Create new task |
| PUT | `/api/tasks/{id}` | Update task |
| DELETE | `/api/tasks/{id}` | Delete task |
| PATCH | `/api/tasks/{id}/start` | Start task (CREATED → IN_PROGRESS) |
| PATCH | `/api/tasks/{id}/complete` | Complete task (IN_PROGRESS → COMPLETED) |
| PATCH | `/api/tasks/{id}/cancel` | Cancel task |
| PATCH | `/api/tasks/{id}/reopen` | Reopen task (COMPLETED/CANCELLED → CREATED) |

## Lab Duration
Approximately 90-120 minutes

## Prerequisites
- Java 17+ installed
- Maven 3.6+ installed
- IDE (IntelliJ IDEA, Eclipse, or VS Code)
- Basic understanding of REST principles
- Familiarity with Spring Boot and JPA
- Understanding of HTTP methods and status codes

## Getting Started

### For Students (Starter)
1. Navigate to `starter/` directory
2. Read `starter/README.md` for specific instructions
3. Follow `LAB_INSTRUCTIONS.md` step-by-step
4. Implement HATEOAS features incrementally

### For Instructors (Solution)
1. Navigate to `solution/` directory
2. Read `solution/README.md` for implementation details
3. Run with `mvn spring-boot:run`
4. Access API at `http://localhost:8080/api`
5. Use for demonstrations and reference

## Testing the Solution

### Build and Run
```bash
cd solution
mvn clean install
mvn spring-boot:run
```

### Test API Discovery
```bash
curl http://localhost:8080/api
```

### Test Task Operations
```bash
# Get all tasks
curl http://localhost:8080/api/tasks

# Get specific task
curl http://localhost:8080/api/tasks/1

# Create task
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"New Task","description":"Test","priority":"HIGH"}'

# Start task
curl -X PATCH http://localhost:8080/api/tasks/3/start
```

## Common Challenges Students Face

1. **Understanding RepresentationModel**
   - Solution: Explain it's just a base class that holds links
   - Show examples of adding links

2. **Link Creation Syntax**
   - `linkTo(methodOn(Controller.class).method(params)).withRel("relation")`
   - Practice with simple examples first

3. **Conditional Links Logic**
   - Draw state diagram showing transitions
   - Implement one status at a time

4. **Pagination Complexity**
   - Use provided PagedResourcesAssembler
   - Let Spring handle the complexity

5. **HAL Format Confusion**
   - Show examples of JSON responses
   - Explain `_links` and `_embedded`

## Extension Ideas

For advanced students or additional challenges:
1. Add HAL Browser for interactive exploration
2. Implement search functionality with result links
3. Add affordances (documenting available operations)
4. Create filtering by due date range
5. Implement custom link relations
6. Add ALPS (Application-Level Profile Semantics)
7. Build a hypermedia-driven client

## Assessment Criteria

Students should demonstrate:
- [ ] Correct use of RepresentationModel
- [ ] Proper link generation with WebMvcLinkBuilder
- [ ] Status-based conditional links
- [ ] Pagination with navigation links
- [ ] API root endpoint implementation
- [ ] Error handling with Problem Details
- [ ] Proper HTTP status codes
- [ ] HAL-compliant responses

## Additional Resources
- [Spring HATEOAS Reference Documentation](https://docs.spring.io/spring-hateoas/docs/current/reference/html/)
- [HAL Specification](http://stateless.co/hal_specification.html)
- [RFC 7807 Problem Details for HTTP APIs](https://tools.ietf.org/html/rfc7807)
- [Richardson Maturity Model](https://martinfowler.com/articles/richardsonMaturityModel.html)
- [REST API Tutorial - HATEOAS](https://restfulapi.net/hateoas/)

## Support
For questions or issues:
1. Check `LAB_INSTRUCTIONS.md` for detailed guidance
2. Review `solution/README.md` for implementation details
3. Examine solution code for working examples
4. Consult Spring HATEOAS documentation

## License
This lab is part of the Spring Boot training curriculum.
