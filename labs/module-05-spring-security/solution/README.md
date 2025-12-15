# Lab 6: Spring Security with JWT Authentication - Solution

This is the complete solution for Lab 6, implementing a secured Task API with JWT authentication using Spring Security.

## Overview

This solution demonstrates:
- JWT-based authentication using jjwt 0.12.3
- Spring Security configuration with stateless sessions
- Role-based access control (RBAC)
- User registration and login endpoints
- Secured REST API endpoints
- Custom exception handling
- HSQLDB in-memory database

## Project Structure

```
src/main/java/com/example/taskapi/
├── TaskApiApplication.java           # Main Spring Boot application
├── config/
│   ├── DataInitializer.java         # Initialize default users
│   └── SecurityConfig.java          # Spring Security configuration
├── controller/
│   ├── AuthController.java          # Authentication endpoints (register/login)
│   └── TaskController.java          # Secured task endpoints
├── dto/
│   ├── auth/
│   │   ├── AuthResponse.java        # Authentication response DTO
│   │   ├── LoginRequest.java        # Login request DTO
│   │   └── RegisterRequest.java     # Registration request DTO
│   ├── CreateTaskRequest.java       # Create task request DTO
│   └── UpdateTaskRequest.java       # Update task request DTO
├── entity/
│   ├── Role.java                    # Role enum (ROLE_USER, ROLE_ADMIN)
│   ├── Task.java                    # Task entity
│   ├── TaskPriority.java            # Priority enum (LOW, MEDIUM, HIGH)
│   ├── TaskStatus.java              # Status enum (TODO, IN_PROGRESS, COMPLETED)
│   └── User.java                    # User entity implementing UserDetails
├── exception/
│   ├── GlobalExceptionHandler.java  # Global exception handler
│   └── TaskNotFoundException.java   # Custom exception
├── repository/
│   ├── TaskRepository.java          # Task repository
│   └── UserRepository.java          # User repository
├── security/
│   ├── JwtAuthenticationFilter.java # JWT authentication filter
│   └── JwtService.java              # JWT token generation/validation
└── service/
    ├── CustomUserDetailsService.java # UserDetailsService implementation
    └── TaskService.java             # Task business logic
```

## Technologies Used

- Spring Boot 3.2.1
- Java 17
- Spring Security
- Spring Data JPA
- HSQLDB (in-memory database)
- JJWT 0.12.3 for JWT handling
- Jakarta Validation

## Default Users

The application initializes with two default users:

### Regular User
- Username: `user`
- Password: `password`
- Roles: `ROLE_USER`
- Email: `user@example.com`

### Admin User
- Username: `admin`
- Password: `admin123`
- Roles: `ROLE_USER`, `ROLE_ADMIN`
- Email: `admin@example.com`

## API Endpoints

### Authentication Endpoints (Public)

#### Register User
```
POST /api/auth/register
Content-Type: application/json

{
  "username": "newuser",
  "password": "password123",
  "email": "newuser@example.com"
}

Response:
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "newuser",
  "email": "newuser@example.com"
}
```

#### Login
```
POST /api/auth/login
Content-Type: application/json

{
  "username": "user",
  "password": "password"
}

Response:
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "user",
  "email": "user@example.com"
}
```

### Task Endpoints (Secured)

All task endpoints require authentication via JWT token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

#### Get All Tasks (Requires ROLE_USER)
```
GET /api/tasks
Authorization: Bearer <token>
```

#### Get Task by ID (Requires ROLE_USER)
```
GET /api/tasks/{id}
Authorization: Bearer <token>
```

#### Create Task (Requires ROLE_USER)
```
POST /api/tasks
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "Complete assignment",
  "description": "Finish the Spring Security lab",
  "status": "TODO",
  "priority": "HIGH",
  "dueDate": "2025-12-31T23:59:59"
}
```

#### Update Task (Requires ROLE_USER)
```
PUT /api/tasks/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "Updated title",
  "status": "IN_PROGRESS"
}
```

