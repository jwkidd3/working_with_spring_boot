# Module 5: Spring Security

---

## Module Overview

### What You'll Learn
- Understand Spring Security fundamentals
- Implement authentication and authorization
- Secure REST APIs with JWT
- Configure method-level security

### Duration: ~3 hours (Day 3 Morning)

---

## Section 1: Spring Security Fundamentals

---

### What is Spring Security?

- Powerful and customizable authentication and access-control framework
- De facto standard for securing Spring applications
- Protection against common attacks (CSRF, session fixation, etc.)

```
┌─────────────────────────────────────────────────────────┐
│                  Spring Security                         │
├─────────────────────────────────────────────────────────┤
│  Authentication   │  Who are you?                       │
│  Authorization    │  What can you do?                   │
│  Protection       │  Defense against attacks            │
└─────────────────────────────────────────────────────────┘
```

---

### Security Architecture

```
HTTP Request
     │
     ▼
┌─────────────────────────────────────────────────────────┐
│              Security Filter Chain                       │
│  ┌─────────────────────────────────────────────────┐    │
│  │  CorsFilter                                      │    │
│  │  CsrfFilter                                      │    │
│  │  UsernamePasswordAuthenticationFilter            │    │
│  │  BasicAuthenticationFilter                       │    │
│  │  BearerTokenAuthenticationFilter                 │    │
│  │  ExceptionTranslationFilter                      │    │
│  │  FilterSecurityInterceptor                       │    │
│  └─────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────┘
     │
     ▼
Controller / Resource
```

---

### Adding Spring Security

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

**Default behavior:**
- All endpoints require authentication
- Form login enabled
- Generated password in console
- User: `user`

---

### Default Security (Auto-configured)

When you add the dependency with no configuration:

```
Using generated security password: 8e3d7b2a-...

This generated password is for development use only.
```

Access any endpoint:
- Browser → Login form appears
- API → 401 Unauthorized

---

## Section 2: Basic Authentication

---

### What is Basic Auth?

```
Authorization: Basic dXNlcm5hbWU6cGFzc3dvcmQ=
                    └── base64(username:password)
```

**Characteristics:**
- Credentials sent with every request
- Must use HTTPS in production
- Simple but limited
- No logout mechanism

---

### Configuring Basic Auth

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())  // Disable for REST APIs
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
```

---

### In-Memory Users

```java
@Bean
public UserDetailsService userDetailsService() {
    UserDetails user = User.builder()
        .username("user")
        .password(passwordEncoder().encode("password"))
        .roles("USER")
        .build();

    UserDetails admin = User.builder()
        .username("admin")
        .password(passwordEncoder().encode("admin123"))
        .roles("ADMIN", "USER")
        .build();

    return new InMemoryUserDetailsManager(user, admin);
}

@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

---

### Testing Basic Auth

```bash
# Without credentials - 401 Unauthorized
curl http://localhost:8080/api/tasks

# With credentials
curl -u user:password http://localhost:8080/api/tasks

# Base64 encoded
curl -H "Authorization: Basic dXNlcjpwYXNzd29yZA==" \
     http://localhost:8080/api/tasks
```

---

## Section 3: Role-Based Authorization

---

### Authorization Concepts

```
┌─────────────────────────────────────────────────────────┐
│                    Authorization                         │
├───────────────────────┬─────────────────────────────────┤
│  Role-Based (RBAC)    │  User has ROLE_ADMIN            │
│  Permission-Based     │  User has task:read permission  │
│  Attribute-Based      │  User owns the resource         │
└───────────────────────┴─────────────────────────────────┘
```

---

### URL-Based Authorization

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            // Public endpoints
            .requestMatchers("/api/public/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/tasks/**").permitAll()

            // Role-based access
            .requestMatchers(HttpMethod.POST, "/api/tasks/**").hasRole("USER")
            .requestMatchers(HttpMethod.DELETE, "/api/tasks/**").hasRole("ADMIN")

            // Authority-based (more flexible)
            .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")

            // All other requests
            .anyRequest().authenticated()
        );

    return http.build();
}
```

---

### Method-Level Security

Enable with annotation:
```java
@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    // ...
}
```

Use annotations on methods:
```java
@Service
public class TaskService {

