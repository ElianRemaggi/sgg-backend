# SGG-API — Documento Técnico del Backend

**Repositorio:** `sgg-api`
**Stack:** Java 21 + Spring Boot 3.3.5 + PostgreSQL 16 + Flyway + SpringDoc OpenAPI
**Versión:** 1.3
**Fecha:** 22 de febrero de 2026

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
  - OAuth2 Resource Server     (comentado en dev — activar al integrar Supabase)
  - PostgreSQL Driver
  - Flyway Migration
  - Lombok
  - Spring Boot DevTools (solo dev)
  - Spring Boot Actuator
  - SpringDoc OpenAPI 2.6.0   (Swagger UI en /doc)
  - H2 Database               (test scope — para SggApiApplicationTests)
```

### 2.2 Estructura de Directorios Completa

```
sgg-api/
├── src/
│   ├── main/
│   │   ├── java/com/sgg/
│   │   │   ├── SggApiApplication.java
│   │   │   ├── package-info.java             (@FilterDef global — fix multi-tenant)
│   │   │   │
│   │   │   ├── common/
│   │   │   │   ├── config/
│   │   │   │   │   ├── SecurityConfig.java
│   │   │   │   │   ├── OpenApiConfig.java            (Swagger / SpringDoc)
│   │   │   │   │   ├── CorsConfig.java
│   │   │   │   │   ├── WebMvcConfig.java
│   │   │   │   │   └── JacksonConfig.java
│   │   │   │   ├── security/
│   │   │   │   │   ├── TenantContext.java
│   │   │   │   │   ├── TenantInterceptor.java
│   │   │   │   │   ├── TenantHibernateFilterAspect.java
│   │   │   │   │   ├── DevUserInterceptor.java       (solo dev — reemplazar por JWT en prod)
│   │   │   │   │   ├── GymAccessChecker.java
│   │   │   │   │   ├── JwtToUserConverter.java       (stub — activar al integrar Supabase)
│   │   │   │   │   ├── CurrentUser.java              (annotation)
│   │   │   │   │   └── CurrentUserResolver.java
│   │   │   │   ├── exception/
│   │   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   │   ├── BusinessException.java
│   │   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   │   ├── UnauthorizedException.java
│   │   │   │   │   └── TenantViolationException.java
│   │   │   │   └── dto/
│   │   │   │       ├── ApiResponse.java
│   │   │   │       └── ApiError.java
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
│   │   │   │       ├── GymInfoDto.java            ← v1.3: +memberCount, +coachCount
│   │   │   │       ├── GymMemberDto.java          ← v1.3: +avatarUrl, +assignedCoachId, +assignedCoachName
│   │   │   │       ├── MembershipDto.java
│   │   │   │       ├── CreateGymRequest.java
│   │   │   │       ├── UpdateGymRequest.java
│   │   │   │       ├── ApproveMemberRequest.java
│   │   │   │       └── UpdateMemberRoleRequest.java
│   │   │   │
│   │   │   ├── coaching/
│   │   │   │   ├── controller/
│   │   │   │   │   └── CoachAssignmentController.java
│   │   │   │   ├── service/
│   │   │   │   │   └── CoachAssignmentService.java
│   │   │   │   ├── repository/
│   │   │   │   │   └── CoachAssignmentRepository.java  ← v1.3: +findActiveByGymId()
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
│   │   │   │   │   ├── RoutineTemplateService.java     ← v1.3: resuelve createdByName
│   │   │   │   │   └── RoutineAssignmentService.java   ← v1.3: retorna RoutineAssignmentDto
│   │   │   │   ├── repository/
│   │   │   │   │   ├── RoutineTemplateRepository.java
│   │   │   │   │   ├── TemplateBlockRepository.java
│   │   │   │   │   ├── TemplateExerciseRepository.java
│   │   │   │   │   └── RoutineAssignmentRepository.java  ← v1.3: +findActiveByMemberUserIdAndGymId()
│   │   │   │   ├── entity/
│   │   │   │   │   ├── RoutineTemplate.java
│   │   │   │   │   ├── TemplateBlock.java
│   │   │   │   │   ├── TemplateExercise.java
│   │   │   │   │   └── RoutineAssignment.java
│   │   │   │   └── dto/
│   │   │   │       ├── RoutineTemplateDto.java         ← v1.3: +createdByName
│   │   │   │       ├── RoutineAssignmentDto.java       ← v1.3: NUEVO
│   │   │   │       ├── TemplateBlockDto.java
│   │   │   │       ├── TemplateExerciseDto.java
│   │   │   │       ├── CreateTemplateRequest.java
│   │   │   │       ├── AssignRoutineRequest.java
│   │   │   │       └── MemberRoutineDto.java
│   │   │   │
│   │   │   ├── tracking/
│   │   │   │   ├── controller/
│   │   │   │   │   └── TrackingController.java         ← v1.3: +endpoint coach progress
│   │   │   │   ├── service/
│   │   │   │   │   └── TrackingService.java            ← v1.3: +lastActivityAt, +getProgressByMember()
│   │   │   │   ├── repository/
│   │   │   │   │   └── ExerciseCompletionRepository.java  ← v1.3: +findLastActivityByAssignmentId()
│   │   │   │   ├── entity/
│   │   │   │   │   └── ExerciseCompletion.java
│   │   │   │   └── dto/
│   │   │   │       ├── CompleteExerciseRequest.java
│   │   │   │       └── ProgressDto.java               ← v1.3: +lastActivityAt, BlockProgressDto +percentComplete
│   │   │   │
│   │   │   └── schedule/
│   │   │       ├── controller/
│   │   │       │   └── ScheduleController.java
│   │   │       ├── service/
│   │   │       │   └── ScheduleService.java
│   │   │       ├── repository/
│   │   │       │   └── ScheduleActivityRepository.java
│   │   │       ├── entity/
│   │   │       │   └── ScheduleActivity.java           ← v1.3: +instructor, +maxCapacity
│   │   │       └── dto/
│   │   │           ├── ScheduleActivityDto.java        ← v1.3: +instructor, +maxCapacity
│   │   │           └── CreateScheduleRequest.java      ← v1.3: +instructor, +maxCapacity
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
│   │           ├── V12__create_indexes.sql
│   │           └── V13__add_schedule_fields.sql        ← v1.3: NUEVO
│   │
│   └── test/
│       └── java/com/sgg/
│           ├── SggApiApplicationTests.java           (context load — H2)
│           ├── identity/service/
│           │   └── UserServiceTest.java
│           ├── tenancy/service/
│           │   ├── GymServiceTest.java
│           │   └── MembershipServiceTest.java
│           ├── coaching/service/
│           │   └── CoachAssignmentServiceTest.java
│           ├── training/service/
│           │   ├── RoutineTemplateServiceTest.java
│           │   └── RoutineAssignmentServiceTest.java
│           ├── tracking/service/
│           │   └── TrackingServiceTest.java
│           └── schedule/service/
│               └── ScheduleServiceTest.java
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
    password: ${SPRING_DATASOURCE_PASSWORD:sgg_local_pass}
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

  # JWT/OAuth2 deshabilitado en dev — activar cuando configures Supabase:
  # security:
  #   oauth2:
  #     resourceserver:
  #       jwt:
  #         jwk-set-uri: ${SUPABASE_JWKS_URI}

