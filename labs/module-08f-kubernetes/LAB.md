# Lab 8f: Spring Cloud Kubernetes

## Objectives
- Understand Spring Cloud Kubernetes and its features
- Configure a Spring Boot application for Kubernetes deployment
- Use Kubernetes ConfigMaps and Secrets for configuration
- Implement service discovery in Kubernetes
- Deploy and test the application in a Kubernetes cluster

## Prerequisites
- Completed previous Spring Cloud labs
- Docker installed
- Kubernetes cluster (minikube, kind, Docker Desktop, or cloud provider)
- kubectl CLI installed
- Java 17 or higher
- Maven 3.6+

## Duration
60-75 minutes

---

## Part 1: Understanding Spring Cloud Kubernetes

### What is Spring Cloud Kubernetes?

Spring Cloud Kubernetes provides implementations of well-known Spring Cloud interfaces allowing developers to build and run Spring Cloud applications on Kubernetes.

### Key Features
- **ConfigMap PropertySource**: Load configuration from Kubernetes ConfigMaps
- **Secrets PropertySource**: Load sensitive data from Kubernetes Secrets
- **Service Discovery**: Discover services using Kubernetes native service discovery
- **Load Balancer**: Client-side load balancing using Kubernetes endpoints
- **Health Indicators**: Kubernetes-aware health checks

### Why Use It?
- Native Kubernetes integration without Eureka/Config Server
- Leverage Kubernetes as the service mesh
- Simplified cloud-native configuration management
- Better alignment with Kubernetes-native operations

---

## Part 2: Setting Up the Application

### Step 2.1: Open the Starter Project

Open the starter project located in:
```
labs/module-08f-kubernetes/starter/k8s-demo/
```

### Step 2.2: Review Dependencies

The `pom.xml` includes:
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-kubernetes-client-config</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-kubernetes-client-all</artifactId>
</dependency>
```

### Step 2.3: Create the Application Configuration

Create `src/main/resources/application.yml`:

```yaml
server:
  port: 8080

spring:
  application:
    name: k8s-demo
  cloud:
    kubernetes:
      config:
        enabled: true
        name: k8s-demo-config
        namespace: default
        enable-api: true
      secrets:
        enabled: true
        name: k8s-demo-secrets
        namespace: default
        enable-api: true
      reload:
        enabled: true
        mode: polling
        period: 15000
      discovery:
        enabled: true
        all-namespaces: false

app:
  greeting: "Hello from default config!"
  feature:
    enabled: false

management:
  endpoints:
    web:
      exposure:
        include: health,info,refresh
  endpoint:
    health:
      show-details: always
      probes:
        enabled: true
  health:
    livenessState:
      enabled: true
    readinessState:
      enabled: true
```

### Step 2.4: Create Configuration Properties Class

Create `src/main/java/com/example/k8sdemo/config/AppProperties.java`:

```java
package com.example.k8sdemo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String greeting = "Hello!";
    private Feature feature = new Feature();

    public String getGreeting() {
        return greeting;
    }

    public void setGreeting(String greeting) {
        this.greeting = greeting;
    }

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public static class Feature {
        private boolean enabled = false;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
```

### Step 2.5: Create REST Controller

Create `src/main/java/com/example/k8sdemo/controller/DemoController.java`:

```java
package com.example.k8sdemo.controller;

import com.example.k8sdemo.config.AppProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DemoController {

    private final AppProperties appProperties;

    @Value("${app.secret.api-key:not-set}")
    private String apiKey;

    @Value("${app.secret.db-password:not-set}")
    private String dbPassword;

    public DemoController(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @GetMapping("/hello")
    public Map<String, Object> hello() throws UnknownHostException {
        Map<String, Object> response = new HashMap<>();
        response.put("message", appProperties.getGreeting());
        response.put("hostname", InetAddress.getLocalHost().getHostName());
        response.put("featureEnabled", appProperties.getFeature().isEnabled());
        return response;
    }

    @GetMapping("/config")
    public Map<String, Object> getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("greeting", appProperties.getGreeting());
        config.put("featureEnabled", appProperties.getFeature().isEnabled());
        config.put("apiKeyConfigured", !apiKey.equals("not-set"));
        config.put("dbPasswordConfigured", !dbPassword.equals("not-set"));
        return config;
    }

    @GetMapping("/health/custom")
    public Map<String, String> customHealth() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("application", "k8s-demo");
        return health;
    }
}
```

### Step 2.6: Create Service Discovery Controller

Create `src/main/java/com/example/k8sdemo/controller/DiscoveryController.java`:

```java
package com.example.k8sdemo.controller;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/discovery")
public class DiscoveryController {

