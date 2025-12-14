package com.example.springfundamentals.controller;

import com.example.springfundamentals.config.AppConfig;
import com.example.springfundamentals.service.CacheService;
import com.example.springfundamentals.service.GreetingApplicationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.LocalDateTime;

@RestController
public class GreetingController {

    private final GreetingApplicationService applicationService;
    private final CacheService cacheService;
    private final Clock clock;
    private final AppConfig.AppInfo appInfo;

    public GreetingController(GreetingApplicationService applicationService,
                              CacheService cacheService,
                              Clock clock,
                              AppConfig.AppInfo appInfo) {
        this.applicationService = applicationService;
        this.cacheService = cacheService;
        this.clock = clock;
        this.appInfo = appInfo;
    }

    @GetMapping("/greet")
    public String greet(@RequestParam(defaultValue = "World") String name) {
        String cacheKey = "greeting_" + name;
        if (cacheService.contains(cacheKey)) {
            return "CACHED: " + cacheService.get(cacheKey);
        }

        String greeting = applicationService.getFormattedGreeting(name);
        cacheService.put(cacheKey, greeting);
        return greeting;
    }

    @GetMapping("/info")
    public String info() {
        return String.format("%s%nService: %s%nCache entries: %d%nServer time: %s",
                appInfo,
                applicationService.getServiceInfo(),
                cacheService.size(),
                LocalDateTime.now(clock));
    }

    @GetMapping("/cache/stats")
    public String cacheStats() {
        return "Cache contains " + cacheService.size() + " entries";
    }
}
