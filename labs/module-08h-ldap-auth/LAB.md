# Lab 8h: Active Directory / LDAP Authentication with Spring Security

## Objectives

By the end of this lab, you will be able to:
- Understand LDAP and Active Directory authentication concepts
- Configure Spring Security for LDAP authentication
- Map LDAP groups to Spring Security roles
- Implement role-based authorization with AD groups
- Test authentication using an embedded LDAP server
- Configure for production Active Directory environments

## Prerequisites

- Completed Labs 1-5 (especially Spring Security lab)
- Understanding of Spring Security basics
- Familiarity with authentication and authorization concepts

## Duration

60-75 minutes

---

## Scenario

Your organization uses Active Directory for identity management. You need to integrate your Spring Boot application with AD so that:
- Users authenticate with their AD credentials
- AD group memberships determine application roles
- Different endpoints require different roles

For this lab, we'll use an embedded LDAP server that mimics Active Directory structure, making it easy to test without a real AD environment.

---

## Part 1: Understanding LDAP and Active Directory

### Key Concepts

**LDAP (Lightweight Directory Access Protocol)**
- Protocol for accessing directory services
- Hierarchical structure (tree-like)
- Used by Active Directory, OpenLDAP, etc.

**Active Directory (AD)**
- Microsoft's directory service
- Uses LDAP protocol
- Stores users, groups, computers, policies

**DN (Distinguished Name)**
- Unique identifier for each entry
- Example: `cn=John Doe,ou=Users,dc=example,dc=com`

**Common LDAP Attributes:**
| Attribute | Description |
|-----------|-------------|
| `cn` | Common Name |
| `sAMAccountName` | Windows login name (AD) |
| `userPrincipalName` | User@domain format (AD) |
| `memberOf` | Group memberships |
| `ou` | Organizational Unit |
| `dc` | Domain Component |

### LDAP Directory Structure

```
dc=example,dc=com (Domain)
├── ou=Users
│   ├── cn=john.doe (User)
│   ├── cn=jane.smith (User)
│   └── cn=admin.user (User)
└── ou=Groups
    ├── cn=ROLE_USER (Group)
    ├── cn=ROLE_ADMIN (Group)
    └── cn=ROLE_MANAGER (Group)
```

---

## Part 2: Project Setup

### Step 2.1: Create the Project

Create a new Spring Boot project or use the starter provided.

**pom.xml:**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.4.1</version>
        <relativePath/>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>ldap-demo</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>ldap-demo</name>
    <description>LDAP/Active Directory Authentication Demo</description>

    <properties>
        <java.version>21</java.version>
    </properties>

    <dependencies>
        <!-- Spring Security -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>

        <!-- Spring Security LDAP -->
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-ldap</artifactId>
        </dependency>

        <!-- Embedded LDAP Server for testing -->
        <dependency>
            <groupId>com.unboundid</groupId>
            <artifactId>unboundid-ldapsdk</artifactId>
        </dependency>

        <!-- Spring LDAP -->
        <dependency>
            <groupId>org.springframework.ldap</groupId>
            <artifactId>spring-ldap-core</artifactId>
        </dependency>

        <!-- Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Actuator -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

### Step 2.2: Configure Application Properties

Create `src/main/resources/application.yml`:

```yaml
spring:
  application:
    name: ldap-demo

  # Embedded LDAP Server Configuration
  ldap:
    embedded:
      base-dn: dc=example,dc=com
      ldif: classpath:test-users.ldif
      port: 8389

# For production Active Directory, use these settings instead:
# ldap:
#   urls: ldap://ad.example.com:389
#   base: dc=example,dc=com
#   username: cn=service-account,ou=Service Accounts,dc=example,dc=com
#   password: ${LDAP_PASSWORD}

management:
  endpoints:
    web:
      exposure:
        include: health,info

server:
  port: 8080

logging:
  level:
    org.springframework.security: DEBUG
    org.springframework.ldap: DEBUG
```

### Step 2.3: Create LDIF File with Test Users

Create `src/main/resources/test-users.ldif`:

