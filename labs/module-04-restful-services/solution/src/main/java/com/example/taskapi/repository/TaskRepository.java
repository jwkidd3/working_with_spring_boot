package com.example.taskapi.repository;

import com.example.taskapi.entity.Task;
import com.example.taskapi.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Find all tasks by status with pagination support
     */
    Page<Task> findByStatus(TaskStatus status, Pageable pageable);

    /**
     * Count tasks by status
     */
    long countByStatus(TaskStatus status);
}
