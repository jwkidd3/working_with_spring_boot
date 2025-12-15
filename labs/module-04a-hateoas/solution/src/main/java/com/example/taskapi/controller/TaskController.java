package com.example.taskapi.controller;

import com.example.taskapi.assembler.TaskModelAssembler;
import com.example.taskapi.dto.CreateTaskRequest;
import com.example.taskapi.dto.TaskResponse;
import com.example.taskapi.dto.UpdateTaskRequest;
import com.example.taskapi.entity.Task;
import com.example.taskapi.entity.TaskPriority;
import com.example.taskapi.entity.TaskStatus;
import com.example.taskapi.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

/**
 * REST Controller for Task operations.
 * Demonstrates HATEOAS principles with hypermedia-driven responses.
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;
    private final TaskModelAssembler taskModelAssembler;
    private final PagedResourcesAssembler<Task> pagedResourcesAssembler;

    public TaskController(TaskService taskService,
                          TaskModelAssembler taskModelAssembler,
                          PagedResourcesAssembler<Task> pagedResourcesAssembler) {
        this.taskService = taskService;
        this.taskModelAssembler = taskModelAssembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    /**
     * Get all tasks with pagination.
     * Returns a PagedModel with navigation links (first, last, next, prev).
     */
    @GetMapping
    public ResponseEntity<PagedModel<TaskResponse>> getAllTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Task> taskPage = taskService.getAllTasks(pageable);

        PagedModel<TaskResponse> pagedModel = pagedResourcesAssembler.toModel(taskPage, taskModelAssembler);

        return ResponseEntity.ok(pagedModel);
    }

    /**
     * Get tasks by status with pagination.
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<PagedModel<TaskResponse>> getTasksByStatus(
            @PathVariable TaskStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Task> taskPage = taskService.getTasksByStatus(status, pageable);

        PagedModel<TaskResponse> pagedModel = pagedResourcesAssembler.toModel(taskPage, taskModelAssembler);

        return ResponseEntity.ok(pagedModel);
    }

    /**
     * Get tasks by priority with pagination.
     */
    @GetMapping("/priority/{priority}")
    public ResponseEntity<PagedModel<TaskResponse>> getTasksByPriority(
            @PathVariable TaskPriority priority,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Task> taskPage = taskService.getTasksByPriority(priority, pageable);

        PagedModel<TaskResponse> pagedModel = pagedResourcesAssembler.toModel(taskPage, taskModelAssembler);

        return ResponseEntity.ok(pagedModel);
    }

    /**
     * Get a single task by ID.
     * Returns the task with HATEOAS links.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long id) {
        Task task = taskService.getTaskById(id);
        TaskResponse taskResponse = taskModelAssembler.toModel(task);

        return ResponseEntity.ok(taskResponse);
    }

    /**
     * Create a new task.
     * Returns 201 Created with Location header and HATEOAS links.
     */
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request) {
        Task task = taskService.createTask(request);
        TaskResponse taskResponse = taskModelAssembler.toModel(task);

        return ResponseEntity
                .created(linkTo(methodOn(TaskController.class).getTaskById(task.getId())).toUri())
                .body(taskResponse);
    }

    /**
     * Update an existing task.
     */
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskRequest request) {

        Task task = taskService.updateTask(id, request);
        TaskResponse taskResponse = taskModelAssembler.toModel(task);

        return ResponseEntity.ok(taskResponse);
    }

    /**
     * Delete a task.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Start a task (CREATED -> IN_PROGRESS).
     */
    @PatchMapping("/{id}/start")
    public ResponseEntity<TaskResponse> startTask(@PathVariable Long id) {
        Task task = taskService.startTask(id);
        TaskResponse taskResponse = taskModelAssembler.toModel(task);

        return ResponseEntity.ok(taskResponse);
    }

    /**
     * Complete a task (IN_PROGRESS -> COMPLETED).
     */
    @PatchMapping("/{id}/complete")
    public ResponseEntity<TaskResponse> completeTask(@PathVariable Long id) {
        Task task = taskService.completeTask(id);
        TaskResponse taskResponse = taskModelAssembler.toModel(task);

        return ResponseEntity.ok(taskResponse);
    }

    /**
     * Cancel a task (any status except COMPLETED -> CANCELLED).
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<TaskResponse> cancelTask(@PathVariable Long id) {
        Task task = taskService.cancelTask(id);
        TaskResponse taskResponse = taskModelAssembler.toModel(task);

        return ResponseEntity.ok(taskResponse);
    }

    /**
     * Reopen a task (COMPLETED or CANCELLED -> CREATED).
     */
    @PatchMapping("/{id}/reopen")
    public ResponseEntity<TaskResponse> reopenTask(@PathVariable Long id) {
        Task task = taskService.reopenTask(id);
        TaskResponse taskResponse = taskModelAssembler.toModel(task);

        return ResponseEntity.ok(taskResponse);
    }
}