```ldif
# Root
dn: dc=example,dc=com
objectClass: top
objectClass: domain
dc: example

# Users OU
dn: ou=Users,dc=example,dc=com
objectClass: organizationalUnit
ou: Users

# Groups OU
dn: ou=Groups,dc=example,dc=com
objectClass: organizationalUnit
ou: Groups

# Regular User
dn: uid=john.doe,ou=Users,dc=example,dc=com
objectClass: inetOrgPerson
objectClass: organizationalPerson
objectClass: person
objectClass: top
cn: John Doe
sn: Doe
uid: john.doe
userPassword: password123
mail: john.doe@example.com

# Manager User
dn: uid=jane.smith,ou=Users,dc=example,dc=com
objectClass: inetOrgPerson
objectClass: organizationalPerson
objectClass: person
objectClass: top
cn: Jane Smith
sn: Smith
uid: jane.smith
userPassword: password123
mail: jane.smith@example.com

# Admin User
dn: uid=admin.user,ou=Users,dc=example,dc=com
objectClass: inetOrgPerson
objectClass: organizationalPerson
objectClass: person
objectClass: top
cn: Admin User
sn: User
uid: admin.user
userPassword: admin123
mail: admin@example.com

# ROLE_USER Group
dn: cn=ROLE_USER,ou=Groups,dc=example,dc=com
objectClass: groupOfNames
cn: ROLE_USER
member: uid=john.doe,ou=Users,dc=example,dc=com
member: uid=jane.smith,ou=Users,dc=example,dc=com
member: uid=admin.user,ou=Users,dc=example,dc=com

# ROLE_MANAGER Group
dn: cn=ROLE_MANAGER,ou=Groups,dc=example,dc=com
objectClass: groupOfNames
cn: ROLE_MANAGER
member: uid=jane.smith,ou=Users,dc=example,dc=com
member: uid=admin.user,ou=Users,dc=example,dc=com

# ROLE_ADMIN Group
dn: cn=ROLE_ADMIN,ou=Groups,dc=example,dc=com
objectClass: groupOfNames
cn: ROLE_ADMIN
member: uid=admin.user,ou=Users,dc=example,dc=com
```

**Test Users Summary:**
| Username | Password | Roles |
|----------|----------|-------|
| john.doe | password123 | USER |
| jane.smith | password123 | USER, MANAGER |
| admin.user | admin123 | USER, MANAGER, ADMIN |

---

## Part 3: Configure Spring Security for LDAP

### Step 3.1: Create Security Configuration

Create `src/main/java/com/example/ldapdemo/config/SecurityConfig.java`:

```java
package com.example.ldapdemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/user/**").hasRole("USER")
                .requestMatchers("/api/manager/**").hasRole("MANAGER")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/actuator/health").permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable());

        return http.build();
    }

    @Bean
    public LdapAuthenticationProvider ldapAuthenticationProvider(
            BaseLdapPathContextSource contextSource) {

        // Configure user search
        FilterBasedLdapUserSearch userSearch = new FilterBasedLdapUserSearch(
            "ou=Users",                    // Search base
            "(uid={0})",                   // Search filter ({0} = username)
            contextSource
        );

        // Configure bind authenticator
        BindAuthenticator authenticator = new BindAuthenticator(contextSource);
        authenticator.setUserSearch(userSearch);

        // Configure authorities populator (maps LDAP groups to roles)
        DefaultLdapAuthoritiesPopulator authoritiesPopulator =
            new DefaultLdapAuthoritiesPopulator(contextSource, "ou=Groups");
        authoritiesPopulator.setGroupSearchFilter("(member={0})");
        authoritiesPopulator.setGroupRoleAttribute("cn");
        authoritiesPopulator.setRolePrefix("");  // Groups already prefixed with ROLE_
        authoritiesPopulator.setSearchSubtree(true);

        return new LdapAuthenticationProvider(authenticator, authoritiesPopulator);
    }
}
```

---

## Part 4: Create REST Controllers

### Step 4.1: Create Public Controller

Create `src/main/java/com/example/ldapdemo/controller/PublicController.java`:

