package com.example.dilab.lifecycle;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class demonstrating @Bean with initMethod and destroyMethod.
 */
@Configuration
public class LifecycleConfig {

    @Bean(initMethod = "start", destroyMethod = "stop")
    public ExternalService externalService() {
        return new ExternalService();
    }
}
