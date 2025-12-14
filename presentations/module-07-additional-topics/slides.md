# Module 7: Additional Topics & Wrap-up

---

## Module Overview

### What You'll Learn
- Spring Boot CLI for rapid development
- Testing strategies for Spring Boot applications
- Packaging and deployment options
- Best practices and patterns
- Q&A and course wrap-up

### Duration: ~2 hours (Day 3 Afternoon)

---

## Section 1: Spring Boot CLI

---

### What is Spring Boot CLI?

- Command-line tool for rapid Spring development
- Run Groovy scripts with Spring Boot features
- Quick prototyping and testing
- Automatic dependency resolution

```bash
# Install on macOS
brew install spring-boot-cli

# Or download from spring.io
```

---

### Quick Start with CLI

Create `hello.groovy`:
```groovy
@RestController
class HelloController {

    @GetMapping("/")
    String hello() {
        "Hello from Spring Boot CLI!"
    }
}
```

Run it:
```bash
spring run hello.groovy
```

That's it! No `pom.xml`, no main class, no build step!

---

### CLI Features

```bash
# Run Groovy scripts
spring run app.groovy

# Create new project
spring init --dependencies=web,data-jpa myproject

# Run tests
spring test test.groovy

# Package as JAR
spring jar my-app.jar app.groovy

# Show dependencies
spring grab --list
```

---

### CLI Project Generation

```bash
# Interactive project creation
spring init

# Specific dependencies
spring init --dependencies=web,data-jpa,security myproject

# With build tool
spring init --build=gradle myproject

# List available dependencies
spring init --list
```

---

### When to Use CLI

**Good for:**
- Quick prototypes
- Testing ideas
- Learning Spring
- Simple scripts

**Not for:**
- Production applications
- Complex projects
- Team development

---

## Section 2: Testing Spring Boot Applications

---

### Testing Pyramid

```
           ╱╲
          ╱  ╲
         ╱ E2E╲        Few, slow, expensive
        ╱──────╲
       ╱Integration╲   Some, medium speed
      ╱────────────╲
     ╱    Unit       ╲  Many, fast, cheap
    ╱────────────────╲
```

---

### Test Dependencies

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

Includes:
- JUnit 5
- Mockito
- AssertJ
- Spring Test
- JSONPath
- Hamcrest

---

### Unit Testing Services

```java
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void shouldCreateTask() {
        // Arrange
        Task task = new Task("Test Task");
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // Act
        Task result = taskService.create("Test Task", "Description");

        // Assert
        assertThat(result.getTitle()).isEqualTo("Test Task");
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void shouldThrowWhenTaskNotFound() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.findById(999L))
            .isInstanceOf(TaskNotFoundException.class)
            .hasMessage("Task not found: 999");
    }
}
```

---

### Integration Testing with @SpringBootTest

```java
@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void setup() {
        taskRepository.deleteAll();
    }

    @Test
    void shouldCreateTask() throws Exception {
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "Integration Test Task",
                        "description": "Testing"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("Integration Test Task"));
    }
}
```

---

### Sliced Tests

Test only specific layers:

```java
// Web layer only
@WebMvcTest(TaskController.class)
class TaskControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;
}

// JPA layer only
@DataJpaTest
class TaskRepositoryTest {
    @Autowired
    private TaskRepository repository;
}

// JSON serialization only
@JsonTest
class TaskJsonTest {
    @Autowired
    private JacksonTester<Task> json;
}
```

---

### Testing with Security

```java
@WebMvcTest(TaskController.class)
@Import(SecurityConfig.class)
class SecuredTaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @Test
    @WithMockUser(roles = "USER")
    void userCanGetTasks() throws Exception {
        mockMvc.perform(get("/api/tasks"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void userCannotDeleteTasks() throws Exception {
        mockMvc.perform(delete("/api/tasks/1"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanDeleteTasks() throws Exception {
        mockMvc.perform(delete("/api/tasks/1"))
            .andExpect(status().isNoContent());
    }
}
```

---

### TestContainers for Real Databases

```java
@SpringBootTest
@Testcontainers
class TaskRepositoryContainerTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private TaskRepository repository;

    @Test
    void shouldSaveTask() {
        Task task = repository.save(new Task("Test"));
        assertThat(task.getId()).isNotNull();
    }
}
```

---

## Section 3: Packaging & Deployment

---

### Building for Production

```bash
# Maven
./mvnw clean package -DskipTests

# Creates executable JAR in target/
java -jar target/myapp-0.0.1-SNAPSHOT.jar
```

```properties
# Production profile
spring.profiles.active=prod
```

---

### Externalized Configuration

**Environment variables:**
```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://prod:5432/mydb
export SPRING_DATASOURCE_USERNAME=produser
java -jar myapp.jar
```

**Command line:**
```bash
java -jar myapp.jar \
  --spring.datasource.url=jdbc:postgresql://prod:5432/mydb \
  --server.port=9090
```

**Config file location:**
```bash
java -jar myapp.jar \
  --spring.config.location=/etc/myapp/application.properties
```

---

### Docker Basics

```dockerfile
# Dockerfile
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

```bash
# Build image
docker build -t myapp:latest .

# Run container
docker run -p 8080:8080 myapp:latest
```

---

### Multi-Stage Docker Build

```dockerfile
# Build stage
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Smaller final image, build tools not included.

---

### Spring Boot Docker Support

```bash
# Build image with Spring Boot Maven plugin
./mvnw spring-boot:build-image -Dspring-boot.build-image.imageName=myapp:latest
```

Uses Cloud Native Buildpacks - no Dockerfile needed!

---

### Docker Compose for Local Development

