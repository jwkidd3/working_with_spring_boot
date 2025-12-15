# Service Orchestration Architecture

## System Overview

```
┌─────────────────────────────────────────────────────────────────────────┐
│                     Service Orchestration Lab                           │
│                Multi-Service Event-Driven Architecture                  │
└─────────────────────────────────────────────────────────────────────────┘

                              ┌──────────────┐
                              │    Client    │
                              │  (curl/REST) │
                              └──────┬───────┘
                                     │
                     ┌───────────────┼───────────────┐
                     │               │               │
                     ▼               ▼               ▼
              POST /tasks     PUT /assign    PUT /complete
                     │               │               │
                     └───────────────┴───────────────┘
                                     │
                     ┌───────────────▼────────────────┐
                     │       Task Service             │
                     │       (Port 8081)              │
                     │                                │
                     │  ┌──────────────────────────┐  │
                     │  │   TaskController         │  │
                     │  └──────────┬───────────────┘  │
                     │             │                  │
                     │  ┌──────────▼───────────────┐  │
                     │  │   TaskService            │  │
                     │  │   @Transactional         │  │
                     │  └──────┬────────────┬──────┘  │
                     │         │            │         │
                     │  ┌──────▼───────┐    │         │
                     │  │TaskRepository│    │         │
                     │  │  (JPA)       │    │         │
                     │  └──────┬───────┘    │         │
                     │         │            │         │
                     │  ┌──────▼───────┐    │         │
                     │  │   HSQLDB     │    │         │
                     │  │  (in-memory) │    │         │
                     │  └──────────────┘    │         │
                     │                      │         │
                     │         ┌────────────▼──────┐  │
                     │         │ TaskEventPublisher│  │
                     │         └────────┬──────────┘  │
                     │                  │             │
                     └──────────────────┼─────────────┘
                                        │
                                        │ ApplicationEventPublisher
                                        │ .publishEvent()
                                        │
                     ┌──────────────────▼─────────────┐
                     │       Spring Event Bus         │
                     │     (In-Memory, Async)         │
                     └──────────────────┬─────────────┘
                                        │
                                        │ @EventListener
                                        │ @Async("taskExecutor")
                                        │
                     ┌──────────────────▼─────────────┐
                     │    TaskEventListener           │
                     │    (Async Thread Pool)         │
                     │                                │
                     │  ┌──────────────────────────┐  │
                     │  │  handleTaskEvent()       │  │
                     │  │  - TASK_CREATED          │  │
                     │  │  - TASK_ASSIGNED         │  │
                     │  │  - TASK_COMPLETED        │  │
                     │  └──────────┬───────────────┘  │
                     │             │                  │
                     │             │ RestTemplate     │
                     │             │ HTTP GET         │
                     └─────────────┼──────────────────┘
                                   │
                                   ▼
                     ┌─────────────────────────────────┐
                     │       User Service              │
                     │       (Port 8082)               │
                     │                                 │
                     │  ┌──────────────────────────┐   │
                     │  │   UserController         │   │
                     │  └──────────┬───────────────┘   │
                     │             │                   │
                     │  ┌──────────▼───────────────┐   │
                     │  │  In-Memory User Store    │   │
                     │  │  Map<Long, User>         │   │
                     │  │                          │   │
                     │  │  Sample Data:            │   │
                     │  │  1 - John Doe            │   │
                     │  │  2 - Jane Smith          │   │
                     │  │  3 - Bob Johnson         │   │
                     │  │  4 - Alice Williams      │   │
                     │  │  5 - Charlie Brown       │   │
                     │  └──────────────────────────┘   │
                     └─────────────────────────────────┘
```

## Event Flow Diagram