server:
  port: 8080

logging:
  level:
    com.sgg: DEBUG
    org.hibernate.SQL: DEBUG
    org.springframework.security: INFO

springdoc:
  swagger-ui:
    path: /doc
    tags-sorter: alpha
    operations-sorter: alpha
  api-docs:
    path: /doc/api-docs

app:
  cors:
    web-origin: ${APP_CORS_WEB_ORIGIN:http://localhost:3000}
    additional-origins: ${APP_CORS_ADDITIONAL_ORIGINS:http://localhost:8081,http://localhost:19006}
  supabase:
    url: ${SUPABASE_URL:disabled}
    jwt-secret: ${SUPABASE_JWT_SECRET:dev-secret-placeholder}
  dev-mode: ${APP_DEV_MODE:true}
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

### 4.2 Seguridad — Dev Mode (sin JWT)

> **Estado actual:** modo desarrollo. La autenticación JWT con Supabase está comentada.
> Para activarla: descomentar `oauth2-resource-server` en `pom.xml` y la sección
> `security.oauth2` en `application.yml`, y reemplazar `SecurityConfig` + `DevUserInterceptor`
> por la versión con `JwtToUserConverter`.

**SecurityConfig.java** (dev-mode):

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // DEV MODE: todos los endpoints son públicos
            // En producción reemplazar por autenticación JWT con Supabase
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }
}
```

**DevUserInterceptor.java** — Simula un usuario autenticado via header HTTP:

```java
@Slf4j
@Component
public class DevUserInterceptor implements HandlerInterceptor {

