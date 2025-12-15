package com.example.taskapi.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Task status values")
public enum TaskStatus {
    @Schema(description = "Task is pending and not yet started")
    PENDING,

    @Schema(description = "Task is currently in progress")
    IN_PROGRESS,

    @Schema(description = "Task has been completed")
    COMPLETED,

    @Schema(description = "Task has been cancelled")
    CANCELLED
}
