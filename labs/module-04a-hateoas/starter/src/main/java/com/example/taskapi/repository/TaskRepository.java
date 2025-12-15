package com.example.taskapi.repository;

import com.example.taskapi.entity.Task;
import com.example.taskapi.entity.TaskPriority;
import com.example.taskapi.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Task entity.
 * Provides CRUD operations and custom query methods.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Find tasks by status with pagination support.
     */
    Page<Task> findByStatus(TaskStatus status, Pageable pageable);

    /**
     * Find tasks by priority with pagination support.
     */
    Page<Task> findByPriority(TaskPriority priority, Pageable pageable);

    /**
     * Find tasks by status and priority.
     */
    List<Task> findByStatusAndPriority(TaskStatus status, TaskPriority priority);

    /**
     * Find tasks with due date before the specified date.
     */
    List<Task> findByDueDateBefore(LocalDateTime dateTime);

    /**
     * Find tasks with due date between two dates.
     */
    List<Task> findByDueDateBetween(LocalDateTime start, LocalDateTime end);
}
