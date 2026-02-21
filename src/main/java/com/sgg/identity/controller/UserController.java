package com.sgg.identity.controller;

import com.sgg.common.dto.ApiResponse;
import com.sgg.common.security.CurrentUser;
import com.sgg.identity.dto.AuthSyncRequest;
import com.sgg.identity.dto.UpdateProfileRequest;
import com.sgg.identity.dto.UserProfileDto;
import com.sgg.identity.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth & Users", description = "Sincronización de usuarios con Supabase y gestión de perfil")
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Sincronizar usuario", description = "Crea o actualiza el usuario local a partir del perfil de Supabase. Llamar en cada login.")
    @PostMapping("/api/auth/sync")
    public ResponseEntity<ApiResponse<UserProfileDto>> syncUser(
            @RequestBody @Valid AuthSyncRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.syncUser(request)));
    }

    @Operation(summary = "Mi perfil", description = "Retorna el perfil del usuario autenticado (requiere X-Dev-User-Id).")
    @GetMapping("/api/users/me")
    public ResponseEntity<ApiResponse<UserProfileDto>> getMyProfile(
            @CurrentUser Long userId) {
        return ResponseEntity.ok(ApiResponse.success(userService.getProfile(userId)));
    }

    @Operation(summary = "Actualizar perfil", description = "Actualiza nombre y avatar del usuario autenticado.")
    @PutMapping("/api/users/me")
    public ResponseEntity<ApiResponse<UserProfileDto>> updateProfile(
            @CurrentUser Long userId,
            @RequestBody @Valid UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateProfile(userId, request)));
    }
}
