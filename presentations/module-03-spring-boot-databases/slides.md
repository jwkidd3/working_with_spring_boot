# Module 3: Using Spring Boot with Databases

---

## Module Overview

### What You'll Learn
- Configure datasources and connection pooling
- Understand JPA and Hibernate basics
- Use Spring Data JPA for data access
- Define custom queries with Spring Data

### Duration: ~3 hours (Day 2 Morning)

---

## Section 1: Database Configuration

---

### Spring Boot Auto-Configuration Magic

Spring Boot automatically configures:
- **DataSource** based on classpath dependencies
- **JPA/Hibernate** with sensible defaults
- **Connection pooling** (HikariCP by default)

Just add a dependency and configure properties!

---

### Adding Database Support

**Maven dependencies:**
```xml
<!-- Spring Data JPA -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- HSQLDB for development -->
<dependency>
    <groupId>org.hsqldb</groupId>
    <artifactId>hsqldb</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- PostgreSQL for production -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

---

### DataSource Configuration

```properties
# HSQLDB In-Memory Database (Development)
spring.datasource.url=jdbc:hsqldb:mem:testdb
spring.datasource.driver-class-name=org.hsqldb.jdbc.JDBCDriver
spring.datasource.username=sa
spring.datasource.password=
```

```properties
# PostgreSQL (Production)
spring.datasource.url=jdbc:postgresql://localhost:5432/mydb
spring.datasource.username=postgres
spring.datasource.password=secret
```

---

### Connection Pooling with HikariCP

Spring Boot uses HikariCP by default (fastest pool)

```properties
# Pool configuration
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.max-lifetime=1200000

# Connection test
spring.datasource.hikari.connection-test-query=SELECT 1
```

---

### Connection Pool Visualization

```
┌─────────────────────────────────────────────────────────┐
│                   HikariCP Pool                          │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐       │
│  │  Conn   │ │  Conn   │ │  Conn   │ │  Conn   │ ...   │
│  │  IDLE   │ │  BUSY   │ │  IDLE   │ │  IDLE   │       │
│  └─────────┘ └─────────┘ └─────────┘ └─────────┘       │
│                                                          │
│  minimum-idle: 5    maximum-pool-size: 10               │
└─────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────┐
│                      Database                            │
│           PostgreSQL / MySQL / HSQLDB                    │
└─────────────────────────────────────────────────────────┘
```

---

### Environment-Specific Configuration

**application.properties (defaults):**
```properties
spring.jpa.hibernate.ddl-auto=none
```

**application-dev.properties:**
```properties
spring.datasource.url=jdbc:hsqldb:mem:devdb
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
```

**application-prod.properties:**
```properties
spring.datasource.url=jdbc:postgresql://prod-server:5432/mydb
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
```

---

## Section 2: JPA Fundamentals

---

### What is JPA?

**Java Persistence API**

- Standard specification for ORM in Java
- Defines how to map Java objects to database tables
- Abstracts database interactions

**Implementations:**
- Hibernate (most popular, Spring Boot default)
- EclipseLink
- OpenJPA

---

### JPA Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Your Application                      │
│                         │                                │
│                         ▼                                │
│            ┌─────────────────────────┐                  │
│            │    EntityManager API     │                  │
│            └─────────────────────────┘                  │
│                         │                                │
│                         ▼                                │
│            ┌─────────────────────────┐                  │
│            │  JPA Provider (Hibernate)│                  │
│            └─────────────────────────┘                  │
│                         │                                │
│                         ▼                                │
│            ┌─────────────────────────┐                  │
│            │         JDBC             │                  │
│            └─────────────────────────┘                  │
│                         │                                │
└─────────────────────────┼───────────────────────────────┘
                          │
                          ▼
                    ┌──────────┐
                    │ Database │
                    └──────────┘
```

---

### Entity Mapping

```java
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // getters and setters
}
```

---

### Key JPA Annotations

| Annotation | Purpose |
|------------|---------|
| `@Entity` | Marks class as JPA entity |
| `@Table` | Specifies table name |
| `@Id` | Marks primary key field |
| `@GeneratedValue` | Auto-generation strategy |
| `@Column` | Column customization |
| `@Transient` | Exclude from persistence |
| `@Temporal` | Date/time handling |
| `@Enumerated` | Enum persistence strategy |

