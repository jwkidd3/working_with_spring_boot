# Module 4: Building RESTful Web Services with Spring Boot

---

## Module Overview

### What You'll Learn
- Initialize Spring Boot projects effectively
- Implement complete CRUD operations
- Add HATEOAS support
- Monitor and manage your application
- Implement filtering and content negotiation

### Duration: ~4 hours (Day 2 Afternoon)

---

## Section 1: Project Initialization

---

### Spring Initializr

The fastest way to start a Spring Boot project:

- **Web:** https://start.spring.io/
- **IDE Integration:** IntelliJ, Eclipse, VS Code
- **CLI:** `spring init --dependencies=web,data-jpa myproject`

---

### Project Structure

```
my-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/myservice/
│   │   │       ├── MyServiceApplication.java
│   │   │       ├── controller/
│   │   │       ├── service/
│   │   │       ├── repository/
│   │   │       ├── entity/
│   │   │       ├── dto/
│   │   │       ├── exception/
│   │   │       └── config/
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       └── application-prod.properties
│   └── test/
│       └── java/
├── pom.xml
└── README.md
```

---

### Essential Dependencies

```xml
<!-- Core web functionality -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Data persistence -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- HATEOAS -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-hateoas</artifactId>
</dependency>

<!-- Actuator (monitoring) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

---

## Section 2: Complete CRUD Implementation

---

### RESTful Resource Design

For a `Product` resource:

| Operation | HTTP Method | Endpoint | Response Code |
|-----------|-------------|----------|---------------|
| Create | POST | /api/products | 201 Created |
| Read All | GET | /api/products | 200 OK |
| Read One | GET | /api/products/{id} | 200 OK |
| Update | PUT | /api/products/{id} | 200 OK |
| Partial Update | PATCH | /api/products/{id} | 200 OK |
| Delete | DELETE | /api/products/{id} | 204 No Content |

---

### Entity Design

```java
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    private ProductCategory category;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ... lifecycle callbacks, getters, setters
}
```

---

### Request DTOs

```java
public class CreateProductRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100)
    private String name;

    @Size(max = 1000)
    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    @NotNull(message = "Category is required")
    private ProductCategory category;

    // getters and setters
}
```

---

### Response DTOs

```java
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer quantity;
    private ProductCategory category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Static factory method
    public static ProductResponse from(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setQuantity(product.getQuantity());
        response.setCategory(product.getCategory());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());
        return response;
    }
}
```

---

### Complete Controller

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> products = productService.findAll()
            .stream()
            .map(ProductResponse::from)
            .toList();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {
        Product product = productService.findById(id);
        return ResponseEntity.ok(ProductResponse.from(product));
    }

    // ... continued
}
```

---

### Controller (continued)

```java
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request) {
        Product product = productService.create(request);
        URI location = URI.create("/api/products/" + product.getId());
        return ResponseEntity
            .created(location)
            .body(ProductResponse.from(product));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {
        Product product = productService.update(id, request);
        return ResponseEntity.ok(ProductResponse.from(product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
```

---

### PATCH for Partial Updates

```java
@PatchMapping("/{id}")
public ResponseEntity<ProductResponse> patchProduct(
        @PathVariable Long id,
        @RequestBody Map<String, Object> updates) {
    Product product = productService.patch(id, updates);
    return ResponseEntity.ok(ProductResponse.from(product));
}

// In service
public Product patch(Long id, Map<String, Object> updates) {
    Product product = findById(id);

    updates.forEach((key, value) -> {
        switch (key) {
            case "name" -> product.setName((String) value);
            case "price" -> product.setPrice(new BigDecimal(value.toString()));
            case "quantity" -> product.setQuantity((Integer) value);
            // ... other fields
        }
    });

    return productRepository.save(product);
}
```

---

## Section 3: HATEOAS

---

### What is HATEOAS?

**Hypermedia as the Engine of Application State**

- Clients navigate the API through links
- Reduces coupling between client and server
- Self-documenting API responses

