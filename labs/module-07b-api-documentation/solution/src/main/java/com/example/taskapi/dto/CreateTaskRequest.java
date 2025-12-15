package com.example.taskapi.dto;

import com.example.taskapi.entity.TaskPriority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Request body for creating a new task")
public class CreateTaskRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    @Schema(
            description = "Title of the task",
            example = "Implement user authentication",
            minLength = 3,
            maxLength = 100,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String title;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Schema(
            description = "Detailed description of the task",
            example = "Add JWT-based authentication to all API endpoints",
            maxLength = 500
    )
    private String description;

    @Schema(
            description = "Priority level of the task",
            example = "HIGH",
            defaultValue = "MEDIUM"
    )
    private TaskPriority priority = TaskPriority.MEDIUM;

    @Schema(
            description = "Due date for the task completion",
            example = "2024-12-31"
    )
    private LocalDate dueDate;

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TaskPriority getPriority() { return priority; }
    public void setPriority(TaskPriority priority) { this.priority = priority; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
}
