# SGG-API — Documento Técnico del Backend

**Repositorio:** `sgg-api`  
**Stack:** Java 21 + Spring Boot 3.x + PostgreSQL 16 + Flyway  
**Versión:** 1.0  
**Fecha:** 19 de febrero de 2026  

---

## 1. Objetivo

Este documento detalla la implementación del backend del SaaS multi-tenant para gimnasios. El backend es un monolito modular que expone una API REST consumida por dos clientes: el panel web (Next.js) y la app móvil (React Native).

Es la **única fuente de verdad** de la lógica de negocio. Ni el frontend web ni la app móvil deben contener reglas de negocio — solo presentación y consumo de la API.

---

## 2. Setup del Proyecto

### 2.1 Inicialización con Spring Initializr

```
Project:    Maven
Language:   Java 21
Spring Boot: 3.3.x (última estable)
Group:      com.sgg
Artifact:   sgg-api
Package:    com.sgg

Dependencies:
  - Spring Web
  - Spring Security
  - Spring Data JPA
  - Spring Validation (Bean Validation)
  - OAuth2 Resource Server
  - PostgreSQL Driver
  - Flyway Migration
  - Lombok
  - Spring Boot DevTools (solo dev)
  - Spring Boot Actuator
```

### 2.2 Estructura de Directorios Completa