```java
package com.example.ldapdemo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    @GetMapping("/info")
    public Map<String, String> getInfo() {
        return Map.of(
            "application", "LDAP Authentication Demo",
            "status", "running",
            "message", "This endpoint is public - no authentication required"
        );
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
```

### Step 4.2: Create User Controller

Create `src/main/java/com/example/ldapdemo/controller/UserController.java`:

```java
package com.example.ldapdemo.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @GetMapping("/profile")
    public Map<String, Object> getProfile(Authentication authentication) {
        Map<String, Object> profile = new HashMap<>();
        profile.put("username", authentication.getName());
        profile.put("roles", authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList()));
        profile.put("authenticated", authentication.isAuthenticated());
        profile.put("message", "Welcome! You have USER access.");
        return profile;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> getDashboard(Authentication authentication) {
        return Map.of(
            "user", authentication.getName(),
            "dashboard", "User Dashboard",
            "features", new String[]{
                "View profile",
                "Update settings",
                "View reports"
            }
        );
    }
}
```

### Step 4.3: Create Manager Controller

Create `src/main/java/com/example/ldapdemo/controller/ManagerController.java`:

```java
package com.example.ldapdemo.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/manager")
public class ManagerController {

    @GetMapping("/dashboard")
    public Map<String, Object> getManagerDashboard(Authentication authentication) {
        return Map.of(
            "user", authentication.getName(),
            "dashboard", "Manager Dashboard",
            "features", new String[]{
                "View team reports",
                "Approve requests",
                "Manage team members",
                "View analytics"
            }
        );
    }

    @GetMapping("/reports")
    public Map<String, Object> getReports() {
        return Map.of(
            "reports", new String[]{
                "Q1 Sales Report",
                "Team Performance",
                "Budget Analysis"
            },
            "accessLevel", "MANAGER"
        );
    }

    @GetMapping("/team")
    @PreAuthorize("hasRole('MANAGER')")
    public Map<String, Object> getTeamInfo() {
        return Map.of(
            "teamSize", 5,
            "department", "Engineering",
            "members", new String[]{
                "john.doe",
                "alice.wong",
                "bob.johnson"
            }
        );
    }
}
```

### Step 4.4: Create Admin Controller

Create `src/main/java/com/example/ldapdemo/controller/AdminController.java`:

```java
package com.example.ldapdemo.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/dashboard")
    public Map<String, Object> getAdminDashboard(Authentication authentication) {
        return Map.of(
            "user", authentication.getName(),
            "dashboard", "Admin Dashboard",
            "features", new String[]{
                "User management",
                "System configuration",
                "Audit logs",
                "Security settings",
                "Database management"
            }
        );
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> getAllUsers() {
        return Map.of(
            "users", new Object[]{
                Map.of("username", "john.doe", "role", "USER", "status", "active"),
                Map.of("username", "jane.smith", "role", "MANAGER", "status", "active"),
                Map.of("username", "admin.user", "role", "ADMIN", "status", "active")
            },
            "totalCount", 3
        );
    }

    @GetMapping("/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> getAuditLog() {
        return Map.of(
            "auditEntries", new Object[]{
                Map.of("action", "LOGIN", "user", "john.doe", "timestamp", "2024-01-15T10:30:00"),
                Map.of("action", "ACCESS", "user", "jane.smith", "timestamp", "2024-01-15T11:00:00"),
                Map.of("action", "CONFIG_CHANGE", "user", "admin.user", "timestamp", "2024-01-15T11:30:00")
            }
        );
    }

    @PostMapping("/config")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, String> updateConfig(@RequestBody Map<String, String> config) {
        return Map.of(
            "status", "success",
            "message", "Configuration updated",
            "updatedKeys", String.join(", ", config.keySet())
        );
    }
}
```

---

## Part 5: Create Main Application

### Step 5.1: Create Application Class

Create `src/main/java/com/example/ldapdemo/LdapDemoApplication.java`:

```java
package com.example.ldapdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LdapDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(LdapDemoApplication.class, args);
    }
}
```

---

## Part 6: Testing

### Step 6.1: Run the Application

```bash
./mvnw spring-boot:run
```

### Step 6.2: Test Public Endpoints

```bash
# No authentication required
curl http://localhost:8080/api/public/info | jq
```

