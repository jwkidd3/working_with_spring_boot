# Module 2: Introduction to Web Services

---

## Module Overview

### What You'll Learn
- Understand HTTP fundamentals and request/response patterns
- Compare SOAP and REST web services
- Handle exceptions in web services
- Implement request validation

### Duration: ~2.5 hours (Day 1 Afternoon)

---

## Section 1: HTTP Fundamentals

---

### What is HTTP?

**HyperText Transfer Protocol**

- Application layer protocol for distributed systems
- Foundation of data communication on the web
- Request-response protocol
- Stateless by default

```
┌────────────┐              ┌────────────┐
│   Client   │  ─Request─>  │   Server   │
│  (Browser) │  <─Response─ │   (API)    │
└────────────┘              └────────────┘
```

---

### HTTP Request Structure

```http
POST /api/users HTTP/1.1
Host: api.example.com
Content-Type: application/json
Authorization: Bearer eyJhbGc...
Accept: application/json

{
  "name": "John Doe",
  "email": "john@example.com"
}
```

**Components:**
1. **Request Line:** Method + URI + HTTP Version
2. **Headers:** Metadata about the request
3. **Body:** Data being sent (optional)

---

### HTTP Methods

| Method | Purpose | Idempotent | Safe | Has Body |
|--------|---------|------------|------|----------|
| GET | Retrieve resource | Yes | Yes | No |
| POST | Create resource | No | No | Yes |
| PUT | Replace resource | Yes | No | Yes |
| PATCH | Partial update | No | No | Yes |
| DELETE | Remove resource | Yes | No | Optional |
| HEAD | Get headers only | Yes | Yes | No |
| OPTIONS | Get capabilities | Yes | Yes | No |

---

### HTTP Response Structure

```http
HTTP/1.1 201 Created
Content-Type: application/json
Location: /api/users/123
X-Request-Id: abc-123

{
  "id": 123,
  "name": "John Doe",
  "email": "john@example.com",
  "createdAt": "2024-01-15T10:30:00Z"
}
```

**Components:**
1. **Status Line:** HTTP Version + Status Code + Reason
2. **Headers:** Metadata about the response
3. **Body:** Response data (optional)

---

### HTTP Status Codes

```
┌─────────────────────────────────────────────────────┐
│                  Status Codes                        │
├─────────┬───────────────────────────────────────────┤
│   1xx   │  Informational (100 Continue)             │
├─────────┼───────────────────────────────────────────┤
│   2xx   │  Success (200 OK, 201 Created, 204 No...) │
├─────────┼───────────────────────────────────────────┤
│   3xx   │  Redirection (301 Moved, 304 Not Modified)│
├─────────┼───────────────────────────────────────────┤
│   4xx   │  Client Error (400 Bad, 401 Unauth, 404..)│
├─────────┼───────────────────────────────────────────┤
│   5xx   │  Server Error (500 Internal, 503 Service.)│
└─────────┴───────────────────────────────────────────┘
```

---

### Common Status Codes in APIs

| Code | Name | When to Use |
|------|------|-------------|
| 200 | OK | Successful GET, PUT, PATCH |
| 201 | Created | Successful POST |
| 204 | No Content | Successful DELETE |
| 400 | Bad Request | Invalid request data |
| 401 | Unauthorized | Missing/invalid credentials |
| 403 | Forbidden | Valid credentials, no permission |
| 404 | Not Found | Resource doesn't exist |
| 409 | Conflict | Resource state conflict |
| 422 | Unprocessable Entity | Validation failure |
| 500 | Internal Server Error | Server-side error |

---

### HTTP Headers

**Request Headers:**
```
Content-Type: application/json     # Body format
Accept: application/json           # Preferred response format
Authorization: Bearer token123     # Authentication
User-Agent: MyApp/1.0              # Client info
X-Request-Id: uuid                 # Request tracking
```

**Response Headers:**
```
Content-Type: application/json     # Body format
Location: /users/123               # New resource URL
Cache-Control: max-age=3600        # Caching directives
X-Rate-Limit-Remaining: 99         # Rate limiting info
```

---

## Section 2: SOAP Web Services

---