```
sgg-api/
├── src/
│   ├── main/
│   │   ├── java/com/sgg/
│   │   │   ├── SggApiApplication.java
│   │   │   │
│   │   │   ├── common/
│   │   │   │   ├── config/
│   │   │   │   │   ├── SecurityConfig.java
│   │   │   │   │   ├── CorsConfig.java
│   │   │   │   │   ├── WebMvcConfig.java
│   │   │   │   │   └── JacksonConfig.java
│   │   │   │   ├── security/
│   │   │   │   │   ├── TenantContext.java
│   │   │   │   │   ├── TenantInterceptor.java
│   │   │   │   │   ├── TenantHibernateFilterAspect.java
│   │   │   │   │   ├── GymAccessChecker.java
│   │   │   │   │   ├── CurrentUser.java              (annotation)
│   │   │   │   │   ├── CurrentUserResolver.java
│   │   │   │   │   └── JwtToUserConverter.java
│   │   │   │   ├── exception/
│   │   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   │   ├── BusinessException.java
│   │   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   │   ├── UnauthorizedException.java
│   │   │   │   │   └── TenantViolationException.java
│   │   │   │   └── dto/
│   │   │   │       ├── ApiResponse.java
│   │   │   │       ├── ApiError.java
│   │   │   │       └── PageResponse.java
│   │   │   │
│   │   │   ├── identity/
│   │   │   │   ├── controller/
│   │   │   │   │   └── UserController.java
│   │   │   │   ├── service/
│   │   │   │   │   └── UserService.java
│   │   │   │   ├── repository/
│   │   │   │   │   ├── UserRepository.java
│   │   │   │   │   └── AuthIdentityRepository.java
│   │   │   │   ├── entity/
│   │   │   │   │   ├── User.java
│   │   │   │   │   └── AuthIdentity.java
│   │   │   │   └── dto/
│   │   │   │       ├── UserProfileDto.java
│   │   │   │       ├── UpdateProfileRequest.java
│   │   │   │       └── AuthSyncRequest.java
│   │   │   │
│   │   │   ├── tenancy/
│   │   │   │   ├── controller/
│   │   │   │   │   ├── GymController.java
│   │   │   │   │   └── MembershipController.java
│   │   │   │   ├── service/
│   │   │   │   │   ├── GymService.java
│   │   │   │   │   └── MembershipService.java
│   │   │   │   ├── repository/
│   │   │   │   │   ├── GymRepository.java
│   │   │   │   │   └── GymMemberRepository.java
│   │   │   │   ├── entity/
│   │   │   │   │   ├── Gym.java
│   │   │   │   │   ├── GymMember.java
│   │   │   │   │   ├── MemberRole.java            (enum)
│   │   │   │   │   └── MembershipStatus.java      (enum)
│   │   │   │   └── dto/
│   │   │   │       ├── GymInfoDto.java
│   │   │   │       ├── GymMemberDto.java
│   │   │   │       ├── JoinRequestDto.java
│   │   │   │       ├── ApproveMemberRequest.java
│   │   │   │       └── MembershipDto.java
│   │   │   │
│   │   │   ├── coaching/
│   │   │   │   ├── controller/
│   │   │   │   │   └── CoachAssignmentController.java
│   │   │   │   ├── service/
│   │   │   │   │   └── CoachAssignmentService.java
│   │   │   │   ├── repository/
│   │   │   │   │   └── CoachAssignmentRepository.java
│   │   │   │   ├── entity/
│   │   │   │   │   └── CoachAssignment.java
│   │   │   │   └── dto/
│   │   │   │       ├── CoachAssignmentDto.java
│   │   │   │       └── AssignCoachRequest.java
│   │   │   │
│   │   │   ├── training/
│   │   │   │   ├── controller/
│   │   │   │   │   ├── RoutineTemplateController.java
│   │   │   │   │   └── RoutineAssignmentController.java
│   │   │   │   ├── service/
│   │   │   │   │   ├── RoutineTemplateService.java
│   │   │   │   │   └── RoutineAssignmentService.java
│   │   │   │   ├── repository/
│   │   │   │   │   ├── RoutineTemplateRepository.java
│   │   │   │   │   ├── TemplateBlockRepository.java
│   │   │   │   │   ├── TemplateExerciseRepository.java
│   │   │   │   │   └── RoutineAssignmentRepository.java
│   │   │   │   ├── entity/
│   │   │   │   │   ├── RoutineTemplate.java
│   │   │   │   │   ├── TemplateBlock.java
│   │   │   │   │   ├── TemplateExercise.java
│   │   │   │   │   └── RoutineAssignment.java
│   │   │   │   └── dto/
│   │   │   │       ├── RoutineTemplateDto.java
│   │   │   │       ├── TemplateBlockDto.java
│   │   │   │       ├── TemplateExerciseDto.java
│   │   │   │       ├── CreateTemplateRequest.java
│   │   │   │       ├── AssignRoutineRequest.java
│   │   │   │       └── MemberRoutineDto.java
│   │   │   │
│   │   │   ├── tracking/
│   │   │   │   ├── controller/
│   │   │   │   │   └── TrackingController.java
│   │   │   │   ├── service/
│   │   │   │   │   └── TrackingService.java
│   │   │   │   ├── repository/
│   │   │   │   │   └── ExerciseCompletionRepository.java
│   │   │   │   ├── entity/
│   │   │   │   │   └── ExerciseCompletion.java
│   │   │   │   └── dto/
│   │   │   │       ├── CompleteExerciseRequest.java
│   │   │   │       └── ProgressDto.java
│   │   │   │
│   │   │   └── schedule/
│   │   │       ├── controller/
│   │   │       │   └── ScheduleController.java
│   │   │       ├── service/
│   │   │       │   └── ScheduleService.java
│   │   │       ├── repository/
│   │   │       │   └── ScheduleActivityRepository.java
│   │   │       ├── entity/
│   │   │       │   └── ScheduleActivity.java
│   │   │       └── dto/
│   │   │           ├── ScheduleActivityDto.java
│   │   │           └── CreateScheduleRequest.java
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       └── db/migration/
│   │           ├── V1__create_users.sql
│   │           ├── V2__create_auth_identities.sql
│   │           ├── V3__create_gyms.sql
│   │           ├── V4__create_gym_members.sql
│   │           ├── V5__create_coach_assignments.sql
│   │           ├── V6__create_routine_templates.sql
│   │           ├── V7__create_template_blocks.sql
│   │           ├── V8__create_template_exercises.sql
│   │           ├── V9__create_routine_assignments.sql
│   │           ├── V10__create_exercise_completions.sql
│   │           ├── V11__create_schedule_activities.sql
│   │           └── V12__create_indexes.sql
│   │
│   └── test/
│       └── java/com/sgg/
│           ├── common/
│           │   └── TenantIsolationTest.java
│           ├── identity/
│           │   └── UserServiceTest.java
│           ├── tenancy/
│           │   ├── MembershipServiceTest.java
│           │   └── MembershipIntegrationTest.java
│           ├── training/
│           │   └── RoutineTemplateServiceTest.java
│           └── tracking/
│               └── TrackingServiceTest.java
│
├── pom.xml
├── Dockerfile
├── .env.example
└── README.md
```

---

## 3. Configuración

### 3.1 application.yml

