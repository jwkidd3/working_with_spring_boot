# Lab 8g: Batch Processing with Spring Batch

## Objectives

By the end of this lab, you will be able to:
- Understand Spring Batch architecture and concepts
- Configure batch jobs with readers, processors, and writers
- Implement chunk-oriented processing
- Handle job parameters and execution context
- Monitor batch job execution
- Implement error handling and skip logic

## Prerequisites

- Completed Labs 1-4
- Understanding of Spring Boot basics
- Familiarity with databases and JPA

## Duration

60-75 minutes

---

## Scenario

You need to build a batch processing system that imports product data from CSV files into a database. The system should:
- Read products from a CSV file
- Validate and transform the data
- Write valid products to the database
- Skip invalid records and log errors
- Track job execution progress

---

## Part 1: Understanding Spring Batch

### Key Concepts

**Job**: A batch process that runs from start to finish. Contains one or more steps.

**Step**: A phase of a job. Contains a reader, processor, and writer.

**ItemReader**: Reads data from a source (file, database, etc.)

**ItemProcessor**: Transforms or validates data

**ItemWriter**: Writes data to a destination

**Chunk Processing**: Process data in chunks (e.g., 10 records at a time) for efficiency and transaction management.

```
┌─────────────────────────────────────────────────────┐
│                       JOB                           │
│  ┌───────────────────────────────────────────────┐  │
│  │                    STEP                       │  │
│  │  ┌─────────┐  ┌───────────┐  ┌────────────┐  │  │
│  │  │ Reader  │→ │ Processor │→ │   Writer   │  │  │
│  │  └─────────┘  └───────────┘  └────────────┘  │  │
│  │         ↑                           │        │  │
│  │         └───────── chunk ───────────┘        │  │
│  └───────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────┘
```

---

## Part 2: Project Setup

### Step 2.1: Create the Project

Create a new Spring Boot project or use the starter provided.

**pom.xml:**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.4.1</version>
        <relativeTo/>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>batch-demo</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>batch-demo</name>
    <description>Spring Batch Demo</description>

    <properties>
        <java.version>21</java.version>
    </properties>

    <dependencies>
        <!-- Spring Batch -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-batch</artifactId>
        </dependency>

        <!-- Web for REST endpoints to trigger jobs -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- JPA for database -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <!-- HSQLDB for in-memory database -->
        <dependency>
            <groupId>org.hsqldb</groupId>
            <artifactId>hsqldb</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Actuator for monitoring -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>org.springframework.batch</groupId>
            <artifactId>spring-batch-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

### Step 2.2: Configure Application Properties

Create `src/main/resources/application.yml`:

```yaml
spring:
  application:
    name: batch-demo

  datasource:
    url: jdbc:hsqldb:mem:batchdb
    driver-class-name: org.hsqldb.jdbc.JDBCDriver
    username: sa
    password:

  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true

  batch:
    jdbc:
      initialize-schema: always
    job:
      enabled: false  # Don't run jobs on startup

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,batch
```

### Step 2.3: Create Sample Data File

Create `src/main/resources/products.csv`:

```csv
productId,name,description,price,quantity
P001,Laptop,High-performance laptop,999.99,50
P002,Mouse,Wireless mouse,29.99,200
P003,Keyboard,Mechanical keyboard,79.99,150
P004,Monitor,27-inch 4K monitor,399.99,75
P005,Headphones,Noise-canceling headphones,199.99,100
P006,Webcam,HD webcam,89.99,120
P007,USB Hub,7-port USB hub,39.99,300
P008,SSD,1TB SSD drive,109.99,80
P009,RAM,16GB DDR5 RAM,89.99,90
P010,GPU,Graphics card,599.99,25
INVALID,Bad Product,Missing price,,10
P011,Tablet,10-inch tablet,449.99,60
P012,Charger,Fast charger,24.99,500
```

---

## Part 3: Create the Domain Model

