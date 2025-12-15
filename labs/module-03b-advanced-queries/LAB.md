# Lab 3b: Advanced Queries & Transactions

## Objectives

By the end of this lab, you will be able to:
- Write custom JPQL and native SQL queries
- Build dynamic queries with Specifications
- Understand @Transactional behavior and propagation
- Implement optimistic locking with @Version
- Add auditing with @CreatedDate and @LastModifiedDate

## Prerequisites

- Completed Lab 4: Database Integration
- Understanding of JPA basics and Spring Data repositories

## Duration

45-60 minutes

---

## Part 1: Custom JPQL Queries

### Step 1.1: Enhance the Task Entity

Update your Task entity with additional fields for this lab:

```java
package com.example.taskapi.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    private TaskStatus status = TaskStatus.TODO;

    @Enumerated(EnumType.STRING)
    private TaskPriority priority = TaskPriority.MEDIUM;

    private String assignee;

    private LocalDate dueDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
    public TaskPriority getPriority() { return priority; }
    public void setPriority(TaskPriority priority) { this.priority = priority; }
    public String getAssignee() { return assignee; }
    public void setAssignee(String assignee) { this.assignee = assignee; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
```

Create `entity/TaskStatus.java`:

```java
package com.example.taskapi.entity;

public enum TaskStatus {
    TODO, IN_PROGRESS, COMPLETED, CANCELLED
}
```

Create `entity/TaskPriority.java`:

```java
package com.example.taskapi.entity;

public enum TaskPriority {
    LOW, MEDIUM, HIGH, URGENT
}
```

### Step 1.2: Add JPQL Queries to Repository

Update `repository/TaskRepository.java`:

```java
package com.example.taskapi.repository;

import com.example.taskapi.entity.Task;
import com.example.taskapi.entity.TaskPriority;
import com.example.taskapi.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    // Derived query methods (from Lab 4)
    List<Task> findByStatus(TaskStatus status);
    List<Task> findByAssignee(String assignee);

    // JPQL Query - Find tasks by status and priority
    @Query("SELECT t FROM Task t WHERE t.status = :status AND t.priority = :priority")
    List<Task> findByStatusAndPriority(
            @Param("status") TaskStatus status,
            @Param("priority") TaskPriority priority);

    // JPQL Query - Find overdue tasks
    @Query("SELECT t FROM Task t WHERE t.dueDate < :today AND t.status != 'COMPLETED'")
    List<Task> findOverdueTasks(@Param("today") LocalDate today);

    // JPQL Query - Find tasks with pagination and sorting
    @Query("SELECT t FROM Task t WHERE t.assignee = :assignee")
    Page<Task> findByAssigneePaged(@Param("assignee") String assignee, Pageable pageable);

    // JPQL Query - Count tasks by status
    @Query("SELECT t.status, COUNT(t) FROM Task t GROUP BY t.status")
    List<Object[]> countTasksByStatus();

    // JPQL Query - Search in title and description
    @Query("SELECT t FROM Task t WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Task> searchByKeyword(@Param("keyword") String keyword);

    // JPQL Query - Find high priority unassigned tasks
    @Query("SELECT t FROM Task t WHERE t.priority IN :priorities AND t.assignee IS NULL")
    List<Task> findUnassignedByPriorities(@Param("priorities") List<TaskPriority> priorities);

    // JPQL Update Query - Bulk update status
    @Modifying
    @Query("UPDATE Task t SET t.status = :newStatus WHERE t.status = :oldStatus")
    int bulkUpdateStatus(
            @Param("oldStatus") TaskStatus oldStatus,
            @Param("newStatus") TaskStatus newStatus);

    // JPQL Delete Query - Delete completed tasks older than date
    @Modifying
    @Query("DELETE FROM Task t WHERE t.status = 'COMPLETED' AND t.updatedAt < :beforeDate")
    int deleteOldCompletedTasks(@Param("beforeDate") java.time.LocalDateTime beforeDate);
}
```

### Step 1.3: Test JPQL Queries

Create `service/TaskQueryService.java`:

```java
package com.example.taskapi.service;

import com.example.taskapi.entity.Task;
import com.example.taskapi.entity.TaskPriority;
import com.example.taskapi.entity.TaskStatus;
import com.example.taskapi.repository.TaskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class TaskQueryService {

    private final TaskRepository taskRepository;

    public TaskQueryService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> findHighPriorityTodo() {
        return taskRepository.findByStatusAndPriority(TaskStatus.TODO, TaskPriority.HIGH);
    }

    public List<Task> findOverdueTasks() {
        return taskRepository.findOverdueTasks(LocalDate.now());
    }

    public Page<Task> findTasksByAssignee(String assignee, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("dueDate").ascending());
        return taskRepository.findByAssigneePaged(assignee, pageRequest);
    }

    public Map<TaskStatus, Long> getTaskCountsByStatus() {
        Map<TaskStatus, Long> counts = new HashMap<>();
        List<Object[]> results = taskRepository.countTasksByStatus();
        for (Object[] result : results) {
            counts.put((TaskStatus) result[0], (Long) result[1]);
        }
        return counts;
    }

    public List<Task> searchTasks(String keyword) {
        return taskRepository.searchByKeyword(keyword);
    }

    public List<Task> findUrgentUnassignedTasks() {
        return taskRepository.findUnassignedByPriorities(
                List.of(TaskPriority.HIGH, TaskPriority.URGENT));
    }
}
```

---

## Part 2: Native SQL Queries

### Step 2.1: Add Native Queries

Add to `TaskRepository.java`:

```java
    // Native SQL Query - Complex aggregation
    @Query(value = """
            SELECT
                assignee,
                COUNT(*) as total_tasks,
                SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) as completed,
                SUM(CASE WHEN status = 'IN_PROGRESS' THEN 1 ELSE 0 END) as in_progress
            FROM tasks
            WHERE assignee IS NOT NULL
            GROUP BY assignee
            ORDER BY total_tasks DESC
            """, nativeQuery = true)
    List<Object[]> getAssigneeStatistics();

    // Native SQL Query with pagination
    @Query(value = "SELECT * FROM tasks WHERE status = :status",
           countQuery = "SELECT COUNT(*) FROM tasks WHERE status = :status",
           nativeQuery = true)
    Page<Task> findByStatusNative(@Param("status") String status, Pageable pageable);

    // Native SQL Query - Database-specific functions
    @Query(value = "SELECT * FROM tasks WHERE created_at >= CURRENT_DATE - :days",
           nativeQuery = true)
    List<Task> findTasksCreatedInLastDays(@Param("days") int days);
```

### Step 2.2: Create DTO for Statistics

Create `dto/AssigneeStats.java`:

```java
package com.example.taskapi.dto;

public record AssigneeStats(
    String assignee,
    long totalTasks,
    long completed,
    long inProgress
) {
    public static AssigneeStats fromObjectArray(Object[] row) {
        return new AssigneeStats(
            (String) row[0],
            ((Number) row[1]).longValue(),
            ((Number) row[2]).longValue(),
            ((Number) row[3]).longValue()
        );
    }

    public double getCompletionRate() {
        return totalTasks > 0 ? (double) completed / totalTasks * 100 : 0;
    }
}
```

---

## Part 3: Dynamic Queries with Specifications

### Step 3.1: Create Task Specifications

Create `repository/TaskSpecifications.java`:

```java
package com.example.taskapi.repository;

import com.example.taskapi.entity.Task;
import com.example.taskapi.entity.TaskPriority;
import com.example.taskapi.entity.TaskStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class TaskSpecifications {

    public static Specification<Task> hasStatus(TaskStatus status) {
        return (root, query, cb) -> {
            if (status == null) return cb.conjunction();
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<Task> hasPriority(TaskPriority priority) {
        return (root, query, cb) -> {
            if (priority == null) return cb.conjunction();
            return cb.equal(root.get("priority"), priority);
        };
    }

    public static Specification<Task> hasAssignee(String assignee) {
        return (root, query, cb) -> {
            if (assignee == null || assignee.isBlank()) return cb.conjunction();
            return cb.equal(root.get("assignee"), assignee);
        };
    }

    public static Specification<Task> isUnassigned() {
        return (root, query, cb) -> cb.isNull(root.get("assignee"));
    }

    public static Specification<Task> dueBefore(LocalDate date) {
        return (root, query, cb) -> {
            if (date == null) return cb.conjunction();
            return cb.lessThan(root.get("dueDate"), date);
        };
    }

    public static Specification<Task> dueAfter(LocalDate date) {
        return (root, query, cb) -> {
            if (date == null) return cb.conjunction();
            return cb.greaterThan(root.get("dueDate"), date);
        };
    }

    public static Specification<Task> titleContains(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return cb.conjunction();
            return cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%");
        };
    }

    public static Specification<Task> isOverdue() {
        return (root, query, cb) -> cb.and(
            cb.lessThan(root.get("dueDate"), LocalDate.now()),
            cb.notEqual(root.get("status"), TaskStatus.COMPLETED)
        );
    }

    public static Specification<Task> priorityIn(TaskPriority... priorities) {
        return (root, query, cb) -> root.get("priority").in((Object[]) priorities);
    }
}
```