```yaml
spring:
  application:
    name: sgg-api

  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/sgg}
    username: ${SPRING_DATASOURCE_USERNAME:sgg_admin}
    password: ${SPRING_DATASOURCE_PASSWORD:}
    hikari:
      maximum-pool-size: 10
      minimum-idle: 2

  jpa:
    hibernate:
      ddl-auto: validate    # Flyway maneja el schema, Hibernate solo valida
    open-in-view: false      # Buena práctica: deshabilitar OSIV
    properties:
      hibernate:
        default_schema: public
        format_sql: true

  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true

  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: ${SUPABASE_JWKS_URI}

server:
  port: 8080

logging:
  level:
    com.sgg: DEBUG
    org.hibernate.SQL: DEBUG
    org.springframework.security: DEBUG

# Custom properties
app:
  cors:
    web-origin: ${APP_CORS_WEB_ORIGIN:http://localhost:3000}
    additional-origins: ${APP_CORS_ADDITIONAL_ORIGINS:}
  supabase:
    url: ${SUPABASE_URL}
    jwt-secret: ${SUPABASE_JWT_SECRET}
```

### 3.2 application-dev.yml

```yaml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true

logging:
  level:
    com.sgg: TRACE

app:
  cors:
    web-origin: http://localhost:3000
    additional-origins: http://localhost:8081,http://localhost:19006
```

### 3.3 application-prod.yml

```yaml
spring:
  jpa:
    show-sql: false

logging:
  level:
    com.sgg: INFO
    org.hibernate.SQL: WARN

app:
  cors:
    web-origin: https://web.tudominio.com
```

---

## 4. Implementaciones Core

### 4.1 Multi-Tenancy

Este es el componente más crítico del sistema. Toda operación debe estar scoped al gym correcto.

**TenantContext.java** — ThreadLocal para el gym_id actual:

```java
public final class TenantContext {
    private static final ThreadLocal<Long> currentGymId = new ThreadLocal<>();
    private static final ThreadLocal<Long> currentUserId = new ThreadLocal<>();

    public static void setGymId(Long gymId) { currentGymId.set(gymId); }
    public static Long getGymId() { return currentGymId.get(); }
    public static Long requireGymId() {
        Long id = currentGymId.get();
        if (id == null) throw new TenantViolationException("gym_id not set in context");
        return id;
    }

    public static void setUserId(Long userId) { currentUserId.set(userId); }
    public static Long getUserId() { return currentUserId.get(); }

    public static void clear() {
        currentGymId.remove();
        currentUserId.remove();
    }
}
```

**TenantInterceptor.java** — Extrae gym_id de la URL y valida acceso:

```java
@Component
@RequiredArgsConstructor
public class TenantInterceptor implements HandlerInterceptor {

    private final GymMemberRepository gymMemberRepository;
    private static final Pattern GYM_PATH_PATTERN =
        Pattern.compile("/api/gyms/(\\d+)/.*");

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        Matcher matcher = GYM_PATH_PATTERN.matcher(request.getRequestURI());
        if (matcher.matches()) {
            Long gymId = Long.valueOf(matcher.group(1));
            Long userId = TenantContext.getUserId();

            // Verificar que el usuario tiene membresía activa en este gym
            if (userId != null) {
                GymMember membership = gymMemberRepository
                    .findByUserIdAndGymIdAndStatusIn(userId, gymId,
                        List.of(MembershipStatus.ACTIVE))
                    .orElseThrow(() -> new UnauthorizedException(
                        "No active membership in gym " + gymId));

                TenantContext.setGymId(gymId);
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler, Exception ex) {
        TenantContext.clear();
    }
}
```

**TenantHibernateFilterAspect.java** — Activa el Hibernate Filter en cada request:

```java
@Aspect
@Component
@RequiredArgsConstructor
public class TenantHibernateFilterAspect {

    private final EntityManager entityManager;

    @Before("execution(* com.sgg..repository.*Repository.*(..))")
    public void enableTenantFilter() {
        Long gymId = TenantContext.getGymId();
        if (gymId != null) {
            Session session = entityManager.unwrap(Session.class);
            session.enableFilter("tenantFilter")
                   .setParameter("gymId", gymId);
        }
    }
}
```

**Anotaciones en entidades** (ejemplo RoutineTemplate):