    private static final String DEV_USER_HEADER = "X-Dev-User-Id";

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        String userIdHeader = request.getHeader(DEV_USER_HEADER);
        if (userIdHeader != null) {
            try {
                Long userId = Long.valueOf(userIdHeader.trim());
                TenantContext.setUserId(userId);
                log.debug("[DEV] userId={} seteado desde header {}", userId, DEV_USER_HEADER);
            } catch (NumberFormatException e) {
                log.warn("[DEV] Header {} inválido: {}", DEV_USER_HEADER, userIdHeader);
            }
        }
        return true;
    }
}
```

> En producción este interceptor se reemplaza por `JwtToUserConverter` que extrae
> el `userId` del JWT de Supabase y lo inyecta en `TenantContext`.

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

### 4.3 Documentación API — Swagger UI

Disponible en **`GET /doc`** (UI interactiva) y **`GET /doc/api-docs`** (JSON OpenAPI).

**OpenApiConfig.java:**

```java
@Configuration
public class OpenApiConfig {

    static {
        // Oculta @CurrentUser de la UI — se resuelve internamente desde el header
        SpringDocUtils.getConfig().addAnnotationsToIgnore(CurrentUser.class);
    }

    @Bean
    public OpenAPI sggOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SGG API")
                        .description("Backend multi-tenant para gimnasios. ...")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList("Dev-Auth"))
                .components(new Components()
                        .addSecuritySchemes("Dev-Auth", new SecurityScheme()
                                .name("X-Dev-User-Id")
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .description("ID del usuario (dev-mode). Ej: 1")));
    }
}
```

**Uso de Swagger en dev:**
1. Abrir `http://localhost:8080/doc`
2. Click en **Authorize** (candado)
3. Ingresar el `userId` (ej: `1`) en el campo `X-Dev-User-Id`
4. Ejecutar cualquier endpoint — el header se envía automáticamente

Todos los controllers están anotados con `@Tag` (agrupa endpoints) y `@Operation` (describe cada método, incluyendo el rol requerido en el prefijo: `[ADMIN]`, `[COACH]`, `[MEMBER]`).

---

### 4.4 Resolución del Usuario Actual

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

### 4.5 Exception Handling Global

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

### 5.9 ScheduleActivity *(v1.3: +instructor, +maxCapacity)*

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

    private String instructor;             // ← v1.3

    @Column(name = "max_capacity")
    private Integer maxCapacity;           // ← v1.3

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

### V13__add_schedule_fields.sql *(v1.3)*

```sql
ALTER TABLE schedule_activities ADD COLUMN instructor VARCHAR(255);
ALTER TABLE schedule_activities ADD COLUMN max_capacity INTEGER;
```

---

## 7. DTOs — Referencia Completa

### 7.1 GymInfoDto *(v1.3: +memberCount, +coachCount)*

```java
public class GymInfoDto {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String logoUrl;
    private String routineCycle;
    private String userRole;    // rol del usuario autenticado en este gym (null si no es miembro)
    private int memberCount;    // cantidad de miembros ACTIVE
    private int coachCount;     // cantidad de COACH o ADMIN_COACH con status ACTIVE
}
```

> `memberCount` y `coachCount` solo se populan en `GET /api/gyms/{gymId}/info`.
> En `createGym`, `updateSettings` y `searchGyms` se retornan como `0`.