---

### Primary Key Generation Strategies

```java
// Database auto-increment (recommended)
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

// Database sequence
@Id
@GeneratedValue(strategy = GenerationType.SEQUENCE,
                generator = "user_seq")
@SequenceGenerator(name = "user_seq",
                   sequenceName = "user_sequence",
                   allocationSize = 1)
private Long id;

// UUID
@Id
@GeneratedValue(strategy = GenerationType.UUID)
private UUID id;
```

---

### Relationship Mappings

```java
@Entity
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many orders belong to one user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // One order has many items
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items = new ArrayList<>();
}
```

---

### Relationship Types

```
┌─────────────────────────────────────────────────────────┐
│                  JPA Relationships                       │
├─────────────────────────────────────────────────────────┤
│  @OneToOne    │  User ←──────→ Profile                  │
├───────────────┼─────────────────────────────────────────┤
│  @OneToMany   │  User ←──────→ Orders (many)            │
├───────────────┼─────────────────────────────────────────┤
│  @ManyToOne   │  Order ──────→ User                     │
├───────────────┼─────────────────────────────────────────┤
│  @ManyToMany  │  User ←─────→ Roles (many)              │
└───────────────┴─────────────────────────────────────────┘
```

---

### Fetch Types

```java
// LAZY - Load on demand (recommended for collections)
@OneToMany(fetch = FetchType.LAZY)
private List<Order> orders;

// EAGER - Load immediately (use sparingly)
@ManyToOne(fetch = FetchType.EAGER)
private User user;
```

**Default fetch types:**
- `@OneToMany` / `@ManyToMany` → LAZY
- `@ManyToOne` / `@OneToOne` → EAGER

---

### Cascade Types

```java
@OneToMany(cascade = CascadeType.ALL)
private List<OrderItem> items;

// Individual cascade operations
@OneToMany(cascade = {
    CascadeType.PERSIST,  // Save child when parent is saved
    CascadeType.MERGE,    // Update child when parent is updated
    CascadeType.REMOVE    // Delete child when parent is deleted
})
private List<OrderItem> items;
```

---

## Section 3: Spring Data JPA

---

### What is Spring Data JPA?

- Simplifies data access layer implementation
- Reduces boilerplate code significantly
- Provides repository abstraction
- Automatic query generation from method names

```
┌─────────────────────────────────────────────────────────┐
│                  Traditional Approach                    │
│    EntityManager → JPQL → Manual Implementation         │
├─────────────────────────────────────────────────────────┤
│                 Spring Data JPA                          │
│    Interface → Auto Implementation → Queries            │
└─────────────────────────────────────────────────────────┘
```

---

### Repository Hierarchy

```
┌─────────────────────────────────────────────────────────┐
│              Repository<T, ID>                           │
│                     │                                    │
│                     ▼                                    │
│           CrudRepository<T, ID>                          │
│  save(), findById(), delete(), count(), existsById()     │
│                     │                                    │
│                     ▼                                    │
│       PagingAndSortingRepository<T, ID>                  │
│  findAll(Pageable), findAll(Sort)                        │
│                     │                                    │
│                     ▼                                    │
│           JpaRepository<T, ID>                           │
│  flush(), saveAndFlush(), deleteInBatch()                │
└─────────────────────────────────────────────────────────┘
```

---

### Creating a Repository

```java
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String email;
    // getters, setters
}

// That's all you need!
public interface UserRepository extends JpaRepository<User, Long> {
}
```

Spring Data automatically implements:
- `save(User user)`
- `findById(Long id)`
- `findAll()`
- `delete(User user)`
- `count()`
- And many more...

---

### Using the Repository

```java
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
```

---

### CrudRepository Methods

| Method | Description |
|--------|-------------|
| `save(S entity)` | Save entity (insert or update) |
| `saveAll(Iterable<S>)` | Save multiple entities |
| `findById(ID id)` | Find by primary key |
| `existsById(ID id)` | Check if exists |
| `findAll()` | Get all entities |
| `findAllById(Iterable<ID>)` | Get by multiple IDs |
| `count()` | Count all entities |
| `deleteById(ID id)` | Delete by ID |
| `delete(T entity)` | Delete entity |
| `deleteAll()` | Delete all entities |