```java
@Entity
@Table(name = "routine_templates")
@FilterDef(name = "tenantFilter",
           parameters = @ParamDef(name = "gymId", type = Long.class))
@Filter(name = "tenantFilter", condition = "gym_id = :gymId")
@Getter @Setter
@NoArgsConstructor
public class RoutineTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "gym_id", nullable = false)
    private Long gymId;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<TemplateBlock> blocks = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        // Asegurar que gym_id está seteado
        if (gymId == null) {
            gymId = TenantContext.requireGymId();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

### 4.2 Seguridad — Spring Security + Supabase JWT

**SecurityConfig.java:**

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwksUri;

    private final JwtToUserConverter jwtToUserConverter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwkSetUri(jwksUri)
                    .jwtAuthenticationConverter(jwtToUserConverter)
                )
            )
            .authorizeHttpRequests(auth -> auth
                // Endpoints públicos
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/auth/sync").permitAll()
                .requestMatchers("/api/gyms/search").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                // Endpoints por rol (validación adicional en controllers)
                .requestMatchers("/api/gyms/*/admin/**").authenticated()
                .requestMatchers("/api/gyms/*/coach/**").authenticated()
                .requestMatchers("/api/gyms/*/member/**").authenticated()
                // Todo lo demás requiere auth
                .anyRequest().authenticated()
            );

        return http.build();
    }
}
```

**JwtToUserConverter.java** — Convierte el JWT de Supabase en un usuario local:

```java
@Component
@RequiredArgsConstructor
public class JwtToUserConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserRepository userRepository;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String supabaseUid = jwt.getSubject(); // sub = user UUID en Supabase

        User user = userRepository.findBySupabaseUid(supabaseUid)
            .orElse(null); // Puede ser null en /auth/sync (primer login)

        if (user != null) {
            TenantContext.setUserId(user.getId());
        }

        // Authorities vacías — los permisos se validan contra gym_members por gym
        List<GrantedAuthority> authorities = List.of();

        JwtAuthenticationToken token = new JwtAuthenticationToken(jwt, authorities);
        return token;
    }
}
```

**GymAccessChecker.java** — Verifica roles por gimnasio (usado en @PreAuthorize):

```java
@Component("gymAccess")
@RequiredArgsConstructor
public class GymAccessChecker {

    private final GymMemberRepository gymMemberRepository;

    public boolean hasRole(Long gymId, String... roles) {
        Long userId = TenantContext.getUserId();
        if (userId == null) return false;

        return gymMemberRepository
            .findByUserIdAndGymIdAndStatusIn(userId, gymId,
                List.of(MembershipStatus.ACTIVE))
            .map(member -> {
                MemberRole memberRole = member.getRole();
                for (String role : roles) {
                    if (memberRole.name().equals(role)) return true;
                    // ADMIN_COACH tiene ambos permisos
                    if (memberRole == MemberRole.ADMIN_COACH &&
                        (role.equals("ADMIN") || role.equals("COACH"))) return true;
                }
                return false;
            })
            .orElse(false);
    }

    public boolean isMemberActive(Long gymId) {
        Long userId = TenantContext.getUserId();
        if (userId == null) return false;

        return gymMemberRepository
            .existsByUserIdAndGymIdAndStatus(userId, gymId, MembershipStatus.ACTIVE);
    }

    public boolean isMembershipNotExpired(Long gymId) {
        Long userId = TenantContext.getUserId();
        if (userId == null) return false;

        return gymMemberRepository
            .findByUserIdAndGymIdAndStatusIn(userId, gymId,
                List.of(MembershipStatus.ACTIVE))
            .map(m -> m.getMembershipExpiresAt() == null ||
                      m.getMembershipExpiresAt().isAfter(LocalDateTime.now()))
            .orElse(false);
    }
}
```

### 4.3 Resolución del Usuario Actual

**CurrentUser.java** — Annotation para inyectar el usuario actual en controllers:

```java
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUser {}
```

**CurrentUserResolver.java:**

```java
@Component
public class CurrentUserResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class) &&
               parameter.getParameterType().equals(Long.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ...) {
        return TenantContext.getUserId();
    }
}
```

**Uso en controllers:**

```java
@GetMapping("/api/users/me")
public UserProfileDto getProfile(@CurrentUser Long userId) {
    return userService.getProfile(userId);
}
```

