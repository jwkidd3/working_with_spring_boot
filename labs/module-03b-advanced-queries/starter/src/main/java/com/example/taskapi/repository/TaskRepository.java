package com.example.taskapi.repository;

import com.example.taskapi.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// TODO: Extend JpaSpecificationExecutor for dynamic queries
// TODO: Add custom JPQL queries with @Query annotation
// TODO: Add native SQL query example

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

}