---

### JpaRepository Additional Methods

```java
public interface UserRepository extends JpaRepository<User, Long> {
}

// Additional methods available:
userRepository.flush();                    // Sync with database
userRepository.saveAndFlush(user);        // Save and flush
userRepository.deleteAllInBatch();        // Batch delete (faster)
userRepository.getById(id);               // Get reference (lazy)
userRepository.findAll(Sort.by("name"));  // Sorted results
userRepository.findAll(PageRequest.of(0, 10)); // Paginated
```

---

## Section 4: Query Methods

---

### Derived Query Methods

Spring Data generates queries from method names!

```java
public interface UserRepository extends JpaRepository<User, Long> {

    // SELECT * FROM users WHERE username = ?
    User findByUsername(String username);

    // SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);

    // SELECT * FROM users WHERE active = true
    List<User> findByActiveTrue();

    // SELECT * FROM users WHERE age > ?
    List<User> findByAgeGreaterThan(int age);
}
```

---

### Query Method Keywords

| Keyword | Sample | JPQL Snippet |
|---------|--------|--------------|
| `And` | `findByNameAndAge` | `WHERE name = ? AND age = ?` |
| `Or` | `findByNameOrAge` | `WHERE name = ? OR age = ?` |
| `Between` | `findByAgeBetween` | `WHERE age BETWEEN ? AND ?` |
| `LessThan` | `findByAgeLessThan` | `WHERE age < ?` |
| `GreaterThan` | `findByAgeGreaterThan` | `WHERE age > ?` |
| `IsNull` | `findByNameIsNull` | `WHERE name IS NULL` |
| `Like` | `findByNameLike` | `WHERE name LIKE ?` |
| `Containing` | `findByNameContaining` | `WHERE name LIKE %?%` |
| `OrderBy` | `findByAgeOrderByName` | `ORDER BY name` |

---

### Complex Query Methods

```java
public interface UserRepository extends JpaRepository<User, Long> {

    // Multiple conditions
    List<User> findByFirstNameAndLastNameAndAgeGreaterThan(
        String firstName, String lastName, int age);

    // Ordering
    List<User> findByDepartmentOrderByLastNameAsc(String department);

    // Limiting results
    User findFirstByOrderByCreatedAtDesc();
    List<User> findTop10ByOrderByScoreDesc();

    // Distinct
    List<User> findDistinctByLastName(String lastName);

    // Counting
    long countByStatus(String status);

    // Existence
    boolean existsByEmail(String email);
}
```

---

### @Query Annotation

For complex queries, use JPQL:

```java
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE u.email LIKE %:domain")
    List<User> findByEmailDomain(@Param("domain") String domain);

    @Query("SELECT u FROM User u WHERE u.status = :status " +
           "AND u.createdAt > :date ORDER BY u.createdAt DESC")
    List<User> findRecentActiveUsers(
        @Param("status") String status,
        @Param("date") LocalDateTime date);

    @Query("SELECT u FROM User u JOIN u.orders o " +
           "WHERE o.total > :minTotal")
    List<User> findUsersWithLargeOrders(@Param("minTotal") BigDecimal minTotal);
}
```

---

### Native Queries

When you need raw SQL:

```java
public interface UserRepository extends JpaRepository<User, Long> {

    @Query(value = "SELECT * FROM users WHERE YEAR(created_at) = :year",
           nativeQuery = true)
    List<User> findUsersCreatedInYear(@Param("year") int year);

    @Query(value = """
        SELECT u.*, COUNT(o.id) as order_count
        FROM users u
        LEFT JOIN orders o ON u.id = o.user_id
        GROUP BY u.id
        HAVING COUNT(o.id) > :minOrders
        """,
        nativeQuery = true)
    List<User> findActiveCustomers(@Param("minOrders") int minOrders);
}
```

---

### Modifying Queries

```java
public interface UserRepository extends JpaRepository<User, Long> {

    @Modifying
    @Query("UPDATE User u SET u.status = :status WHERE u.id = :id")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    @Modifying
    @Query("DELETE FROM User u WHERE u.lastLoginAt < :date")
    int deleteInactiveUsers(@Param("date") LocalDateTime date);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.emailVerified = true WHERE u.id = :id")
    void verifyEmail(@Param("id") Long id);
}
```