### 4.4 Exception Handling Global

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(404)
            .body(new ApiError(404, ex.getMessage()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusiness(BusinessException ex) {
        return ResponseEntity.status(422)
            .body(new ApiError(422, ex.getMessage()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiError> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(403)
            .body(new ApiError(403, ex.getMessage()));
    }

    @ExceptionHandler(TenantViolationException.class)
    public ResponseEntity<ApiError> handleTenantViolation(TenantViolationException ex) {
        // Log como CRITICAL — esto nunca debería pasar
        log.error("TENANT VIOLATION: {}", ex.getMessage());
        return ResponseEntity.status(403)
            .body(new ApiError(403, "Access denied"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
            .forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.status(400)
            .body(new ApiError(400, "Validation failed", errors));
    }
}
```

---

## 5. Entidades JPA Completas

### 5.1 User

```java
@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "supabase_uid", unique = true, nullable = false)
    private String supabaseUid;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

### 5.2 Gym

```java
@Entity
@Table(name = "gyms")
@Getter @Setter @NoArgsConstructor
public class Gym {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String slug;

    private String description;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "routine_cycle", nullable = false)
    @Enumerated(EnumType.STRING)
    private RoutineCycle routineCycle = RoutineCycle.WEEKLY;

    @Column(name = "owner_user_id", nullable = false)
    private Long ownerUserId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum RoutineCycle { WEEKLY, MONTHLY }
}
```

### 5.3 GymMember

```java
@Entity
@Table(name = "gym_members")
@FilterDef(name = "tenantFilter",
           parameters = @ParamDef(name = "gymId", type = Long.class))
@Filter(name = "tenantFilter", condition = "gym_id = :gymId")
@Getter @Setter @NoArgsConstructor
public class GymMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "gym_id", nullable = false)
    private Long gymId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MembershipStatus status = MembershipStatus.PENDING;

    @Column(name = "membership_expires_at")
    private LocalDateTime membershipExpiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

### 5.4 Enums

```java
public enum MemberRole {
    MEMBER,
    COACH,
    ADMIN,
    ADMIN_COACH
}

public enum MembershipStatus {
    PENDING,
    ACTIVE,
    REJECTED,
    BLOCKED
}
```

### 5.5 CoachAssignment

```java
@Entity
@Table(name = "coach_assignments")
@FilterDef(name = "tenantFilter",
           parameters = @ParamDef(name = "gymId", type = Long.class))
@Filter(name = "tenantFilter", condition = "gym_id = :gymId")
@Getter @Setter @NoArgsConstructor
public class CoachAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "gym_id", nullable = false)
    private Long gymId;

    @Column(name = "coach_user_id", nullable = false)
    private Long coachUserId;

    @Column(name = "member_user_id", nullable = false)
    private Long memberUserId;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @Column(name = "unassigned_at")
    private LocalDateTime unassignedAt;
}
```

### 5.6 TemplateBlock y TemplateExercise

```java
@Entity
@Table(name = "template_blocks")
@Getter @Setter @NoArgsConstructor
public class TemplateBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private RoutineTemplate template;

    @Column(nullable = false)
    private String name;                      // "Día 1 — Pecho y Tríceps"

    @Column(name = "day_number", nullable = false)
    private Integer dayNumber;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @OneToMany(mappedBy = "block", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<TemplateExercise> exercises = new ArrayList<>();
}

@Entity
@Table(name = "template_exercises")
@Getter @Setter @NoArgsConstructor
public class TemplateExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "block_id", nullable = false)
    private TemplateBlock block;

    @Column(nullable = false)
    private String name;                      // "Press banca"

    private Integer sets;
    private String reps;                       // "10-12" o "Al fallo"
    private Integer restSeconds;
    private String notes;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
}
```

### 5.7 RoutineAssignment

```java
@Entity
@Table(name = "routine_assignments")
@FilterDef(name = "tenantFilter",
           parameters = @ParamDef(name = "gymId", type = Long.class))
@Filter(name = "tenantFilter", condition = "gym_id = :gymId")
@Getter @Setter @NoArgsConstructor
public class RoutineAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "gym_id", nullable = false)
    private Long gymId;

    @Column(name = "template_id", nullable = false)
    private Long templateId;

    @Column(name = "member_user_id", nullable = false)
    private Long memberUserId;

    @Column(name = "assigned_by", nullable = false)
    private Long assignedBy;

    @Column(name = "starts_at", nullable = false)
    private LocalDate startsAt;

    @Column(name = "ends_at", nullable = false)
    private LocalDate endsAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```

### 5.8 ExerciseCompletion

```java
@Entity
@Table(name = "exercise_completions")
@FilterDef(name = "tenantFilter",
           parameters = @ParamDef(name = "gymId", type = Long.class))
@Filter(name = "tenantFilter", condition = "gym_id = :gymId")
@Getter @Setter @NoArgsConstructor
public class ExerciseCompletion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "assignment_id", nullable = false)
    private Long assignmentId;

    @Column(name = "exercise_id", nullable = false)
    private Long exerciseId;

    @Column(name = "gym_id", nullable = false)
    private Long gymId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = true;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

### 5.9 ScheduleActivity

