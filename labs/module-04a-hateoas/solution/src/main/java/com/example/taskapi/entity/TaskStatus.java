package com.example.taskapi.entity;

/**
 * Enumeration representing the status of a task.
 */
public enum TaskStatus {
    /**
     * Task is newly created and not yet started
     */
    CREATED,

    /**
     * Task is currently in progress
     */
    IN_PROGRESS,

    /**
     * Task has been completed
     */
    COMPLETED,

    /**
     * Task has been cancelled
     */
    CANCELLED
}
