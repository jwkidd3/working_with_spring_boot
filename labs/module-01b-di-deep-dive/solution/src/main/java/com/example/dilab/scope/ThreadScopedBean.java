package com.example.dilab.scope;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Thread scope - one instance per thread.
 */
@Component
@Scope("thread")
public class ThreadScopedBean {

    private final String threadId;

    public ThreadScopedBean() {
        this.threadId = "Thread-" + Thread.currentThread().getName() +
                        "-" + System.identityHashCode(this);
        System.out.println("ThreadScopedBean created: " + threadId);
    }

    public String getThreadId() {
        return threadId;
    }
}
