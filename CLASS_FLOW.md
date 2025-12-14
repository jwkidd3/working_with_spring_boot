# Spring Boot Workshop - Class Flow

## Day 1: Foundations

### Module 1: Spring Framework Fundamentals
**Presentation:** `presentations/module-01-spring-fundamentals/`
- Introduction to Spring Framework
- Inversion of Control (IoC)
- Dependency Injection (DI)
- Java Annotations
- Spring Configuration
- Advanced DI Patterns

**Lab 1: Spring Fundamentals** (45-60 minutes)
`labs/module-01-spring-fundamentals/`
- Create a Spring Boot application
- Implement dependency injection
- Work with profiles
- Use lifecycle callbacks

**Lab 1b: DI Deep Dive** (45-60 minutes)
`labs/module-01b-di-deep-dive/`
- Injection patterns (constructor, setter, field)
- @Qualifier and @Primary
- Custom qualifier annotations
- Bean scopes (singleton, prototype, request, thread)
- Lifecycle callbacks
- Circular dependency resolution

---

### Module 2: Introduction to Web Services
**Presentation:** `presentations/module-02-web-services-intro/`
- HTTP Fundamentals
- SOAP vs REST
- Exception Handling
- Request Validation
- Configuration & Profiles

**Lab 2: REST Controller Basics** (45-60 minutes)
`labs/module-02-web-services-intro/`
- Create REST endpoints
- Handle HTTP methods
- Return proper status codes

**Lab 3: Configuration & Profiles** (45-60 minutes)
`labs/module-03-configuration/`
- Use @Value and @ConfigurationProperties
- Create profile-specific configurations
- Work with @ConditionalOnProperty

---

## Day 2: Data & REST APIs

### Module 3: Using Spring Boot with Databases
**Presentation:** `presentations/module-03-spring-boot-databases/`
- Database Configuration
- JPA and Hibernate Basics
- Spring Data JPA
- Custom Queries
- Query Methods
- Advanced JPA Features (Specifications, Versioning, Auditing)

**Lab 4: Spring Data JPA** (60-75 minutes)
`labs/module-03-spring-boot-databases/`
- Configure HSQLDB datasource
- Create JPA entities
- Implement repositories
- Use query methods

**Lab 4b: Advanced Queries** (45-60 minutes)
`labs/module-04b-advanced-queries/`
- JPA Specifications for dynamic queries
- @Query with JPQL and native SQL
- Optimistic locking with @Version
- JPA Auditing (@CreatedDate, @LastModifiedDate)
- Transaction propagation

---

### Module 4: Building RESTful Web Services
**Presentation:** `presentations/module-04-restful-services/`
- Project Initialization
- CRUD Operations
- HATEOAS
- Spring Boot Actuator
- Filtering and Content Negotiation

**Lab 5: Complete REST API** (60-75 minutes)
`labs/module-04-restful-services/`
- Build full CRUD REST API
- Implement proper HTTP semantics
- Add validation

**Lab 5a: HATEOAS** (30-45 minutes)
`labs/module-05a-hateoas/`
- Add hypermedia links
- Use RepresentationModel
- Create link relations

**Lab 5b: Actuator** (30-45 minutes)
`labs/module-05b-actuator/`
- Enable Actuator endpoints
- Create custom health indicators
- Expose application metrics

---

## Day 3: Security, Integration & Advanced Topics

### Module 5: Spring Security
**Presentation:** `presentations/module-05-spring-security/`
- Spring Security Fundamentals
- Authentication
- Authorization
- JWT Security

**Lab 6: Spring Security** (60-75 minutes)
`labs/module-05-spring-security/`
- Configure Spring Security
- Implement authentication
- Add role-based authorization
- Secure REST endpoints

---

### Module 6: Service Orchestration
**Presentation:** `presentations/module-06-service-orchestration/`
- Microservices Communication
- REST Clients (RestClient, WebClient)
- Spring Application Events
- Production Messaging Overview

**Lab 7: Service Integration** (60-75 minutes)
`labs/module-06-service-orchestration/`
- Use Spring Events for decoupling
- Implement async event processing
- Create event-driven workflows

---

### Module 7: Additional Topics & Wrap-up
**Presentation:** `presentations/module-07-additional-topics/`
- Spring Boot CLI
- Testing Strategies
- Packaging and Deployment
- API Documentation with OpenAPI
- Best Practices

**Lab 8b: API Documentation** (45-60 minutes)
`labs/module-08b-api-documentation/`
- Add SpringDoc OpenAPI dependency
- Document APIs with @Operation, @Schema
- Configure Swagger UI
- Generate OpenAPI specification

**Lab 8: Testing & Packaging** (45-60 minutes)
`labs/module-07-additional-topics/`
- Write unit and integration tests
- Package application as JAR
- Configure for deployment

---

## Quick Reference

| Day | Module | Labs |
|-----|--------|------|
| 1 | Module 1: Spring Fundamentals | Lab 1, Lab 1b |
| 1 | Module 2: Web Services Intro | Lab 2, Lab 3 |
| 2 | Module 3: Databases | Lab 4, Lab 4b |
| 2 | Module 4: RESTful Services | Lab 5, Lab 5a, Lab 5b |
| 3 | Module 5: Security | Lab 6 |
| 3 | Module 6: Service Orchestration | Lab 7 |
| 3 | Module 7: Additional Topics | Lab 8b, Lab 8 |

## Lab Folder Reference

| Lab | Folder | Focus |
|-----|--------|-------|
| Lab 1 | `module-01-spring-fundamentals` | DI basics, profiles, lifecycle |
| Lab 1b | `module-01b-di-deep-dive` | Advanced DI patterns |
| Lab 2 | `module-02-web-services-intro` | REST controllers |
| Lab 3 | `module-03-configuration` | Properties, profiles |
| Lab 4 | `module-03-spring-boot-databases` | JPA, repositories |
| Lab 4b | `module-04b-advanced-queries` | Specifications, transactions |
| Lab 5 | `module-04-restful-services` | Full CRUD API |
| Lab 5a | `module-05a-hateoas` | Hypermedia |
| Lab 5b | `module-05b-actuator` | Monitoring |
| Lab 6 | `module-05-spring-security` | Authentication, authorization |
| Lab 7 | `module-06-service-orchestration` | Events, integration |
| Lab 8 | `module-07-additional-topics` | Testing, packaging |
| Lab 8b | `module-08b-api-documentation` | OpenAPI/Swagger |
