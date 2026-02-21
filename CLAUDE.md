# SGG-API — Contexto para Claude CLI

## Qué es este proyecto
Backend de un SaaS multi-tenant para gimnasios.
Stack: Java 21 + Spring Boot 3.3.5 + PostgreSQL 16 + Flyway + Lombok

## Estado actual
- Proyecto generado y estructurado
- Migraciones Flyway V1-V12 corren exitosamente (12 tablas creadas)
- La app NO levanta todavía con PostgreSQL — errores en depuración
- Tests unitarios: 62 tests, todos passing (mvn test)
- Documentación Swagger disponible en /doc (una vez que levante la app)

## Errores resueltos hasta ahora
1. `Multiple '@FilterDef' annotations define a filter named 'tenantFilter'`
   - Fix: Mover @FilterDef a un único package-info.java en com.sgg
   - Cada entidad solo tiene @Filter (no @FilterDef)

2. `Invalid JWK Set URL "disabled"`
   - Fix: Comentar dependencia oauth2-resource-server en pom.xml
   - Fix: Simplificar SecurityConfig sin JWT
   - Fix: Agregar DevUserInterceptor para simular auth con header X-Dev-User-Id

## Arquitectura multi-tenant (crítico)
- Todas las entidades tenant-scoped tienen gym_id
- TenantContext: ThreadLocal con gymId y userId del request
- TenantInterceptor: extrae gymId de URL /api/gyms/{gymId}/... y valida membresía
- TenantHibernateFilterAspect: activa Hibernate Filter "tenantFilter" en cada query
- GymAccessChecker: bean para @PreAuthorize con roles por gimnasio
- DevUserInterceptor: en dev-mode, lee header X-Dev-User-Id para simular usuario autenticado

## Estructura de módulos
```
com.sgg/
├── common/
│   ├── config/         SecurityConfig, CorsConfig, WebMvcConfig, JacksonConfig,
│   │                   OpenApiConfig (Swagger)
│   ├── security/       TenantContext, TenantInterceptor, TenantHibernateFilterAspect,
│   │                   GymAccessChecker, CurrentUser, CurrentUserResolver,
│   │                   JwtToUserConverter (stub), DevUserInterceptor
│   ├── exception/      GlobalExceptionHandler, BusinessException,
│   │                   ResourceNotFoundException, UnauthorizedException,
│   │                   TenantViolationException
│   └── dto/            ApiResponse<T>, ApiError
├── identity/           User, AuthIdentity, UserService, UserController
├── tenancy/            Gym, GymMember, MemberRole(enum), MembershipStatus(enum),
│                       GymService, MembershipService, GymController, MembershipController
├── coaching/           CoachAssignment, CoachAssignmentService, CoachAssignmentController
├── training/           RoutineTemplate, TemplateBlock, TemplateExercise, RoutineAssignment,
│                       RoutineTemplateService, RoutineAssignmentService,
│                       RoutineTemplateController, RoutineAssignmentController
├── tracking/           ExerciseCompletion, TrackingService, TrackingController
└── schedule/           ScheduleActivity, ScheduleService, ScheduleController
```

## Base de datos — tablas creadas por Flyway
users, auth_identities, gyms, gym_members, coach_assignments,
routine_templates, template_blocks, template_exercises,
routine_assignments, exercise_completions, schedule_activities

## Reglas de negocio críticas
- Un usuario puede tener múltiples membresías (una por gimnasio)
- Solo puede existir una solicitud PENDING por usuario por gimnasio
- MembershipStatus: PENDING | ACTIVE | REJECTED | BLOCKED
- MemberRole: MEMBER | COACH | ADMIN | ADMIN_COACH
- ADMIN_COACH tiene permisos de ADMIN y COACH simultáneamente
- Si membresía vence: sin acceso a rutinas, con acceso a info del gym
- Coach solo opera sobre participantes que tiene asignados