### 7.2 GymMemberDto *(v1.3: +avatarUrl, +assignedCoachId, +assignedCoachName)*

```java
public class GymMemberDto {
    private Long id;
    private Long userId;
    private Long gymId;
    private String fullName;          // desde User
    private String email;             // desde User
    private String avatarUrl;         // desde User  ← v1.3
    private String role;              // MemberRole.name()
    private String status;            // MembershipStatus.name()
    private LocalDateTime membershipExpiresAt;
    private LocalDateTime createdAt;
    private Long assignedCoachId;     // userId del coach activo ← v1.3
    private String assignedCoachName; // fullName del coach activo ← v1.3
}
```

> `assignedCoachId` y `assignedCoachName` solo se populan en `GET /api/gyms/{gymId}/admin/members`.
> En operaciones de un solo miembro (approve, reject, block, etc.) se retornan como `null`.

### 7.3 ProgressDto *(v1.3: +lastActivityAt, BlockProgressDto +percentComplete)*

```java
public class ProgressDto {
    private Long assignmentId;
    private String templateName;
    private int totalExercises;
    private int completedExercises;
    private int percentComplete;
    private List<BlockProgressDto> blockProgress;
    private LocalDateTime lastActivityAt; // MAX(completed_at) — null si nada completado ← v1.3

    public static class BlockProgressDto {
        private Long blockId;
        private String blockName;
        private int totalExercises;
        private int completedExercises;
        private int percentComplete;        // ← v1.3
        private List<Long> completedExerciseIds;
    }
}
```

### 7.4 RoutineTemplateDto *(v1.3: +createdByName)*

```java
public class RoutineTemplateDto {
    private Long id;
    private String name;
    private String description;
    private Long createdBy;
    private String createdByName; // fullName del usuario que creó la plantilla ← v1.3
    private List<TemplateBlockDto> blocks;
    private LocalDateTime createdAt;
}
```

### 7.5 RoutineAssignmentDto *(v1.3: NUEVO)*

```java
public class RoutineAssignmentDto {
    private Long id;
    private Long gymId;
    private Long templateId;
    private String templateName;    // nombre de la plantilla asignada
    private Long memberUserId;
    private String memberName;      // fullName del miembro
    private String memberAvatarUrl; // avatarUrl del miembro
    private Long assignedBy;
    private LocalDate startsAt;
    private LocalDate endsAt;
}
```

### 7.6 ScheduleActivityDto *(v1.3: +instructor, +maxCapacity)*

```java
public class ScheduleActivityDto {
    private Long id;
    private String name;
    private String description;
    private Integer dayOfWeek;    // 1=Lunes ... 7=Domingo
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean isActive;
    private String instructor;    // ← v1.3
    private Integer maxCapacity;  // ← v1.3
}
```

### 7.7 ApiResponse (wrapper estándar)

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

---

## 8. Repositories — Queries Relevantes

### GymMemberRepository

```java
// Membership check (sin filtro Hibernate — para TenantInterceptor)
@Query("SELECT gm FROM GymMember gm WHERE gm.userId = :userId AND gm.gymId = :gymId AND gm.status IN :statuses")
Optional<GymMember> findMembershipDirect(Long userId, Long gymId, List<MembershipStatus> statuses);

// Conteo para GymInfoDto (v1.3)
long countByGymIdAndStatus(Long gymId, MembershipStatus status);
long countByGymIdAndRoleInAndStatus(Long gymId, List<MemberRole> roles, MembershipStatus status);
```

### CoachAssignmentRepository

```java
// Asignaciones activas de un coach (unassignedAt IS NULL)
List<CoachAssignment> findByGymIdAndCoachUserIdAndUnassignedAtIsNull(Long gymId, Long coachUserId);

// Todas las asignaciones activas del gym — para enriquecer GymMemberDto (v1.3)
@Query("SELECT ca FROM CoachAssignment ca WHERE ca.gymId = :gymId AND ca.unassignedAt IS NULL")
List<CoachAssignment> findActiveByGymId(Long gymId);
```

### RoutineAssignmentRepository