### What is SOAP?

**Simple Object Access Protocol**

- XML-based messaging protocol
- Uses HTTP, SMTP, or other transports
- Defined by WSDL (Web Services Description Language)
- Enterprise standard for complex integrations

---

### SOAP Message Structure

```xml
<?xml version="1.0"?>
<soap:Envelope
    xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">

    <soap:Header>
        <!-- Optional metadata -->
        <auth:Token>xyz123</auth:Token>
    </soap:Header>

    <soap:Body>
        <!-- Actual message content -->
        <m:GetUser xmlns:m="http://example.com/user">
            <m:UserId>123</m:UserId>
        </m:GetUser>
    </soap:Body>

</soap:Envelope>
```

---

### SOAP Response

```xml
<?xml version="1.0"?>
<soap:Envelope
    xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">

    <soap:Body>
        <m:GetUserResponse xmlns:m="http://example.com/user">
            <m:User>
                <m:Id>123</m:Id>
                <m:Name>John Doe</m:Name>
                <m:Email>john@example.com</m:Email>
            </m:User>
        </m:GetUserResponse>
    </soap:Body>

</soap:Envelope>
```

---

### SOAP Characteristics

**Advantages:**
- Strong typing via WSDL
- Built-in error handling (SOAP Faults)
- Security standards (WS-Security)
- Transaction support (WS-AtomicTransaction)
- Formal contract between client and server

**Disadvantages:**
- Verbose XML payloads
- More complex to implement
- Slower than REST
- Less suitable for mobile/web apps

---

### When to Use SOAP

- **Enterprise integrations** with existing SOAP services
- **Banking/Financial** systems requiring transactions
- **ACID compliance** requirements
- **Formal contracts** needed between systems
- **Asynchronous processing** with reliable messaging

---

## Section 3: REST Web Services

---

### What is REST?

**REpresentational State Transfer**

- Architectural style (not a protocol)
- Defined by Roy Fielding in 2000
- Uses HTTP methods semantically
- Resource-oriented design

---

### REST Principles

```
┌────────────────────────────────────────────────────────┐
│                   REST Constraints                      │
├────────────────────────────────────────────────────────┤
│  1. Client-Server      │  Separation of concerns       │
│  2. Stateless          │  No client state on server    │
│  3. Cacheable          │  Responses declare cacheability│
│  4. Uniform Interface  │  Standardized interactions    │
│  5. Layered System     │  Intermediaries allowed       │
│  6. Code on Demand     │  Optional executable code     │
└────────────────────────────────────────────────────────┘
```

---

### Resource-Oriented Design

Resources are identified by URIs:

```
/users                    # Collection of users
/users/123                # Specific user
/users/123/orders         # User's orders (sub-resource)
/users/123/orders/456     # Specific order
```

**Anti-patterns to avoid:**
```
/getUser?id=123           # Verb in URL
/users/123/delete         # Action in URL
/api/v1/getAllUsers       # Redundant naming
```

---

### RESTful URL Design

| HTTP Method | URL | Action |
|-------------|-----|--------|
| GET | /products | List all products |
| GET | /products/42 | Get product 42 |
| POST | /products | Create new product |
| PUT | /products/42 | Replace product 42 |
| PATCH | /products/42 | Update product 42 |
| DELETE | /products/42 | Delete product 42 |
| GET | /products/42/reviews | Get reviews for product 42 |

---

### REST Response Example

**Request:**
```http
GET /api/users/123 HTTP/1.1
Accept: application/json
```

**Response:**
```json
{
  "id": 123,
  "name": "John Doe",
  "email": "john@example.com",
  "createdAt": "2024-01-15T10:30:00Z",
  "_links": {
    "self": { "href": "/api/users/123" },
    "orders": { "href": "/api/users/123/orders" }
  }
}
```

---

### REST vs SOAP

| Aspect | REST | SOAP |
|--------|------|------|
| Protocol | HTTP only | HTTP, SMTP, etc. |
| Data Format | JSON, XML, etc. | XML only |
| Contract | Optional (OpenAPI) | Required (WSDL) |
| Complexity | Simple | Complex |
| Performance | Fast | Slower |
| Caching | Built-in (HTTP) | Manual |
| Security | SSL/OAuth/JWT | WS-Security |
| Best For | Web/Mobile APIs | Enterprise SOA |

