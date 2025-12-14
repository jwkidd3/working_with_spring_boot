package com.example.dilab.scope;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

/**
 * Request scope - new instance for each HTTP request.
 * Requires proxyMode for injection into singleton beans.
 */
@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RequestScopedBean {

    private final String requestId;
    private final LocalDateTime createdAt;

    public RequestScopedBean() {
        this.requestId = "Request-" + System.identityHashCode(this);
        this.createdAt = LocalDateTime.now();
        System.out.println("RequestScopedBean created: " + requestId);
    }

    public String getRequestId() {
        return requestId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