### Step 3.1: Create Product Entity

Create `src/main/java/com/example/batchdemo/entity/Product.java`:

```java
package com.example.batchdemo.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String productId;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    private Integer quantity;

    private LocalDateTime importedAt;

    @PrePersist
    public void prePersist() {
        this.importedAt = LocalDateTime.now();
    }

    // Constructors
    public Product() {}

    public Product(String productId, String name, String description,
                   BigDecimal price, Integer quantity) {
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public LocalDateTime getImportedAt() { return importedAt; }
    public void setImportedAt(LocalDateTime importedAt) { this.importedAt = importedAt; }

    @Override
    public String toString() {
        return "Product{productId='" + productId + "', name='" + name +
               "', price=" + price + ", quantity=" + quantity + "}";
    }
}
```

### Step 3.2: Create ProductInput DTO

Create `src/main/java/com/example/batchdemo/entity/ProductInput.java`:

```java
package com.example.batchdemo.entity;

/**
 * DTO for reading raw CSV data before transformation
 */
public class ProductInput {

    private String productId;
    private String name;
    private String description;
    private String price;
    private String quantity;

    // Getters and Setters
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }

    @Override
    public String toString() {
        return "ProductInput{productId='" + productId + "', name='" + name + "'}";
    }
}
```

### Step 3.3: Create Product Repository

Create `src/main/java/com/example/batchdemo/repository/ProductRepository.java`:

```java
package com.example.batchdemo.repository;

import com.example.batchdemo.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByProductId(String productId);

    boolean existsByProductId(String productId);
}
```

---

## Part 4: Create the Item Processor

### Step 4.1: Create Product Processor

Create `src/main/java/com/example/batchdemo/processor/ProductProcessor.java`:

```java
package com.example.batchdemo.processor;

import com.example.batchdemo.entity.Product;
import com.example.batchdemo.entity.ProductInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ProductProcessor implements ItemProcessor<ProductInput, Product> {

    private static final Logger log = LoggerFactory.getLogger(ProductProcessor.class);

    @Override
    public Product process(ProductInput input) throws Exception {
        log.debug("Processing product: {}", input.getProductId());

        // Validate required fields
        if (input.getProductId() == null || input.getProductId().isBlank()) {
            log.warn("Skipping record with missing productId");
            return null; // Returning null skips this record
        }

        if (input.getName() == null || input.getName().isBlank()) {
            log.warn("Skipping record {} with missing name", input.getProductId());
            return null;
        }

        // Parse and validate price
        BigDecimal price;
        try {
            if (input.getPrice() == null || input.getPrice().isBlank()) {
                log.warn("Skipping record {} with missing price", input.getProductId());
                return null;
            }
            price = new BigDecimal(input.getPrice());
            if (price.compareTo(BigDecimal.ZERO) < 0) {
                log.warn("Skipping record {} with negative price", input.getProductId());
                return null;
            }
        } catch (NumberFormatException e) {
            log.warn("Skipping record {} with invalid price: {}",
                     input.getProductId(), input.getPrice());
            return null;
        }

        // Parse quantity
        Integer quantity = 0;
        try {
            if (input.getQuantity() != null && !input.getQuantity().isBlank()) {
                quantity = Integer.parseInt(input.getQuantity());
            }
        } catch (NumberFormatException e) {
            log.warn("Invalid quantity for {}, defaulting to 0", input.getProductId());
        }

        // Transform: uppercase name, trim description
        Product product = new Product();
        product.setProductId(input.getProductId().trim());
        product.setName(input.getName().trim().toUpperCase());
        product.setDescription(input.getDescription() != null ?
                               input.getDescription().trim() : "");
        product.setPrice(price);
        product.setQuantity(quantity);

        log.info("Processed product: {}", product.getProductId());
        return product;
    }
}
```

---

## Part 5: Create Job Listener

### Step 5.1: Create Job Completion Listener

