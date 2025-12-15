# Lab 5a: HATEOAS with Spring Boot - Solution

## Overview
This is the complete solution for the HATEOAS lab. It demonstrates a fully-functional RESTful Task Management API implementing Level 3 (Hypermedia Controls) of the Richardson Maturity Model.

## Features
- Full HATEOAS implementation with Spring Boot 3.2.1
- Hypermedia-driven API responses with HAL format
- Status-based conditional links
- Pagination with navigation links
- API discoverability through root endpoint
- RFC 7807 Problem Details for error responses
- Complete CRUD operations with state transitions

## Project Structure
```
src/main/java/com/example/taskapi/
├── TaskApiApplication.java
├── entity/
│   ├── Task.java
│   ├── TaskStatus.java
│   └── TaskPriority.java
├── repository/
│   └── TaskRepository.java
├── dto/
│   ├── TaskResponse.java
│   ├── CreateTaskRequest.java
│   └── UpdateTaskRequest.java
├── assembler/
│   └── TaskModelAssembler.java
├── service/
│   └── TaskService.java
├── controller/
│   ├── TaskController.java
│   └── RootController.java
├── exception/
│   ├── TaskNotFoundException.java
│   └── GlobalExceptionHandler.java
└── config/
    └── DataInitializer.java
```

## Running the Application

### Build
```bash
mvn clean install
```

### Run
```bash
mvn spring-boot:run
```

The application starts on port 8080 with 10 sample tasks pre-loaded.

## API Endpoints

### Root API Discovery
```bash
GET http://localhost:8080/api
```
Returns links to all available resources.

### Task Operations

#### Get All Tasks (Paginated)
```bash
GET http://localhost:8080/api/tasks?page=0&size=10
```

#### Get Tasks by Status
```bash
GET http://localhost:8080/api/tasks/status/IN_PROGRESS
```

#### Get Tasks by Priority
```bash
GET http://localhost:8080/api/tasks/priority/HIGH
```

#### Get Single Task
```bash
GET http://localhost:8080/api/tasks/1
```

#### Create Task
```bash
POST http://localhost:8080/api/tasks
Content-Type: application/json

{
  "title": "Learn HATEOAS",
  "description": "Complete the HATEOAS lab exercise",
  "priority": "HIGH",
  "dueDate": "2025-12-20T10:00:00"
}
```

#### Update Task
```bash
PUT http://localhost:8080/api/tasks/1
Content-Type: application/json

{
  "title": "Updated Title",
  "description": "Updated description",
  "priority": "CRITICAL"
}
```

#### Delete Task
```bash
DELETE http://localhost:8080/api/tasks/1
```

### State Transition Endpoints

#### Start Task (CREATED -> IN_PROGRESS)
```bash
PATCH http://localhost:8080/api/tasks/1/start
```

#### Complete Task (IN_PROGRESS -> COMPLETED)
```bash
PATCH http://localhost:8080/api/tasks/1/complete
```

#### Cancel Task
```bash
PATCH http://localhost:8080/api/tasks/1/cancel
```

#### Reopen Task (COMPLETED/CANCELLED -> CREATED)
```bash
PATCH http://localhost:8080/api/tasks/1/reopen
```

## HATEOAS Implementation Details

### RepresentationModel
The `TaskResponse` DTO extends `RepresentationModel<TaskResponse>` to support hypermedia links:
```java
@Relation(collectionRelation = "tasks", itemRelation = "task")
public class TaskResponse extends RepresentationModel<TaskResponse> {
    // fields
}
```

### Model Assembler
`TaskModelAssembler` converts entities to representation models with links:
- Self link to individual task
- Collection link to all tasks
- Conditional links based on task status
- Update and delete links

### Status-Based Links
The assembler adds different links depending on task status:

**CREATED tasks** can:
- start (transition to IN_PROGRESS)
- cancel (transition to CANCELLED)