### Step 6.3: Test User Authentication

```bash
# Authenticate as john.doe (USER role only)
curl -u john.doe:password123 http://localhost:8080/api/user/profile | jq
```

Expected response:
```json
{
  "username": "john.doe",
  "roles": ["ROLE_USER"],
  "authenticated": true,
  "message": "Welcome! You have USER access."
}
```

### Step 6.4: Test Role-Based Access

```bash
# john.doe trying to access manager endpoint (should fail)
curl -u john.doe:password123 http://localhost:8080/api/manager/dashboard
# Returns 403 Forbidden

# jane.smith accessing manager endpoint (should succeed)
curl -u jane.smith:password123 http://localhost:8080/api/manager/dashboard | jq

# jane.smith trying to access admin endpoint (should fail)
curl -u jane.smith:password123 http://localhost:8080/api/admin/dashboard
# Returns 403 Forbidden

# admin.user accessing admin endpoint (should succeed)
curl -u admin.user:admin123 http://localhost:8080/api/admin/dashboard | jq
```

### Step 6.5: Test All Access Levels with Admin

```bash
# Admin can access all endpoints
curl -u admin.user:admin123 http://localhost:8080/api/user/profile | jq
curl -u admin.user:admin123 http://localhost:8080/api/manager/dashboard | jq
curl -u admin.user:admin123 http://localhost:8080/api/admin/users | jq
```

---

## Part 7: Production Active Directory Configuration

### Step 7.1: Active Directory Configuration

For production Active Directory, update your configuration:

```yaml
# application-production.yml
spring:
  ldap:
    urls: ldap://ad.yourcompany.com:389
    base: dc=yourcompany,dc=com
    username: cn=svc-springapp,ou=Service Accounts,dc=yourcompany,dc=com
    password: ${AD_SERVICE_PASSWORD}

app:
  ldap:
    user-search-base: ou=Users
    user-search-filter: (sAMAccountName={0})
    group-search-base: ou=Groups
    group-search-filter: (member={0})
```

### Step 7.2: Production Security Configuration

Create `src/main/java/com/example/ldapdemo/config/ProductionLdapConfig.java`:

```java
package com.example.ldapdemo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;

/**
 * Production configuration for Active Directory
 * Activate with: --spring.profiles.active=production
 */
@Configuration
@Profile("production")
public class ProductionLdapConfig {

    @Value("${spring.ldap.urls}")
    private String ldapUrl;

    @Value("${spring.ldap.base}")
    private String ldapBase;

    @Value("${spring.ldap.username}")
    private String ldapUsername;

    @Value("${spring.ldap.password}")
    private String ldapPassword;

    @Value("${app.ldap.user-search-base:ou=Users}")
    private String userSearchBase;

    @Value("${app.ldap.user-search-filter:(sAMAccountName={0})}")
    private String userSearchFilter;

    @Value("${app.ldap.group-search-base:ou=Groups}")
    private String groupSearchBase;

    @Bean
    public LdapContextSource contextSource() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(ldapUrl);
        contextSource.setBase(ldapBase);
        contextSource.setUserDn(ldapUsername);
        contextSource.setPassword(ldapPassword);
        contextSource.setReferral("follow");
        contextSource.afterPropertiesSet();
        return contextSource;
    }

    @Bean
    public LdapAuthenticationProvider ldapAuthenticationProvider() {
        // User search for Active Directory
        FilterBasedLdapUserSearch userSearch = new FilterBasedLdapUserSearch(
            userSearchBase,
            userSearchFilter,  // sAMAccountName for AD
            contextSource()
        );

        BindAuthenticator authenticator = new BindAuthenticator(contextSource());
        authenticator.setUserSearch(userSearch);

        // Map AD groups to Spring Security roles
        DefaultLdapAuthoritiesPopulator authoritiesPopulator =
            new DefaultLdapAuthoritiesPopulator(contextSource(), groupSearchBase);
        authoritiesPopulator.setGroupSearchFilter("(member={0})");
        authoritiesPopulator.setGroupRoleAttribute("cn");
        authoritiesPopulator.setRolePrefix("ROLE_");
        authoritiesPopulator.setConvertToUpperCase(true);
        authoritiesPopulator.setSearchSubtree(true);

        return new LdapAuthenticationProvider(authenticator, authoritiesPopulator);
    }
}
```