```java
// Rutina activa por miembro (para endpoint de miembro/coach)
Optional<RoutineAssignment> findFirstByMemberUserIdAndGymIdAndStartsAtLessThanEqualAndEndsAtGreaterThanEqual(
    Long memberUserId, Long gymId, LocalDate today1, LocalDate today2);

// Rutina activa por miembro — para coach progress endpoint (v1.3)
@Query("SELECT ra FROM RoutineAssignment ra WHERE ra.memberUserId = :memberUserId " +
       "AND ra.gymId = :gymId AND ra.startsAt <= :today AND ra.endsAt >= :today")
Optional<RoutineAssignment> findActiveByMemberUserIdAndGymId(Long memberUserId, Long gymId, LocalDate today);
```

### ExerciseCompletionRepository

```java
// Progreso del miembro
List<ExerciseCompletion> findByAssignmentIdAndUserIdAndIsCompleted(Long assignmentId, Long userId, Boolean isCompleted);

// Última actividad para ProgressDto (v1.3)
@Query("SELECT MAX(ec.completedAt) FROM ExerciseCompletion ec " +
       "WHERE ec.assignmentId = :assignmentId AND ec.isCompleted = true")
Optional<LocalDateTime> findLastActivityByAssignmentId(Long assignmentId);
```

---

## 9. Ejemplo de Service y Controller

### 9.1 MembershipService (lógica de negocio clave)

```java
@Service
@RequiredArgsConstructor
@Transactional
public class MembershipService {

    private final GymMemberRepository gymMemberRepository;
    private final GymRepository gymRepository;
    private final UserRepository userRepository;
    private final CoachAssignmentRepository coachAssignmentRepository; // ← v1.3

    public GymMemberDto requestJoin(Long userId, Long gymId) {
        gymRepository.findById(gymId)
            .orElseThrow(() -> new ResourceNotFoundException("Gym not found"));

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

        return toDto(gymMemberRepository.save(member));
    }

    // listMembers() enriquece cada GymMemberDto con datos de coach asignado (v1.3)
    public List<GymMemberDto> listMembers(Long gymId) {
        List<CoachAssignment> activeAssignments = coachAssignmentRepository.findActiveByGymId(gymId);

        Map<Long, CoachAssignment> coachByMemberId = activeAssignments.stream()
            .collect(Collectors.toMap(CoachAssignment::getMemberUserId, ca -> ca, (a, b) -> a));

        Map<Long, String> coachNameById = new HashMap<>();
        for (CoachAssignment ca : activeAssignments) {
            coachNameById.computeIfAbsent(ca.getCoachUserId(),
                id -> userRepository.findById(id).map(User::getFullName).orElse(null));
        }

        return gymMemberRepository.findByGymId(gymId).stream()
            .map(m -> toDtoWithCoach(m, coachByMemberId.get(m.getUserId()), coachNameById))
            .toList();
    }
}
```

### 9.2 TrackingService — getProgressByMember (v1.3)

```java
// Retorna el progreso de la rutina activa de un miembro (vista de coach)
@Transactional(readOnly = true)
public ProgressDto getProgressByMember(Long gymId, Long coachUserId, Long memberUserId) {
    LocalDate today = LocalDate.now();
    RoutineAssignment assignment = assignmentRepository
        .findActiveByMemberUserIdAndGymId(memberUserId, gymId, today)
        .orElseThrow(() -> new ResourceNotFoundException(
            "No hay rutina activa para el miembro: " + memberUserId));
    return getProgress(gymId, memberUserId, assignment.getId());
}
```

### 9.3 MembershipController