```java
@Entity
@Table(name = "schedule_activities")
@FilterDef(name = "tenantFilter",
           parameters = @ParamDef(name = "gymId", type = Long.class))
@Filter(name = "tenantFilter", condition = "gym_id = :gymId")
@Getter @Setter @NoArgsConstructor
public class ScheduleActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "gym_id", nullable = false)
    private Long gymId;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek;             // 1=Lunes ... 7=Domingo

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```

---

## 6. Migraciones Flyway

### V1__create_users.sql

```sql
CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    supabase_uid    VARCHAR(255) NOT NULL UNIQUE,
    full_name       VARCHAR(255) NOT NULL,
    email           VARCHAR(255) NOT NULL UNIQUE,
    avatar_url      VARCHAR(500),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP
);
```

### V2__create_auth_identities.sql

```sql
CREATE TABLE auth_identities (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id),
    provider        VARCHAR(50) NOT NULL,        -- 'google', 'email'
    provider_uid    VARCHAR(255) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(provider, provider_uid)
);
```

### V3__create_gyms.sql

```sql
CREATE TABLE gyms (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    slug            VARCHAR(100) NOT NULL UNIQUE,
    description     TEXT,
    logo_url        VARCHAR(500),
    routine_cycle   VARCHAR(20) NOT NULL DEFAULT 'WEEKLY',
    owner_user_id   BIGINT NOT NULL REFERENCES users(id),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP
);
```

### V4__create_gym_members.sql

```sql
CREATE TABLE gym_members (
    id                      BIGSERIAL PRIMARY KEY,
    user_id                 BIGINT NOT NULL REFERENCES users(id),
    gym_id                  BIGINT NOT NULL REFERENCES gyms(id),
    role                    VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    status                  VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    membership_expires_at   TIMESTAMP,
    created_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP,
    CONSTRAINT chk_role CHECK (role IN ('MEMBER','COACH','ADMIN','ADMIN_COACH')),
    CONSTRAINT chk_status CHECK (status IN ('PENDING','ACTIVE','REJECTED','BLOCKED'))
);
```

### V5__create_coach_assignments.sql

```sql
CREATE TABLE coach_assignments (
    id                BIGSERIAL PRIMARY KEY,
    gym_id            BIGINT NOT NULL REFERENCES gyms(id),
    coach_user_id     BIGINT NOT NULL REFERENCES users(id),
    member_user_id    BIGINT NOT NULL REFERENCES users(id),
    assigned_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    unassigned_at     TIMESTAMP
);
```

### V6__create_routine_templates.sql

```sql
CREATE TABLE routine_templates (
    id              BIGSERIAL PRIMARY KEY,
    gym_id          BIGINT NOT NULL REFERENCES gyms(id),
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    created_by      BIGINT NOT NULL REFERENCES users(id),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP
);
```

### V7__create_template_blocks.sql

```sql
CREATE TABLE template_blocks (
    id              BIGSERIAL PRIMARY KEY,
    template_id     BIGINT NOT NULL REFERENCES routine_templates(id) ON DELETE CASCADE,
    name            VARCHAR(255) NOT NULL,
    day_number      INTEGER NOT NULL,
    sort_order      INTEGER NOT NULL DEFAULT 0
);
```

### V8__create_template_exercises.sql

```sql
CREATE TABLE template_exercises (
    id              BIGSERIAL PRIMARY KEY,
    block_id        BIGINT NOT NULL REFERENCES template_blocks(id) ON DELETE CASCADE,
    name            VARCHAR(255) NOT NULL,
    sets            INTEGER,
    reps            VARCHAR(50),
    rest_seconds    INTEGER,
    notes           TEXT,
    sort_order      INTEGER NOT NULL DEFAULT 0
);
```

### V9__create_routine_assignments.sql

```sql
CREATE TABLE routine_assignments (
    id                BIGSERIAL PRIMARY KEY,
    gym_id            BIGINT NOT NULL REFERENCES gyms(id),
    template_id       BIGINT NOT NULL REFERENCES routine_templates(id),
    member_user_id    BIGINT NOT NULL REFERENCES users(id),
    assigned_by       BIGINT NOT NULL REFERENCES users(id),
    starts_at         DATE NOT NULL,
    ends_at           DATE NOT NULL,
    created_at        TIMESTAMP NOT NULL DEFAULT NOW()
);
```

### V10__create_exercise_completions.sql