#### Delete Task (Requires ROLE_ADMIN)
```
DELETE /api/tasks/{id}
Authorization: Bearer <token>
```

#### Get Tasks by Status (Requires ROLE_USER)
```
GET /api/tasks/status/{status}
Authorization: Bearer <token>

Example: GET /api/tasks/status/TODO
```

#### Search Tasks by Title (Requires ROLE_USER)
```
GET /api/tasks/search?title=assignment
Authorization: Bearer <token>
```

## Security Features

### JWT Configuration
- Secret key: Base64-encoded 256-bit key (configured in application.properties)
- Token expiration: 24 hours (86400000 milliseconds)
- Algorithm: HMAC SHA-256

### Role-Based Access Control
- `ROLE_USER`: Can view, create, and update tasks
- `ROLE_ADMIN`: Has all USER permissions plus can delete tasks

### Session Management
- Stateless session management (no server-side sessions)
- All authentication state maintained via JWT tokens

### Password Encoding
- BCrypt password encoder for secure password storage

## Running the Application

1. Build the project:
```bash
mvn clean install
```

2. Run the application:
```bash
mvn spring-boot:run
```

The application will start on port 8080 (default).

## Testing the Application

### Using cURL

1. Login to get a token:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"password"}'
```

2. Use the token to access secured endpoints:
```bash
curl -X GET http://localhost:8080/api/tasks \
  -H "Authorization: Bearer <your-token-here>"
```

3. Try to delete a task as regular user (should fail):
```bash
curl -X DELETE http://localhost:8080/api/tasks/1 \
  -H "Authorization: Bearer <user-token>"
```

4. Login as admin and delete a task:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

curl -X DELETE http://localhost:8080/api/tasks/1 \
  -H "Authorization: Bearer <admin-token>"
```

## Key Implementation Details

### User Entity
- Implements `UserDetails` interface for Spring Security integration
- Uses `@ElementCollection` for roles (many-to-many relationship)
- Passwords are stored as BCrypt hashes

### JWT Service
- Generates tokens with username as subject
- Validates tokens by checking signature and expiration
- Uses HMAC SHA-256 for token signing

### JWT Authentication Filter
- Extends `OncePerRequestFilter`
- Extracts JWT from Authorization header
- Validates token and sets authentication in SecurityContext

### Security Configuration
- Disables CSRF (not needed for stateless JWT authentication)
- Permits public access to `/api/auth/**` endpoints
- Requires authentication for all other endpoints
- Configures stateless session management

### Exception Handling
- Global exception handler for consistent error responses
- Handles validation errors, authentication errors, and access denied scenarios
- Returns structured JSON error responses

## Database

The application uses HSQLDB in-memory database:
- Database is created on startup
- Schema is auto-generated from JPA entities
- Default users are initialized via DataInitializer
- Data is lost when application stops

## Configuration

Key configurations in `application.properties`:
```properties
# Database
spring.datasource.url=jdbc:hsqldb:mem:taskdb
spring.jpa.hibernate.ddl-auto=create-drop

# JWT
jwt.secret=dGhpcy1pcy1hLXZlcnktc2VjdXJlLWFuZC1sb25nLXNlY3JldC1rZXktZm9yLWp3dC1zaWduaW5nLXdpdGgtaG1hYy1zaGEyNTY=
jwt.expiration=86400000
```

## Learning Objectives Achieved

1. Implementing JWT-based authentication
2. Configuring Spring Security for stateless applications
3. Role-based access control with method security
4. Custom UserDetailsService implementation
5. Password encoding with BCrypt
6. Creating authentication filters
7. Handling security exceptions
8. Securing REST API endpoints
9. Working with JWT libraries (jjwt)
10. Database integration with Spring Security

## Notes

- The JWT secret in this solution is for demonstration purposes only
- In production, use environment variables for sensitive configuration
- Consider implementing refresh tokens for better security
- Add rate limiting to prevent brute force attacks
- Implement password complexity requirements
- Consider adding account lockout after failed login attempts