```yaml
# docker-compose.yml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/mydb
    depends_on:
      - db
      - rabbitmq

  db:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: mydb
      POSTGRES_USER: user
      POSTGRES_PASSWORD: password
    volumes:
      - postgres_data:/var/lib/postgresql/data

  rabbitmq:
    image: rabbitmq:3-management
    ports:
      - "15672:15672"

volumes:
  postgres_data:
```

---

### Kubernetes Basics

```yaml
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: task-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: task-service
  template:
    metadata:
      labels:
        app: task-service
    spec:
      containers:
      - name: task-service
        image: myregistry/task-service:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
```

---

## Section 4: Best Practices

---

### Project Structure

```
src/main/java/com/example/myapp/
├── config/           # Configuration classes
├── controller/       # REST controllers
├── dto/              # Request/Response DTOs
├── entity/           # JPA entities
├── exception/        # Custom exceptions
├── repository/       # Data repositories
├── service/          # Business logic
├── security/         # Security components
└── MyAppApplication.java
```

---

### Configuration Best Practices

```java
// Use @ConfigurationProperties for type-safe config
@ConfigurationProperties(prefix = "app.task")
public class TaskProperties {
    private int maxTitleLength = 100;
    private int defaultPageSize = 20;
    private Duration timeout = Duration.ofSeconds(30);

    // getters and setters
}

@Configuration
@EnableConfigurationProperties(TaskProperties.class)
public class TaskConfig {
}
```

```yaml
# application.yml
app:
  task:
    max-title-length: 150
    default-page-size: 25
    timeout: 45s
```

---

### Exception Handling Patterns

```java
// Base exception
public abstract class BusinessException extends RuntimeException {
    private final String errorCode;

    protected BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}

// Specific exceptions
public class TaskNotFoundException extends BusinessException {
    public TaskNotFoundException(Long id) {
        super("TASK_NOT_FOUND", "Task not found: " + id);
    }
}

// Global handler
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        // Log, build response, return appropriate status
    }
}
```

---

### Logging Best Practices

```java
@Service
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    public Task create(CreateTaskRequest request) {
        log.info("Creating task: {}", request.getTitle());

        try {
            Task task = // ... create task
            log.debug("Task created with ID: {}", task.getId());
            return task;
        } catch (Exception e) {
            log.error("Failed to create task: {}", request.getTitle(), e);
            throw e;
        }
    }
}
```

```properties
# Structured logging
logging.pattern.level=%5p [${spring.application.name:},%X{traceId:-},%X{spanId:-}]
```

---

### Performance Tips

1. **Use pagination** for large datasets
2. **Enable caching** for frequently accessed data
3. **Optimize JPA queries** (avoid N+1)
4. **Use async** for long-running operations
5. **Configure connection pools** properly
6. **Enable GZIP compression**

```properties
# Enable compression
server.compression.enabled=true
server.compression.mime-types=application/json,text/html

# Caching headers
spring.web.resources.cache.cachecontrol.max-age=365d
```

---

### Security Checklist

- [ ] Use HTTPS in production
- [ ] Validate all input
- [ ] Use parameterized queries (JPA does this)
- [ ] Implement proper authentication
- [ ] Use role-based authorization
- [ ] Secure sensitive endpoints
- [ ] Don't expose stack traces
- [ ] Keep dependencies updated
- [ ] Use secrets management
- [ ] Enable CORS properly

---

## Section 5: What We Learned

---

### Course Summary

| Day | Topics |
|-----|--------|
| **1** | Spring Fundamentals, IoC/DI, Web Services Intro |
| **2** | Databases, JPA, Advanced REST, HATEOAS, Actuator |
| **3** | Security, JWT, Service Orchestration, Messaging |

---

### Key Technologies

```
┌─────────────────────────────────────────────────────────┐
│                  Spring Boot Stack                       │
├─────────────────────────────────────────────────────────┤
│  Spring MVC   │  REST Controllers, Exception Handling   │
├───────────────┼─────────────────────────────────────────┤
│  Spring Data  │  JPA, Repositories, Queries             │
├───────────────┼─────────────────────────────────────────┤
│  Security     │  Authentication, JWT, Authorization      │
├───────────────┼─────────────────────────────────────────┤
│  AMQP         │  RabbitMQ, Event-Driven Architecture    │
├───────────────┼─────────────────────────────────────────┤
│  Actuator     │  Health, Metrics, Monitoring            │
└───────────────┴─────────────────────────────────────────┘
```

---

### Next Steps

1. **Practice** - Build your own projects
2. **Explore** - Spring Cloud, Spring Batch
3. **Learn** - Kubernetes, CI/CD pipelines
4. **Contribute** - Open source Spring projects
5. **Stay Updated** - spring.io/blog

---

### Resources

**Documentation:**
- https://spring.io/projects/spring-boot
- https://docs.spring.io/spring-boot/docs/current/reference/html/

**Tutorials:**
- https://spring.io/guides
- https://www.baeldung.com/spring-boot

**Community:**
- Stack Overflow (spring-boot tag)
- GitHub Discussions
- Spring Community Gitter

---

## Q&A Session

### Questions?

Let's discuss:
- Any topics you'd like to revisit?
- Real-world application questions?
- Best practices for your specific use case?

---

## Thank You!

### Contact & Feedback

**Workshop Materials:**
- All presentations and labs available in course repository

**Further Learning:**
- Spring Certification
- Spring Cloud Workshop
- Microservices Architecture

---

## Course Completion

### You've Learned:

✅ Spring Framework Core Concepts
✅ Building REST APIs
✅ Database Integration with JPA
✅ Security with Spring Security & JWT
✅ Microservices Communication
✅ Testing Strategies
✅ Deployment Best Practices

**Congratulations on completing the course!**