```
Task Creation Flow:
──────────────────

Client                 TaskService         EventPublisher       EventBus       EventListener
  │                         │                     │                │                 │
  │  POST /api/tasks        │                     │                │                 │
  ├────────────────────────>│                     │                │                 │
  │                         │                     │                │                 │
  │                         │ save(task)          │                │                 │
  │                         ├──────────┐          │                │                 │
  │                         │          │          │                │                 │
  │                         │<─────────┘          │                │                 │
  │                         │                     │                │                 │
  │                         │ publishTaskCreated()│                │                 │
  │                         ├────────────────────>│                │                 │
  │                         │                     │                │                 │
  │                         │                     │ publishEvent() │                 │
  │                         │                     ├───────────────>│                 │
  │                         │                     │                │                 │
  │<────────────────────────┤                     │                │  [async thread] │
  │  201 Created            │                     │                │                 │
  │                         │                     │                │  handleEvent()  │
  │                         │                     │                ├────────────────>│
  │                         │                     │                │                 │
  │                         │                     │                │    Log event    │
  │                         │                     │                │                 │


Task Assignment Flow:
─────────────────────

Client              TaskService      EventPublisher    EventBus    EventListener    UserService
  │                      │                  │              │              │               │
  │  PUT /assign         │                  │              │              │               │
  ├─────────────────────>│                  │              │              │               │
  │                      │                  │              │              │               │
  │                      │ update(task)     │              │              │               │
  │                      ├─────────┐        │              │              │               │
  │                      │         │        │              │              │               │
  │                      │<────────┘        │              │              │               │
  │                      │                  │              │              │               │
  │                      │ publishAssigned()│              │              │               │
  │                      ├─────────────────>│              │              │               │
  │                      │                  │              │              │               │
  │                      │                  │publishEvent()│              │               │
  │                      │                  ├─────────────>│              │               │
  │<─────────────────────┤                  │              │              │               │
  │  200 OK              │                  │              │ [async]      │               │
  │                      │                  │              ├─────────────>│               │
  │                      │                  │              │              │               │
  │                      │                  │              │              │ GET /users/id │
  │                      │                  │              │              ├──────────────>│
  │                      │                  │              │              │               │
  │                      │                  │              │              │<──────────────┤
  │                      │                  │              │              │  User details │
  │                      │                  │              │              │               │
  │                      │                  │              │              │ Log notification
  │                      │                  │              │              │               │
```

## Component Architecture

```
┌────────────────────────────────────────────────────────────────────┐
│                         Task Service                               │
├────────────────────────────────────────────────────────────────────┤
│                                                                    │
│  ┌────────────────────┐         ┌────────────────────┐            │
│  │   Controller       │         │   Configuration    │            │
│  │   Layer            │         │   Layer            │            │
│  │                    │         │                    │            │
│  │ - TaskController   │         │ - AsyncConfig      │            │
│  │ - REST endpoints   │         │ - @EnableAsync     │            │
│  │ - Request/Response │         │ - ThreadPool       │            │
│  └──────────┬─────────┘         └────────────────────┘            │
│             │                                                      │
│  ┌──────────▼─────────┐         ┌────────────────────┐            │
│  │   Service          │         │   Event Layer      │            │
│  │   Layer            │         │                    │            │
│  │                    │         │ - TaskEvent        │            │
│  │ - TaskService      │◀───────▶│ - EventPublisher   │            │
│  │ - Business Logic   │         │ - EventListener    │            │
│  │ - @Transactional   │         │ - @Async           │            │
│  └──────────┬─────────┘         └────────────────────┘            │
│             │                                                      │
│  ┌──────────▼─────────┐                                           │
│  │   Repository       │                                           │
│  │   Layer            │                                           │
│  │                    │                                           │
│  │ - TaskRepository   │                                           │
│  │ - JPA Interface    │                                           │
│  └──────────┬─────────┘                                           │
│             │                                                      │
│  ┌──────────▼─────────┐                                           │
│  │   Entity           │                                           │
│  │   Layer            │                                           │
│  │                    │                                           │
│  │ - Task             │                                           │
│  │ - TaskStatus       │                                           │
│  │ - JPA Entities     │                                           │
│  └────────────────────┘                                           │
└────────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────────┐
│                         User Service                               │
├────────────────────────────────────────────────────────────────────┤
│                                                                    │
│  ┌────────────────────┐                                           │
│  │   Controller       │                                           │
│  │   Layer            │                                           │
│  │                    │                                           │
│  │ - UserController   │                                           │
│  │ - REST endpoints   │                                           │
│  └──────────┬─────────┘                                           │
│             │                                                      │
│  ┌──────────▼─────────┐                                           │
│  │   Model            │                                           │
│  │   Layer            │                                           │
│  │                    │                                           │
│  │ - User (record)    │                                           │
│  │ - In-Memory Store  │                                           │
│  └────────────────────┘                                           │
└────────────────────────────────────────────────────────────────────┘
```

