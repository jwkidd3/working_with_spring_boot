package com.example.springfundamentals.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class AppConfig {

    @Value("${app.timezone:UTC}")
    private String timezone;

    @Bean
    public Clock applicationClock() {
        System.out.println("Creating Clock bean with timezone: " + timezone);
        return Clock.system(ZoneId.of(timezone));
    }

    @Bean
    public AppInfo appInfo(@Value("${spring.application.name:unknown}") String appName,
                           @Value("${app.version:1.0.0}") String version) {
        return new AppInfo(appName, version);
    }

    public static class AppInfo {
        private final String name;
        private final String version;

        public AppInfo(String name, String version) {
            this.name = name;
            this.version = version;
        }

        public String getName() {
            return name;
        }

        public String getVersion() {
            return version;
        }

        @Override
        public String toString() {
            return name + " v" + version;
        }
    }
}
