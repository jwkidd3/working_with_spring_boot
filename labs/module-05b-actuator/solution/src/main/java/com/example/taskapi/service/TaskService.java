package com.example.taskapi.service;

import com.example.taskapi.dto.CreateTaskRequest;
import com.example.taskapi.dto.UpdateTaskRequest;
import com.example.taskapi.entity.Task;
import com.example.taskapi.entity.TaskStatus;
import com.example.taskapi.exception.TaskNotFoundException;
import com.example.taskapi.repository.TaskRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service layer for Task management with Micrometer metrics
 */
@Service
public class TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final MeterRegistry meterRegistry;

    // Counters for task operations
    private final Counter tasksCreatedCounter;
    private final Counter tasksCompletedCounter;
    private final Counter tasksDeletedCounter;

    // Timer for task creation
    private final Timer taskCreationTimer;

    public TaskService(TaskRepository taskRepository, MeterRegistry meterRegistry) {
        this.taskRepository = taskRepository;
        this.meterRegistry = meterRegistry;

        // Initialize counters
        this.tasksCreatedCounter = Counter.builder("tasks.created")
                .description("Total number of tasks created")
                .tag("service", "task-api")
                .register(meterRegistry);

        this.tasksCompletedCounter = Counter.builder("tasks.completed")
                .description("Total number of tasks completed")
                .tag("service", "task-api")
                .register(meterRegistry);

        this.tasksDeletedCounter = Counter.builder("tasks.deleted")
                .description("Total number of tasks deleted")
                .tag("service", "task-api")
                .register(meterRegistry);

        // Initialize timer
        this.taskCreationTimer = Timer.builder("tasks.creation.time")
                .description("Time taken to create a task")
                .tag("service", "task-api")
                .register(meterRegistry);

        // Register gauges for active tasks and overdue tasks
        meterRegistry.gauge("tasks.active", this, service -> {
            long todo = taskRepository.countByStatus(TaskStatus.TODO);
            long inProgress = taskRepository.countByStatus(TaskStatus.IN_PROGRESS);
            return todo + inProgress;
        });

        meterRegistry.gauge("tasks.overdue", this, service ->
                taskRepository.findOverdueTasks(LocalDate.now()).size()
        );
    }

    /**
     * Get all tasks
     */
    public List<Task> getAllTasks() {
        logger.debug("Retrieving all tasks");
        return taskRepository.findAll();
    }

    /**
     * Get task by ID
     */
    public Task getTaskById(Long id) {
        logger.debug("Retrieving task with id: {}", id);
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    /**
     * Create a new task with timing metrics
     */
    @Transactional
    public Task createTask(CreateTaskRequest request) {
        return taskCreationTimer.record(() -> {
            logger.info("Creating new task: {}", request.getTitle());

            Task task = new Task();
            task.setTitle(request.getTitle());
            task.setDescription(request.getDescription());
            task.setStatus(request.getStatus());
            task.setPriority(request.getPriority());
            task.setDueDate(request.getDueDate());

            Task savedTask = taskRepository.save(task);

            // Increment counter
            tasksCreatedCounter.increment();
            logger.info("Task created successfully with id: {}", savedTask.getId());

            return savedTask;
        });
    }

    /**
     * Update an existing task
     */
    @Transactional
    public Task updateTask(Long id, UpdateTaskRequest request) {
        logger.info("Updating task with id: {}", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        TaskStatus oldStatus = task.getStatus();

        // Update fields if provided
        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }

        Task updatedTask = taskRepository.save(task);

        // Increment completed counter if status changed to COMPLETED
        if (oldStatus != TaskStatus.COMPLETED && updatedTask.getStatus() == TaskStatus.COMPLETED) {
            tasksCompletedCounter.increment();
            logger.info("Task completed: {}", id);
        }

        logger.info("Task updated successfully: {}", id);
        return updatedTask;
    }

    /**
     * Delete a task
     */
    @Transactional
    public void deleteTask(Long id) {
        logger.info("Deleting task with id: {}", id);

        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException(id);
        }

        taskRepository.deleteById(id);
        tasksDeletedCounter.increment();

        logger.info("Task deleted successfully: {}", id);
    }

    /**
     * Get tasks by status
     */
    public long getTaskCountByStatus(TaskStatus status) {
        return taskRepository.countByStatus(status);
    }

    /**
     * Get overdue tasks
     */
    public List<Task> getOverdueTasks() {
        return taskRepository.findOverdueTasks(LocalDate.now());
    }
}
