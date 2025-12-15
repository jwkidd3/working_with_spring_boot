# Module 1: Spring Framework Fundamentals

---

## Module Overview

### What You'll Learn
- Understand the Spring Framework core concepts
- Master Inversion of Control (IoC) and Dependency Injection (DI)
- Work with Java Annotations
- Configure Spring applications

### Duration: ~3 hours (Day 1 Morning)

---

## Section 1: Introduction to Spring Framework

---

### What is Spring?

- **Open-source application framework** for Java
- Created by Rod Johnson in 2003
- Provides comprehensive infrastructure support
- Makes Java development easier and more productive

> "Spring is not just a framework, it's a platform for your applications"

---

### Why Spring?

| Challenge | Spring Solution |
|-----------|-----------------|
| Complex J2EE/Jakarta EE | Simplified programming model |
| Tight coupling | Loose coupling via DI |
| Hard to test | Easy unit testing |
| Boilerplate code | Convention over configuration |
| XML configuration hell | Annotation-based configuration |

---

### Spring Ecosystem

```
┌─────────────────────────────────────────────────────────┐
│                    Spring Boot                          │
├─────────────────────────────────────────────────────────┤
│  Spring   │  Spring  │  Spring  │  Spring  │  Spring   │
│   Data    │ Security │   MVC    │  Cloud   │   Batch   │
├─────────────────────────────────────────────────────────┤
│                  Spring Framework Core                   │
│         (IoC Container, AOP, Events, Resources)         │
└─────────────────────────────────────────────────────────┘
```

---

### Spring vs Spring Boot

| Spring Framework | Spring Boot |
|-----------------|-------------|
| Requires manual configuration | Auto-configuration |
| XML or Java config needed | Minimal configuration |
| External server deployment | Embedded server |
| Dependency management manual | Starter dependencies |
| Production setup complex | Production-ready features |

---

## Section 2: Inversion of Control (IoC)

---

### Traditional Approach (Without IoC)

```java
public class OrderService {
    // Tight coupling - OrderService creates its dependencies
    private PaymentService paymentService = new PaymentService();
    private InventoryService inventoryService = new InventoryService();

    public void processOrder(Order order) {
        inventoryService.reserve(order);
        paymentService.charge(order);
    }
}
```