    private final DiscoveryClient discoveryClient;

    public DiscoveryController(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @GetMapping("/services")
    public List<String> getServices() {
        return discoveryClient.getServices();
    }

    @GetMapping("/services/{serviceName}")
    public List<Map<String, Object>> getServiceInstances(@PathVariable String serviceName) {
        return discoveryClient.getInstances(serviceName).stream()
                .map(this::mapInstance)
                .collect(Collectors.toList());
    }

    private Map<String, Object> mapInstance(ServiceInstance instance) {
        Map<String, Object> map = new HashMap<>();
        map.put("serviceId", instance.getServiceId());
        map.put("host", instance.getHost());
        map.put("port", instance.getPort());
        map.put("uri", instance.getUri().toString());
        map.put("metadata", instance.getMetadata());
        return map;
    }
}
```

---

## Part 3: Creating Kubernetes Manifests

### Step 3.1: Create Namespace (Optional)

Create `k8s/namespace.yaml`:

```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: spring-demo
```

### Step 3.2: Create ConfigMap

Create `k8s/configmap.yaml`:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: k8s-demo-config
  namespace: default
data:
  application.yml: |
    app:
      greeting: "Hello from Kubernetes ConfigMap!"
      feature:
        enabled: true
```

### Step 3.3: Create Secret

Create `k8s/secret.yaml`:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: k8s-demo-secrets
  namespace: default
type: Opaque
stringData:
  application.yml: |
    app:
      secret:
        api-key: "my-secret-api-key-12345"
        db-password: "super-secret-password"
```

### Step 3.4: Create Deployment

Create `k8s/deployment.yaml`:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: k8s-demo
  namespace: default
  labels:
    app: k8s-demo
spec:
  replicas: 2
  selector:
    matchLabels:
      app: k8s-demo
  template:
    metadata:
      labels:
        app: k8s-demo
    spec:
      serviceAccountName: spring-cloud-kubernetes
      containers:
        - name: k8s-demo
          image: k8s-demo:latest
          imagePullPolicy: IfNotPresent
          ports:
            - containerPort: 8080
          env:
            - name: SPRING_PROFILES_ACTIVE
              value: "kubernetes"
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: 8080
            initialDelaySeconds: 30
            periodSeconds: 10
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: 8080
            initialDelaySeconds: 10
            periodSeconds: 5
          resources:
            requests:
              memory: "256Mi"
              cpu: "200m"
            limits:
              memory: "512Mi"
              cpu: "500m"
```

### Step 3.5: Create Service

Create `k8s/service.yaml`:

```yaml
apiVersion: v1
kind: Service
metadata:
  name: k8s-demo
  namespace: default
  labels:
    app: k8s-demo
spec:
  type: ClusterIP
  ports:
    - port: 80
      targetPort: 8080
      protocol: TCP
      name: http
  selector:
    app: k8s-demo
```

### Step 3.6: Create ServiceAccount and RBAC

Create `k8s/rbac.yaml`:

```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: spring-cloud-kubernetes
  namespace: default
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
  name: spring-cloud-kubernetes
rules:
  - apiGroups: [""]
    resources: ["configmaps", "secrets", "services", "endpoints", "pods"]
    verbs: ["get", "list", "watch"]
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: spring-cloud-kubernetes
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: spring-cloud-kubernetes
subjects:
  - kind: ServiceAccount
    name: spring-cloud-kubernetes
    namespace: default
```

---

## Part 4: Building and Deploying

### Step 4.1: Create Dockerfile

Create `Dockerfile` in the project root:

```dockerfile
FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
```

### Step 4.2: Build the Application

```bash
cd starter/k8s-demo
mvn clean package -DskipTests
```

### Step 4.3: Build Docker Image

For Minikube:
```bash
eval $(minikube docker-env)
docker build -t k8s-demo:latest .
```

For Docker Desktop:
```bash
docker build -t k8s-demo:latest .
```

### Step 4.4: Deploy to Kubernetes

```bash
# Apply RBAC first
kubectl apply -f k8s/rbac.yaml

# Apply ConfigMap and Secret
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secret.yaml

# Deploy the application
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
```

### Step 4.5: Verify Deployment

```bash
# Check pods
kubectl get pods -l app=k8s-demo

# Check service
kubectl get svc k8s-demo

# View logs
kubectl logs -l app=k8s-demo --tail=100
```

---

## Part 5: Testing the Application

### Step 5.1: Port Forward to Access the Service

```bash
kubectl port-forward svc/k8s-demo 8080:80
```

### Step 5.2: Test Endpoints

```bash
# Test hello endpoint
curl http://localhost:8080/api/hello

# Test config endpoint
curl http://localhost:8080/api/config

# Test health endpoints
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/health/liveness
curl http://localhost:8080/actuator/health/readiness

# Test service discovery
curl http://localhost:8080/api/discovery/services
```

### Step 5.3: Test Configuration Reload

Update the ConfigMap:

```bash
kubectl edit configmap k8s-demo-config
```

Change the greeting message and wait for reload (15 seconds default), then test:

```bash
curl http://localhost:8080/api/hello
```

---

## Part 6: Advanced Topics

### Step 6.1: Profile-Specific Configuration

Create `k8s/configmap-prod.yaml`:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: k8s-demo-config
  namespace: default
data:
  application-prod.yml: |
    app:
      greeting: "Hello from Production!"
      feature:
        enabled: false
    logging:
      level:
        root: WARN
        com.example: INFO
```

### Step 6.2: Horizontal Pod Autoscaler

Create `k8s/hpa.yaml`:

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: k8s-demo-hpa
  namespace: default
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: k8s-demo
  minReplicas: 2
  maxReplicas: 10
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 50
```

### Step 6.3: Ingress Configuration

Create `k8s/ingress.yaml`:

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: k8s-demo-ingress
  namespace: default
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
spec:
  ingressClassName: nginx
  rules:
    - host: k8s-demo.local
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: k8s-demo
                port:
                  number: 80
```

---

## Exercises

### Exercise 1: Multiple Environments
Create separate ConfigMaps for dev, staging, and prod environments and switch between them using Spring profiles.

### Exercise 2: Service-to-Service Communication
Create a second service and implement service discovery and communication between them using Spring Cloud Kubernetes.

### Exercise 3: External Secrets
Integrate with an external secrets manager (like HashiCorp Vault) using Kubernetes External Secrets.

---

## Cleanup

```bash
# Delete all resources
kubectl delete -f k8s/

# Or delete specific resources
kubectl delete deployment k8s-demo
kubectl delete service k8s-demo
kubectl delete configmap k8s-demo-config
kubectl delete secret k8s-demo-secrets
```

---

## Summary

In this lab, you learned:
- How to configure Spring Boot applications for Kubernetes
- How to use ConfigMaps and Secrets for configuration
- How to implement Kubernetes-native service discovery
- How to set up health probes for Kubernetes
- How to deploy and manage Spring applications in Kubernetes

## Key Concepts

| Concept | Description |
|---------|-------------|
| ConfigMap | Kubernetes object for non-sensitive configuration |
| Secret | Kubernetes object for sensitive data |
| Service Discovery | Finding services using Kubernetes DNS |
| Health Probes | Liveness and readiness checks |
| RBAC | Role-based access control for API access |
| HPA | Horizontal Pod Autoscaler for scaling |

## Spring Cloud Kubernetes vs Eureka

| Feature | Spring Cloud Kubernetes | Eureka |
|---------|------------------------|--------|
| Service Registry | Kubernetes native | Separate server |
| Config Management | ConfigMaps/Secrets | Config Server |
| Infrastructure | Requires Kubernetes | Any environment |
| Complexity | Simpler in K8s | More components |
| Portability | K8s only | Any cloud/on-prem |

## Next Steps
- Explore service mesh integration (Istio, Linkerd)
- Learn about Kubernetes operators for Spring
- Investigate GitOps deployment with ArgoCD/Flux
- Study distributed tracing with Jaeger/Zipkin
