# Working with Spring Boot 
A comprehensive hands-on workshop for building microservices using the Spring Boot framework.

## Course Overview

**Duration:** 3 Days (12 Labs - 4 per day, ~7 hours/day)
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

### Day 1: Foundations (~7 hours)

| Time | Activity | Duration | Topic |
|------|----------|----------|-------|
| AM | Presentation | ~1.5 hrs | Spring Framework Fundamentals |
| AM | **Lab 1** | 45-60 min | Spring Fundamentals |
| AM | **Lab 1b** | 45-60 min | Dependency Injection Deep Dive |
| PM | Presentation | ~1 hr | Introduction to Web Services |
| PM | **Lab 2** | 45-60 min | REST API Basics |
| PM | **Lab 3** | 45-60 min | Configuration & Profiles |

### Day 2: Building RESTful Services (~7 hours)

| Time | Activity | Duration | Topic |
|------|----------|----------|-------|
| AM | Presentation | ~1.5 hrs | Spring Boot with Databases |
| AM | **Lab 4** | 60-75 min | Database Integration |
| AM | **Lab 4b** | 45-60 min | Advanced Queries & Transactions |
| PM | Presentation | ~1.5 hrs | HATEOAS & Actuator |
| PM | **Lab 5a** | 45-60 min | Implementing HATEOAS |
| PM | **Lab 5b** | 45-60 min | Actuator & Monitoring |

### Day 3: Advanced Topics (~7 hours)

| Time | Activity | Duration | Topic |
|------|----------|----------|-------|
| AM | Presentation | ~1.5 hrs | Spring Security |
| AM | **Lab 6** | 60-75 min | Spring Security & JWT |
| PM | Presentation | ~1 hr | Service Orchestration |
| PM | **Lab 7** | 60-75 min | Service Orchestration |
| PM | Presentation | ~1 hr | Testing & Best Practices |
| PM | **Lab 8** | 45-60 min | Testing & Deployment |
| PM | **Lab 8b** | 45-60 min | API Documentation with OpenAPI |

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

| Lab | Title | Duration | Key Skills |
|-----|-------|----------|------------|
| 1 | Spring Fundamentals | 45-60 min | IoC, DI, Beans, Profiles |
| 1b | DI Deep Dive | 45-60 min | Injection Types, @Qualifier, Scopes, Lifecycle |
| 2 | REST API Basics | 45-60 min | Controllers, DTOs, Validation |
| 3 | Configuration | 45-60 min | @ConfigurationProperties, Profiles |
| 4 | Database Integration | 60-75 min | JPA, Repositories, Basic Queries |
| 4b | Advanced Queries | 45-60 min | JPQL, Specifications, Transactions, Auditing |
| 5a | HATEOAS | 45-60 min | Hypermedia, Links, Assemblers |
| 5b | Actuator | 45-60 min | Health, Metrics, Monitoring |
| 6 | Security | 60-75 min | Authentication, JWT, Authorization |
| 7 | Service Orchestration | 60-75 min | REST Clients, Events, Multi-Service |
| 8 | Testing & Deployment | 45-60 min | JUnit, MockMvc, Packaging |
| 8b | API Documentation | 45-60 min | OpenAPI, Swagger UI, Schema Customization |

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