---

## Section 4: REST in Spring Boot

---

### Spring Boot REST Stack

```
┌─────────────────────────────────────────────────────┐
│              Spring Boot Auto-Config                 │
├─────────────────────────────────────────────────────┤
│                   Spring MVC                         │
│  ┌───────────────────────────────────────────────┐  │
│  │  @RestController  @RequestMapping  @GetMapping │  │
│  │  @PostMapping     @PutMapping     @DeleteMapping│  │
│  └───────────────────────────────────────────────┘  │
├─────────────────────────────────────────────────────┤
│  Jackson (JSON)  │  Validation  │  Exception Handler│
├─────────────────────────────────────────────────────┤
│              Embedded Tomcat/Jetty/Undertow         │
└─────────────────────────────────────────────────────┘
```

---

### Basic REST Controller

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.findById(id);
    }
}
```

---

### Request Mappings

```java
// Path variables
@GetMapping("/{id}")
public User getById(@PathVariable Long id) { ... }

// Multiple path variables
@GetMapping("/{userId}/orders/{orderId}")
public Order getOrder(@PathVariable Long userId,
                      @PathVariable Long orderId) { ... }

// Query parameters
@GetMapping
public List<User> search(
    @RequestParam(required = false) String name,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size) { ... }

// Request headers
@GetMapping
public User getCurrent(@RequestHeader("Authorization") String auth) { ... }
```

---

### Request Body

```java
@PostMapping
public User createUser(@RequestBody CreateUserRequest request) {
    return userService.create(request);
}

// The request body DTO
public class CreateUserRequest {
    private String name;
    private String email;
    private String password;

    // getters and setters
}
```

**JSON automatically converted by Jackson:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "secret123"
}
```

---

### Response Handling

```java
// Return entity directly (200 OK)
@GetMapping("/{id}")
public User getUser(@PathVariable Long id) {
    return userService.findById(id);
}

// Return with specific status
@PostMapping
@ResponseStatus(HttpStatus.CREATED)
public User createUser(@RequestBody CreateUserRequest request) {
    return userService.create(request);
}

// Full control with ResponseEntity
@GetMapping("/{id}")
public ResponseEntity<User> getUser(@PathVariable Long id) {
    User user = userService.findById(id);
    if (user == null) {
        return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(user);
}
```

---

### ResponseEntity Examples

```java
// 201 Created with Location header
@PostMapping
public ResponseEntity<User> create(@RequestBody CreateUserRequest req) {
    User user = userService.create(req);
    URI location = URI.create("/api/users/" + user.getId());
    return ResponseEntity.created(location).body(user);
}

// 204 No Content
@DeleteMapping("/{id}")
public ResponseEntity<Void> delete(@PathVariable Long id) {
    userService.delete(id);
    return ResponseEntity.noContent().build();
}

// Custom headers
@GetMapping
public ResponseEntity<List<User>> getAll() {
    List<User> users = userService.findAll();
    return ResponseEntity.ok()
        .header("X-Total-Count", String.valueOf(users.size()))
        .body(users);
}
```

---

## Section 5: Exception Handling

---

### Why Proper Exception Handling?

- Consistent error responses for API consumers
- Hide internal implementation details
- Provide meaningful error messages
- Log errors for debugging
- Return appropriate HTTP status codes

---

### Custom Exception

```java
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;

    public ResourceNotFoundException(String resourceName,
                                     String fieldName,
                                     Object fieldValue) {
        super(String.format("%s not found with %s: '%s'",
                resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    // Getters
}
```

---

### Error Response DTO

```java
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private List<FieldError> fieldErrors;

    public static class FieldError {
        private String field;
        private String message;
        private Object rejectedValue;

        // constructor, getters
    }

    // Builder pattern or constructor
}
```

---

