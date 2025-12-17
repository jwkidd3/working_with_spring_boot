package com.example.batchdemo.controller;

import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobLauncher jobLauncher;
    private final Job importProductJob;
    private final JobExplorer jobExplorer;

    public JobController(JobLauncher jobLauncher,
                         Job importProductJob,
                         JobExplorer jobExplorer) {
        this.jobLauncher = jobLauncher;
        this.importProductJob = importProductJob;
        this.jobExplorer = jobExplorer;
    }

    /**
     * Start the import job
     */
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> startImportJob() {
        try {
            JobParameters params = new JobParametersBuilder()
                .addLong("startTime", System.currentTimeMillis())
                .toJobParameters();

            JobExecution execution = jobLauncher.run(importProductJob, params);

            Map<String, Object> response = new HashMap<>();
            response.put("jobId", execution.getJobId());
            response.put("status", execution.getStatus().toString());
            response.put("startTime", execution.getStartTime());

            return ResponseEntity.ok(response);

        } catch (JobExecutionAlreadyRunningException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Job is already running"
            ));
        } catch (JobRestartException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Job cannot be restarted"
            ));
        } catch (JobInstanceAlreadyCompleteException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Job instance already completed"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Get job execution status
     */
    @GetMapping("/status/{executionId}")
    public ResponseEntity<Map<String, Object>> getJobStatus(
            @PathVariable Long executionId) {

        JobExecution execution = jobExplorer.getJobExecution(executionId);

        if (execution == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("jobId", execution.getJobId());
        response.put("executionId", execution.getId());
        response.put("status", execution.getStatus().toString());
        response.put("startTime", execution.getStartTime());
        response.put("endTime", execution.getEndTime());
        response.put("exitStatus", execution.getExitStatus().getExitCode());

        // Add step execution details
        execution.getStepExecutions().forEach(step -> {
            Map<String, Object> stepInfo = new HashMap<>();
            stepInfo.put("stepName", step.getStepName());
            stepInfo.put("readCount", step.getReadCount());
            stepInfo.put("writeCount", step.getWriteCount());
            stepInfo.put("skipCount", step.getSkipCount());
            stepInfo.put("status", step.getStatus().toString());
            response.put("step_" + step.getStepName(), stepInfo);
        });

        return ResponseEntity.ok(response);
    }

    /**
     * Get all job executions
     */
    @GetMapping("/executions")
    public ResponseEntity<List<Map<String, Object>>> getAllExecutions() {
        List<JobInstance> instances = jobExplorer.getJobInstances(
            "importProductJob", 0, 10);

        List<Map<String, Object>> executions = instances.stream()
            .flatMap(instance -> jobExplorer.getJobExecutions(instance).stream())
            .map(exec -> {
                Map<String, Object> map = new HashMap<>();
                map.put("executionId", exec.getId());
                map.put("status", exec.getStatus().toString());
                map.put("startTime", exec.getStartTime());
                map.put("endTime", exec.getEndTime());
                return map;
            })
            .toList();

        return ResponseEntity.ok(executions);
    }
}