### Step 3.2: Create Search Criteria DTO

Create `dto/TaskSearchCriteria.java`:

```java
package com.example.taskapi.dto;

import com.example.taskapi.entity.TaskPriority;
import com.example.taskapi.entity.TaskStatus;

import java.time.LocalDate;

public record TaskSearchCriteria(
    String keyword,
    TaskStatus status,
    TaskPriority priority,
    String assignee,
    LocalDate dueBefore,
    LocalDate dueAfter,
    Boolean overdueOnly
) {
    public static TaskSearchCriteria empty() {
        return new TaskSearchCriteria(null, null, null, null, null, null, null);
    }
}
```

### Step 3.3: Dynamic Query Service

Create `service/TaskSearchService.java`:

```java
package com.example.taskapi.service;

import com.example.taskapi.dto.TaskSearchCriteria;
import com.example.taskapi.entity.Task;
import com.example.taskapi.repository.TaskRepository;
import com.example.taskapi.repository.TaskSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TaskSearchService {

    private final TaskRepository taskRepository;

    public TaskSearchService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Page<Task> search(TaskSearchCriteria criteria, Pageable pageable) {
        Specification<Task> spec = Specification.where(null);

        if (criteria.keyword() != null) {
            spec = spec.and(TaskSpecifications.titleContains(criteria.keyword()));
        }

        if (criteria.status() != null) {
            spec = spec.and(TaskSpecifications.hasStatus(criteria.status()));
        }

        if (criteria.priority() != null) {
            spec = spec.and(TaskSpecifications.hasPriority(criteria.priority()));
        }

        if (criteria.assignee() != null) {
            spec = spec.and(TaskSpecifications.hasAssignee(criteria.assignee()));
        }

        if (criteria.dueBefore() != null) {
            spec = spec.and(TaskSpecifications.dueBefore(criteria.dueBefore()));
        }

        if (criteria.dueAfter() != null) {
            spec = spec.and(TaskSpecifications.dueAfter(criteria.dueAfter()));
        }

        if (Boolean.TRUE.equals(criteria.overdueOnly())) {
            spec = spec.and(TaskSpecifications.isOverdue());
        }

        return taskRepository.findAll(spec, pageable);
    }
}
```

### Step 3.4: Search Controller

Create `controller/TaskSearchController.java`:

```java
package com.example.taskapi.controller;

import com.example.taskapi.dto.TaskSearchCriteria;
import com.example.taskapi.entity.Task;
import com.example.taskapi.entity.TaskPriority;
import com.example.taskapi.entity.TaskStatus;
import com.example.taskapi.service.TaskSearchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/tasks/search")
public class TaskSearchController {

    private final TaskSearchService searchService;

    public TaskSearchController(TaskSearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public Page<Task> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) String assignee,
            @RequestParam(required = false) LocalDate dueBefore,
            @RequestParam(required = false) LocalDate dueAfter,
            @RequestParam(required = false) Boolean overdueOnly,
            Pageable pageable) {

        TaskSearchCriteria criteria = new TaskSearchCriteria(
            keyword, status, priority, assignee, dueBefore, dueAfter, overdueOnly
        );

        return searchService.search(criteria, pageable);
    }
}
```

---

## Part 4: Transactions

### Step 4.1: Understand @Transactional

Create `service/TaskTransactionService.java`:

