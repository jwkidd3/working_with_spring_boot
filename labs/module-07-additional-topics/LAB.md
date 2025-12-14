# Lab 7: Testing & Deployment

## Objectives

By the end of this lab, you will be able to:
- Write unit tests for Spring Boot services
- Write integration tests with MockMvc
- Test secured endpoints
- Package your application as an executable JAR
- Configure production-ready settings

## Prerequisites

- Completed Labs 1-6
- Understanding of testing concepts

## Duration

45-60 minutes

---

## Part 1: Unit Testing

### Step 1.1: Add Test Dependencies

Ensure your `pom.xml` has the test starter (usually included by default):

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

### Step 1.2: Create Service Unit Test

Create `src/test/java/com/example/taskapi/service/TaskServiceTest.java`:

```java
package com.example.taskapi.service;

import com.example.taskapi.dto.CreateTaskRequest;
import com.example.taskapi.entity.Task;
import com.example.taskapi.entity.TaskPriority;
import com.example.taskapi.exception.TaskNotFoundException;
import com.example.taskapi.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private Task sampleTask;

    @BeforeEach
    void setUp() {
        sampleTask = new Task();
        sampleTask.setId(1L);
        sampleTask.setTitle("Test Task");
        sampleTask.setDescription("Test Description");
        sampleTask.setPriority(TaskPriority.HIGH);
    }

    @Test
    @DisplayName("Should find task by ID when task exists")
    void findById_WhenTaskExists_ReturnsTask() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));

        // Act
        Task result = taskService.findById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test Task");
        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when task not found")
    void findById_WhenTaskNotExists_ThrowsException() {
        // Arrange
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> taskService.findById(999L))
            .isInstanceOf(TaskNotFoundException.class)
            .hasMessageContaining("999");
    }

    @Test
    @DisplayName("Should create task successfully")
    void create_WithValidRequest_ReturnsCreatedTask() {
        // Arrange
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("New Task");
        request.setDescription("New Description");
        request.setPriority(TaskPriority.MEDIUM);

        Task savedTask = new Task();
        savedTask.setId(2L);
        savedTask.setTitle(request.getTitle());
        savedTask.setDescription(request.getDescription());
        savedTask.setPriority(request.getPriority());

        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        // Act
        Task result = taskService.create(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getTitle()).isEqualTo("New Task");
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    @DisplayName("Should delete task when it exists")
    void delete_WhenTaskExists_DeletesSuccessfully() {
        // Arrange
        when(taskRepository.existsById(1L)).thenReturn(true);
        doNothing().when(taskRepository).deleteById(1L);

        // Act
        taskService.delete(1L);

        // Assert
        verify(taskRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent task")
    void delete_WhenTaskNotExists_ThrowsException() {
        // Arrange
        when(taskRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> taskService.delete(999L))
            .isInstanceOf(TaskNotFoundException.class);

        verify(taskRepository, never()).deleteById(any());
    }
}
```

### Step 1.3: Run Unit Tests

```bash
./mvnw test -Dtest=TaskServiceTest
```

---

## Part 2: Integration Testing

### Step 2.1: Create Controller Integration Test

Create `src/test/java/com/example/taskapi/controller/TaskControllerIntegrationTest.java`:

```java
package com.example.taskapi.controller;

import com.example.taskapi.entity.Task;
import com.example.taskapi.entity.TaskPriority;
import com.example.taskapi.repository.TaskRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /api/tasks - Should return empty list when no tasks")
    void getAllTasks_WhenNoTasks_ReturnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/tasks"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/tasks - Should return tasks when tasks exist")
    void getAllTasks_WhenTasksExist_ReturnsTasks() throws Exception {
        // Arrange
        Task task = new Task();
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        taskRepository.save(task);

        // Act & Assert
        mockMvc.perform(get("/api/tasks"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content[0].title").value("Test Task"));
    }

    @Test
    @DisplayName("GET /api/tasks/{id} - Should return task when found")
    void getTask_WhenExists_ReturnsTask() throws Exception {
        // Arrange
        Task task = new Task();
        task.setTitle("Find Me");
        Task saved = taskRepository.save(task);

        // Act & Assert
        mockMvc.perform(get("/api/tasks/{id}", saved.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(saved.getId()))
            .andExpect(jsonPath("$.title").value("Find Me"));
    }

    @Test
    @DisplayName("GET /api/tasks/{id} - Should return 404 when not found")
    void getTask_WhenNotExists_Returns404() throws Exception {
        mockMvc.perform(get("/api/tasks/{id}", 999))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("POST /api/tasks - Should create task with valid data")
    void createTask_WithValidData_ReturnsCreated() throws Exception {
        String requestBody = """
            {
                "title": "New Task",
                "description": "Task Description",
                "priority": "HIGH"
            }
            """;

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("New Task"))
            .andExpect(jsonPath("$.priority").value("HIGH"))
            .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    @DisplayName("POST /api/tasks - Should return 400 with invalid data")
    void createTask_WithInvalidData_Returns400() throws Exception {
        String requestBody = """
            {
                "title": "",
                "description": "No title provided"
            }
            """;

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("PUT /api/tasks/{id} - Should update existing task")
    void updateTask_WhenExists_ReturnsUpdatedTask() throws Exception {
        // Arrange
        Task task = new Task();
        task.setTitle("Original Title");
        Task saved = taskRepository.save(task);

        String requestBody = """
            {
                "title": "Updated Title",
                "priority": "URGENT"
            }
            """;

        // Act & Assert
        mockMvc.perform(put("/api/tasks/{id}", saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Updated Title"))
            .andExpect(jsonPath("$.priority").value("URGENT"));
    }

    @Test
    @DisplayName("DELETE /api/tasks/{id} - Should delete existing task")
    void deleteTask_WhenExists_Returns204() throws Exception {
        // Arrange
        Task task = new Task();
        task.setTitle("To Delete");
        Task saved = taskRepository.save(task);

        // Act & Assert
        mockMvc.perform(delete("/api/tasks/{id}", saved.getId()))
            .andExpect(status().isNoContent());

        // Verify deleted
        mockMvc.perform(get("/api/tasks/{id}", saved.getId()))
            .andExpect(status().isNotFound());
    }
}
```

### Step 2.2: Run Integration Tests

```bash
./mvnw test -Dtest=TaskControllerIntegrationTest
```

---

## Part 3: Testing with Security

### Step 3.1: Create Secured Controller Test

Create `src/test/java/com/example/taskapi/controller/SecuredTaskControllerTest.java`:

```java
package com.example.taskapi.controller;

import com.example.taskapi.entity.Task;
import com.example.taskapi.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecuredTaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();

        Task task = new Task();
        task.setTitle("Test Task");
        taskRepository.save(task);
    }

    @Test
    @DisplayName("Unauthenticated user should get 401")
    void getAllTasks_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/tasks"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("USER role can read tasks")
    @WithMockUser(roles = "USER")
    void getAllTasks_AsUser_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/tasks"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("USER role can create tasks")
    @WithMockUser(roles = "USER")
    void createTask_AsUser_ReturnsCreated() throws Exception {
        String requestBody = """
            {
                "title": "User Task",
                "description": "Created by user"
            }
            """;

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("USER role cannot delete tasks")
    @WithMockUser(roles = "USER")
    void deleteTask_AsUser_Returns403() throws Exception {
        Task task = taskRepository.findAll().get(0);

        mockMvc.perform(delete("/api/tasks/{id}", task.getId()))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMIN role can delete tasks")
    @WithMockUser(roles = "ADMIN")
    void deleteTask_AsAdmin_Returns204() throws Exception {
        Task task = taskRepository.findAll().get(0);

        mockMvc.perform(delete("/api/tasks/{id}", task.getId()))
            .andExpect(status().isNoContent());
    }
}
```

### Step 3.2: Run Security Tests

```bash
./mvnw test -Dtest=SecuredTaskControllerTest
```

---

## Part 4: Packaging for Deployment

