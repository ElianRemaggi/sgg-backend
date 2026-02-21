# SGG API — Backend

Backend del SaaS multi-tenant para gimnasios.  
**Stack:** Java 21 + Spring Boot 3.3 + PostgreSQL 16 + Flyway

---

## Setup rápido (desarrollo local)

### 1. Requisitos
- Java 21 (Temurin)
- Docker Desktop
- IntelliJ IDEA Community

### 2. Levantar PostgreSQL

```bash
docker compose up -d
```

Verifica que está corriendo:
```bash
docker compose ps
```

### 3. Configurar variables de entorno en IntelliJ

En IntelliJ: `Run > Edit Configurations > SggApiApplication > Environment Variables`

```
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/sgg
SPRING_DATASOURCE_USERNAME=sgg_admin
SPRING_DATASOURCE_PASSWORD=sgg_local_pass
APP_DEV_MODE=true
```

### 4. Correr la aplicación

Desde IntelliJ: Click derecho en `SggApiApplication.java` → `Run`

La app arranca en `http://localhost:8080`

Flyway ejecuta automáticamente las migraciones (V1 a V12) y crea todas las tablas.

---

## Verificar que funciona

```bash
# Health check
curl http://localhost:8080/actuator/health

# Crear un gimnasio (dev-mode: sin token requerido)
curl -X POST http://localhost:8080/api/gyms \
  -H "Content-Type: application/json" \
  -d '{"name":"Mi Gym","slug":"mi-gym","routineCycle":"WEEKLY"}'
```

---

## Estructura del proyecto

```
src/main/java/com/sgg/
├── common/          # Seguridad, excepciones, multi-tenancy
├── identity/        # Usuarios y autenticación
├── tenancy/         # Gimnasios y membresías
├── coaching/        # Asignaciones coach-miembro
├── training/        # Plantillas y asignación de rutinas
├── tracking/        # Registro de ejercicios completados
└── schedule/        # Horarios y actividades
```

---

## Módulo de multi-tenancy

El sistema usa `gym_id` para aislar datos entre gimnasios.

- **TenantContext**: ThreadLocal que guarda el `gym_id` del request actual
- **TenantInterceptor**: Extrae `gym_id` de la URL y valida membresía activa
- **TenantHibernateFilterAspect**: Activa el Hibernate Filter en cada query JPA
- **GymAccessChecker**: Bean para `@PreAuthorize` con roles por gimnasio

---

## Activar autenticación (cuando tengas Supabase)

1. Crear proyecto en [supabase.com](https://supabase.com)
2. Obtener el JWKS URI: `Project Settings > API > JWT Settings`
3. Actualizar variables de entorno:
   ```
   SUPABASE_JWKS_URI=https://xxxx.supabase.co/auth/v1/jwks
   APP_DEV_MODE=false
   ```

---

## Migraciones Flyway

Las migraciones se ejecutan automáticamente al iniciar la app.  
Archivos en: `src/main/resources/db/migration/`

| Migración | Tabla |
|---|---|
| V1 | users |
| V2 | auth_identities |
| V3 | gyms |
| V4 | gym_members |
| V5 | coach_assignments |
| V6 | routine_templates |
| V7 | template_blocks |
| V8 | template_exercises |
| V9 | routine_assignments |
| V10 | exercise_completions |
| V11 | schedule_activities |
| V12 | indexes |
