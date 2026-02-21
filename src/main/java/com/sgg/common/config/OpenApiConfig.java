package com.sgg.common.config;

import com.sgg.common.security.CurrentUser;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de Swagger / OpenAPI.
 * UI disponible en: GET /doc
 * JSON disponible en: GET /doc/api-docs
 */
@Configuration
public class OpenApiConfig {

    static {
        // Oculta los parámetros @CurrentUser de la UI — se resuelven internamente
        // desde el header X-Dev-User-Id (dev-mode) o del JWT (producción)
        SpringDocUtils.getConfig().addAnnotationsToIgnore(CurrentUser.class);
    }

    @Bean
    public OpenAPI sggOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SGG API")
                        .description("""
                                Backend multi-tenant para gimnasios.

                                ## Autenticación (dev-mode)
                                Enviar el header **X-Dev-User-Id: {userId}** en cada request para simular un usuario autenticado.
                                El gymId se toma del path `/api/gyms/{gymId}/...`.

                                ## Roles
                                - **MEMBER** — acceso a su rutina y tracking
                                - **COACH** — gestión de templates y asignaciones
                                - **ADMIN** — gestión de miembros, coaches y horarios
                                - **ADMIN_COACH** — permisos de ADMIN + COACH simultáneamente
                                """)
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList("Dev-Auth"))
                .components(new Components()
                        .addSecuritySchemes("Dev-Auth", new SecurityScheme()
                                .name("X-Dev-User-Id")
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .description("ID del usuario autenticado (solo dev-mode). Ej: 1")));
    }
}