Create `src/main/java/com/example/batchdemo/listener/JobCompletionListener.java`:

```java
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
                log.info("  - {} : {} (${}) ",
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
```

### Step 5.2: Create Skip Listener

Create `src/main/java/com/example/batchdemo/listener/SkipListener.java`:

```java
package com.example.batchdemo.listener;

import com.example.batchdemo.entity.Product;
import com.example.batchdemo.entity.ProductInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.SkipListener;
import org.springframework.stereotype.Component;

@Component
public class ProductSkipListener implements SkipListener<ProductInput, Product> {

    private static final Logger log = LoggerFactory.getLogger(ProductSkipListener.class);

    @Override
    public void onSkipInRead(Throwable t) {
        log.error("Skipped during READ: {}", t.getMessage());
    }

    @Override
    public void onSkipInProcess(ProductInput item, Throwable t) {
        log.error("Skipped during PROCESS - Item: {}, Error: {}", item, t.getMessage());
    }

    @Override
    public void onSkipInWrite(Product item, Throwable t) {
        log.error("Skipped during WRITE - Item: {}, Error: {}", item, t.getMessage());
    }
}
```

---

## Part 6: Configure the Batch Job

### Step 6.1: Create Batch Configuration

Create `src/main/java/com/example/batchdemo/config/BatchConfig.java`:

```java
package com.example.batchdemo.config;

import com.example.batchdemo.entity.Product;
import com.example.batchdemo.entity.ProductInput;
import com.example.batchdemo.listener.JobCompletionListener;
import com.example.batchdemo.listener.ProductSkipListener;
import com.example.batchdemo.processor.ProductProcessor;
import com.example.batchdemo.repository.ProductRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchConfig {

    private final ProductRepository productRepository;
    private final ProductProcessor productProcessor;
    private final JobCompletionListener jobCompletionListener;
    private final ProductSkipListener skipListener;

    public BatchConfig(ProductRepository productRepository,
                       ProductProcessor productProcessor,
                       JobCompletionListener jobCompletionListener,
                       ProductSkipListener skipListener) {
        this.productRepository = productRepository;
        this.productProcessor = productProcessor;
        this.jobCompletionListener = jobCompletionListener;
        this.skipListener = skipListener;
    }

    /**
     * ItemReader - reads from CSV file
     */
    @Bean
    public FlatFileItemReader<ProductInput> reader() {
        return new FlatFileItemReaderBuilder<ProductInput>()
            .name("productReader")
            .resource(new ClassPathResource("products.csv"))
            .linesToSkip(1)  // Skip header row
            .delimited()
            .names("productId", "name", "description", "price", "quantity")
            .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                setTargetType(ProductInput.class);
            }})
            .build();
    }

    /**
     * ItemWriter - writes to database using repository
     */
    @Bean
    public RepositoryItemWriter<Product> writer() {
        return new RepositoryItemWriterBuilder<Product>()
            .repository(productRepository)
            .methodName("save")
            .build();
    }

    /**
     * Step - defines the chunk-oriented processing
     */
    @Bean
    public Step importProductStep(JobRepository jobRepository,
                                   PlatformTransactionManager transactionManager) {
        return new StepBuilder("importProductStep", jobRepository)
            .<ProductInput, Product>chunk(5, transactionManager)  // Process 5 at a time
            .reader(reader())
            .processor(productProcessor)
            .writer(writer())
            .faultTolerant()
            .skipLimit(10)  // Allow up to 10 skipped records
            .skip(FlatFileParseException.class)
            .skip(Exception.class)
            .listener(skipListener)
            .build();
    }

    /**
     * Job - the complete batch job
     */
    @Bean
    public Job importProductJob(JobRepository jobRepository,
                                 Step importProductStep) {
        return new JobBuilder("importProductJob", jobRepository)
            .incrementer(new RunIdIncrementer())
            .listener(jobCompletionListener)
            .start(importProductStep)
            .build();
    }
}
```

