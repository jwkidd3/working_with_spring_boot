package com.example.taskservice.event;

import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;
import java.util.UUID;

public class TaskEvent extends ApplicationEvent {

    public enum EventType {
        TASK_CREATED,
        TASK_ASSIGNED,
        TASK_COMPLETED
    }

    private final String eventId;
    private final EventType eventType;
    private final Long taskId;
    private final String taskTitle;
    private final Long assigneeId;
    private final LocalDateTime timestamp;

    public TaskEvent(Object source, EventType eventType, Long taskId, String taskTitle, Long assigneeId) {
        super(source);
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.taskId = taskId;
        this.taskTitle = taskTitle;
        this.assigneeId = assigneeId;
        this.timestamp = LocalDateTime.now();
    }

    // Getters
    public String getEventId() {
        return eventId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public Long getTaskId() {
        return taskId;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "TaskEvent{" +
                "eventId='" + eventId + '\'' +
                ", eventType=" + eventType +
                ", taskId=" + taskId +
                ", taskTitle='" + taskTitle + '\'' +
                ", assigneeId=" + assigneeId +
                ", timestamp=" + timestamp +
                '}';
    }
}
