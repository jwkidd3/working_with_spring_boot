package com.example.taskapi.controller;

import com.example.taskapi.dto.CreateTaskRequest;
import com.example.taskapi.dto.ErrorResponse;
import com.example.taskapi.dto.TaskResponse;
import com.example.taskapi.entity.Task;
import com.example.taskapi.entity.TaskStatus;
import com.example.taskapi.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tasks", description = "Task management operations")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    @Operation(
            summary = "Get all tasks",
            description = "Retrieves a list of all tasks in the system"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved list of tasks",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = TaskResponse.class))
            )
    )
    public List<Task> getAllTasks() {
        return taskService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get task by ID",
            description = "Retrieves a specific task by its unique identifier"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Task found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TaskResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Task not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public Task getTask(
            @Parameter(description = "ID of the task to retrieve", required = true, example = "1")
            @PathVariable Long id) {
        return taskService.findById(id);
    }

    @PostMapping
    @Operation(
            summary = "Create a new task",
            description = "Creates a new task with the provided details"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Task created successfully",
                    headers = @Header(
                            name = "Location",
                            description = "URL of the created task",
                            schema = @Schema(type = "string", example = "/api/tasks/1")
                    ),
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TaskResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<Task> createTask(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Task creation request",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateTaskRequest.class))
            )
            @Valid @RequestBody CreateTaskRequest request) {
        Task task = taskService.create(request);
        return ResponseEntity
                .created(URI.create("/api/tasks/" + task.getId()))
                .body(task);
    }

    @PatchMapping("/{id}/status")
    @Operation(
            summary = "Update task status",
            description = "Updates the status of an existing task"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Task status updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TaskResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Task not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public Task updateStatus(
            @Parameter(description = "ID of the task to update", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "New status for the task", required = true, example = "IN_PROGRESS")
            @RequestParam TaskStatus status) {
        return taskService.updateStatus(id, status);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Delete a task",
            description = "Deletes a task by its ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Task deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Task not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public void deleteTask(
            @Parameter(description = "ID of the task to delete", required = true, example = "1")
            @PathVariable Long id) {
        taskService.delete(id);
    }

    @GetMapping("/search")
    @Operation(
            summary = "Search tasks",
            description = "Search tasks by status or keyword in title. If no parameters are provided, returns all tasks."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Search results",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = TaskResponse.class))
            )
    )
    public List<Task> searchTasks(
            @Parameter(description = "Filter by task status", example = "PENDING")
            @RequestParam(required = false) TaskStatus status,
            @Parameter(description = "Search keyword in task title", example = "project")
            @RequestParam(required = false) String keyword) {
        if (status != null) {
            return taskService.findByStatus(status);
        }
        if (keyword != null) {
            return taskService.searchByTitle(keyword);
        }
        return taskService.findAll();
    }
}
