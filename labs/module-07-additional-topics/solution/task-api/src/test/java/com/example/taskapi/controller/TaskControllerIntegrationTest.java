package com.example.taskapi.controller;

import com.example.taskapi.dto.CreateTaskRequest;
import com.example.taskapi.dto.UpdateTaskRequest;
import com.example.taskapi.entity.Task;
import com.example.taskapi.entity.TaskPriority;
import com.example.taskapi.entity.TaskStatus;
import com.example.taskapi.repository.TaskRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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
    void getAllTasks_ReturnsOk() throws Exception {
        // Given
        Task task1 = new Task("Task 1", "Description 1", TaskStatus.TODO, TaskPriority.HIGH);
        Task task2 = new Task("Task 2", "Description 2", TaskStatus.IN_PROGRESS, TaskPriority.LOW);
        taskRepository.save(task1);
        taskRepository.save(task2);

        // When/Then
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("Task 1")))
                .andExpect(jsonPath("$[1].title", is("Task 2")));
    }

    @Test
    void getTask_WhenExists_ReturnsTask() throws Exception {
        // Given
        Task task = new Task("Test Task", "Test Description", TaskStatus.TODO, TaskPriority.MEDIUM);
        Task savedTask = taskRepository.save(task);

        // When/Then
        mockMvc.perform(get("/api/tasks/" + savedTask.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedTask.getId().intValue())))
                .andExpect(jsonPath("$.title", is("Test Task")))
                .andExpect(jsonPath("$.description", is("Test Description")))
                .andExpect(jsonPath("$.status", is("TODO")))
                .andExpect(jsonPath("$.priority", is("MEDIUM")));
    }

    @Test
    void getTask_WhenNotExists_Returns404() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/tasks/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Task not found with id: 999")));
    }

    @Test
    void createTask_WithValidData_ReturnsCreated() throws Exception {
        // Given
        CreateTaskRequest request = new CreateTaskRequest(
                "New Task",
                "New Description",
                TaskStatus.TODO,
                TaskPriority.HIGH
        );

        // When/Then
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.title", is("New Task")))
                .andExpect(jsonPath("$.description", is("New Description")))
                .andExpect(jsonPath("$.status", is("TODO")))
                .andExpect(jsonPath("$.priority", is("HIGH")));
    }

    @Test
    void createTask_WithInvalidData_Returns400() throws Exception {
        // Given - Missing required title field
        CreateTaskRequest request = new CreateTaskRequest(
                "",
                "Description",
                TaskStatus.TODO,
                TaskPriority.HIGH
        );

        // When/Then
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Validation failed")))
                .andExpect(jsonPath("$.errors.title", notNullValue()));
    }

    @Test
    void createTask_WithMissingStatus_Returns400() throws Exception {
        // Given - Missing required status field
        CreateTaskRequest request = new CreateTaskRequest(
                "Task Title",
                "Description",
                null,
                TaskPriority.HIGH
        );

        // When/Then
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Validation failed")))
                .andExpect(jsonPath("$.errors.status", notNullValue()));
    }

    @Test
    void updateTask_WhenExists_ReturnsUpdated() throws Exception {
        // Given
        Task task = new Task("Original Task", "Original Description", TaskStatus.TODO, TaskPriority.LOW);
        Task savedTask = taskRepository.save(task);

        UpdateTaskRequest request = new UpdateTaskRequest(
                "Updated Task",
                "Updated Description",
                TaskStatus.DONE,
                TaskPriority.HIGH
        );

        // When/Then
        mockMvc.perform(put("/api/tasks/" + savedTask.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedTask.getId().intValue())))
                .andExpect(jsonPath("$.title", is("Updated Task")))
                .andExpect(jsonPath("$.description", is("Updated Description")))
                .andExpect(jsonPath("$.status", is("DONE")))
                .andExpect(jsonPath("$.priority", is("HIGH")));
    }

    @Test
    void updateTask_WhenNotExists_Returns404() throws Exception {
        // Given
        UpdateTaskRequest request = new UpdateTaskRequest(
                "Updated Task",
                "Updated Description",
                TaskStatus.DONE,
                TaskPriority.HIGH
        );

        // When/Then
        mockMvc.perform(put("/api/tasks/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Task not found with id: 999")));
    }

    @Test
    void deleteTask_WhenExists_Returns204() throws Exception {
        // Given
        Task task = new Task("Task to Delete", "Description", TaskStatus.TODO, TaskPriority.LOW);
        Task savedTask = taskRepository.save(task);

        // When/Then
        mockMvc.perform(delete("/api/tasks/" + savedTask.getId()))
                .andExpect(status().isNoContent());

        // Verify task was deleted
        mockMvc.perform(get("/api/tasks/" + savedTask.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTask_WhenNotExists_Returns404() throws Exception {
        // When/Then
        mockMvc.perform(delete("/api/tasks/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Task not found with id: 999")));
    }
}