```java
@RestController
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipService membershipService;

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

## 10. Dockerfile

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

## 11. Testing

**73 tests unitarios** — todos passing. Framework: JUnit 5 + Mockito (`@ExtendWith(MockitoExtension.class)`). No levanta contexto Spring (excepto `SggApiApplicationTests`).

### Archivos de test

| Archivo | Tests | Qué cubre |
|---|---|---|
| `SggApiApplicationTests` | 1 | Context load con H2 |
| `UserServiceTest` | 6 | syncUser (crear/actualizar), getProfile, updateProfile |
| `GymServiceTest` | 10 | createGym, getGymInfo (+memberCount/coachCount), searchGyms, updateSettings |
| `MembershipServiceTest` | 19 | requestJoin, approve, reject, block, listMembers (+coach asignado), getUserMemberships, listCoaches, updateRole |
| `CoachAssignmentServiceTest` | 7 | assign, unassign, listByGym, listByCoach |
| `RoutineTemplateServiceTest` | 8 | create (con blocks/exercises), listByGym, getById, delete |
| `RoutineAssignmentServiceTest` | 7 | assign (→ RoutineAssignmentDto), getActiveRoutineForMember, getAssignmentsForMember |
| `TrackingServiceTest` | 9 | complete (nuevo/existente), undo, getProgress (+lastActivityAt), getProgressByMember |
| `ScheduleServiceTest` | 6 | create (+instructor/maxCapacity), delete (ok/not found/gym erróneo), listActive |

### Patrón de test

```java
@ExtendWith(MockitoExtension.class)
class MembershipServiceTest {

    @Mock GymMemberRepository gymMemberRepository;
    @Mock GymRepository gymRepository;
    @Mock UserRepository userRepository;
    @Mock CoachAssignmentRepository coachAssignmentRepository;  // ← v1.3

    @InjectMocks MembershipService membershipService;