**Problems:**
- Hard to test (can't mock dependencies)
- Hard to change implementations
- Violates Single Responsibility Principle

---

### IoC Approach

```java
public class OrderService {
    // Dependencies are injected, not created
    private final PaymentService paymentService;
    private final InventoryService inventoryService;

    public OrderService(PaymentService paymentService,
                        InventoryService inventoryService) {
        this.paymentService = paymentService;
        this.inventoryService = inventoryService;
    }

    public void processOrder(Order order) {
        inventoryService.reserve(order);
        paymentService.charge(order);
    }
}
```

**Benefits:**
- Loose coupling
- Easy to test with mocks
- Easy to swap implementations

---

### The IoC Container

```
┌─────────────────────────────────────────────┐
│           Spring IoC Container               │
│  ┌─────────────────────────────────────┐    │
│  │         Bean Definitions             │    │
│  │  - Class metadata                    │    │
│  │  - Dependencies                      │    │
│  │  - Scope                            │    │
│  │  - Lifecycle callbacks              │    │
│  └─────────────────────────────────────┘    │
│                    │                         │
│                    ▼                         │
│  ┌─────────────────────────────────────┐    │
│  │      Fully Configured Beans          │    │
│  │  (Ready to use in application)       │    │
│  └─────────────────────────────────────┘    │
└─────────────────────────────────────────────┘
```

---

### ApplicationContext

The central interface for the Spring IoC container

```java
// Creating an ApplicationContext
ApplicationContext context =
    new AnnotationConfigApplicationContext(AppConfig.class);

// Getting a bean from the container
OrderService orderService = context.getBean(OrderService.class);
```

**Common Implementations:**
- `AnnotationConfigApplicationContext` - Java config
- `ClassPathXmlApplicationContext` - XML config
- `WebApplicationContext` - Web applications

---

## Section 3: Dependency Injection (DI)

---

### What is Dependency Injection?

> Dependency Injection is a design pattern where objects receive their dependencies from an external source rather than creating them internally.

**The "Hollywood Principle":**
> "Don't call us, we'll call you"

---

### Types of Dependency Injection

```
┌────────────────────────────────────────────────────┐
│              Dependency Injection                   │
├────────────────┬────────────────┬─────────────────┤
│  Constructor   │    Setter      │     Field       │
│   Injection    │   Injection    │   Injection     │
│   (Preferred)  │   (Optional)   │  (Avoid in     │
│                │   dependencies │   production)   │
└────────────────┴────────────────┴─────────────────┘
```

---

### Constructor Injection (Recommended)

```java
@Service
public class OrderService {

    private final PaymentService paymentService;
    private final InventoryService inventoryService;

    // @Autowired is optional for single constructor (Spring 4.3+)
    public OrderService(PaymentService paymentService,
                        InventoryService inventoryService) {
        this.paymentService = paymentService;
        this.inventoryService = inventoryService;
    }
}
```

**Advantages:**
- Immutable dependencies (final fields)
- Required dependencies are explicit
- Easy to test
- Fails fast if dependency missing

---

### Setter Injection

```java
@Service
public class NotificationService {

    private EmailService emailService;
    private SmsService smsService;  // Optional dependency

    @Autowired
    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    @Autowired(required = false)
    public void setSmsService(SmsService smsService) {
        this.smsService = smsService;
    }
}
```

**Use Cases:**
- Optional dependencies
- Circular dependencies (use sparingly!)
- Reconfigurable dependencies

---

### Field Injection (Avoid)

```java
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;  // Not recommended!

    @Autowired
    private PasswordEncoder passwordEncoder;  // Not recommended!
}
```

**Why Avoid:**
- Hides dependencies
- Hard to test without Spring
- Can't make fields final
- Harder to detect design problems

---

## Section 4: Java Annotations

---

### Core Stereotype Annotations

```java
@Component      // Generic Spring-managed component
@Service        // Business logic layer
@Repository     // Data access layer (+ exception translation)
@Controller     // Web layer (MVC controller)
@RestController // Web layer (REST API) = @Controller + @ResponseBody
```

```
┌─────────────────────────────────────────────────┐
│                  @Component                      │
│  ┌───────────┬───────────┬───────────────────┐  │
│  │ @Service  │@Repository│   @Controller     │  │
│  │           │           │ ┌───────────────┐ │  │
│  │           │           │ │@RestController│ │  │
│  │           │           │ └───────────────┘ │  │
│  └───────────┴───────────┴───────────────────┘  │
└─────────────────────────────────────────────────┘
```

---

### @Component Example

```java
@Component
public class EmailValidator {

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    public boolean isValid(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
}
```

Spring automatically detects and registers this as a bean

---

### @Service Example

```java
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        return userRepository.save(user);
    }
}
```

---

### @Repository Example

```java
@Repository
public class JpaUserRepository implements UserRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public User findById(Long id) {
        return entityManager.find(User.class, id);
    }

    @Override
    public User save(User user) {
        entityManager.persist(user);
        return user;
    }
}
```

`@Repository` provides automatic exception translation (SQL exceptions → Spring's DataAccessException)

---

### Dependency Injection Annotations

```java
@Autowired    // Inject by type (Spring)
@Qualifier    // Specify which bean when multiple candidates
@Primary      // Mark as default bean when multiple candidates
@Resource     // Inject by name (Jakarta EE)
@Inject       // Inject by type (Jakarta EE)
```

---

### @Qualifier Example

```java
public interface MessageSender {
    void send(String message);
}

@Service
@Qualifier("email")
public class EmailSender implements MessageSender {
    public void send(String message) { /* send email */ }
}

@Service
@Qualifier("sms")
public class SmsSender implements MessageSender {
    public void send(String message) { /* send SMS */ }
}

@Service
public class NotificationService {
    private final MessageSender sender;

    public NotificationService(@Qualifier("email") MessageSender sender) {
        this.sender = sender;
    }
}
```

---

### Bean Scopes

| Scope | Description |
|-------|-------------|
| `singleton` | One instance per container (default) |
| `prototype` | New instance each time requested |
| `request` | One instance per HTTP request |
| `session` | One instance per HTTP session |
| `application` | One instance per ServletContext |

```java
@Service
@Scope("prototype")
public class ShoppingCart {
    private List<Item> items = new ArrayList<>();
}
```

---

## Section 5: Spring Configuration

---

### Configuration Approaches

```
┌────────────────────────────────────────────────────────┐
│                 Spring Configuration                    │
├──────────────────┬──────────────────┬─────────────────┤
│    XML-based     │   Java-based     │  Annotation     │
│   (Legacy)       │   (Preferred)    │    Scanning     │
│                  │                  │  (Component)    │
│  applicationContext │ @Configuration │  @Component    │
│      .xml        │  @Bean          │  @Service, etc  │
└──────────────────┴──────────────────┴─────────────────┘
```

---

### Java-based Configuration

```java
@Configuration
public class AppConfig {

    @Bean
    public DataSource dataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:postgresql://localhost:5432/mydb");
        ds.setUsername("user");
        ds.setPassword("password");
        return ds;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
```

---

### Component Scanning

```java
@Configuration
@ComponentScan(basePackages = "com.example.myapp")
public class AppConfig {
    // Beans in com.example.myapp will be auto-detected
}

// More specific scanning
@ComponentScan(
    basePackages = "com.example",
    includeFilters = @ComponentScan.Filter(
        type = FilterType.ANNOTATION,
        classes = Service.class
    ),
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = ".*Test.*"
    )
)
```

---

### Property Injection

```java
// application.properties
app.name=My Application
app.max-connections=100

@Configuration
@PropertySource("classpath:application.properties")
public class AppConfig {

    @Value("${app.name}")
    private String appName;

    @Value("${app.max-connections:50}")  // Default value
    private int maxConnections;
}
```

---

### Environment and Profiles

```java
@Configuration
public class AppConfig {

    @Autowired
    private Environment env;

    @Bean
    public DataSource dataSource() {
        String url = env.getProperty("database.url");
        // ...
    }
}

@Configuration
@Profile("development")
public class DevConfig {
    @Bean
    public DataSource dataSource() {
        // H2 in-memory database for development
    }
}

@Configuration
@Profile("production")
public class ProdConfig {
    @Bean
    public DataSource dataSource() {
        // PostgreSQL for production
    }
}
```

---

### Bean Lifecycle

```
┌─────────────────────────────────────────────────────────┐
│                   Bean Lifecycle                         │
├─────────────────────────────────────────────────────────┤
│  1. Instantiation (Constructor)                          │
│  2. Populate Properties (DI)                             │
│  3. BeanNameAware.setBeanName()                         │
│  4. BeanFactoryAware.setBeanFactory()                   │
│  5. ApplicationContextAware.setApplicationContext()      │
│  6. @PostConstruct / InitializingBean.afterPropertiesSet│
│  7. Custom init-method                                   │
│  ─────────────── BEAN READY ───────────────             │
│  8. @PreDestroy / DisposableBean.destroy()              │
│  9. Custom destroy-method                                │
└─────────────────────────────────────────────────────────┘
```

---

### Lifecycle Callbacks

```java
@Service
public class CacheService {

    private Cache cache;

    @PostConstruct
    public void init() {
        System.out.println("Initializing cache...");
        cache = new Cache();
        cache.warmUp();
    }

    @PreDestroy
    public void cleanup() {
        System.out.println("Cleaning up cache...");
        cache.clear();
    }
}
```

---

## Module 1 Summary

### Key Takeaways

1. **IoC** inverts the control of object creation from your code to the container
2. **DI** is the mechanism Spring uses to provide dependencies
3. **Constructor injection** is the preferred DI method
4. **Stereotype annotations** (@Service, @Repository, etc.) register beans
5. **Java configuration** with @Configuration is the modern approach
6. **Profiles** enable environment-specific configuration

---

## Lab Exercises

### Lab 1: Spring Framework Fundamentals
`labs/module-01-spring-fundamentals/`

You will build a simple Spring application that demonstrates:
- Creating and configuring beans
- Using dependency injection
- Working with profiles

### Lab 1b: Dependency Injection Deep Dive
`labs/module-01b-di-deep-dive/`

You will explore advanced DI concepts:
- Constructor, setter, and field injection
- @Qualifier and @Primary annotations
- Bean scopes and lifecycle callbacks

---

## Questions?

### Next Module: Introduction to Web Services