**Note:** `@Modifying` queries require `@Transactional`

---

### Pagination and Sorting

```java
public interface UserRepository extends JpaRepository<User, Long> {

    // Pagination
    Page<User> findByStatus(String status, Pageable pageable);

    // Sorting
    List<User> findByDepartment(String dept, Sort sort);

    // Both
    Page<User> findByActiveTrue(Pageable pageable);
}

// Usage
Page<User> page = userRepository.findByStatus("ACTIVE",
    PageRequest.of(0, 20, Sort.by("lastName").ascending()));

List<User> users = userRepository.findByDepartment("IT",
    Sort.by(Sort.Order.asc("lastName"), Sort.Order.desc("firstName")));
```

---

### Page Object

```java
Page<User> page = userRepository.findAll(PageRequest.of(0, 10));

page.getContent();        // List of items on current page
page.getTotalElements();  // Total items across all pages
page.getTotalPages();     // Total number of pages
page.getNumber();         // Current page number (0-indexed)
page.getSize();           // Page size
page.hasNext();           // Has next page?
page.hasPrevious();       // Has previous page?
page.isFirst();           // Is first page?
page.isLast();            // Is last page?
```

---

## Section 5: JPA Configuration

---

### Hibernate DDL Auto

```properties
# DDL strategies
spring.jpa.hibernate.ddl-auto=none      # Do nothing (production)
spring.jpa.hibernate.ddl-auto=validate  # Validate schema matches
spring.jpa.hibernate.ddl-auto=update    # Update schema (dev)
spring.jpa.hibernate.ddl-auto=create    # Create fresh on startup
spring.jpa.hibernate.ddl-auto=create-drop # Create and drop
```

**Recommendations:**
- **Production:** `none` or `validate`
- **Development:** `update` or `create-drop`
- Use migration tools (Flyway/Liquibase) for production

---

### Show SQL

```properties
# Show SQL statements
spring.jpa.show-sql=true

# Format SQL for readability
spring.jpa.properties.hibernate.format_sql=true

# Show parameter values
logging.level.org.hibernate.orm.jdbc.bind=TRACE
```

Output:
```sql
Hibernate:
    select
        u.id,
        u.username,
        u.email
    from
        users u
    where
        u.username = ?
```

---

### Entity Auditing

```java
@Entity
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(updatable = false)
    private String createdBy;

    @LastModifiedBy
    private String updatedBy;
}
```

---

### Enable Auditing

```java
@Configuration
@EnableJpaAuditing
public class JpaConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            // Get current user from security context
            return Optional.ofNullable(
                SecurityContextHolder.getContext()
                    .getAuthentication()
                    .getName()
            );
        };
    }
}
```

---

### Common Pitfalls

**N+1 Problem:**
```java
// BAD - Causes N+1 queries
List<User> users = userRepository.findAll();
users.forEach(u -> System.out.println(u.getOrders().size()));

// GOOD - Use JOIN FETCH
@Query("SELECT u FROM User u LEFT JOIN FETCH u.orders")
List<User> findAllWithOrders();
```

**LazyInitializationException:**
```java
// BAD - Session closed before lazy load
User user = userRepository.findById(1L).get();
// ... session closed ...
user.getOrders(); // LazyInitializationException!

// GOOD - Use @Transactional or fetch eagerly
```

---

## Module 3 Summary

### Key Takeaways

1. **Auto-configuration** makes database setup easy
2. **HikariCP** is the default connection pool
3. **JPA entities** map Java objects to tables
4. **Spring Data repositories** eliminate boilerplate
5. **Query methods** generate queries from names
6. **@Query** provides full control over queries
7. **Pagination** is built-in with Pageable

---

## Lab Exercise

### Lab 3: Building a Data Layer

You will enhance the Task API to:
- Persist tasks in a database
- Use Spring Data JPA repositories
- Implement pagination and sorting
- Create custom queries

**Time:** 60-75 minutes

---

## Questions?

### Next Module: Building RESTful Web Services with Spring Boot