### @ExceptionHandler

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    // Handle exceptions in this controller only
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(ResourceNotFoundException ex) {
        return ErrorResponse.builder()
            .status(404)
            .error("Not Found")
            .message(ex.getMessage())
            .build();
    }
}
```

---

### Global Exception Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(ResourceNotFoundException ex,
                                        HttpServletRequest request) {
        return ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(404)
            .error("Not Found")
            .message(ex.getMessage())
            .path(request.getRequestURI())
            .build();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneric(Exception ex,
                                       HttpServletRequest request) {
        return ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(500)
            .error("Internal Server Error")
            .message("An unexpected error occurred")
            .path(request.getRequestURI())
            .build();
    }
}
```

---

### Exception Response Example

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "User not found with id: '999'",
  "path": "/api/users/999"
}
```

---

## Section 6: Request Validation

---

### Why Validate?

- Prevent invalid data from entering the system
- Provide clear feedback to API consumers
- Reduce bugs from unexpected data
- Security (prevent injection attacks)

---

### Jakarta Bean Validation

Add dependency:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

Common annotations:
```
@NotNull      - Must not be null
@NotEmpty     - Must not be null or empty (strings/collections)
@NotBlank     - Must not be null and must have non-whitespace
@Size         - Size constraints for strings/collections
@Min / @Max   - Numeric bounds
@Email        - Valid email format
@Pattern      - Regex match
@Past/@Future - Date constraints
```

---

### DTO with Validation

```java
public class CreateUserRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be 2-100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
        message = "Password must contain uppercase, lowercase, and digit"
    )
    private String password;

    @Min(value = 0, message = "Age must be positive")
    @Max(value = 150, message = "Age must be realistic")
    private Integer age;

    // getters and setters
}
```

---

### Enable Validation in Controller

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @PostMapping
    public ResponseEntity<User> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        User user = userService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
}
```

The `@Valid` annotation triggers validation.

---

### Handle Validation Errors

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> new ErrorResponse.FieldError(
                error.getField(),
                error.getDefaultMessage(),
                error.getRejectedValue()
            ))
            .toList();

        return ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(400)
            .error("Validation Failed")
            .message("Request validation failed")
            .path(request.getRequestURI())
            .fieldErrors(fieldErrors)
            .build();
    }
}
```

---

### Validation Error Response

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Request validation failed",
  "path": "/api/users",
  "fieldErrors": [
    {
      "field": "email",
      "message": "Invalid email format",
      "rejectedValue": "invalid-email"
    },
    {
      "field": "password",
      "message": "Password must be at least 8 characters",
      "rejectedValue": "short"
    }
  ]
}
```

---

### Custom Validator

```java
// Custom annotation
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueEmailValidator.class)
public @interface UniqueEmail {
    String message() default "Email already exists";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

// Validator implementation
@Component
public class UniqueEmailValidator
        implements ConstraintValidator<UniqueEmail, String> {

    private final UserRepository userRepository;

    public UniqueEmailValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext ctx) {
        return email == null || !userRepository.existsByEmail(email);
    }
}
```

---

### Using Custom Validator

```java
public class CreateUserRequest {

    @NotBlank
    private String name;

    @NotBlank
    @Email
    @UniqueEmail  // Custom validator
    private String email;

    @NotBlank
    @Size(min = 8)
    private String password;

    // getters and setters
}
```

---

## Module 2 Summary

### Key Takeaways

1. **HTTP fundamentals** are the foundation of web services
2. **SOAP** is XML-based with formal contracts (WSDL)
3. **REST** is resource-oriented using HTTP methods semantically
4. **Spring Boot** simplifies REST API development
5. **@RestControllerAdvice** provides global exception handling
6. **Bean Validation** ensures request data integrity

---

## Lab Exercises

### Lab 2: Introduction to Web Services - Building a REST API
`labs/module-02-web-services-intro/`

You will build a REST API that:
- Implements CRUD operations for a resource
- Handles exceptions globally
- Validates request data
- Returns proper HTTP status codes

### Lab 2b: Spring Boot Configuration & Profiles
`labs/module-02b-configuration/`

You will learn to:
- Use @ConfigurationProperties for type-safe configuration
- Work with multiple profiles (dev, prod)
- Externalize configuration

---

## Questions?

### Next Module: Using Spring Boot with Databases