### Step 7.3: Active Directory Group Mapping

For Active Directory, you may need to map AD group names to application roles:

```java
package com.example.ldapdemo.config;

import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class ActiveDirectoryGroupMapper implements LdapAuthoritiesPopulator {

    // Map AD group names to application roles
    private static final Map<String, String> GROUP_ROLE_MAP = Map.of(
        "APP-Users", "ROLE_USER",
        "APP-Managers", "ROLE_MANAGER",
        "APP-Administrators", "ROLE_ADMIN",
        "Domain Users", "ROLE_USER"
    );

    @Override
    public Collection<? extends GrantedAuthority> getGrantedAuthorities(
            DirContextOperations userData, String username) {

        Set<GrantedAuthority> authorities = new HashSet<>();

        // Get memberOf attribute from AD
        String[] groups = userData.getStringAttributes("memberOf");
        if (groups != null) {
            for (String groupDn : groups) {
                // Extract CN from DN (e.g., "CN=APP-Users,OU=Groups,DC=example,DC=com")
                String groupName = extractCn(groupDn);

                // Map to application role
                String role = GROUP_ROLE_MAP.get(groupName);
                if (role != null) {
                    authorities.add(new SimpleGrantedAuthority(role));
                }
            }
        }

        // Default role if no specific mappings found
        if (authorities.isEmpty()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return authorities;
    }

    private String extractCn(String dn) {
        // Extract CN value from Distinguished Name
        if (dn != null && dn.toUpperCase().startsWith("CN=")) {
            int endIndex = dn.indexOf(',');
            return endIndex > 0 ? dn.substring(3, endIndex) : dn.substring(3);
        }
        return dn;
    }
}
```

---

## Part 8: Challenge Exercises

### Challenge 1: User Details Service

Create a custom `LdapUserDetailsService` that loads additional user attributes:

```java
// Load email, department, manager from LDAP
public class CustomLdapUserDetails extends LdapUserDetails {
    private String email;
    private String department;
    private String manager;
    // ...
}
```

### Challenge 2: Remember Me with LDAP

Implement "Remember Me" functionality:
- Store persistent tokens in database
- Configure token validity period
- Handle token refresh

### Challenge 3: Multi-Domain Support

Support authentication against multiple AD domains:
- Primary domain: `corp.example.com`
- Secondary domain: `partner.example.com`
- Route authentication based on username format

### Challenge 4: JWT Integration

Combine LDAP authentication with JWT tokens:
- Authenticate against AD
- Issue JWT token on successful auth
- Validate JWT for subsequent requests

---

## Summary

In this lab, you learned:

1. **LDAP Concepts**: Distinguished Names, attributes, directory structure
2. **Spring Security LDAP**: Configuring authentication providers
3. **Embedded LDAP**: Testing without real AD infrastructure
4. **Group-to-Role Mapping**: Converting LDAP groups to Spring roles
5. **Role-Based Authorization**: Protecting endpoints by role
6. **Production Configuration**: Connecting to real Active Directory

## Key Concepts

- **Bind Authentication**: Verifies credentials by attempting to bind to LDAP
- **User Search**: Locates user entry in directory
- **Authorities Populator**: Maps LDAP groups to Spring Security roles
- **Filter-Based Search**: Uses LDAP filter syntax to find entries

## Common LDAP Filters

| Filter | Description |
|--------|-------------|
| `(uid={0})` | Find by UID (OpenLDAP) |
| `(sAMAccountName={0})` | Find by login name (AD) |
| `(userPrincipalName={0})` | Find by UPN (AD) |
| `(member={0})` | Find groups containing user |
| `(&(objectClass=user)(mail=*))` | All users with email |

## Next Steps

- Integrate with OAuth2/OIDC for modern authentication
- Implement LDAP connection pooling for performance
- Add caching for LDAP queries
- Configure SSL/TLS for secure LDAP connections (LDAPS)
