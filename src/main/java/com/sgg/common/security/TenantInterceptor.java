package com.sgg.common.security;

import com.sgg.common.exception.UnauthorizedException;
import com.sgg.tenancy.entity.MembershipStatus;
import com.sgg.tenancy.repository.GymMemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Interceptor que extrae el gymId de la URL y valida que el usuario
 * tenga membresía activa en ese gimnasio.
 *
 * Patrón de URL: /api/gyms/{gymId}/...
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantInterceptor implements HandlerInterceptor {

    private final GymMemberRepository gymMemberRepository;

    private static final Pattern GYM_PATH_PATTERN =
            Pattern.compile("/api/gyms/(\\d+)/.*");

    // Rutas que no requieren membresía activa (join-request es el caso clave)
    private static final Pattern EXEMPT_PATTERN =
            Pattern.compile("/api/gyms/(\\d+)/join-request");

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        String uri = request.getRequestURI();
        Matcher matcher = GYM_PATH_PATTERN.matcher(uri);

        if (!matcher.matches()) {
            return true; // No es una ruta de gym, continuar normalmente
        }

        Long gymId = Long.valueOf(matcher.group(1));
        Long userId = TenantContext.getUserId();

        // Si es join-request, setear gymId pero no validar membresía
        if (EXEMPT_PATTERN.matcher(uri).matches()) {
            TenantContext.setGymId(gymId);
            return true;
        }

        if (userId == null) {
            throw new UnauthorizedException("Authentication required");
        }

        // Verificar membresía activa
        gymMemberRepository
                .findByUserIdAndGymIdAndStatusIn(userId, gymId, List.of(MembershipStatus.ACTIVE))
                .orElseThrow(() -> new UnauthorizedException(
                        "No active membership in gym " + gymId));

        TenantContext.setGymId(gymId);
        log.debug("TenantContext set: userId={}, gymId={}", userId, gymId);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        // CRÍTICO: siempre limpiar para evitar contaminación entre requests
        TenantContext.clear();
    }
}