```json
{
  "id": 1,
  "name": "Widget",
  "price": 29.99,
  "_links": {
    "self": { "href": "/api/products/1" },
    "update": { "href": "/api/products/1" },
    "delete": { "href": "/api/products/1" },
    "category": { "href": "/api/categories/electronics" }
  }
}
```

---

### Spring HATEOAS Setup

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-hateoas</artifactId>
</dependency>
```

Key classes:
- `RepresentationModel` - Base class for resources
- `EntityModel<T>` - Wrapper for single entity
- `CollectionModel<T>` - Wrapper for collections
- `Link` - Represents a hypermedia link
- `WebMvcLinkBuilder` - Builds links from controllers

---

### Creating a HATEOAS Resource

```java
@GetMapping("/{id}")
public EntityModel<ProductResponse> getProduct(@PathVariable Long id) {
    Product product = productService.findById(id);
    ProductResponse response = ProductResponse.from(product);

    return EntityModel.of(response,
        linkTo(methodOn(ProductController.class).getProduct(id))
            .withSelfRel(),
        linkTo(methodOn(ProductController.class).getAllProducts())
            .withRel("products"),
        linkTo(methodOn(ProductController.class).deleteProduct(id))
            .withRel("delete")
    );
}
```

---

### HATEOAS Response

```json
{
  "id": 1,
  "name": "Widget",
  "description": "A useful widget",
  "price": 29.99,
  "quantity": 100,
  "category": "ELECTRONICS",
  "_links": {
    "self": {
      "href": "http://localhost:8080/api/products/1"
    },
    "products": {
      "href": "http://localhost:8080/api/products"
    },
    "delete": {
      "href": "http://localhost:8080/api/products/1"
    }
  }
}
```

---

### Collection with HATEOAS

```java
@GetMapping
public CollectionModel<EntityModel<ProductResponse>> getAllProducts() {
    List<EntityModel<ProductResponse>> products = productService.findAll()
        .stream()
        .map(product -> {
            ProductResponse response = ProductResponse.from(product);
            return EntityModel.of(response,
                linkTo(methodOn(ProductController.class)
                    .getProduct(product.getId())).withSelfRel());
        })
        .toList();

    return CollectionModel.of(products,
        linkTo(methodOn(ProductController.class).getAllProducts())
            .withSelfRel());
}
```

---

### RepresentationModelAssembler

For cleaner code, create an assembler:

```java
@Component
public class ProductModelAssembler
        implements RepresentationModelAssembler<Product, EntityModel<ProductResponse>> {

    @Override
    public EntityModel<ProductResponse> toModel(Product product) {
        ProductResponse response = ProductResponse.from(product);

        return EntityModel.of(response,
            linkTo(methodOn(ProductController.class)
                .getProduct(product.getId())).withSelfRel(),
            linkTo(methodOn(ProductController.class)
                .getAllProducts()).withRel("products")
        );
    }
}
```

---

### Using the Assembler

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final ProductModelAssembler assembler;

    // constructor...

    @GetMapping
    public CollectionModel<EntityModel<ProductResponse>> getAllProducts() {
        List<Product> products = productService.findAll();
        return assembler.toCollectionModel(products);
    }

    @GetMapping("/{id}")
    public EntityModel<ProductResponse> getProduct(@PathVariable Long id) {
        Product product = productService.findById(id);
        return assembler.toModel(product);
    }
}
```

---

## Section 4: Monitoring with Actuator

---

### Spring Boot Actuator

Production-ready features for monitoring:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

Default endpoint: `/actuator`

---

### Actuator Endpoints

| Endpoint | Description |
|----------|-------------|
| `/actuator/health` | Application health status |
| `/actuator/info` | Application information |
| `/actuator/metrics` | Application metrics |
| `/actuator/env` | Environment properties |
| `/actuator/loggers` | Logger configurations |
| `/actuator/beans` | All Spring beans |
| `/actuator/mappings` | All @RequestMappings |
| `/actuator/threaddump` | Thread dump |
| `/actuator/heapdump` | Heap dump file |

---

### Enable Endpoints

