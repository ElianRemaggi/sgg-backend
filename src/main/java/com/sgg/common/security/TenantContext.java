package com.sgg.common.security;

import com.sgg.common.exception.TenantViolationException;

/**
 * Almacena el gym_id y user_id del request actual usando ThreadLocal.
 * Se setea en JwtToUserConverter y TenantInterceptor al inicio de cada request,
 * y se limpia en TenantInterceptor.afterCompletion().
 */
public final class TenantContext {

    private static final ThreadLocal<Long> currentGymId = new ThreadLocal<>();
    private static final ThreadLocal<Long> currentUserId = new ThreadLocal<>();

    private TenantContext() {}

    public static void setGymId(Long gymId) {
        currentGymId.set(gymId);
    }

    public static Long getGymId() {
        return currentGymId.get();
    }

    /**
     * Retorna el gym_id o lanza excepción si no está seteado.
     * Usar en entidades (@PrePersist) para garantizar que gym_id nunca sea null.
     */
    public static Long requireGymId() {
        Long id = currentGymId.get();
        if (id == null) {
            throw new TenantViolationException("gym_id not set in TenantContext — possible tenant isolation breach");
        }
        return id;
    }

    public static void setUserId(Long userId) {
        currentUserId.set(userId);
    }

    public static Long getUserId() {
        return currentUserId.get();
    }

    /**
     * Limpia el contexto. SIEMPRE llamar en afterCompletion del interceptor.
     * Si no se limpia, el ThreadLocal puede "contaminar" requests futuros
     * en servidores con thread pools reutilizados (Tomcat).
     */
    public static void clear() {
        currentGymId.remove();
        currentUserId.remove();
    }
}
