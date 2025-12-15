# Working with Spring Boot
A comprehensive hands-on workshop for building microservices using the Spring Boot framework.

## Course Overview

**Skill Level:** Intermediate
**Prerequisites:**
- Solid background in Java programming
- Basic exposure to the Spring Framework

## Course Objectives

By the end of this workshop, participants will be able to:
1. Understand the difference between Spring and Spring Boot
2. Explain code-level concepts that are key to Spring Boot
3. Implement several microservices that interact as a single application using Spring Boot

## Course Structure

### Day 1: Foundations

| Activity | Topic |
|----------|-------|
| Presentation | Spring Framework Fundamentals |
| **Lab 1** | Spring Fundamentals |
| **Lab 1b** | Dependency Injection Deep Dive |
| Presentation | Introduction to Web Services |
| **Lab 2** | REST API Basics |
| **Lab 3** | Configuration & Profiles |

### Day 2: Building RESTful Services

| Activity | Topic |
|----------|-------|
| Presentation | Spring Boot with Databases |
| **Lab 4** | Database Integration |
| **Lab 4b** | Advanced Queries & Transactions |
| Presentation | HATEOAS & Actuator |
| **Lab 5a** | Implementing HATEOAS |
| **Lab 5b** | Actuator & Monitoring |

### Day 3: Advanced Topics

| Activity | Topic |
|----------|-------|
| Presentation | Spring Security |
| **Lab 6** | Spring Security & JWT |
| Presentation | Service Orchestration |
| **Lab 7** | Service Orchestration |
| Presentation | Testing & Best Practices |
| **Lab 8** | Testing & Deployment |
| **Lab 8b** | API Documentation with OpenAPI |

## Module Details

### Module 1: Spring Framework Fundamentals
- Inversion of Control (IoC) and Dependency Injection
- Java Annotations
- Spring Configuration
- Bean Lifecycle
- Injection Types (Constructor, Setter, Field)
- @Qualifier and @Primary
- Bean Scopes

### Module 2: Introduction to Web Services
- HTTP Fundamentals
- SOAP vs REST
- Exception Handling
- Request Validation
- Configuration & Profiles

### Module 3: Using Spring Boot with Databases
- DataSource Configuration
- Connection Pooling (HikariCP)
- JPA and Hibernate
- Spring Data JPA Repositories
- Custom Queries (JPQL, Native SQL)
- Specifications for Dynamic Queries
- Transactions and Propagation
- Optimistic Locking
- JPA Auditing

### Module 4: Building RESTful Web Services
- Project Initialization
- CRUD Operations
- HATEOAS Implementation
- Monitoring with Actuator
- Custom Metrics
- Filtering and Content Negotiation

### Module 5: Spring Security
- Authentication and Authorization
- Basic Authentication
- JWT Token-Based Authentication
- Method-Level Security
- Spring Boot Data REST Security

### Module 6: Service Orchestration
- Synchronous Communication (REST, RestTemplate)
- Event-Driven Architecture with Spring Events
- Building Multi-Service Applications

### Module 7: Additional Topics
- Spring Boot CLI
- Testing Strategies (Unit, Integration, Security)
- Packaging & Deployment
- API Documentation with OpenAPI/Swagger
- Best Practices

## Lab Summary

| Lab | Title | Key Skills |
|-----|-------|------------|
| 1 | Spring Fundamentals | IoC, DI, Beans, Profiles |
| 1b | DI Deep Dive | Injection Types, @Qualifier, Scopes, Lifecycle |
| 2 | REST API Basics | Controllers, DTOs, Validation |
| 3 | Configuration | @ConfigurationProperties, Profiles |
| 4 | Database Integration | JPA, Repositories, Basic Queries |
| 4b | Advanced Queries | JPQL, Specifications, Transactions, Auditing |
| 5a | HATEOAS | Hypermedia, Links, Assemblers |
| 5b | Actuator | Health, Metrics, Monitoring |
| 6 | Security | Authentication, JWT, Authorization |
| 7 | Service Orchestration | REST Clients, Events, Multi-Service |
| 8 | Testing & Deployment | JUnit, MockMvc, Packaging |
| 8b | API Documentation | OpenAPI, Swagger UI, Schema Customization |

## Directory Structure

```
working_with_spring_boot/
├── presentations/
│   ├── module-01-spring-fundamentals/
│   │   ├── index.html          # reveal.js presentation
│   │   └── slides.md           # markdown source
│   ├── module-02-web-services-intro/
│   ├── module-03-spring-boot-databases/
│   ├── module-04-restful-services/
│   ├── module-05-spring-security/
│   ├── module-06-service-orchestration/
│   └── module-07-additional-topics/
├── labs/
│   ├── module-01-spring-fundamentals/
│   │   ├── LAB.md
│   │   ├── starter/
│   │   └── solution/
│   ├── module-01b-di-deep-dive/
│   │   └── LAB.md
│   ├── module-02-web-services-intro/
│   │   ├── LAB.md
│   │   ├── starter/
│   │   └── solution/
│   ├── module-03-configuration/
│   │   ├── LAB.md
│   │   ├── starter/
│   │   └── solution/
│   ├── module-04-spring-boot-databases/
│   │   ├── LAB.md
│   │   ├── starter/
│   │   └── solution/
│   ├── module-04b-advanced-queries/
│   │   └── LAB.md
│   ├── module-05a-hateoas/
│   │   ├── LAB.md
│   │   ├── starter/
│   │   └── solution/
│   ├── module-05b-actuator/
│   │   ├── LAB.md
│   │   ├── starter/
│   │   └── solution/
│   ├── module-06-spring-security/
│   │   ├── LAB.md
│   │   ├── starter/
│   │   └── solution/
│   ├── module-07-service-orchestration/
│   │   └── LAB.md
│   ├── module-08-testing-deployment/
│   │   └── LAB.md
│   └── module-08b-api-documentation/
│       └── LAB.md
└── README.md
```

## Prerequisites Setup

### Required Software
- JDK 17 or higher
- Maven 3.6+ or Gradle 7+
- IDE (IntelliJ IDEA recommended)
- Git

### Recommended
- Postman or similar API testing tool
- VS Code with REST Client extension

## Running the Labs

Each lab module contains:
- **LAB.md**: Step-by-step instructions
- **starter/**: Starting point for the lab
- **solution/**: Complete solution for reference

### Quick Start
```bash
# Navigate to a lab's starter project
cd labs/module-01-spring-fundamentals/starter

# Build and run
./mvnw spring-boot:run

# Run tests
./mvnw test
```

### Running Presentations
Open any `index.html` file in a web browser to view the reveal.js presentation.

## Technologies Covered

- **Spring Boot 3.2.x**
- **Spring MVC**
- **Spring Data JPA**
- **Spring Security**
- **Spring Boot Actuator**
- **Spring HATEOAS**
- **Micrometer (Metrics)**
- **HSQLDB (In-Memory Database)**
- **PostgreSQL**
- **JWT (JSON Web Tokens)**
- **SpringDoc OpenAPI (Swagger)**

## Additional Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Guides](https://spring.io/guides)
- [Spring Initializr](https://start.spring.io/)
- [Baeldung Spring Tutorials](https://www.baeldung.com/spring-boot)

## License

This workshop material is provided for educational purposes.
