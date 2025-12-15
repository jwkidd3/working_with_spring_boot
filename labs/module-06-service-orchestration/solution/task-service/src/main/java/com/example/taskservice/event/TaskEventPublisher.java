package com.example.taskservice.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class TaskEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(TaskEventPublisher.class);

    private final ApplicationEventPublisher eventPublisher;

    public TaskEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void publishTaskCreated(Long taskId, String taskTitle) {
        logger.info("Publishing event: TASK_CREATED for task: {}", taskTitle);
        TaskEvent event = new TaskEvent(this, TaskEvent.EventType.TASK_CREATED, taskId, taskTitle, null);
        eventPublisher.publishEvent(event);
    }

    public void publishTaskAssigned(Long taskId, String taskTitle, Long assigneeId) {
        logger.info("Publishing event: TASK_ASSIGNED for task: {} to assignee: {}", taskTitle, assigneeId);
        TaskEvent event = new TaskEvent(this, TaskEvent.EventType.TASK_ASSIGNED, taskId, taskTitle, assigneeId);
        eventPublisher.publishEvent(event);
    }

    public void publishTaskCompleted(Long taskId, String taskTitle, Long assigneeId) {
        logger.info("Publishing event: TASK_COMPLETED for task: {} by assignee: {}", taskTitle, assigneeId);
        TaskEvent event = new TaskEvent(this, TaskEvent.EventType.TASK_COMPLETED, taskId, taskTitle, assigneeId);
        eventPublisher.publishEvent(event);
    }
}