```properties
# Expose specific endpoints
management.endpoints.web.exposure.include=health,info,metrics,env

# Expose all endpoints (not recommended in production)
management.endpoints.web.exposure.include=*

# Change base path
management.endpoints.web.base-path=/manage

# Enable detailed health info
management.endpoint.health.show-details=always

# Application info
info.app.name=Product Service
info.app.version=1.0.0
info.app.description=Product management microservice
```

---

### Health Endpoint

```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "HSQLDB",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 499963174912,
        "free": 123456789012
      }
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

---

### Custom Health Indicator

```java
@Component
public class ExternalServiceHealthIndicator implements HealthIndicator {

    private final ExternalServiceClient client;

    public ExternalServiceHealthIndicator(ExternalServiceClient client) {
        this.client = client;
    }

    @Override
    public Health health() {
        try {
            boolean isHealthy = client.healthCheck();
            if (isHealthy) {
                return Health.up()
                    .withDetail("service", "external-api")
                    .withDetail("status", "reachable")
                    .build();
            }
            return Health.down()
                .withDetail("service", "external-api")
                .withDetail("error", "Service unavailable")
                .build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
```

---

### Metrics

```properties
# Enable metrics
management.endpoints.web.exposure.include=metrics
```

Access metrics:
- `/actuator/metrics` - List all metrics
- `/actuator/metrics/jvm.memory.used` - JVM memory
- `/actuator/metrics/http.server.requests` - HTTP requests

---

### Custom Metrics

```java
@Service
public class ProductService {

    private final Counter productCreatedCounter;
    private final Timer productSearchTimer;

    public ProductService(MeterRegistry registry) {
        this.productCreatedCounter = Counter.builder("products.created")
            .description("Number of products created")
            .register(registry);

        this.productSearchTimer = Timer.builder("products.search")
            .description("Time taken to search products")
            .register(registry);
    }

    public Product create(CreateProductRequest request) {
        Product product = // ... create product
        productCreatedCounter.increment();
        return product;
    }

    public List<Product> search(String query) {
        return productSearchTimer.record(() -> {
            // ... search logic
        });
    }
}
```

---

## Section 5: Filtering and Content Negotiation

---

### Filtering Strategies

```
┌─────────────────────────────────────────────────────────┐
│                  Filtering Options                       │
├───────────────────┬─────────────────────────────────────┤
│  Static Filtering │  Same fields for all responses      │
│  Dynamic Filtering│  Client specifies fields            │
│  Query Filtering  │  Filter by query parameters         │
└───────────────────┴─────────────────────────────────────┘
```

---

### Static Filtering with @JsonIgnore

```java
public class UserResponse {
    private Long id;
    private String username;
    private String email;

    @JsonIgnore  // Never include in JSON
    private String password;

    @JsonIgnore
    private String ssn;

    // getters and setters
}
```

---

### Static Filtering with @JsonIgnoreProperties

```java
@JsonIgnoreProperties({"password", "ssn", "internalId"})
public class UserResponse {
    private Long id;
    private Long internalId;
    private String username;
    private String password;
    private String ssn;

    // getters and setters
}
```

---

### Dynamic Filtering with @JsonFilter

```java
@JsonFilter("ProductFilter")
public class Product {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal cost;  // Internal field
    private Integer quantity;
}

// In controller
@GetMapping("/{id}")
public MappingJacksonValue getProduct(@PathVariable Long id) {
    Product product = productService.findById(id);

    SimpleBeanPropertyFilter filter = SimpleBeanPropertyFilter
        .filterOutAllExcept("id", "name", "price", "quantity");

    FilterProvider filters = new SimpleFilterProvider()
        .addFilter("ProductFilter", filter);

    MappingJacksonValue mapping = new MappingJacksonValue(product);
    mapping.setFilters(filters);

    return mapping;
}
```

---

### Query Parameter Filtering

```java
@GetMapping
public List<ProductResponse> getProducts(
        @RequestParam(required = false) String category,
        @RequestParam(required = false) BigDecimal minPrice,
        @RequestParam(required = false) BigDecimal maxPrice,
        @RequestParam(required = false) Boolean inStock) {

    return productService.findWithFilters(category, minPrice, maxPrice, inStock)
        .stream()
        .map(ProductResponse::from)
        .toList();
}
```

---

### Specification Pattern for Complex Filtering

```java
public class ProductSpecification {

    public static Specification<Product> hasCategory(String category) {
        return (root, query, cb) ->
            category == null ? null : cb.equal(root.get("category"), category);
    }

    public static Specification<Product> priceBetween(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return null;
            if (min == null) return cb.lessThanOrEqualTo(root.get("price"), max);
            if (max == null) return cb.greaterThanOrEqualTo(root.get("price"), min);
            return cb.between(root.get("price"), min, max);
        };
    }

    public static Specification<Product> inStock() {
        return (root, query, cb) ->
            cb.greaterThan(root.get("quantity"), 0);
    }
}
```

---

### Using Specifications

```java
@Service
public class ProductService {

    private final ProductRepository repository;

    public List<Product> findWithFilters(String category,
                                         BigDecimal minPrice,
                                         BigDecimal maxPrice,
                                         Boolean inStock) {
        Specification<Product> spec = Specification
            .where(ProductSpecification.hasCategory(category))
            .and(ProductSpecification.priceBetween(minPrice, maxPrice));

        if (Boolean.TRUE.equals(inStock)) {
            spec = spec.and(ProductSpecification.inStock());
        }

        return repository.findAll(spec);
    }
}
```

---

### Content Negotiation

Spring Boot supports multiple response formats:

```properties
# Enable content negotiation
spring.mvc.contentnegotiation.favor-parameter=true
spring.mvc.contentnegotiation.parameter-name=format
```

Request formats:
- `Accept: application/json` → JSON response
- `Accept: application/xml` → XML response
- `/api/products?format=json` → JSON response
- `/api/products?format=xml` → XML response

---

### XML Support

```xml
<!-- Add Jackson XML support -->
<dependency>
    <groupId>com.fasterxml.jackson.dataformat</groupId>
    <artifactId>jackson-dataformat-xml</artifactId>
</dependency>
```

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @GetMapping(produces = {
        MediaType.APPLICATION_JSON_VALUE,
        MediaType.APPLICATION_XML_VALUE
    })
    public List<ProductResponse> getProducts() {
        return productService.findAll()
            .stream()
            .map(ProductResponse::from)
            .toList();
    }
}
```

---

## Section 6: API Versioning

---

### Versioning Strategies

```
┌─────────────────────────────────────────────────────────┐
│                  API Versioning                          │
├─────────────────────────────────────────────────────────┤
│  URI Path      │  /api/v1/products                      │
│  Query Param   │  /api/products?version=1               │
│  Header        │  X-API-Version: 1                      │
│  Media Type    │  Accept: application/vnd.api.v1+json   │
└─────────────────────────────────────────────────────────┘
```

---

### URI Path Versioning

```java
@RestController
@RequestMapping("/api/v1/products")
public class ProductControllerV1 {
    // V1 implementation
}

@RestController
@RequestMapping("/api/v2/products")
public class ProductControllerV2 {
    // V2 implementation with new features
}
```

**Pros:** Clear, cacheable, easy to understand
**Cons:** URL pollution, breaks REST principles

---

### Header Versioning

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @GetMapping(headers = "X-API-Version=1")
    public List<ProductResponseV1> getProductsV1() {
        // V1 response
    }

    @GetMapping(headers = "X-API-Version=2")
    public List<ProductResponseV2> getProductsV2() {
        // V2 response with additional fields
    }
}
```

**Pros:** Clean URLs, flexible
**Cons:** Not visible in URL, harder to test

---

## Module 4 Summary

### Key Takeaways

1. **CRUD operations** follow RESTful conventions
2. **DTOs** separate API contracts from entities
3. **HATEOAS** enables discoverable APIs
4. **Actuator** provides production-ready monitoring
5. **Filtering** can be static or dynamic
6. **Content negotiation** supports multiple formats
7. **Versioning** enables API evolution

---

## Lab Exercise

### Lab 4: Advanced REST Features

You will enhance the Task API with:
- HATEOAS links
- Actuator monitoring
- Custom metrics
- Advanced filtering
- API documentation

**Time:** 60-75 minutes

---

## Questions?

### Next Module: Spring Security
