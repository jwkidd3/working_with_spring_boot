package com.example.taskapi.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Task priority levels")
public enum TaskPriority {
    @Schema(description = "Low priority task")
    LOW,

    @Schema(description = "Medium priority task (default)")
    MEDIUM,

    @Schema(description = "High priority task")
    HIGH,

    @Schema(description = "Critical priority task requiring immediate attention")
    CRITICAL
}
