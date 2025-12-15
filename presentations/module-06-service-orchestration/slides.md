# Module 6: Service Orchestration

---

## Module Overview

### What You'll Learn
- Understand microservices communication patterns
- Send messages between services
- Receive and process messages
- Build a sample multi-service application

### Duration: ~3 hours (Day 3 Afternoon)

---

## Section 1: Microservices Communication

---

### Communication Patterns

```
┌─────────────────────────────────────────────────────────┐
│            Service Communication Patterns                │
├───────────────────┬─────────────────────────────────────┤
│   Synchronous     │  REST, gRPC, GraphQL                │
│   Asynchronous    │  Message Queues, Event Streaming    │
└───────────────────┴─────────────────────────────────────┘
```

---

### Synchronous vs Asynchronous

**Synchronous (REST/HTTP):**
```
┌─────────┐  HTTP Request  ┌─────────┐
│Service A├───────────────>│Service B│
│         │<───────────────┤         │
└─────────┘  HTTP Response └─────────┘
       │
       └── Service A waits for response
```

**Asynchronous (Messaging):**
```
┌─────────┐  Publish   ┌─────────┐  Subscribe  ┌─────────┐
│Service A├───────────>│  Queue  ├────────────>│Service B│
└─────────┘            └─────────┘             └─────────┘
       │
       └── Service A continues immediately
```

---

### When to Use Each Pattern

| Pattern | Use When |
|---------|----------|
| **Synchronous** | Need immediate response |
| | Simple request-response |
| | Strong consistency required |
| **Asynchronous** | Fire-and-forget operations |
| | Long-running processes |
| | Decoupling required |
| | High throughput needs |

---

## Section 2: REST Communication

---

### RestTemplate (Traditional)

```java
@Service
public class OrderService {

    private final RestTemplate restTemplate;

    public OrderService(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    public Product getProduct(Long productId) {
        return restTemplate.getForObject(
            "http://product-service/api/products/{id}",
            Product.class,
            productId
        );
    }

    public void createOrder(Order order) {
        restTemplate.postForObject(
            "http://order-service/api/orders",
            order,
            Order.class
        );
    }
}
```

---

### RestTemplate Configuration

```java
@Configuration
public class RestConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
            .setConnectTimeout(Duration.ofSeconds(5))
            .setReadTimeout(Duration.ofSeconds(10))
            .build();
    }
}
```

---

### WebClient (Reactive/Modern)

```java
@Service
public class ProductService {

    private final WebClient webClient;

    public ProductService(WebClient.Builder builder) {
        this.webClient = builder
            .baseUrl("http://product-service")
            .build();
    }

    public Mono<Product> getProduct(Long id) {
        return webClient.get()
            .uri("/api/products/{id}", id)
            .retrieve()
            .bodyToMono(Product.class);
    }

    public Flux<Product> getAllProducts() {
        return webClient.get()
            .uri("/api/products")
            .retrieve()
            .bodyToFlux(Product.class);
    }
}
```

---

### WebClient - Blocking Style

For non-reactive applications:

```java
@Service
public class ProductService {

    private final WebClient webClient;

    public Product getProduct(Long id) {
        return webClient.get()
            .uri("/api/products/{id}", id)
            .retrieve()
            .bodyToMono(Product.class)
            .block();  // Blocks until response
    }

    public List<Product> getAllProducts() {
        return webClient.get()
            .uri("/api/products")
            .retrieve()
            .bodyToFlux(Product.class)
            .collectList()
            .block();
    }
}
```

---

### Error Handling with WebClient

```java
public Product getProduct(Long id) {
    return webClient.get()
        .uri("/api/products/{id}", id)
        .retrieve()
        .onStatus(HttpStatusCode::is4xxClientError, response ->
            Mono.error(new ProductNotFoundException(id)))
        .onStatus(HttpStatusCode::is5xxServerError, response ->
            Mono.error(new ServiceUnavailableException("Product service down")))
        .bodyToMono(Product.class)
        .block();
}
```

---

### OpenFeign Client

Declarative REST client:

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

```java
@EnableFeignClients
@SpringBootApplication
public class Application { }

@FeignClient(name = "product-service", url = "http://localhost:8081")
public interface ProductClient {

    @GetMapping("/api/products/{id}")
    Product getProduct(@PathVariable Long id);

    @GetMapping("/api/products")
    List<Product> getAllProducts();

    @PostMapping("/api/products")
    Product createProduct(@RequestBody CreateProductRequest request);
}
```

