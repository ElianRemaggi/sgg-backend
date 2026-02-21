package com.sgg.common.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para inyectar el userId del usuario autenticado en parámetros de controllers.
 *
 * Uso: public ResponseEntity<?> getProfile(@CurrentUser Long userId)
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUser {
}
