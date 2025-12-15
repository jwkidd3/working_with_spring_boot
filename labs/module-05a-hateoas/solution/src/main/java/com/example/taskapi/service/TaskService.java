package com.example.taskapi.service;

import com.example.taskapi.dto.CreateTaskRequest;
import com.example.taskapi.dto.UpdateTaskRequest;
import com.example.taskapi.entity.Task;
import com.example.taskapi.entity.TaskPriority;
import com.example.taskapi.entity.TaskStatus;
import com.example.taskapi.exception.TaskNotFoundException;
import com.example.taskapi.repository.TaskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer for Task business logic.
 * Handles CRUD operations and task status transitions.
 */
@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * Get all tasks with pagination support.
     */
    @Transactional(readOnly = true)
    public Page<Task> getAllTasks(Pageable pageable) {
        return taskRepository.findAll(pageable);
    }

    /**
     * Get tasks by status with pagination.
     */
    @Transactional(readOnly = true)
    public Page<Task> getTasksByStatus(TaskStatus status, Pageable pageable) {
        return taskRepository.findByStatus(status, pageable);
    }

    /**
     * Get tasks by priority with pagination.
     */
    @Transactional(readOnly = true)
    public Page<Task> getTasksByPriority(TaskPriority priority, Pageable pageable) {
        return taskRepository.findByPriority(priority, pageable);
    }

    /**
     * Get a task by ID.
     */
    @Transactional(readOnly = true)
    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));
    }

    /**
     * Create a new task.
     */
    public Task createTask(CreateTaskRequest request) {
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(TaskStatus.CREATED);
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());

        return taskRepository.save(task);
    }

    /**
     * Update an existing task.
     */
    public Task updateTask(Long id, UpdateTaskRequest request) {
        Task task = getTaskById(id);

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

        return taskRepository.save(task);
    }

    /**
     * Delete a task.
     */
    public void deleteTask(Long id) {
        Task task = getTaskById(id);
        taskRepository.delete(task);
    }

    /**
     * Start a task (transition from CREATED to IN_PROGRESS).
     */
    public Task startTask(Long id) {
        Task task = getTaskById(id);
        if (task.getStatus() != TaskStatus.CREATED) {
            throw new IllegalStateException("Task can only be started from CREATED status");
        }
        task.setStatus(TaskStatus.IN_PROGRESS);
        return taskRepository.save(task);
    }

    /**
     * Complete a task (transition from IN_PROGRESS to COMPLETED).
     */
    public Task completeTask(Long id) {
        Task task = getTaskById(id);
        if (task.getStatus() != TaskStatus.IN_PROGRESS) {
            throw new IllegalStateException("Task can only be completed from IN_PROGRESS status");
        }
        task.setStatus(TaskStatus.COMPLETED);
        return taskRepository.save(task);
    }

    /**
     * Cancel a task (transition to CANCELLED).
     */
    public Task cancelTask(Long id) {
        Task task = getTaskById(id);
        if (task.getStatus() == TaskStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel a completed task");
        }
        task.setStatus(TaskStatus.CANCELLED);
        return taskRepository.save(task);
    }

    /**
     * Reopen a task (transition from COMPLETED or CANCELLED to CREATED).
     */
    public Task reopenTask(Long id) {
        Task task = getTaskById(id);
        if (task.getStatus() != TaskStatus.COMPLETED && task.getStatus() != TaskStatus.CANCELLED) {
            throw new IllegalStateException("Task can only be reopened from COMPLETED or CANCELLED status");
        }
        task.setStatus(TaskStatus.CREATED);
        return taskRepository.save(task);
    }
}