---

### Using Feign Client

```java
@Service
public class OrderService {

    private final ProductClient productClient;

    public OrderService(ProductClient productClient) {
        this.productClient = productClient;
    }

    public Order createOrder(CreateOrderRequest request) {
        // Call product service
        Product product = productClient.getProduct(request.getProductId());

        // Validate availability
        if (product.getQuantity() < request.getQuantity()) {
            throw new InsufficientStockException();
        }

        // Create order
        return orderRepository.save(new Order(product, request.getQuantity()));
    }
}
```

---

## Section 3: Asynchronous Messaging

---

### Message Queue Benefits

- **Decoupling**: Services don't need to know about each other
- **Resilience**: Messages persist if consumer is down
- **Scalability**: Multiple consumers can process messages
- **Load Leveling**: Buffer during traffic spikes

---

### RabbitMQ Architecture

```
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│ Producer │───>│ Exchange │───>│  Queue   │───>│ Consumer │
└──────────┘    └──────────┘    └──────────┘    └──────────┘
                     │                │
                     │    Binding     │
                     └────────────────┘
```

---

### Spring AMQP Setup

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```

```properties
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
```

---

### RabbitMQ Configuration

```java
@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_NAME = "task-events";
    public static final String EXCHANGE_NAME = "task-exchange";
    public static final String ROUTING_KEY = "task.#";

    @Bean
    public Queue taskQueue() {
        return new Queue(QUEUE_NAME, true);  // durable
    }

    @Bean
    public TopicExchange taskExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue)
            .to(exchange)
            .with(ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
```

---

### Sending Messages

```java
@Service
public class TaskEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public TaskEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishTaskCreated(Task task) {
        TaskEvent event = new TaskEvent("CREATED", task.getId(), task.getTitle());
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE_NAME,
            "task.created",
            event
        );
    }

    public void publishTaskCompleted(Task task) {
        TaskEvent event = new TaskEvent("COMPLETED", task.getId(), task.getTitle());
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE_NAME,
            "task.completed",
            event
        );
    }
}

public record TaskEvent(String type, Long taskId, String title) {}
```

---

### Receiving Messages

```java
@Service
public class TaskEventListener {

    private static final Logger log = LoggerFactory.getLogger(TaskEventListener.class);

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleTaskEvent(TaskEvent event) {
        log.info("Received task event: {}", event);

        switch (event.type()) {
            case "CREATED" -> handleTaskCreated(event);
            case "COMPLETED" -> handleTaskCompleted(event);
            default -> log.warn("Unknown event type: {}", event.type());
        }
    }

    private void handleTaskCreated(TaskEvent event) {
        // Send notification, update analytics, etc.
        log.info("Processing task created: {}", event.taskId());
    }

    private void handleTaskCompleted(TaskEvent event) {
        // Update reports, trigger workflows, etc.
        log.info("Processing task completed: {}", event.taskId());
    }
}
```

---

### Kafka Alternative

For high-throughput event streaming:

```xml
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
```

```java
@Service
public class KafkaTaskPublisher {

    private final KafkaTemplate<String, TaskEvent> kafkaTemplate;

    public void publishTaskEvent(TaskEvent event) {
        kafkaTemplate.send("task-events", event.taskId().toString(), event);
    }
}

@Service
public class KafkaTaskListener {

    @KafkaListener(topics = "task-events", groupId = "task-service")
    public void handleTaskEvent(TaskEvent event) {
        // Process event
    }
}
```

---

## Section 4: Building Multi-Service Application

---

### Sample Architecture

```
┌──────────────────────────────────────────────────────────┐
│                      API Gateway                          │
│                    (Port 8080)                           │
└─────────────┬────────────────────────┬──────────────────┘
              │                        │
              ▼                        ▼
┌─────────────────────┐    ┌─────────────────────┐
│   Task Service      │    │  Notification       │
│   (Port 8081)       │    │  Service (8082)     │
│                     │    │                     │
│  - CRUD operations  │    │  - Send emails      │
│  - Task management  │    │  - Push notifications│
└──────────┬──────────┘    └─────────────────────┘
           │                         ▲
           │      Task Events        │
           └────────────────────────-┘
                  (RabbitMQ)