### Step 4.1: Build Executable JAR

Spring Boot creates a fully executable JAR with all dependencies included:

```bash
# Build the JAR (skip tests for faster build)
./mvnw clean package -DskipTests

# Or run tests during build
./mvnw clean package
```

The JAR file will be created in `target/` directory.

### Step 4.2: Run the JAR

```bash
# Run the application
java -jar target/task-api-0.0.1-SNAPSHOT.jar

# Run with specific profile
java -jar target/task-api-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod

# Run with custom port
java -jar target/task-api-0.0.1-SNAPSHOT.jar --server.port=9090

# Run with external configuration
java -jar target/task-api-0.0.1-SNAPSHOT.jar --spring.config.location=file:./config/
```

### Step 4.3: Create Production Configuration

Create `src/main/resources/application-prod.properties`:

```properties
# Production Profile
spring.application.name=task-api

# Production Database (PostgreSQL example)
spring.datasource.url=${DATABASE_URL:jdbc:postgresql://localhost:5432/taskdb}
spring.datasource.username=${DATABASE_USER:postgres}
spring.datasource.password=${DATABASE_PASSWORD:}
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA Production Settings
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Actuator - Secure for production
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=when_authorized

# Security
logging.level.org.springframework.security=WARN

# Performance
spring.jpa.open-in-view=false
```

### Step 4.4: Configure Maven for Production

Update `pom.xml` with production-ready settings:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <configuration>
                <!-- Make JAR executable -->
                <executable>true</executable>
                <!-- Add build info -->
                <additionalProperties>
                    <encoding.source>UTF-8</encoding.source>
                    <encoding.reporting>UTF-8</encoding.reporting>
                    <java.version>${java.version}</java.version>
                </additionalProperties>
            </configuration>
            <executions>
                <execution>
                    <goals>
                        <goal>build-info</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

---

## Part 5: Running All Tests

### Step 5.1: Run All Tests with Coverage

Add JaCoCo plugin to `pom.xml`:

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

Run tests with coverage:

```bash
./mvnw clean test

# View coverage report
open target/site/jacoco/index.html
```

### Step 5.2: Test Verification Commands

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=TaskServiceTest

# Run tests matching pattern
./mvnw test -Dtest=*Integration*

# Run tests with verbose output
./mvnw test -X

# Skip tests during build
./mvnw package -DskipTests
```

---

## Part 6: Challenge Exercises

### Challenge 1: Add Test Coverage Threshold

Configure JaCoCo to fail the build if coverage is below a threshold:
- Add coverage rules to JaCoCo configuration
- Set minimum line coverage to 80%
- Set minimum branch coverage to 70%

### Challenge 2: Create Custom Test Slice

Create a custom test annotation that:
- Only loads repository layer
- Uses a test database
- Configures specific test properties

### Challenge 3: Property-Based Testing

Add property-based testing with jqwik:
- Test task creation with random inputs
- Verify invariants hold for all generated data
- Test boundary conditions automatically

### Challenge 4: Performance Testing

Add performance tests:
- Measure response times for API endpoints
- Test under concurrent load
- Verify no memory leaks with repeated requests

---

## Summary

In this lab, you learned:

1. **Unit Testing**: Testing services with Mockito
2. **Integration Testing**: Testing controllers with MockMvc
3. **Security Testing**: Testing secured endpoints with @WithMockUser
4. **Packaging**: Building executable JARs for deployment
5. **Production Configuration**: Setting up production-ready properties

## Testing Best Practices

| Type | Purpose | Speed | Dependencies |
|------|---------|-------|--------------|
| Unit Tests | Test single class | Fast | Mocked |
| Integration Tests | Test component interaction | Medium | Real beans |
| End-to-End Tests | Test full application | Slow | Full context |

## Course Completion

Congratulations! You've completed all labs in the Spring Boot Workshop. You now have the skills to:

- Build REST APIs with Spring Boot
- Persist data with Spring Data JPA
- Secure applications with Spring Security
- Build microservices that communicate
- Test and deploy your applications

**Happy coding!**
