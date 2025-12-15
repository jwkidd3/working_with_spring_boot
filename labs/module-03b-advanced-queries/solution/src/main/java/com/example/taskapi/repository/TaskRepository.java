package com.example.taskapi.repository;

import com.example.taskapi.entity.Task;
import com.example.taskapi.entity.TaskPriority;
import com.example.taskapi.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    // JPQL Query - find by status
    @Query("SELECT t FROM Task t WHERE t.status = :status ORDER BY t.priority DESC")
    List<Task> findByStatusOrderByPriority(@Param("status") TaskStatus status);

    // JPQL Query - search by title and description
    @Query("SELECT t FROM Task t WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Task> searchByKeyword(@Param("keyword") String keyword);

    // JPQL Query - find overdue tasks
    @Query("SELECT t FROM Task t WHERE t.dueDate < :date AND t.status != 'COMPLETED' AND t.status != 'CANCELLED'")
    List<Task> findOverdueTasks(@Param("date") LocalDate date);

    // JPQL Query - count by status
    @Query("SELECT t.status, COUNT(t) FROM Task t GROUP BY t.status")
    List<Object[]> countByStatus();

    // JPQL Query - find by assignee and priority
    @Query("SELECT t FROM Task t WHERE t.assignee = :assignee AND t.priority IN :priorities")
    List<Task> findByAssigneeAndPriorityIn(@Param("assignee") String assignee,
                                            @Param("priorities") List<TaskPriority> priorities);

    // Native SQL Query
    @Query(value = "SELECT * FROM tasks WHERE assignee = :assignee AND status = 'TODO' " +
                   "ORDER BY CASE priority WHEN 'URGENT' THEN 1 WHEN 'HIGH' THEN 2 " +
                   "WHEN 'MEDIUM' THEN 3 ELSE 4 END",
           nativeQuery = true)
    List<Task> findPendingTasksByAssigneeNative(@Param("assignee") String assignee);

    // Modifying query - bulk update
    @Modifying
    @Query("UPDATE Task t SET t.status = :newStatus WHERE t.status = :oldStatus")
    int bulkUpdateStatus(@Param("oldStatus") TaskStatus oldStatus,
                         @Param("newStatus") TaskStatus newStatus);
}
