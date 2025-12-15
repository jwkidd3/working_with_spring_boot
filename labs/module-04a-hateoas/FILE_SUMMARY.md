# Lab 5a File Summary

## Complete Lab Structure

### Root Directory Files
- **README.md** (11 KB) - Complete lab overview and structure
- **LAB_INSTRUCTIONS.md** (17 KB) - Detailed step-by-step instructions
- **QUICK_REFERENCE.md** (12 KB) - Quick reference for common HATEOAS patterns
- **LAB.md** (20 KB) - Original lab file (existing)

### Starter Project (Minimal Implementation)
Total: 7 files

**Configuration:**
- `starter/pom.xml` - Dependencies: Web, JPA, Validation, HSQLDB (no HATEOAS)
- `starter/README.md` - Starter-specific instructions
- `starter/src/main/resources/application.properties` - HSQLDB configuration

**Java Files (4):**
- `TaskApiApplication.java` - Main Spring Boot application
- `entity/Task.java` - JPA entity (135 lines)
- `entity/TaskStatus.java` - Enum with 4 states
- `entity/TaskPriority.java` - Enum with 4 priority levels
- `repository/TaskRepository.java` - JPA repository with query methods

**What Students Build:**
- dto/ package (3 classes)
- assembler/ package (1 class)
- service/ package (1 class)
- controller/ package (2 classes)
- exception/ package (2 classes)
- config/ package (1 class)

### Solution Project (Complete Implementation)
Total: 20 files, 1,278 lines of Java code

**Configuration:**
- `solution/pom.xml` - Includes spring-boot-starter-hateoas
- `solution/README.md` - Solution documentation
- `solution/src/main/resources/application.properties` - HATEOAS + HSQLDB config

**Package Structure:**

1. **Root** (1 file)
   - `TaskApiApplication.java` (16 lines)