    @Test
    void requestJoin_success() {
        when(gymRepository.findById(1L)).thenReturn(Optional.of(new Gym()));
        when(gymMemberRepository.findByUserIdAndGymIdAndStatusIn(any(), any(), any()))
            .thenReturn(Optional.empty());
        when(gymMemberRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        GymMemberDto result = membershipService.requestJoin(10L, 1L);

        assertThat(result.getStatus()).isEqualTo("PENDING");
    }
}
```

### Notas sobre TenantContext en tests

`GymService.getGymInfo()` usa `TenantContext.getUserId()` (ThreadLocal estático). En los tests que lo requieren se setea manualmente y se limpia en `@AfterEach`:

```java
@AfterEach
void clearTenant() {
    TenantContext.clear();
}
```

### Ejecutar tests

```bash
# Con Maven local
mvn test
```

---

## 12. Referencia de Endpoints

> Ver listado interactivo en `http://localhost:8080/doc`

### Auth & Users
| Método | Path | Rol | Descripción |
|---|---|---|---|
| POST | `/api/auth/sync` | Público | Sincroniza/crea usuario tras login en Supabase |
| GET | `/api/users/me` | Autenticado | Perfil del usuario actual |
| PUT | `/api/users/me` | Autenticado | Actualiza nombre / avatar |

### Gyms
| Método | Path | Rol | Descripción |
|---|---|---|---|
| POST | `/api/gyms` | Autenticado | Crea un gimnasio (el creador queda como ADMIN_COACH) |
| GET | `/api/gyms/{gymId}/info` | Autenticado | Info del gimnasio + rol del usuario + memberCount + coachCount |
| PUT | `/api/gyms/{gymId}/admin/settings` | ADMIN | Actualiza nombre, descripción, logo, routineCycle |
| GET | `/api/gyms/search?q=` | Público | Busca gimnasios por nombre |

### Memberships
| Método | Path | Rol | Descripción |
|---|---|---|---|
| POST | `/api/gyms/{gymId}/join-request` | Autenticado | Solicita unirse al gym |
| GET | `/api/users/me/memberships` | Autenticado | Mis membresías activas/pendientes |
| GET | `/api/gyms/{gymId}/admin/members` | ADMIN | Lista miembros con fullName, email, avatarUrl, coach asignado |
| GET | `/api/gyms/{gymId}/admin/coaches` | ADMIN | Lista miembros con rol COACH o ADMIN_COACH activos |
| PUT | `/api/gyms/{gymId}/admin/members/{memberId}/role` | ADMIN | Cambia el rol de un miembro activo |
| PUT | `/api/gyms/{gymId}/admin/members/{memberId}/approve` | ADMIN | Aprueba solicitud pendiente |
| PUT | `/api/gyms/{gymId}/admin/members/{memberId}/reject` | ADMIN | Rechaza solicitud pendiente |
| PUT | `/api/gyms/{gymId}/admin/members/{memberId}/block` | ADMIN | Bloquea miembro |
| PUT | `/api/gyms/{gymId}/admin/members/{memberId}/expiration` | ADMIN | Actualiza fecha de vencimiento |

### Coach Assignments
| Método | Path | Rol | Descripción |
|---|---|---|---|
| POST | `/api/gyms/{gymId}/admin/coach-assignments` | ADMIN | Asigna coach a miembro |
| DELETE | `/api/gyms/{gymId}/admin/coach-assignments/{assignmentId}` | ADMIN | Desasigna coach |
| GET | `/api/gyms/{gymId}/admin/coach-assignments` | ADMIN | Lista asignaciones del gym |
| GET | `/api/gyms/{gymId}/coach/my-assignments` | COACH | Mis miembros asignados |

### Routine Templates
| Método | Path | Rol | Descripción |
|---|---|---|---|
| POST | `/api/gyms/{gymId}/coach/routine-templates` | COACH | Crea plantilla con bloques y ejercicios |
| GET | `/api/gyms/{gymId}/coach/routine-templates` | COACH | Lista plantillas del gym (con createdByName) |
| GET | `/api/gyms/{gymId}/coach/routine-templates/{templateId}` | COACH | Detalle de plantilla (con createdByName) |
| DELETE | `/api/gyms/{gymId}/coach/routine-templates/{templateId}` | COACH | Elimina plantilla |

### Routine Assignments
| Método | Path | Rol | Descripción |
|---|---|---|---|
| POST | `/api/gyms/{gymId}/coach/routine-assignments` | COACH | Asigna plantilla a miembro — retorna RoutineAssignmentDto |
| GET | `/api/gyms/{gymId}/member/routine` | MEMBER | Mi rutina activa hoy |
| GET | `/api/gyms/{gymId}/coach/members/{memberId}/routine` | COACH | Rutina activa de un miembro |

### Tracking
| Método | Path | Rol | Descripción |
|---|---|---|---|
| POST | `/api/gyms/{gymId}/member/tracking/complete` | MEMBER | Marca ejercicio como completado |
| POST | `/api/gyms/{gymId}/member/tracking/undo` | MEMBER | Revierte ejercicio a no completado |
| GET | `/api/gyms/{gymId}/member/tracking/progress/{assignmentId}` | MEMBER | Progreso con lastActivityAt y percentComplete por bloque |
| GET | `/api/gyms/{gymId}/coach/members/{memberId}/progress` | COACH | **v1.3** — Progreso de la rutina activa de un miembro |

### Schedule
| Método | Path | Rol | Descripción |
|---|---|---|---|
| GET | `/api/gyms/{gymId}/schedule` | Público | Lista actividades activas (con instructor y maxCapacity) |
| POST | `/api/gyms/{gymId}/admin/schedule` | ADMIN | Agrega actividad al horario (acepta instructor y maxCapacity) |
| DELETE | `/api/gyms/{gymId}/admin/schedule/{activityId}` | ADMIN | Desactiva actividad (soft delete) |

---

## 13. Convenciones del Proyecto

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

## 14. Changelog

| Versión | Fecha | Cambios |
|---|---|---|
| 1.3 | 22 feb 2026 | GymInfoDto +memberCount/coachCount; GymMemberDto +avatarUrl/assignedCoach; ProgressDto +lastActivityAt, BlockProgressDto +percentComplete; RoutineTemplateDto +createdByName; nuevo RoutineAssignmentDto; ScheduleActivity/Dto/Request +instructor/maxCapacity; nuevo endpoint `GET /coach/members/{memberId}/progress`; V13 migración schedule fields; 73 tests |
| 1.2 | 21 feb 2026 | Flujo completo de coaches implementado; endpoints coach/member/tracking completos |
| 1.1 | — | Estructura inicial, multi-tenancy, autenticación dev-mode |

---

*Este documento cubre la implementación completa del backend. Para detalles de la API consumida por cada cliente, ver los documentos de sgg-web y sgg-app.*
