package com.example.taskservice.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class TaskEventListener {

    private static final Logger logger = LoggerFactory.getLogger(TaskEventListener.class);

    private final RestTemplate restTemplate;
    private final String userServiceUrl;

    public TaskEventListener(RestTemplate restTemplate,
                           @Value("${user.service.url}") String userServiceUrl) {
        this.restTemplate = restTemplate;
        this.userServiceUrl = userServiceUrl;
    }

    @Async("taskExecutor")
    @EventListener
    public void handleTaskEvent(TaskEvent event) {
        logger.info("Processing event: {} for task: {} (Event ID: {})",
                event.getEventType(), event.getTaskTitle(), event.getEventId());

        try {
            // Simulate some processing time
            Thread.sleep(500);

            switch (event.getEventType()) {
                case TASK_CREATED:
                    handleTaskCreated(event);
                    break;
                case TASK_ASSIGNED:
                    handleTaskAssigned(event);
                    break;
                case TASK_COMPLETED:
                    handleTaskCompleted(event);
                    break;
            }

            logger.info("Successfully processed event: {} for task: {}",
                    event.getEventType(), event.getTaskTitle());

        } catch (Exception e) {
            logger.error("Error processing event: {} for task: {}",
                    event.getEventType(), event.getTaskTitle(), e);
        }
    }

    private void handleTaskCreated(TaskEvent event) {
        logger.info("Task created: {} (ID: {})", event.getTaskTitle(), event.getTaskId());
        // Additional logic: send notifications, update dashboards, etc.
    }

    private void handleTaskAssigned(TaskEvent event) {
        logger.info("Task assigned: {} to assignee ID: {}", event.getTaskTitle(), event.getAssigneeId());

        // Fetch user details from User Service
        if (event.getAssigneeId() != null) {
            try {
                String url = userServiceUrl + "/api/users/" + event.getAssigneeId();
                Map<String, Object> user = restTemplate.getForObject(url, Map.class);

                if (user != null) {
                    logger.info("Fetched user details: {}", user);
                    logger.info("Notification would be sent to: {}", user.get("email"));
                } else {
                    logger.warn("User not found with ID: {}", event.getAssigneeId());
                }
            } catch (Exception e) {
                logger.error("Failed to fetch user details for ID: {}", event.getAssigneeId(), e);
            }
        }
    }

    private void handleTaskCompleted(TaskEvent event) {
        logger.info("Task completed: {} by assignee ID: {}", event.getTaskTitle(), event.getAssigneeId());

        // Fetch user details and send completion notification
        if (event.getAssigneeId() != null) {
            try {
                String url = userServiceUrl + "/api/users/" + event.getAssigneeId();
                Map<String, Object> user = restTemplate.getForObject(url, Map.class);

                if (user != null) {
                    logger.info("Task completed by: {}", user.get("name"));
                    logger.info("Completion notification would be sent to: {}", user.get("email"));
                }
            } catch (Exception e) {
                logger.error("Failed to fetch user details for ID: {}", event.getAssigneeId(), e);
            }
        }
    }
}