**IN_PROGRESS tasks** can:
- complete (transition to COMPLETED)
- cancel (transition to CANCELLED)

**COMPLETED tasks** can:
- reopen (transition to CREATED)

**CANCELLED tasks** can:
- reopen (transition to CREATED)

### Pagination
The controller uses `PagedResourcesAssembler` to automatically add pagination links:
- first
- last
- next (when more pages available)
- prev (when not on first page)
- self

## Example Response

### Single Task
```json
{
  "id": 2,
  "title": "Implement HATEOAS Support",
  "description": "Add Spring HATEOAS dependency and create model assemblers",
  "status": "IN_PROGRESS",
  "priority": "HIGH",
  "createdAt": "2025-12-14T10:00:00",
  "updatedAt": "2025-12-14T10:00:00",
  "dueDate": "2025-12-16T10:00:00",
  "_links": {
    "self": {
      "href": "http://localhost:8080/api/tasks/2"
    },
    "tasks": {
      "href": "http://localhost:8080/api/tasks"
    },
    "complete": {
      "href": "http://localhost:8080/api/tasks/2/complete"
    },
    "cancel": {
      "href": "http://localhost:8080/api/tasks/2/cancel"
    },
    "update": {
      "href": "http://localhost:8080/api/tasks/2"
    },
    "delete": {
      "href": "http://localhost:8080/api/tasks/2"
    }
  }
}
```

### Paginated Collection
```json
{
  "_embedded": {
    "tasks": [
      { /* task 1 */ },
      { /* task 2 */ }
    ]
  },
  "_links": {
    "first": {
      "href": "http://localhost:8080/api/tasks?page=0&size=10"
    },
    "self": {
      "href": "http://localhost:8080/api/tasks?page=0&size=10"
    },
    "next": {
      "href": "http://localhost:8080/api/tasks?page=1&size=10"
    },
    "last": {
      "href": "http://localhost:8080/api/tasks?page=0&size=10"
    }
  },
  "page": {
    "size": 10,
    "totalElements": 10,
    "totalPages": 1,
    "number": 0
  }
}
```

## Error Handling
The application uses RFC 7807 Problem Details for consistent error responses:

```json
{
  "type": "about:blank",
  "title": "Task Not Found",
  "status": 404,
  "detail": "Task not found with id: 999"
}
```

## Key Learning Points

1. **HATEOAS Principles**: Clients navigate the API through links, not hardcoded URLs
2. **HAL Format**: Standard hypermedia format with `_links` and `_embedded`
3. **RepresentationModel**: Base class for adding links to DTOs
4. **Model Assemblers**: Centralized link creation logic
5. **Conditional Links**: Different actions available based on resource state
6. **API Discoverability**: Root endpoint provides entry point to entire API
7. **Pagination**: Built-in support for navigating large collections
8. **Richardson Maturity Model Level 3**: Full hypermedia controls

## Testing Tips

1. Start at the root endpoint (`/api`) and follow links
2. Notice how available actions change based on task status
3. Use pagination links to navigate through tasks
4. Try invalid state transitions (e.g., complete a CREATED task)
5. Observe how error responses follow Problem Details format

## Technologies Used
- Spring Boot 3.2.1
- Spring HATEOAS
- Spring Data JPA
- Bean Validation
- HSQLDB (in-memory)
- Java 17

## Further Exploration
- Implement additional filters (by due date, search by title)
- Add authentication and authorization
- Create a hypermedia-driven client application
- Implement Spring Data REST for comparison
- Add HAL Browser for interactive exploration

## Resources
- [Spring HATEOAS Documentation](https://docs.spring.io/spring-hateoas/docs/current/reference/html/)
- [HAL Specification](http://stateless.co/hal_specification.html)
- [RFC 7807 Problem Details](https://tools.ietf.org/html/rfc7807)
- [Richardson Maturity Model](https://martinfowler.com/articles/richardsonMaturityModel.html)