```

---

### Task Service - Publishing Events

```java
@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskEventPublisher eventPublisher;

    public Task create(CreateTaskRequest request) {
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());

        Task saved = taskRepository.save(task);

        // Publish event after successful save
        eventPublisher.publishTaskCreated(saved);

        return saved;
    }

    public Task complete(Long id) {
        Task task = findById(id);
        task.setStatus(TaskStatus.COMPLETED);

        Task saved = taskRepository.save(task);

        // Publish completion event
        eventPublisher.publishTaskCompleted(saved);

        return saved;
    }
}
```

---

### Notification Service - Consuming Events

```java
@SpringBootApplication
public class NotificationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}

@Service
public class NotificationService {

    private final EmailService emailService;

    @RabbitListener(queues = "task-events")
    public void handleTaskEvent(TaskEvent event) {
        switch (event.type()) {
            case "CREATED" -> sendTaskCreatedNotification(event);
            case "COMPLETED" -> sendTaskCompletedNotification(event);
        }
    }

    private void sendTaskCreatedNotification(TaskEvent event) {
        emailService.send(
            "team@example.com",
            "New Task Created",
            "Task '" + event.title() + "' has been created."
        );
    }

    private void sendTaskCompletedNotification(TaskEvent event) {
        emailService.send(
            "team@example.com",
            "Task Completed",
            "Task '" + event.title() + "' has been completed!"
        );
    }
}
```

---

### Service Discovery (Optional)

With Spring Cloud:

```yaml
# application.yml
spring:
  application:
    name: task-service

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

```java
@FeignClient(name = "notification-service")  // Uses service name, not URL
public interface NotificationClient {

    @PostMapping("/api/notifications")
    void sendNotification(@RequestBody Notification notification);
}
```

---

### Circuit Breaker Pattern

Resilience4j for fault tolerance:

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId>
</dependency>
```

```java
@Service
public class ProductService {

    private final ProductClient productClient;

    @CircuitBreaker(name = "productService", fallbackMethod = "getProductFallback")
    public Product getProduct(Long id) {
        return productClient.getProduct(id);
    }

    public Product getProductFallback(Long id, Exception e) {
        // Return cached or default product
        return new Product(id, "Unknown", BigDecimal.ZERO);
    }
}
```

---

### Circuit Breaker States

```
┌─────────────────────────────────────────────────────────┐
│                  Circuit Breaker                         │
├─────────┬─────────────────┬─────────────────────────────┤
│  CLOSED │ Normal operation│ Requests pass through       │
├─────────┼─────────────────┼─────────────────────────────┤
│  OPEN   │ Failure threshold│ Requests fail immediately  │
│         │ exceeded        │ (fallback used)            │
├─────────┼─────────────────┼─────────────────────────────┤
│HALF_OPEN│ Testing         │ Limited requests allowed    │
│         │                 │ Success → CLOSED            │
│         │                 │ Failure → OPEN              │
└─────────┴─────────────────┴─────────────────────────────┘
```

---

## Section 5: Best Practices

---

### Communication Guidelines

1. **Use async for non-blocking operations**
2. **Implement timeouts** for sync calls
3. **Add retry logic** with exponential backoff
4. **Use circuit breakers** to prevent cascade failures
5. **Make services idempotent** for safe retries
6. **Log correlation IDs** across services

---

### Message Design

```java
// Include correlation ID for tracing
public record TaskEvent(
    String eventId,        // Unique event ID
    String correlationId,  // Request correlation
    String type,
    Long taskId,
    String title,
    Instant timestamp,
    Map<String, Object> metadata
) {}
```

---

### Distributed Tracing

With Spring Cloud Sleuth/Micrometer:

```properties
management.tracing.sampling.probability=1.0
```

Log output includes trace IDs:
```
INFO [task-service,abc123,def456] - Processing task created
INFO [notification-service,abc123,ghi789] - Sending notification
                        │      │
                        │      └── Span ID
                        └── Trace ID (same across services)
```

---

## Module 6 Summary

### Key Takeaways

1. **Synchronous** (REST/HTTP) for request-response patterns
2. **Asynchronous** (Messaging) for decoupling and resilience
3. **WebClient** is the modern HTTP client for Spring
4. **RabbitMQ/Kafka** enable event-driven architectures
5. **Circuit breakers** prevent cascade failures
6. **Correlation IDs** enable distributed tracing

---

## Lab Exercise

### Lab 6: Service Orchestration
`labs/module-06-service-orchestration/`

You will build:
- Task Service that publishes events
- User Service for REST communication
- Event-driven communication between services

---

## Questions?

### Next Module: Additional Topics & Wrap-up