2. **entity/** (3 files, 187 lines)
   - `Task.java` (135 lines) - JPA entity with validation
   - `TaskStatus.java` (26 lines) - CREATED, IN_PROGRESS, COMPLETED, CANCELLED
   - `TaskPriority.java` (26 lines) - LOW, MEDIUM, HIGH, CRITICAL

3. **repository/** (1 file, 45 lines)
   - `TaskRepository.java` - JPA repository with custom queries

4. **dto/** (3 files, 259 lines)
   - `TaskResponse.java` (109 lines) - Extends RepresentationModel with @Relation
   - `CreateTaskRequest.java` (71 lines) - Creation DTO with validation
   - `UpdateTaskRequest.java` (79 lines) - Update DTO

5. **assembler/** (1 file, 123 lines)
   - `TaskModelAssembler.java` - Extends RepresentationModelAssemblerSupport
     - Converts Task to TaskResponse
     - Adds self and collection links
     - Adds status-based conditional links

6. **service/** (1 file, 156 lines)
   - `TaskService.java` - Business logic
     - CRUD operations
     - State transitions (start, complete, cancel, reopen)
     - Query methods (by status, by priority)

7. **controller/** (2 files, 257 lines)
   - `TaskController.java` (187 lines) - Main REST controller
     - GET /api/tasks (paginated)
     - GET /api/tasks/{id}
     - GET /api/tasks/status/{status}
     - GET /api/tasks/priority/{priority}
     - POST /api/tasks
     - PUT /api/tasks/{id}
     - DELETE /api/tasks/{id}
     - PATCH /api/tasks/{id}/start
     - PATCH /api/tasks/{id}/complete
     - PATCH /api/tasks/{id}/cancel
     - PATCH /api/tasks/{id}/reopen
   - `RootController.java` (70 lines) - API discovery endpoint
     - GET /api (returns links to all resources)

8. **exception/** (2 files, 108 lines)
   - `TaskNotFoundException.java` (15 lines) - Custom exception
   - `GlobalExceptionHandler.java` (93 lines) - @RestControllerAdvice
     - TaskNotFoundException → 404 with Problem Details
     - IllegalStateException → 409 with Problem Details
     - MethodArgumentNotValidException → 400 with Problem Details
     - Generic Exception → 500 with Problem Details

9. **config/** (1 file, 127 lines)
   - `DataInitializer.java` - CommandLineRunner
     - Creates 10 sample tasks
     - Various states and priorities
     - Different due dates

## Key Features Implemented

### HATEOAS Components
1. **RepresentationModel** - TaskResponse extends it
2. **RepresentationModelAssemblerSupport** - TaskModelAssembler
3. **WebMvcLinkBuilder** - Type-safe link creation
4. **PagedResourcesAssembler** - Automatic pagination links
5. **HAL Format** - Standard hypermedia format

### Links Provided
1. **Self Link** - On every resource
2. **Collection Link** - On single resources
3. **Pagination Links** - first, last, next, prev
4. **Status-Based Conditional Links:**
   - CREATED: start, cancel
   - IN_PROGRESS: complete, cancel
   - COMPLETED: reopen
   - CANCELLED: reopen
5. **CRUD Links** - update, delete

### Error Handling
- RFC 7807 Problem Details format
- Consistent error responses
- Validation error details
- Proper HTTP status codes

### State Machine
```
CREATED → start → IN_PROGRESS → complete → COMPLETED
   ↓                    ↓                      ↓
cancel              cancel                 reopen
   ↓                    ↓                      ↓
CANCELLED ← ← ← ← ← ← ← ← ← ← ← ← ← ← ← ← ← ← ↓
   ↓                                            ↓
reopen ← ← ← ← ← ← ← ← ← ← ← ← ← ← ← ← ← ← ← ←
   ↓
CREATED
```

## File Sizes

### Starter Project
- Total Java: ~250 lines
- Configuration: ~100 lines
- Documentation: ~5 KB

### Solution Project
- Total Java: 1,278 lines
- Configuration: ~150 lines
- Documentation: ~25 KB

## Testing the Solution

### Build
```bash
cd solution
mvn clean install
```

### Run
```bash
mvn spring-boot:run
```

### Access
- API Root: http://localhost:8080/api
- All Tasks: http://localhost:8080/api/tasks
- Single Task: http://localhost:8080/api/tasks/1

## Documentation Files

1. **README.md** - Overview, structure, learning objectives
2. **LAB_INSTRUCTIONS.md** - Step-by-step implementation guide
3. **QUICK_REFERENCE.md** - Code snippets and patterns
4. **starter/README.md** - Starter-specific guidance
5. **solution/README.md** - Solution documentation

## Technologies Used

- Spring Boot 3.2.1
- Spring HATEOAS
- Spring Data JPA
- Bean Validation (Jakarta)
- HSQLDB (in-memory)
- Java 17
- Maven 3.6+

## Line Count Breakdown

| Package    | Files | Lines | Purpose                          |
|------------|-------|-------|----------------------------------|
| entity     | 3     | 187   | Domain models                    |
| repository | 1     | 45    | Data access                      |
| dto        | 3     | 259   | Data transfer objects            |
| assembler  | 1     | 123   | Link generation                  |
| service    | 1     | 156   | Business logic                   |
| controller | 2     | 257   | REST endpoints                   |
| exception  | 2     | 108   | Error handling                   |
| config     | 1     | 127   | Application configuration        |
| root       | 1     | 16    | Main application                 |
| **Total**  | **15**| **1,278** | **Complete HATEOAS implementation** |

## What Makes This Lab Effective

1. **Progressive Learning** - Starter provides foundation, students add HATEOAS
2. **Complete Solution** - Full working implementation for reference
3. **Real-World Example** - Task management is relatable
4. **State Machine** - Demonstrates conditional links well
5. **Best Practices** - DTOs, error handling, validation
6. **Documentation** - Multiple levels of guidance
7. **Testable** - Easy to verify with cURL
8. **Extensible** - Students can add more features

## Common Student Questions Addressed

1. **Why DTOs?** - Separation of concerns, avoid circular references
2. **Why RepresentationModel?** - Foundation for adding links
3. **Why Assembler?** - Centralized link creation logic
4. **Why Conditional Links?** - Guide clients on available actions
5. **Why API Root?** - Discoverability, single entry point
6. **Why Problem Details?** - Standard error format
7. **Why Pagination?** - Handle large datasets efficiently
8. **Why HAL?** - Standard hypermedia format

## Assessment Opportunities

Students can be evaluated on:
- Correct use of Spring HATEOAS annotations
- Proper link generation
- Status-based conditional logic
- Error handling implementation
- API design principles
- Code organization
- Documentation

## Time Estimates

- Reading documentation: 15 minutes
- Understanding starter: 10 minutes
- Implementing DTOs: 15 minutes
- Building assembler: 25 minutes
- Creating service: 20 minutes
- Developing controllers: 30 minutes
- Exception handling: 15 minutes
- Testing: 20 minutes
- **Total**: ~90-120 minutes

This comprehensive lab provides everything needed to learn HATEOAS with Spring Boot!