## Technology Stack

```
┌─────────────────────────────────────────────────────────────┐
│                    Technology Layers                        │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│  Presentation Layer                                         │
│  - REST APIs (JSON)                                         │
│  - Spring MVC                                               │
│  - @RestController                                          │
└─────────────────────────────────────────────────────────────┘
                            │
┌─────────────────────────────────────────────────────────────┐
│  Business Logic Layer                                       │
│  - @Service components                                      │
│  - Transaction management                                   │
│  - Validation (Jakarta Validation)                          │
└─────────────────────────────────────────────────────────────┘
                            │
┌─────────────────────────────────────────────────────────────┐
│  Event Processing Layer                                     │
│  - Spring Application Events                                │
│  - ApplicationEventPublisher                                │
│  - @EventListener                                           │
│  - @Async processing                                        │
│  - ThreadPoolTaskExecutor                                   │
└─────────────────────────────────────────────────────────────┘
                            │
┌─────────────────────────────────────────────────────────────┐
│  Data Access Layer                                          │
│  - Spring Data JPA                                          │
│  - JPA Repositories                                         │
│  - @Entity classes                                          │
└─────────────────────────────────────────────────────────────┘
                            │
┌─────────────────────────────────────────────────────────────┐
│  Database Layer                                             │
│  - HSQLDB (in-memory)                                       │
│  - Auto DDL generation                                      │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│  Service Communication                                      │
│  - RestTemplate                                             │
│  - HTTP/REST                                                │
└─────────────────────────────────────────────────────────────┘
```

## Deployment View

```
┌──────────────────────────────────────────────────────────────┐
│                      Local Development                       │
└──────────────────────────────────────────────────────────────┘

┌─────────────────────┐              ┌─────────────────────┐
│  JVM Process 1      │              │  JVM Process 2      │
│  ─────────────      │              │  ─────────────      │
│                     │              │                     │
│  Task Service       │   HTTP/REST  │  User Service       │
│  Port: 8081         │◀────────────▶│  Port: 8082         │
│                     │              │                     │
│  ┌───────────────┐  │              │  ┌───────────────┐  │
│  │ Spring Boot   │  │              │  │ Spring Boot   │  │
│  │ 3.2.1         │  │              │  │ 3.2.1         │  │
│  └───────────────┘  │              │  └───────────────┘  │
│                     │              │                     │
│  ┌───────────────┐  │              │  ┌───────────────┐  │
│  │ Tomcat        │  │              │  │ Tomcat        │  │
│  │ Embedded      │  │              │  │ Embedded      │  │
│  └───────────────┘  │              │  └───────────────┘  │
│                     │              │                     │
│  ┌───────────────┐  │              │  ┌───────────────┐  │
│  │ HSQLDB        │  │              │  │ HashMap       │  │
│  │ In-Memory     │  │              │  │ In-Memory     │  │
│  └───────────────┘  │              │  └───────────────┘  │
│                     │              │                     │
│  ┌───────────────┐  │              │                     │
│  │ Thread Pool   │  │              │                     │
│  │ async-event-* │  │              │                     │
│  └───────────────┘  │              │                     │
└─────────────────────┘              └─────────────────────┘

         │                                    │
         └────────────────┬───────────────────┘
                          │
                          ▼
                 ┌─────────────────┐
                 │   HTTP Client   │
                 │   (curl/REST)   │
                 └─────────────────┘
```

## Key Patterns

1. **Observer Pattern**: Spring Application Events
2. **Publisher-Subscriber**: Event publishing and listening
3. **Repository Pattern**: Spring Data JPA
4. **Service Layer Pattern**: Business logic separation
5. **DTO Pattern**: Request/Response records
6. **Dependency Injection**: Constructor injection throughout
7. **Async Pattern**: Non-blocking event processing

---

Created for: Spring Boot Service Orchestration Lab
Version: 1.0
Last Updated: 2025-12-14