```java
package com.example.taskapi.service;

import com.example.taskapi.entity.Task;
import com.example.taskapi.entity.TaskStatus;
import com.example.taskapi.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaskTransactionService {

    private static final Logger log = LoggerFactory.getLogger(TaskTransactionService.class);
    private final TaskRepository taskRepository;
    private final AuditService auditService;

    public TaskTransactionService(TaskRepository taskRepository, AuditService auditService) {
        this.taskRepository = taskRepository;
        this.auditService = auditService;
    }

    // Basic transaction - rolls back on any RuntimeException
    @Transactional
    public Task createTask(String title, String description) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        return taskRepository.save(task);
    }

    // Read-only transaction - optimized for reads
    @Transactional(readOnly = true)
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    // Custom rollback rules
    @Transactional(rollbackFor = Exception.class,  // Rollback for checked exceptions too
                   noRollbackFor = IllegalArgumentException.class)  // Don't rollback for this
    public Task updateTaskWithRules(Long id, String newTitle) throws Exception {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        if (newTitle == null || newTitle.isBlank()) {
            throw new Exception("Title cannot be empty");  // This WILL rollback
        }

        task.setTitle(newTitle);
        return taskRepository.save(task);
    }

    // Transaction with timeout
    @Transactional(timeout = 5)  // 5 seconds timeout
    public void longRunningOperation() {
        // If this takes more than 5 seconds, transaction will be rolled back
        taskRepository.findAll().forEach(task -> {
            // Process each task
            task.setDescription(task.getDescription() + " - processed");
            taskRepository.save(task);
        });
    }

    // Transaction isolation levels
    @Transactional(isolation = Isolation.SERIALIZABLE)  // Highest isolation
    public void criticalOperation(Long taskId, TaskStatus newStatus) {
        Task task = taskRepository.findById(taskId).orElseThrow();
        task.setStatus(newStatus);
        taskRepository.save(task);
    }

    // Propagation REQUIRED (default) - joins existing or creates new
    @Transactional(propagation = Propagation.REQUIRED)
    public void completeTaskWithAudit(Long taskId) {
        Task task = taskRepository.findById(taskId).orElseThrow();
        task.setStatus(TaskStatus.COMPLETED);
        taskRepository.save(task);

        // This runs in SAME transaction
        auditService.logAction("TASK_COMPLETED", taskId);
    }

    // Propagation REQUIRES_NEW - always creates new transaction
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void independentOperation(Long taskId) {
        // This runs in its OWN transaction, regardless of caller
        Task task = taskRepository.findById(taskId).orElseThrow();
        task.setDescription("Updated independently");
        taskRepository.save(task);
    }

    // Demonstrating transaction boundary
    @Transactional
    public void batchOperation(List<Long> taskIds, TaskStatus newStatus) {
        log.info("Starting batch operation in transaction");

        for (Long id : taskIds) {
            Task task = taskRepository.findById(id).orElseThrow();
            task.setStatus(newStatus);
            taskRepository.save(task);
            log.info("Updated task {}", id);

            // If ANY task fails, ALL changes are rolled back
            if (task.getTitle().contains("FAIL")) {
                throw new RuntimeException("Simulated failure for task " + id);
            }
        }

        log.info("Batch operation completed successfully");
    }
}
```

### Step 4.2: Create Audit Service

Create `service/AuditService.java`:

```java
package com.example.taskapi.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    // Runs in same transaction as caller
    @Transactional(propagation = Propagation.REQUIRED)
    public void logAction(String action, Long entityId) {
        log.info("AUDIT [{}]: {} on entity {} at {}",
            Thread.currentThread().getName(),
            action, entityId, LocalDateTime.now());
        // In real app: save to audit table
    }

    // Always runs in NEW transaction - won't be rolled back with caller
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logActionIndependent(String action, Long entityId) {
        log.info("INDEPENDENT AUDIT: {} on entity {}", action, entityId);
        // This log is saved even if caller transaction fails
    }
}
```

---

## Part 5: Optimistic Locking

### Step 5.1: Add Version Field

Update `Task.java` to add optimistic locking:

```java
@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version  // Optimistic locking
    private Long version;

    // ... rest of fields

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
```

