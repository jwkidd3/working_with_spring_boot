# Lab 7: Service Orchestration - Complete Index

## Quick Navigation

### Getting Started
- **[README.md](README.md)** - START HERE: Lab overview, prerequisites, and quick start
- **[QUICK_REFERENCE.md](QUICK_REFERENCE.md)** - Quick reference guide for APIs and commands

### For Students
- **[starter/README.md](starter/README.md)** - Step-by-step lab instructions
- **[starter/task-service/](starter/task-service/)** - Starting point with basic structure

### For Instructors/Reference
- **[solution/README.md](solution/README.md)** - Complete solution guide and testing
- **[solution/test-workflow.sh](solution/test-workflow.sh)** - Automated test script
- **[solution/task-service/](solution/task-service/)** - Complete Task Service implementation
- **[solution/user-service/](solution/user-service/)** - Complete User Service implementation

### Technical Documentation
- **[ARCHITECTURE_DIAGRAM.md](ARCHITECTURE_DIAGRAM.md)** - System architecture and diagrams
- **[IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md)** - Comprehensive technical summary
- **[FILE_MANIFEST.md](FILE_MANIFEST.md)** - Complete file listing and descriptions

## Documentation Map

```
module-06-service-orchestration/
│
├── INDEX.md                        ← YOU ARE HERE
├── README.md                       ← Start reading here
├── QUICK_REFERENCE.md              ← Keep this handy while coding
├── ARCHITECTURE_DIAGRAM.md         ← Understand the architecture
├── IMPLEMENTATION_SUMMARY.md       ← Deep dive into implementation
├── FILE_MANIFEST.md                ← Find specific files
│
├── starter/
│   ├── README.md                   ← Follow these instructions to build
│   └── task-service/               ← Begin coding here
│       ├── pom.xml
│       └── src/main/
│
└── solution/
    ├── README.md                   ← Solution walkthrough
    ├── test-workflow.sh            ← Run this to test
    ├── task-service/               ← Reference implementation
    └── user-service/               ← Reference implementation
```

## Learning Path

### Beginner Path (Building from Scratch)
1. Read [README.md](README.md) - Understand what you'll build
2. Open [starter/README.md](starter/README.md) - Follow step-by-step
3. Implement each part progressively
4. Test with [solution/test-workflow.sh](solution/test-workflow.sh)
5. Compare with solution code if stuck

### Advanced Path (Learning from Solution)
1. Read [README.md](README.md) - Overview
2. Study [ARCHITECTURE_DIAGRAM.md](ARCHITECTURE_DIAGRAM.md) - Architecture
3. Review [solution/](solution/) code - Implementation
4. Run [solution/test-workflow.sh](solution/test-workflow.sh) - Test
5. Try bonus challenges from [solution/README.md](solution/README.md)

### Quick Reference Path (Already Know Spring)
1. Skim [QUICK_REFERENCE.md](QUICK_REFERENCE.md) - API endpoints
2. Check [ARCHITECTURE_DIAGRAM.md](ARCHITECTURE_DIAGRAM.md) - Architecture
3. Run [solution/test-workflow.sh](solution/test-workflow.sh) - Test
4. Explore solution code directly

## File Count by Category

| Category | Count | Location |
|----------|-------|----------|
| Documentation | 6 | Root level *.md files |
| Scripts | 1 | solution/test-workflow.sh |
| Starter Files | 3 | starter/task-service/ |
| Solution Files (Task Service) | 11 | solution/task-service/ |
| Solution Files (User Service) | 5 | solution/user-service/ |
| **Total** | **26** | |

## Key Topics Covered

### Spring Concepts
- Application Events (`ApplicationEvent`)
- Event Publishing (`ApplicationEventPublisher`)
- Event Listening (`@EventListener`)
- Asynchronous Processing (`@Async`, `@EnableAsync`)
- Thread Pool Configuration (`ThreadPoolTaskExecutor`)
- Transaction Management (`@Transactional`)
- Dependency Injection (Constructor injection)

### Architecture Patterns
- Event-Driven Architecture
- Microservices Architecture
- Repository Pattern
- Service Layer Pattern
- Observer Pattern
- Publisher-Subscriber Pattern

### Spring Boot Features
- Spring Data JPA
- Spring Web MVC
- Jakarta Validation
- RestTemplate
- Embedded Databases (HSQLDB)
- Auto-configuration

### Development Practices
- Clean code organization
- Separation of concerns
- RESTful API design
- Logging and monitoring
- Error handling
- Testing strategies

