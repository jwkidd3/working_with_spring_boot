package com.example.batchdemo.listener;

import com.example.batchdemo.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class JobCompletionListener implements JobExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(JobCompletionListener.class);

    private final ProductRepository productRepository;

    public JobCompletionListener(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("========================================");
        log.info("JOB STARTING: {}", jobExecution.getJobInstance().getJobName());
        log.info("Job Parameters: {}", jobExecution.getJobParameters());
        log.info("Start Time: {}", jobExecution.getStartTime());
        log.info("========================================");
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        log.info("========================================");
        log.info("JOB FINISHED: {}", jobExecution.getJobInstance().getJobName());
        log.info("Status: {}", jobExecution.getStatus());
        log.info("End Time: {}", jobExecution.getEndTime());

        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            long count = productRepository.count();
            log.info("SUCCESS! {} products imported to database", count);

            // Log all imported products
            productRepository.findAll().forEach(product ->
                log.info("  - {} : {} (${})",
                    product.getProductId(),
                    product.getName(),
                    product.getPrice())
            );
        } else if (jobExecution.getStatus() == BatchStatus.FAILED) {
            log.error("JOB FAILED!");
            jobExecution.getAllFailureExceptions().forEach(ex ->
                log.error("Exception: {}", ex.getMessage())
            );
        }

        log.info("========================================");
    }
}
