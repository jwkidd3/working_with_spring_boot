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
