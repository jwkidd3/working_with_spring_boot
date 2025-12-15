package com.example.taskservice.service;

import com.example.taskservice.entity.Task;
import com.example.taskservice.entity.TaskStatus;
import com.example.taskservice.event.TaskEventPublisher;
import com.example.taskservice.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final TaskEventPublisher eventPublisher;

    public TaskService(TaskRepository taskRepository, TaskEventPublisher eventPublisher) {
        this.taskRepository = taskRepository;
        this.eventPublisher = eventPublisher;
    }

    public Task createTask(String title, String description) {
        logger.info("Creating new task: {}", title);

        Task task = new Task(title, description);
        task = taskRepository.save(task);

        // Publish task created event
        eventPublisher.publishTaskCreated(task.getId(), task.getTitle());

        logger.info("Task created with ID: {}", task.getId());
        return task;
    }

    @Transactional(readOnly = true)
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Task> getTasksByStatus(TaskStatus status) {
        return taskRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Task> getTasksByAssignee(Long assigneeId) {
        return taskRepository.findByAssigneeId(assigneeId);
    }

    public Task assignTask(Long taskId, Long assigneeId) {
        logger.info("Assigning task {} to user {}", taskId, assigneeId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with id: " + taskId));

        task.setAssigneeId(assigneeId);
        if (task.getStatus() == TaskStatus.TODO) {
            task.setStatus(TaskStatus.IN_PROGRESS);
        }
        task = taskRepository.save(task);

        // Publish task assigned event
        eventPublisher.publishTaskAssigned(task.getId(), task.getTitle(), assigneeId);

        logger.info("Task {} assigned to user {}", taskId, assigneeId);
        return task;
    }

    public Task completeTask(Long taskId) {
        logger.info("Completing task {}", taskId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with id: " + taskId));

        task.setStatus(TaskStatus.COMPLETED);
        task = taskRepository.save(task);

        // Publish task completed event
        eventPublisher.publishTaskCompleted(task.getId(), task.getTitle(), task.getAssigneeId());

        logger.info("Task {} completed", taskId);
        return task;
    }

    public Task updateTask(Long taskId, String title, String description) {
        logger.info("Updating task {}", taskId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with id: " + taskId));

        if (title != null && !title.isBlank()) {
            task.setTitle(title);
        }
        if (description != null) {
            task.setDescription(description);
        }

        task = taskRepository.save(task);
        logger.info("Task {} updated", taskId);
        return task;
    }

    public void deleteTask(Long taskId) {
        logger.info("Deleting task {}", taskId);

        if (!taskRepository.existsById(taskId)) {
            throw new IllegalArgumentException("Task not found with id: " + taskId);
        }

        taskRepository.deleteById(taskId);
        logger.info("Task {} deleted", taskId);
    }
}
