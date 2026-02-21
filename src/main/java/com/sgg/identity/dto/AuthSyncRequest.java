package com.sgg.identity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Request enviado por el frontend cuando el usuario hace login por primera vez.
 * Crea o actualiza el User local sincronizando con Supabase.
 */
@Getter
@Setter
public class AuthSyncRequest {

    @NotBlank
    private String supabaseUid;

    @NotBlank
    private String fullName;

    @Email
    @NotBlank
    private String email;

    private String avatarUrl;
}