```sql
CREATE TABLE exercise_completions (
    id              BIGSERIAL PRIMARY KEY,
    assignment_id   BIGINT NOT NULL REFERENCES routine_assignments(id),
    exercise_id     BIGINT NOT NULL REFERENCES template_exercises(id),
    gym_id          BIGINT NOT NULL REFERENCES gyms(id),
    user_id         BIGINT NOT NULL REFERENCES users(id),
    completed_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    is_completed    BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at      TIMESTAMP
);
```

### V11__create_schedule_activities.sql

```sql
CREATE TABLE schedule_activities (
    id              BIGSERIAL PRIMARY KEY,
    gym_id          BIGINT NOT NULL REFERENCES gyms(id),
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    day_of_week     INTEGER NOT NULL CHECK (day_of_week BETWEEN 1 AND 7),
    start_time      TIME NOT NULL,
    end_time        TIME NOT NULL,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);
```

### V12__create_indexes.sql

```sql
-- Multi-tenant indexes
CREATE INDEX idx_gym_members_gym_id ON gym_members(gym_id);
CREATE INDEX idx_gym_members_user_gym ON gym_members(user_id, gym_id);
CREATE INDEX idx_coach_assignments_gym ON coach_assignments(gym_id);
CREATE INDEX idx_coach_assignments_coach ON coach_assignments(coach_user_id, gym_id);
CREATE INDEX idx_routine_templates_gym ON routine_templates(gym_id);
CREATE INDEX idx_routine_assignments_member ON routine_assignments(member_user_id, gym_id);
CREATE INDEX idx_exercise_completions_assignment ON exercise_completions(assignment_id);
CREATE INDEX idx_exercise_completions_gym ON exercise_completions(gym_id);
CREATE INDEX idx_schedule_activities_gym ON schedule_activities(gym_id);

-- Business constraints
CREATE UNIQUE INDEX idx_unique_pending_membership
    ON gym_members(user_id, gym_id)
    WHERE status = 'PENDING';

-- Performance
CREATE UNIQUE INDEX idx_users_supabase_uid ON users(supabase_uid);
CREATE INDEX idx_gyms_slug ON gyms(slug);
CREATE INDEX idx_exercise_completions_user_assignment
    ON exercise_completions(user_id, assignment_id);
```

---

## 7. Ejemplo de Service y Controller

### 7.1 MembershipService (lógica de negocio clave)

```java
@Service
@RequiredArgsConstructor
@Transactional
public class MembershipService {

    private final GymMemberRepository gymMemberRepository;
    private final GymRepository gymRepository;
    private final UserRepository userRepository;

    public GymMemberDto requestJoin(Long userId, Long gymId) {
        // Validar que el gym existe
        gymRepository.findById(gymId)
            .orElseThrow(() -> new ResourceNotFoundException("Gym not found"));

        // Validar que no existe solicitud pendiente o membresía activa
        Optional<GymMember> existing = gymMemberRepository
            .findByUserIdAndGymIdAndStatusIn(userId, gymId,
                List.of(MembershipStatus.PENDING, MembershipStatus.ACTIVE));

        if (existing.isPresent()) {
            throw new BusinessException("Already have a pending or active membership");
        }

        GymMember member = new GymMember();
        member.setUserId(userId);
        member.setGymId(gymId);
        member.setRole(MemberRole.MEMBER);
        member.setStatus(MembershipStatus.PENDING);
        member.setCreatedAt(LocalDateTime.now());

        return toDto(gymMemberRepository.save(member));
    }

    public GymMemberDto approve(Long gymId, Long memberId, LocalDateTime expiresAt) {
        GymMember member = gymMemberRepository.findById(memberId)
            .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        // Validación de tenant
        if (!member.getGymId().equals(gymId)) {
            throw new TenantViolationException("Member does not belong to this gym");
        }

        if (member.getStatus() != MembershipStatus.PENDING) {
            throw new BusinessException("Can only approve PENDING memberships");
        }

        member.setStatus(MembershipStatus.ACTIVE);
        member.setMembershipExpiresAt(expiresAt);
        member.setUpdatedAt(LocalDateTime.now());

        return toDto(gymMemberRepository.save(member));
    }

    public List<MembershipDto> getUserMemberships(Long userId) {
        // Este método NO filtra por gym_id — devuelve todos los gyms del usuario
        return gymMemberRepository.findByUserIdAndStatusIn(userId,
                List.of(MembershipStatus.ACTIVE, MembershipStatus.PENDING))
            .stream()
            .map(this::toMembershipDto)
            .toList();
    }
}
```

### 7.2 MembershipController

