# Lab 5a: HATEOAS with Spring Boot - Starter

## Overview
This is the starter project for learning HATEOAS (Hypermedia as the Engine of Application State) with Spring Boot. You will build a RESTful Task Management API that implements hypermedia-driven responses.

## What's Included
This starter project includes:
- Basic Task entity with JPA annotations
- TaskStatus and TaskPriority enums
- TaskRepository with query methods
- Application configuration for HSQLDB
- Maven dependencies for Web, JPA, Validation, and HSQLDB

## What You'll Build
In this lab, you will:
1. Add the Spring HATEOAS dependency
2. Create DTO classes that extend RepresentationModel
3. Build a TaskModelAssembler to add hypermedia links
4. Implement a service layer with CRUD operations
5. Create controllers that return HATEOAS-compliant responses
6. Add status-based conditional links
7. Implement pagination with navigation links
8. Create an API root endpoint for discoverability

## Prerequisites
- Java 17 or higher
- Maven 3.6 or higher
- Basic understanding of REST principles
- Familiarity with Spring Boot and JPA

## Getting Started

### 1. Build the Project
```bash
mvn clean install
```

### 2. Run the Application
```bash
mvn spring-boot:run
```

The application will start on port 8080.

## Lab Tasks

### Task 1: Add HATEOAS Dependency
Add the Spring HATEOAS starter to your `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-hateoas</artifactId>
</dependency>
```

### Task 2: Create DTOs
Create the following DTOs in `dto` package:
- **TaskResponse** - Extends RepresentationModel, includes @Relation annotation
- **CreateTaskRequest** - For creating new tasks
- **UpdateTaskRequest** - For updating existing tasks

### Task 3: Build Model Assembler
Create `TaskModelAssembler` that:
- Extends RepresentationModelAssemblerSupport<Task, TaskResponse>
- Converts Task entities to TaskResponse DTOs
- Adds self and collection links
- Adds status-based conditional links (start, complete, cancel, reopen)

### Task 4: Implement Service Layer
Create `TaskService` with methods for:
- CRUD operations (create, read, update, delete)
- Status transitions (start, complete, cancel, reopen)
- Querying by status and priority

### Task 5: Create Controllers
Build two controllers:
- **TaskController** - Handles task operations, returns PagedModel
- **RootController** - API root with discovery links

### Task 6: Add Exception Handling
Create:
- **TaskNotFoundException** - Custom exception
- **GlobalExceptionHandler** - @RestControllerAdvice with RFC 7807 Problem Details

### Task 7: Data Initialization
Create `DataInitializer` to populate the database with sample tasks on startup.

## Key Concepts

### HATEOAS Links
Your API should include links like:
```json
{
  "id": 1,
  "title": "Implement HATEOAS",
  "status": "IN_PROGRESS",
  "_links": {
    "self": {
      "href": "http://localhost:8080/api/tasks/1"
    },
    "tasks": {
      "href": "http://localhost:8080/api/tasks"
    },
    "complete": {
      "href": "http://localhost:8080/api/tasks/1/complete"
    },
    "cancel": {
      "href": "http://localhost:8080/api/tasks/1/cancel"
    }
  }
}
```

### Status-Based Links
- **CREATED** - Can start or cancel
- **IN_PROGRESS** - Can complete or cancel
- **COMPLETED** - Can reopen
- **CANCELLED** - Can reopen

### Pagination Links
PagedModel should include:
- first
- last
- next (if available)
- prev (if available)
- self

## Testing Your Implementation

### Get API Root
```bash
curl http://localhost:8080/api
```

### Get All Tasks (Paginated)
```bash
curl http://localhost:8080/api/tasks?page=0&size=5
```

### Get Single Task
```bash
curl http://localhost:8080/api/tasks/1
```

### Create Task
```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Learn HATEOAS",
    "description": "Complete the HATEOAS lab",
    "priority": "HIGH"
  }'
```

### Start Task
```bash
curl -X PATCH http://localhost:8080/api/tasks/1/start
```

## Expected Response Format
All responses should use HAL (Hypertext Application Language) format with `_links` for hypermedia controls.

## Need Help?
Refer to the solution folder for a complete working implementation.

## Resources
- [Spring HATEOAS Reference](https://docs.spring.io/spring-hateoas/docs/current/reference/html/)
- [Richardson Maturity Model](https://martinfowler.com/articles/richardsonMaturityModel.html)
- [RFC 7807 Problem Details](https://tools.ietf.org/html/rfc7807)
