package com.example.taskapi.service;

import com.example.taskapi.dto.CreateTaskRequest;
import com.example.taskapi.dto.TaskSearchCriteria;
import com.example.taskapi.dto.UpdateTaskRequest;
import com.example.taskapi.entity.Task;
import com.example.taskapi.entity.TaskStatus;
import com.example.taskapi.exception.ConflictException;
import com.example.taskapi.exception.TaskNotFoundException;
import com.example.taskapi.repository.TaskRepository;
import com.example.taskapi.specification.TaskSpecifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);
    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> findAll() {
        return taskRepository.findAll();
    }

    public Page<Task> findAll(Pageable pageable) {
        return taskRepository.findAll(pageable);
    }

    public Task findById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    @Transactional
    public Task create(CreateTaskRequest request) {
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        task.setAssignee(request.getAssignee());
        task.setDueDate(request.getDueDate());

        log.info("Creating task: {}", task.getTitle());
        return taskRepository.save(task);
    }

    @Transactional
    public Task update(Long id, UpdateTaskRequest request) {
        Task task = findById(id);

        // Check for optimistic locking
        if (request.getVersion() != null && !request.getVersion().equals(task.getVersion())) {
            throw new ConflictException("Task was modified by another user. Please refresh and try again.");
        }

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
        if (request.getAssignee() != null) {
            task.setAssignee(request.getAssignee());
        }
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }

        log.info("Updating task: {}", id);
        return taskRepository.save(task);
    }

    @Transactional
    public void delete(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException(id);
        }
        log.info("Deleting task: {}", id);
        taskRepository.deleteById(id);
    }

    // Dynamic search using Specifications
    public List<Task> search(TaskSearchCriteria criteria) {
        Specification<Task> spec = Specification.where(null);

        if (criteria.getKeyword() != null && !criteria.getKeyword().isBlank()) {
            spec = spec.and(TaskSpecifications.titleOrDescriptionContains(criteria.getKeyword()));
        }
        if (criteria.getStatus() != null) {
            spec = spec.and(TaskSpecifications.hasStatus(criteria.getStatus()));
        }
        if (criteria.getPriority() != null) {
            spec = spec.and(TaskSpecifications.hasPriority(criteria.getPriority()));
        }
        if (criteria.getPriorities() != null && !criteria.getPriorities().isEmpty()) {
            spec = spec.and(TaskSpecifications.priorityIn(criteria.getPriorities()));
        }
        if (criteria.getAssignee() != null && !criteria.getAssignee().isBlank()) {
            spec = spec.and(TaskSpecifications.assignedTo(criteria.getAssignee()));
        }
        if (criteria.getDueDateFrom() != null || criteria.getDueDateTo() != null) {
            spec = spec.and(TaskSpecifications.dueDateBetween(criteria.getDueDateFrom(), criteria.getDueDateTo()));
        }
        if (Boolean.TRUE.equals(criteria.getOverdue())) {
            spec = spec.and(TaskSpecifications.isOverdue());
        }

        return taskRepository.findAll(spec);
    }

    // JPQL queries
    public List<Task> findByStatus(TaskStatus status) {
        return taskRepository.findByStatusOrderByPriority(status);
    }

    public List<Task> searchByKeyword(String keyword) {
        return taskRepository.searchByKeyword(keyword);
    }

    public List<Task> findOverdueTasks() {
        return taskRepository.findOverdueTasks(LocalDate.now());
    }

    public Map<TaskStatus, Long> getTaskCountByStatus() {
        return taskRepository.countByStatus().stream()
                .collect(Collectors.toMap(
                        row -> (TaskStatus) row[0],
                        row -> (Long) row[1]
                ));
    }

    // Transaction propagation example
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logTaskActivity(Long taskId, String activity) {
        log.info("Activity on task {}: {}", taskId, activity);
        // This runs in its own transaction
    }

    // Bulk update with transaction
    @Transactional
    public int bulkUpdateStatus(TaskStatus oldStatus, TaskStatus newStatus) {
        log.info("Bulk updating tasks from {} to {}", oldStatus, newStatus);
        return taskRepository.bulkUpdateStatus(oldStatus, newStatus);
    }

    // Example with specific isolation level
    @Transactional(isolation = Isolation.SERIALIZABLE, timeout = 30)
    public Task updateWithSerializableIsolation(Long id, UpdateTaskRequest request) {
        return update(id, request);
    }
}