```java
@RestController
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipService membershipService;

    // --- Member endpoints (consumidos por app móvil) ---

    @PostMapping("/api/gyms/{gymId}/join-request")
    public ResponseEntity<ApiResponse<GymMemberDto>> requestJoin(
            @PathVariable Long gymId,
            @CurrentUser Long userId) {
        GymMemberDto result = membershipService.requestJoin(userId, gymId);
        return ResponseEntity.status(201).body(ApiResponse.success(result));
    }

    @GetMapping("/api/users/me/memberships")
    public ResponseEntity<ApiResponse<List<MembershipDto>>> getMyMemberships(
            @CurrentUser Long userId) {
        return ResponseEntity.ok(
            ApiResponse.success(membershipService.getUserMemberships(userId)));
    }

    // --- Admin endpoints (consumidos por panel web) ---

    @PreAuthorize("@gymAccess.hasRole(#gymId, 'ADMIN')")
    @GetMapping("/api/gyms/{gymId}/admin/members")
    public ResponseEntity<ApiResponse<List<GymMemberDto>>> listMembers(
            @PathVariable Long gymId) {
        return ResponseEntity.ok(
            ApiResponse.success(membershipService.listMembers(gymId)));
    }

    @PreAuthorize("@gymAccess.hasRole(#gymId, 'ADMIN')")
    @PutMapping("/api/gyms/{gymId}/admin/members/{memberId}/approve")
    public ResponseEntity<ApiResponse<GymMemberDto>> approve(
            @PathVariable Long gymId,
            @PathVariable Long memberId,
            @RequestBody @Valid ApproveMemberRequest request) {
        GymMemberDto result = membershipService.approve(
            gymId, memberId, request.getExpiresAt());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PreAuthorize("@gymAccess.hasRole(#gymId, 'ADMIN')")
    @PutMapping("/api/gyms/{gymId}/admin/members/{memberId}/reject")
    public ResponseEntity<ApiResponse<GymMemberDto>> reject(
            @PathVariable Long gymId,
            @PathVariable Long memberId) {
        return ResponseEntity.ok(
            ApiResponse.success(membershipService.reject(gymId, memberId)));
    }
}
```

---

## 8. DTOs y Response Wrapper

### ApiResponse.java (wrapper estándar)

```java
@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private ApiError error;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(false, null, new ApiError(code, message));
    }
}
```

Todas las respuestas de la API siguen este formato:

```json
{
  "success": true,
  "data": { ... },
  "error": null
}
```

Esto permite que ambos clientes (web y app) parseen la respuesta con la misma estructura.

---

## 9. Dockerfile

```dockerfile
# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests -B

# Run stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/sgg-api-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## 10. Testing Strategy

### Prioridades para un solo dev:

1. **Tests unitarios de services** — Lógica de negocio pura (mocks de repositories).
2. **Tests de integración de multi-tenancy** — Verificar que los filtros por gym_id funcionan correctamente. Este es el test más importante del sistema.
3. **Tests de integración de endpoints** — Con `@SpringBootTest` y TestContainers para PostgreSQL real.

### Test crítico: Aislamiento de Tenant

```java
@SpringBootTest
@Testcontainers
class TenantIsolationTest {

    @Test
    void routineFromGymA_notVisibleInGymB() {
        // Crear rutina en gym A
        TenantContext.setGymId(gymAId);
        RoutineTemplate template = createTemplate("Rutina Gym A");

        // Buscar desde gym B — NO debe encontrarla
        TenantContext.setGymId(gymBId);
        List<RoutineTemplate> templates = templateRepository.findAll();

        assertThat(templates).isEmpty();
    }
}
```

---

## 11. Convenciones del Proyecto

| Aspecto | Convención |
|---|---|
| Naming de packages | lowercase, singular (`training`, no `trainings`) |
| Naming de entidades | PascalCase, singular (`RoutineTemplate`) |
| Naming de tablas | snake_case, plural (`routine_templates`) |
| DTOs | Sufijo `Dto` para response, `Request` para input |
| Exceptions | Extienden `RuntimeException`, handler global las atrapa |
| Timestamps | `LocalDateTime` en Java, `TIMESTAMP` en SQL, UTC siempre |
| IDs | `Long` (BIGSERIAL en PostgreSQL) |
| Transacciones | `@Transactional` en service layer, nunca en controllers |
| Validación | Bean Validation (`@Valid`) en controllers, lógica de negocio en services |

---

*Este documento cubre la implementación completa del backend. Para detalles de la API consumida por cada cliente, ver los documentos de sgg-web y sgg-app.*
