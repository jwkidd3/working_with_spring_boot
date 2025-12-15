package com.example.taskapi.specification;

import com.example.taskapi.entity.Task;
import com.example.taskapi.entity.TaskPriority;
import com.example.taskapi.entity.TaskStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;

/**
 * JPA Specifications for building dynamic Task queries.
 */
public class TaskSpecifications {

    public static Specification<Task> hasStatus(TaskStatus status) {
        return (root, query, cb) ->
            status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Task> hasPriority(TaskPriority priority) {
        return (root, query, cb) ->
            priority == null ? null : cb.equal(root.get("priority"), priority);
    }

    public static Specification<Task> priorityIn(List<TaskPriority> priorities) {
        return (root, query, cb) ->
            priorities == null || priorities.isEmpty() ? null : root.get("priority").in(priorities);
    }

    public static Specification<Task> titleContains(String keyword) {
        return (root, query, cb) ->
            keyword == null || keyword.isBlank() ? null :
            cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%");
    }

    public static Specification<Task> descriptionContains(String keyword) {
        return (root, query, cb) ->
            keyword == null || keyword.isBlank() ? null :
            cb.like(cb.lower(root.get("description")), "%" + keyword.toLowerCase() + "%");
    }

    public static Specification<Task> titleOrDescriptionContains(String keyword) {
        return Specification.where(titleContains(keyword))
                           .or(descriptionContains(keyword));
    }

    public static Specification<Task> assignedTo(String assignee) {
        return (root, query, cb) ->
            assignee == null || assignee.isBlank() ? null :
            cb.equal(root.get("assignee"), assignee);
    }

    public static Specification<Task> dueDateBefore(LocalDate date) {
        return (root, query, cb) ->
            date == null ? null : cb.lessThan(root.get("dueDate"), date);
    }

    public static Specification<Task> dueDateAfter(LocalDate date) {
        return (root, query, cb) ->
            date == null ? null : cb.greaterThan(root.get("dueDate"), date);
    }

    public static Specification<Task> dueDateBetween(LocalDate start, LocalDate end) {
        return (root, query, cb) -> {
            if (start == null && end == null) return null;
            if (start == null) return cb.lessThanOrEqualTo(root.get("dueDate"), end);
            if (end == null) return cb.greaterThanOrEqualTo(root.get("dueDate"), start);
            return cb.between(root.get("dueDate"), start, end);
        };
    }

    public static Specification<Task> isOverdue() {
        return (root, query, cb) ->
            cb.and(
                cb.lessThan(root.get("dueDate"), LocalDate.now()),
                cb.notEqual(root.get("status"), TaskStatus.COMPLETED),
                cb.notEqual(root.get("status"), TaskStatus.CANCELLED)
            );
    }

    public static Specification<Task> isNotCompleted() {
        return (root, query, cb) ->
            cb.and(
                cb.notEqual(root.get("status"), TaskStatus.COMPLETED),
                cb.notEqual(root.get("status"), TaskStatus.CANCELLED)
            );
    }
}