## Service Specifications

### Task Service (Port 8081)
- **Purpose**: Task management with event orchestration
- **Database**: HSQLDB (in-memory)
- **Key Features**: CRUD operations, event publishing, async processing
- **Dependencies**: Web, JPA, Validation, HSQLDB
- **Files**: 11 Java files + config files

### User Service (Port 8082)
- **Purpose**: User information provider
- **Database**: In-memory Map
- **Key Features**: User lookup, sample data
- **Dependencies**: Web only
- **Files**: 3 Java files + config files

## API Quick Reference

### Task Service Endpoints
```
POST   /api/tasks                    Create task
GET    /api/tasks                    List all tasks
GET    /api/tasks/{id}               Get task by ID
PUT    /api/tasks/{id}/assign        Assign task to user
PUT    /api/tasks/{id}/complete      Complete task
PUT    /api/tasks/{id}               Update task
DELETE /api/tasks/{id}               Delete task
```

### User Service Endpoints
```
GET    /api/users                    List all users
GET    /api/users/{id}               Get user by ID
POST   /api/users                    Create user
```

## Running the Lab

### Start Services
```bash
# Terminal 1
cd solution/user-service
mvn spring-boot:run

# Terminal 2
cd solution/task-service
mvn spring-boot:run
```

### Run Tests
```bash
cd solution
./test-workflow.sh
```

### Manual Testing
```bash
# Create task
curl -X POST http://localhost:8081/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"Test","description":"Test task"}'

# Assign to user 1
curl -X PUT "http://localhost:8081/api/tasks/1/assign?assigneeId=1"

# Complete task
curl -X PUT http://localhost:8081/api/tasks/1/complete
```

## Common Tasks

| Task | File to Check |
|------|---------------|
| See API endpoints | [QUICK_REFERENCE.md](QUICK_REFERENCE.md) |
| Understand architecture | [ARCHITECTURE_DIAGRAM.md](ARCHITECTURE_DIAGRAM.md) |
| Find a specific file | [FILE_MANIFEST.md](FILE_MANIFEST.md) |
| Learn step-by-step | [starter/README.md](starter/README.md) |
| Test the solution | [solution/test-workflow.sh](solution/test-workflow.sh) |
| Troubleshoot issues | [solution/README.md](solution/README.md) - Common Issues |
| See event flow | [ARCHITECTURE_DIAGRAM.md](ARCHITECTURE_DIAGRAM.md) - Event Flow |
| Review implementation | [IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md) |

## Documentation Size

| Document | Size | Purpose |
|----------|------|---------|
| README.md | 7.1 KB | Main overview |
| QUICK_REFERENCE.md | 7.4 KB | Quick reference |
| ARCHITECTURE_DIAGRAM.md | 27 KB | Architecture diagrams |
| IMPLEMENTATION_SUMMARY.md | 9.1 KB | Technical details |
| FILE_MANIFEST.md | 7.0 KB | File listings |
| starter/README.md | ~20 KB | Student instructions |
| solution/README.md | ~25 KB | Solution guide |

## Key Classes to Review

### Event Infrastructure
- `TaskEvent.java` - Custom event class
- `TaskEventPublisher.java` - Publishes events
- `TaskEventListener.java` - Processes events asynchronously
- `AsyncConfig.java` - Configures async processing

### Domain Model
- `Task.java` - Main entity
- `TaskStatus.java` - Enum for task states
- `TaskRepository.java` - JPA repository

### Business Logic
- `TaskService.java` - Service layer with event integration
- `TaskController.java` - REST API endpoints

### User Service
- `UserController.java` - Simple REST API
- `User.java` - Record for user data

## Next Steps

After completing this lab:
1. Implement bonus challenges from solution/README.md
2. Experiment with different event types
3. Add error handling and retry logic
4. Integrate with a message queue (RabbitMQ/Kafka)
5. Add distributed tracing
6. Implement circuit breaker pattern
7. Move to next module

## Support

- **Questions about instructions**: Check starter/README.md
- **Implementation questions**: Review solution code
- **Architecture questions**: See ARCHITECTURE_DIAGRAM.md
- **API questions**: Check QUICK_REFERENCE.md
- **Troubleshooting**: See solution/README.md - Common Issues

---

**Version**: 1.0  
**Last Updated**: 2025-12-14  
**Difficulty**: Intermediate  
**Time Estimate**: 2-3 hours (starter) | 45 minutes (solution review)  
**Prerequisites**: Spring fundamentals, REST APIs, JPA