## Endpoints principales
POST   /api/auth/sync                                        — sync usuario Supabase → local
GET    /api/users/me                                         — perfil usuario
PUT    /api/users/me                                         — actualizar perfil
GET    /api/gyms/search?q=                                   — buscar gyms (público)
POST   /api/gyms                                             — crear gym
GET    /api/gyms/{gymId}/info                                — info del gym
POST   /api/gyms/{gymId}/join-request                        — solicitar membresía
GET    /api/users/me/memberships                             — mis membresías
GET    /api/gyms/{gymId}/admin/members                       — listar miembros (ADMIN)
PUT    /api/gyms/{gymId}/admin/members/{id}/approve          — aprobar miembro (ADMIN)
PUT    /api/gyms/{gymId}/admin/members/{id}/reject           — rechazar (ADMIN)
PUT    /api/gyms/{gymId}/admin/members/{id}/block            — bloquear (ADMIN)
PUT    /api/gyms/{gymId}/admin/members/{id}/expiration       — actualizar vencimiento (ADMIN)
POST   /api/gyms/{gymId}/admin/coach-assignments             — asignar coach (ADMIN)
DELETE /api/gyms/{gymId}/admin/coach-assignments/{id}        — desasignar coach (ADMIN)
GET    /api/gyms/{gymId}/admin/coach-assignments             — listar asignaciones (ADMIN)
GET    /api/gyms/{gymId}/coach/my-assignments                — mis asignados (COACH)
POST   /api/gyms/{gymId}/coach/templates                     — crear plantilla (COACH)
GET    /api/gyms/{gymId}/coach/templates                     — listar plantillas (COACH)
GET    /api/gyms/{gymId}/coach/templates/{templateId}        — obtener plantilla (COACH)
DELETE /api/gyms/{gymId}/coach/templates/{templateId}        — eliminar plantilla (COACH)
POST   /api/gyms/{gymId}/coach/routine-assignments           — asignar rutina (COACH)
GET    /api/gyms/{gymId}/coach/members/{memberId}/routine    — rutina de un miembro (COACH)
GET    /api/gyms/{gymId}/member/routine                      — rutina activa (MEMBER)
POST   /api/gyms/{gymId}/member/tracking/complete            — marcar ejercicio (MEMBER)
POST   /api/gyms/{gymId}/member/tracking/undo                — desmarcar ejercicio (MEMBER)
GET    /api/gyms/{gymId}/member/tracking/progress/{id}       — progreso de rutina (MEMBER)
GET    /api/gyms/{gymId}/schedule                            — horarios del gym (público)
POST   /api/gyms/{gymId}/admin/schedule                      — crear actividad (ADMIN)
DELETE /api/gyms/{gymId}/admin/schedule/{activityId}         — eliminar actividad (ADMIN)

## Documentación interactiva
- Swagger UI:    GET /doc
- OpenAPI JSON:  GET /doc/api-docs
- Implementado con springdoc-openapi-starter-webmvc-ui 2.6.0
- OpenApiConfig oculta @CurrentUser de la UI y expone el header X-Dev-User-Id como autenticación

## Formato de respuesta estándar
Toda respuesta usa ApiResponse<T>:
{ "success": true, "data": {...}, "error": null }
{ "success": false, "data": null, "error": {"code": 404, "message": "..."} }

## Dev mode
- app.dev-mode=true: todos los endpoints son públicos (sin JWT)
- Para simular un usuario: header X-Dev-User-Id: {userId}
- Para simular estar en un gym: el gymId va en la URL
- En Swagger UI: usar el candado (Authorize) para setear X-Dev-User-Id globalmente

## Configuración local
PostgreSQL en Docker: localhost:5432/sgg (user: sgg_admin, pass: sgg_local_pass)
App en: http://localhost:8080
Health check: GET /actuator/health
Swagger UI: GET http://localhost:8080/doc

## Variables de entorno configuradas en IntelliJ
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/sgg
SPRING_DATASOURCE_USERNAME=sgg_admin
SPRING_DATASOURCE_PASSWORD=sgg_local_pass
APP_DEV_MODE=true

## Tests
- Framework: JUnit 5 + Mockito (@ExtendWith(MockitoExtension.class)) — sin Spring context
- Correr: mvn test  (usa Maven local en ~/.m2/wrapper/dists/apache-maven-3.9.9-bin/...)
- 62 tests, 0 fallos
- Cobertura por módulo:
  - UserServiceTest         (6 tests)
  - GymServiceTest          (7 tests)
  - MembershipServiceTest   (13 tests)
  - CoachAssignmentServiceTest (7 tests)
  - RoutineTemplateServiceTest (8 tests)
  - RoutineAssignmentServiceTest (7 tests)
  - TrackingServiceTest     (7 tests)
  - ScheduleServiceTest     (6 tests)
  - SggApiApplicationTests  (1 test — context load con H2)

## Dependencias agregadas al pom.xml
- springdoc-openapi-starter-webmvc-ui 2.6.0 (Swagger)
- h2 (scope: test) — para SggApiApplicationTests context load

## Lo que NO está implementado aún (fuera del MVP)
- Pagos
- Notificaciones push
- Métricas avanzadas
- Autenticación JWT real (pendiente configurar Supabase)

## Pendientes / posibles problemas
- JwtToUserConverter es un stub vacío (no implementado hasta tener Supabase)
- @PreAuthorize en los controllers está activo pero GymAccessChecker
  necesita userId en TenantContext (viene del header X-Dev-User-Id en dev)
- TenantHibernateFilterAspect usa AOP sobre repositorios — verificar que
  spring-boot-starter-aop está en el classpath
- La app aún no levanta con PostgreSQL — investigar errores de arranque
