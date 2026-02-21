package com.sgg.common.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor SOLO para desarrollo.
 *
 * Permite simular un usuario autenticado enviando el header:
 *   X-Dev-User-Id: 1
 *
 * NUNCA debe estar activo en producción.
 * Cuando integres Supabase, este interceptor se reemplaza por JwtToUserConverter.
 */
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
