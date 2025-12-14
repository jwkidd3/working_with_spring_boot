package com.example.dilab;

import com.example.dilab.injection.ConstructorInjectionExample;
import com.example.dilab.qualifier.NotificationManager;
import com.example.dilab.scope.SingletonCounter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DiLabApplicationTests {

    @Autowired
    private ConstructorInjectionExample constructorExample;

    @Autowired
    private NotificationManager notificationManager;

    @Autowired
    private SingletonCounter singleton1;

    @Autowired
    private SingletonCounter singleton2;

    @Test
    void contextLoads() {
    }

    @Test
    void constructorInjectionWorks() {
        assertNotNull(constructorExample);
        assertEquals("Hello from MessageService!", constructorExample.getMessage());
    }

    @Test
    void notificationManagerHasAllServices() {
        assertNotNull(notificationManager);
    }

    @Test
    void singletonScopeReturnsSameInstance() {
        assertSame(singleton1, singleton2);
    }
}