---

## Part 7: Create Job Controller

### Step 7.1: Create REST Controller for Job Management

Create `src/main/java/com/example/batchdemo/controller/JobController.java`:

```java
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
```

---

## Part 8: Create Main Application

### Step 8.1: Create Application Class

Create `src/main/java/com/example/batchdemo/BatchDemoApplication.java`:

```java
package com.example.batchdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BatchDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(BatchDemoApplication.class, args);
    }
}
```

---

## Part 9: Testing

### Step 9.1: Run the Application

```bash
./mvnw spring-boot:run
```

### Step 9.2: Start a Batch Job

```bash
# Start the import job
curl -X POST http://localhost:8080/api/jobs/import | jq
```

Expected response:
```json
{
  "jobId": 1,
  "status": "STARTED",
  "startTime": "2024-01-15T10:30:00"
}
```

### Step 9.3: Check Job Status

```bash
# Get job status (replace 1 with actual execution ID)
curl http://localhost:8080/api/jobs/status/1 | jq
```

Expected response:
```json
{
  "jobId": 1,
  "executionId": 1,
  "status": "COMPLETED",
  "startTime": "2024-01-15T10:30:00",
  "endTime": "2024-01-15T10:30:02",
  "exitStatus": "COMPLETED",
  "step_importProductStep": {
    "stepName": "importProductStep",
    "readCount": 13,
    "writeCount": 12,
    "skipCount": 1,
    "status": "COMPLETED"
  }
}
```

### Step 9.4: View All Executions

```bash
curl http://localhost:8080/api/jobs/executions | jq
```

### Step 9.5: Check Application Logs

Look for output like:
```
========================================
JOB STARTING: importProductJob
========================================
Processing product: P001
Processed product: P001
...
Skipping record INVALID with missing price
...
========================================
JOB FINISHED: importProductJob
Status: COMPLETED
SUCCESS! 12 products imported to database
  - P001 : LAPTOP ($999.99)
  - P002 : MOUSE ($29.99)
  ...
========================================
```

---

## Part 10: Challenge Exercises

### Challenge 1: Add a Second Step

Add a step that generates a summary report after import:
- Count products by price range
- Calculate total inventory value
- Write summary to a file

### Challenge 2: Parameterized Input File

Modify the job to accept the input filename as a job parameter:

```bash
curl -X POST "http://localhost:8080/api/jobs/import?file=products-v2.csv"
```

### Challenge 3: Scheduled Job

Add a scheduled job that runs the import every hour:

```java
@Scheduled(cron = "0 0 * * * *")
public void runScheduledImport() {
    // Launch job
}
```

### Challenge 4: Database Reader

Create a second job that:
- Reads products from one database table
- Filters products below a price threshold
- Writes to an archive table

---

## Summary

In this lab, you learned:

1. **Spring Batch Architecture**: Jobs, Steps, Readers, Processors, Writers
2. **Chunk Processing**: Efficient processing of large datasets in chunks
3. **FlatFileItemReader**: Reading from CSV files
4. **ItemProcessor**: Validating and transforming data
5. **RepositoryItemWriter**: Writing to database via JPA
6. **Job Listeners**: Monitoring job execution
7. **Skip Logic**: Handling and logging failed records
8. **REST Integration**: Triggering and monitoring jobs via API

## Key Concepts

- **Chunk size**: Balance between memory usage and transaction overhead
- **Skip logic**: Define which exceptions to skip vs fail the job
- **Job parameters**: Make jobs unique and restartable
- **Listeners**: Add logging, notifications, cleanup logic

## Next Steps

Explore more Spring Batch features:
- **Partitioning**: Parallel processing across multiple threads
- **Remote chunking**: Distribute processing across multiple JVMs
- **Job scheduling**: Integrate with Quartz or Spring Scheduler
- **Retry logic**: Automatically retry failed items
