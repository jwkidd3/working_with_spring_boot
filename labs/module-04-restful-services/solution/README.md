# Lab 5: Complete REST API - Solution

## Overview

This is a complete solution for Lab 5, implementing a full-featured CRUD REST API for task management using Spring Boot 3.2.1, Spring Data JPA, HSQLDB, and proper validation.

## Project Structure

```
src/main/java/com/example/taskapi/
├── TaskApiApplication.java              # Main Spring Boot application
├── entity/
│   ├── Task.java                        # JPA entity with timestamps
│   ├── TaskStatus.java                  # Enum: TODO, IN_PROGRESS, COMPLETED, CANCELLED
│   └── TaskPriority.java                # Enum: LOW, MEDIUM, HIGH, URGENT
├── repository/
│   └── TaskRepository.java              # JpaRepository with custom queries
├── dto/
│   ├── CreateTaskRequest.java           # Request DTO for creating tasks
│   ├── UpdateTaskRequest.java           # Request DTO for updating tasks
│   ├── PageResponse.java                # Generic pagination wrapper
│   └── ErrorResponse.java               # Error response with field errors
├── exception/
│   ├── TaskNotFoundException.java       # Custom exception
│   └── GlobalExceptionHandler.java      # @RestControllerAdvice for exception handling
├── service/
│   └── TaskService.java                 # Business logic with @Transactional
├── controller/
│   └── TaskController.java              # REST endpoints
└── config/
    └── DataInitializer.java             # CommandLineRunner for sample data
```

## Features

### Entity Layer
- **Task** entity with JPA annotations
- Auto-generated ID (identity strategy)
- Enums for status and priority
- Automatic timestamps using @CreationTimestamp and @UpdateTimestamp
- LocalDate for due dates, LocalDateTime for timestamps

### Repository Layer
- Spring Data JPA repository
- Custom query methods: `findByStatus()`, `countByStatus()`
- Pagination support

### Service Layer
- Transactional CRUD operations
- Proper exception handling
- Read-only transactions for queries
- Null-safe updates (only update provided fields)

### Controller Layer
- RESTful endpoints with proper HTTP methods
- Pagination support with query parameters
- Proper HTTP status codes (200 OK, 201 Created, 204 No Content, 404 Not Found)
- Location header on resource creation
- Request validation with @Valid

### Validation
- @NotBlank on required fields
- @Size constraints on strings
- Field-level validation in DTOs
- Comprehensive error responses with field errors

### Exception Handling
- Global exception handler using @RestControllerAdvice
- Custom TaskNotFoundException
- Validation error handling
- Type mismatch error handling
- Structured error responses with timestamp

### Data Initialization
- CommandLineRunner to seed sample data
- 10 sample tasks with various statuses and priorities
- Only runs if database is empty

## REST API Endpoints

### Get All Tasks (Paginated)
```
GET /api/tasks?page=0&size=10&sort=id&direction=asc&status=TODO
```
Query Parameters:
- `page` (default: 0) - Page number
- `size` (default: 10) - Page size
- `sort` (default: id) - Sort field
- `direction` (default: asc) - Sort direction (asc/desc)
- `status` (optional) - Filter by status

Response: `PageResponse<Task>` with pagination metadata

### Get Task by ID
```
GET /api/tasks/{id}
```
Response: `Task` (200 OK) or `ErrorResponse` (404 Not Found)

### Create Task
```
POST /api/tasks
Content-Type: application/json

{
  "title": "Complete project documentation",
  "description": "Write comprehensive API documentation",
  "priority": "HIGH",
  "dueDate": "2025-12-20"
}
```
Response: `Task` (201 Created) with Location header

### Update Task
```
PUT /api/tasks/{id}
Content-Type: application/json

{
  "status": "IN_PROGRESS",
  "priority": "URGENT"
}
```
Note: All fields are optional - only provided fields are updated
Response: `Task` (200 OK)

### Delete Task
```
DELETE /api/tasks/{id}
```
Response: 204 No Content

### Count by Status
```
GET /api/tasks/stats/count-by-status?status=TODO
```
Response: `Long` (200 OK)

## Configuration

### Database (HSQLDB)
- In-memory database
- Auto-DDL with create-drop
- SQL logging enabled for debugging

### Jackson
- ISO 8601 date serialization
- UTC timezone

### Actuator
- Health endpoint
- Metrics endpoint
- Prometheus support

### OpenAPI/Swagger
- Interactive API documentation at `/swagger-ui.html`
- OpenAPI spec at `/api-docs`

## Running the Application

### Build and Run
```bash
mvn clean package
mvn spring-boot:run
```

### Access Points
- API Base URL: http://localhost:8080/api/tasks
- Swagger UI: http://localhost:8080/swagger-ui.html
- Actuator Health: http://localhost:8080/actuator/health
- Actuator Metrics: http://localhost:8080/actuator/metrics

## Sample Data

The application seeds 10 tasks on startup:
- 5 TODO tasks
- 2 IN_PROGRESS tasks
- 2 COMPLETED tasks
- 1 CANCELLED task

Tasks have various priorities (LOW, MEDIUM, HIGH, URGENT) and due dates.

## Testing with cURL

### Create a new task
```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "New Task",
    "description": "Task description",
    "priority": "HIGH",
    "dueDate": "2025-12-25"
  }'
```

### Get all tasks
```bash
curl http://localhost:8080/api/tasks
```

### Get tasks by status
```bash
curl "http://localhost:8080/api/tasks?status=TODO&page=0&size=5"
```

### Update a task
```bash
curl -X PUT http://localhost:8080/api/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{
    "status": "COMPLETED"
  }'
```

### Delete a task
```bash
curl -X DELETE http://localhost:8080/api/tasks/1
```

## Key Implementation Details

### Validation Annotations
- `@NotBlank` - Required non-empty strings
- `@Size` - String length constraints
- `@Valid` - Trigger validation on request bodies

### HTTP Status Codes
- 200 OK - Successful GET/PUT
- 201 Created - Successful POST
- 204 No Content - Successful DELETE
- 400 Bad Request - Validation errors
- 404 Not Found - Resource not found
- 500 Internal Server Error - Unexpected errors

### Transaction Management
- `@Transactional` on service methods
- `@Transactional(readOnly = true)` for queries
- Proper exception propagation for rollback

### Pagination
- Uses Spring Data's `Pageable` and `Page`
- Custom `PageResponse` DTO for clean API
- Supports sorting and filtering

## Technologies Used
- Spring Boot 3.2.1
- Spring Data JPA
- Spring Validation
- HSQLDB (in-memory)
- Spring HATEOAS
- Spring Boot Actuator
- SpringDoc OpenAPI 3
- Java 17
- Maven