### Step 5.2: Handle Optimistic Lock Exception

Create `service/TaskOptimisticLockService.java`:

```java
package com.example.taskapi.service;

import com.example.taskapi.entity.Task;
import com.example.taskapi.repository.TaskRepository;
import jakarta.persistence.OptimisticLockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskOptimisticLockService {

    private static final Logger log = LoggerFactory.getLogger(TaskOptimisticLockService.class);
    private static final int MAX_RETRIES = 3;

    private final TaskRepository taskRepository;

    public TaskOptimisticLockService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional
    public Task updateWithRetry(Long taskId, String newTitle) {
        int attempts = 0;

        while (attempts < MAX_RETRIES) {
            try {
                attempts++;
                log.info("Attempt {} to update task {}", attempts, taskId);

                Task task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new RuntimeException("Task not found"));

                task.setTitle(newTitle);
                return taskRepository.save(task);

            } catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
                log.warn("Optimistic lock conflict on attempt {}, retrying...", attempts);

                if (attempts >= MAX_RETRIES) {
                    throw new RuntimeException(
                        "Failed to update task after " + MAX_RETRIES + " attempts due to concurrent modifications", e);
                }

                // Small delay before retry
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        throw new RuntimeException("Unexpected: loop exited without return or throw");
    }
}
```

---

## Part 6: JPA Auditing

### Step 6.1: Enable JPA Auditing

Create `config/JpaAuditingConfig.java`:

```java
package com.example.taskapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        // In a real app, get from SecurityContext
        return () -> Optional.of("system");
    }
}
```

### Step 6.2: Create Auditable Base Entity

Create `entity/AuditableEntity.java`:

```java
package com.example.taskapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity {

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;

    // Getters
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getCreatedBy() { return createdBy; }
    public String getUpdatedBy() { return updatedBy; }
}
```

### Step 6.3: Update Task to Use Auditing

Update `Task.java`:

```java
@Entity
@Table(name = "tasks")
public class Task extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(nullable = false)
    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    private TaskStatus status = TaskStatus.TODO;

    @Enumerated(EnumType.STRING)
    private TaskPriority priority = TaskPriority.MEDIUM;

    private String assignee;

    private LocalDate dueDate;

    // Remove @PrePersist and @PreUpdate - handled by AuditingEntityListener now

    // Getters and Setters...
}
```

---

## Part 7: Testing the Features

### Step 7.1: Test Dynamic Search

```bash
# Search by keyword
curl "http://localhost:8080/api/tasks/search?keyword=important"

# Search by status and priority
curl "http://localhost:8080/api/tasks/search?status=TODO&priority=HIGH"

# Search overdue tasks
curl "http://localhost:8080/api/tasks/search?overdueOnly=true"

# Combined search with pagination
curl "http://localhost:8080/api/tasks/search?assignee=john&status=IN_PROGRESS&page=0&size=10&sort=dueDate,asc"
```

### Step 7.2: Verify Auditing

```bash
# Create a task
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title": "Audited Task"}'

# Response will include:
# {
#   "id": 1,
#   "title": "Audited Task",
#   "createdAt": "2024-01-15T10:30:00",
#   "updatedAt": "2024-01-15T10:30:00",
#   "createdBy": "system",
#   "updatedBy": "system",
#   "version": 0
# }
```

---

## Summary

In this lab, you learned:

1. **JPQL Queries**: Custom queries with @Query annotation
2. **Native SQL**: Database-specific queries when needed
3. **Specifications**: Dynamic, type-safe query building
4. **Transactions**: @Transactional options, propagation, isolation
5. **Optimistic Locking**: @Version for concurrent access control
6. **JPA Auditing**: Automatic tracking of created/modified timestamps and users

## Query Types Comparison

| Type | Use When | Pros | Cons |
|------|----------|------|------|
| Derived | Simple queries | Auto-generated, type-safe | Limited complexity |
| JPQL | Complex joins, aggregations | Database agnostic | String-based |
| Native SQL | DB-specific features | Full SQL power | Not portable |
| Specifications | Dynamic filters | Composable, type-safe | More code |

## Next Steps

Continue to Lab 5a to learn about HATEOAS and hypermedia-driven APIs.
