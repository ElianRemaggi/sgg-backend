package com.sgg.common.security;

import com.sgg.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Converter JWT → usuario local.
 *
 * En dev-mode este bean existe pero no se usa (no hay JWT que convertir).
 *
 * Cuando integres Supabase:
 * 1. Agregar dependencia oauth2-resource-server en pom.xml
 * 2. Implementar Converter<Jwt, AbstractAuthenticationToken>
 * 3. Conectar en SecurityConfig
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtToUserConverter {

    private final UserRepository userRepository;

    // En dev-mode el userId se setea manualmente desde el header X-Dev-User-Id
    // Ver DevUserInterceptor
}