    @PreAuthorize("hasRole('USER')")
    public Task create(CreateTaskRequest request) {
        // ...
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void delete(Long id) {
        // ...
    }

    @PreAuthorize("hasRole('ADMIN') or #task.createdBy == authentication.name")
    public Task update(Task task) {
        // ...
    }
}
```

---

### Security Annotations

| Annotation | Description |
|------------|-------------|
| `@PreAuthorize` | Check before method execution |
| `@PostAuthorize` | Check after method execution |
| `@PreFilter` | Filter input collection |
| `@PostFilter` | Filter output collection |
| `@Secured` | Simple role check |

---

### @PreAuthorize Examples

```java
// Simple role check
@PreAuthorize("hasRole('ADMIN')")
public void adminOnly() { }

// Multiple roles
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public void adminOrManager() { }

// Authority check
@PreAuthorize("hasAuthority('task:delete')")
public void deleteTask(Long id) { }

// SpEL expression with method parameters
@PreAuthorize("#userId == authentication.principal.id")
public User getUser(Long userId) { }

// Complex expression
@PreAuthorize("hasRole('ADMIN') or " +
              "(hasRole('USER') and #task.owner == authentication.name)")
public void updateTask(Task task) { }
```

---

### @PostAuthorize and @PostFilter

```java
// Check result after execution
@PostAuthorize("returnObject.owner == authentication.name")
public Task getTask(Long id) {
    return taskRepository.findById(id);
}

// Filter the returned collection
@PostFilter("filterObject.owner == authentication.name or hasRole('ADMIN')")
public List<Task> getAllTasks() {
    return taskRepository.findAll();
}
```

---

## Section 4: JWT Authentication

---

### What is JWT?

**JSON Web Token** - Compact, self-contained token for transmitting information

```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIiwiaWF0IjoxNjE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
     │                      │                                   │
     └── Header             └── Payload                         └── Signature
```

---

### JWT Structure

```json
// Header
{
  "alg": "HS256",
  "typ": "JWT"
}

// Payload
{
  "sub": "user@example.com",
  "name": "John Doe",
  "roles": ["ROLE_USER"],
  "iat": 1616239022,
  "exp": 1616242622
}

// Signature
HMACSHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  secret
)
```

---

### JWT Flow

```
┌────────────┐                           ┌────────────┐
│   Client   │                           │   Server   │
└─────┬──────┘                           └─────┬──────┘
      │                                        │
      │  1. POST /auth/login                   │
      │     {username, password}               │
      ├───────────────────────────────────────>│
      │                                        │ Validate credentials
      │  2. Return JWT                         │
      │<───────────────────────────────────────┤
      │                                        │
      │  3. GET /api/tasks                     │
      │     Authorization: Bearer <JWT>        │
      ├───────────────────────────────────────>│
      │                                        │ Validate JWT
      │  4. Return data                        │
      │<───────────────────────────────────────┤
```

---

### JWT Dependencies

```xml
<!-- JWT Library -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
```

---

### JWT Service

```java
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expiration;

    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
            .subject(userDetails.getUsername())
            .claim("roles", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(getSigningKey())
            .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    // ... helper methods
}
```

---

### JWT Filter

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        final String username = jwtService.extractUsername(jwt);

        if (username != null && SecurityContextHolder.getContext()
                .getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
```

---

### Security Config for JWT

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/swagger-ui/**", "/api-docs/**").permitAll()
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthFilter,
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

---

### Authentication Controller

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        authManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
            )
        );

        UserDetails user = userDetailsService.loadUserByUsername(request.getUsername());
        String token = jwtService.generateToken(user);

        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        // Create user and return token
    }
}
```

---

### Testing JWT Authentication

```bash
# 1. Login to get token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"password"}' | jq -r '.token')

# 2. Use token in requests
curl http://localhost:8080/api/tasks \
  -H "Authorization: Bearer $TOKEN"
```

---

## Section 5: Database User Store

---

### User Entity

```java
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String email;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles")
    @Enumerated(EnumType.STRING)
    private Set<Role> roles = new HashSet<>();

    private boolean enabled = true;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
            .map(role -> new SimpleGrantedAuthority(role.name()))
            .toList();
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    // getters and setters
}
```

---

### User Repository

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
```

---

### Custom UserDetailsService

```java
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
            .orElseThrow(() ->
                new UsernameNotFoundException("User not found: " + username));
    }
}
```

---

## Section 6: Spring Boot Data REST Security

---

### Securing Spring Data REST

```java
@RepositoryRestResource
public interface TaskRepository extends JpaRepository<Task, Long> {

    @Override
    @PreAuthorize("hasRole('USER')")
    Task save(Task task);

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    void deleteById(Long id);

    @Override
    @PostFilter("filterObject.owner == authentication.name or hasRole('ADMIN')")
    List<Task> findAll();
}
```

---

### Repository Event Handlers

```java
@RepositoryEventHandler
@Component
public class TaskEventHandler {

    @HandleBeforeCreate
    @PreAuthorize("hasRole('USER')")
    public void handleBeforeCreate(Task task) {
        // Set owner to current user
        String currentUser = SecurityContextHolder.getContext()
            .getAuthentication().getName();
        task.setOwner(currentUser);
    }

    @HandleBeforeDelete
    @PreAuthorize("hasRole('ADMIN') or #task.owner == authentication.name")
    public void handleBeforeDelete(Task task) {
        // Validate delete permission
    }
}
```

---

## Module 5 Summary

### Key Takeaways

1. **Spring Security** provides comprehensive security features
2. **Basic Auth** is simple but requires HTTPS
3. **JWT** enables stateless authentication for APIs
4. **Role-based authorization** controls access to resources
5. **Method security** provides fine-grained control
6. **Database user store** for production applications

---

## Lab Exercise

### Lab 5: Securing the Task API

You will:
- Add Spring Security to the Task API
- Implement JWT authentication
- Configure role-based authorization
- Secure endpoints with method-level security

**Time:** 60-75 minutes

---

## Questions?

### Next Module: Service Orchestration
