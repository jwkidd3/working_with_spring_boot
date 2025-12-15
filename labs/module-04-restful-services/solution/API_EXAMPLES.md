# API Examples and Testing

This document provides sample API requests for testing the Task Management API.

## Prerequisites

- Application running on `http://localhost:8080`
- cURL installed (or use Postman/Thunder Client/etc.)

## 1. Get All Tasks (Paginated)

### Request
```bash
curl -X GET "http://localhost:8080/api/tasks" -H "Accept: application/json"
```

### With Pagination
```bash
curl -X GET "http://localhost:8080/api/tasks?page=0&size=5&sort=createdAt&direction=desc" \
  -H "Accept: application/json"
```

### Filter by Status
```bash
curl -X GET "http://localhost:8080/api/tasks?status=TODO" -H "Accept: application/json"
```

### Response (200 OK)
```json
{
  "content": [
    {
      "id": 1,
      "title": "Complete project documentation",
      "description": "Write comprehensive documentation for the REST API project",
      "status": "TODO",
      "priority": "HIGH",
      "dueDate": "2025-12-21",
      "createdAt": "2025-12-14T10:30:00",
      "updatedAt": "2025-12-14T10:30:00"
    }
  ],
  "pageNumber": 0,
  "pageSize": 10,
  "totalElements": 10,
  "totalPages": 1,
  "first": true,
  "last": true,
  "empty": false
}
```

## 2. Get Task by ID

### Request
```bash
curl -X GET "http://localhost:8080/api/tasks/1" -H "Accept: application/json"
```

### Response (200 OK)
```json
{
  "id": 1,
  "title": "Complete project documentation",
  "description": "Write comprehensive documentation for the REST API project",
  "status": "TODO",
  "priority": "HIGH",
  "dueDate": "2025-12-21",
  "createdAt": "2025-12-14T10:30:00",
  "updatedAt": "2025-12-14T10:30:00"
}
```

### Response (404 Not Found)
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Task not found with id: 999",
  "timestamp": "2025-12-14T10:30:00",
  "fieldErrors": {}
}
```

## 3. Create New Task

### Request (Minimal)
```bash
curl -X POST "http://localhost:8080/api/tasks" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "title": "New urgent task"
  }'
```

### Request (Complete)
```bash
curl -X POST "http://localhost:8080/api/tasks" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "title": "Implement user authentication",
    "description": "Add JWT-based authentication to the API",
    "priority": "URGENT",
    "dueDate": "2025-12-25"
  }'
```

### Response (201 Created)
```json
{
  "id": 11,
  "title": "Implement user authentication",
  "description": "Add JWT-based authentication to the API",
  "status": "TODO",
  "priority": "URGENT",
  "dueDate": "2025-12-25",
  "createdAt": "2025-12-14T10:35:00",
  "updatedAt": "2025-12-14T10:35:00"
}
```

Headers:
```
Location: /api/tasks/11
```

### Validation Error (400 Bad Request)
```bash
curl -X POST "http://localhost:8080/api/tasks" \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Missing title field"
  }'
```

Response:
```json
{
  "status": 400,
  "error": "Validation Failed",
  "message": "Invalid request data",
  "timestamp": "2025-12-14T10:40:00",
  "fieldErrors": {
    "title": "Title is required"
  }
}
```

## 4. Update Task

### Partial Update (Only Status)
```bash
curl -X PUT "http://localhost:8080/api/tasks/1" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "status": "IN_PROGRESS"
  }'
```

### Update Multiple Fields
```bash
curl -X PUT "http://localhost:8080/api/tasks/1" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "title": "Updated title",
    "status": "COMPLETED",
    "priority": "MEDIUM"
  }'
```

### Response (200 OK)
```json
{
  "id": 1,
  "title": "Updated title",
  "description": "Write comprehensive documentation for the REST API project",
  "status": "COMPLETED",
  "priority": "MEDIUM",
  "dueDate": "2025-12-21",
  "createdAt": "2025-12-14T10:30:00",
  "updatedAt": "2025-12-14T10:45:00"
}
```

## 5. Delete Task

### Request
```bash
curl -X DELETE "http://localhost:8080/api/tasks/1" \
  -H "Accept: application/json"
```

### Response (204 No Content)
No body returned, just HTTP 204 status code.

### Response (404 Not Found)
If task doesn't exist:
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Task not found with id: 1",
  "timestamp": "2025-12-14T10:50:00",
  "fieldErrors": {}
}
```

## 6. Get Count by Status

### Request
```bash
curl -X GET "http://localhost:8080/api/tasks/stats/count-by-status?status=TODO" \
  -H "Accept: application/json"
```

### Response (200 OK)
```json
5
```

### Test All Statuses
```bash
# Count TODO tasks
curl "http://localhost:8080/api/tasks/stats/count-by-status?status=TODO"

# Count IN_PROGRESS tasks
curl "http://localhost:8080/api/tasks/stats/count-by-status?status=IN_PROGRESS"

# Count COMPLETED tasks
curl "http://localhost:8080/api/tasks/stats/count-by-status?status=COMPLETED"

# Count CANCELLED tasks
curl "http://localhost:8080/api/tasks/stats/count-by-status?status=CANCELLED"
```

## 7. Actuator Endpoints

### Health Check
```bash
curl http://localhost:8080/actuator/health
```

Response:
```json
{
  "status": "UP"
}
```

### Metrics
```bash
curl http://localhost:8080/actuator/metrics
```

### Prometheus Metrics
```bash
curl http://localhost:8080/actuator/prometheus
```

## 8. OpenAPI/Swagger UI

Open in browser:
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api-docs

## Complete Test Workflow

Here's a complete workflow to test all functionality:

```bash
# 1. Get initial tasks
curl http://localhost:8080/api/tasks | jq

# 2. Create a new task
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Task",
    "description": "This is a test",
    "priority": "HIGH",
    "dueDate": "2025-12-30"
  }' | jq

# 3. Get the new task (assuming ID is 11)
curl http://localhost:8080/api/tasks/11 | jq

# 4. Update the task
curl -X PUT http://localhost:8080/api/tasks/11 \
  -H "Content-Type: application/json" \
  -d '{
    "status": "IN_PROGRESS",
    "priority": "URGENT"
  }' | jq

# 5. Get tasks by status
curl "http://localhost:8080/api/tasks?status=IN_PROGRESS" | jq

# 6. Count tasks by status
curl "http://localhost:8080/api/tasks/stats/count-by-status?status=IN_PROGRESS" | jq

# 7. Delete the task
curl -X DELETE http://localhost:8080/api/tasks/11 -v

# 8. Verify deletion (should return 404)
curl http://localhost:8080/api/tasks/11 | jq
```

## Testing Validation

### Invalid Priority
```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test",
    "priority": "INVALID_PRIORITY"
  }'
```

### Title Too Long
```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "'$(python3 -c 'print("A" * 300)')'",
    "description": "Test"
  }'
```

### Missing Required Field
```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "description": "No title provided"
  }'
```

## Enums Reference

### TaskStatus
- `TODO`
- `IN_PROGRESS`
- `COMPLETED`
- `CANCELLED`

### TaskPriority
- `LOW`
- `MEDIUM`
- `HIGH`
- `URGENT`

## Notes

- All dates use ISO 8601 format: `YYYY-MM-DD`
- All timestamps include date and time: `YYYY-MM-DDTHH:MM:SS`
- Pagination is zero-indexed (first page is 0)
- Default page size is 10
- Update requests only modify provided fields (null fields are ignored)
- Creating tasks defaults to `status=TODO` and `priority=MEDIUM` if not specified
