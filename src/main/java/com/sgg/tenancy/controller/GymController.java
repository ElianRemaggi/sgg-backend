package com.sgg.tenancy.controller;

import com.sgg.common.dto.ApiResponse;
import com.sgg.common.security.CurrentUser;
import com.sgg.tenancy.dto.CreateGymRequest;
import com.sgg.tenancy.dto.GymInfoDto;
import com.sgg.tenancy.dto.UpdateGymRequest;
import com.sgg.tenancy.service.GymService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Gyms", description = "Creación y consulta de gimnasios")
@RestController
@RequiredArgsConstructor
public class GymController {

    private final GymService gymService;

    @Operation(summary = "Crear gimnasio", description = "Crea un nuevo gimnasio. El creador queda automáticamente con rol ADMIN_COACH.")
    @PostMapping("/api/gyms")
    public ResponseEntity<ApiResponse<GymInfoDto>> createGym(
            @CurrentUser Long userId,
            @RequestBody @Valid CreateGymRequest request) {
        return ResponseEntity.status(201)
                .body(ApiResponse.success(gymService.createGym(userId, request)));
    }

    @Operation(summary = "Info del gimnasio", description = "Retorna los datos del gimnasio. Si el usuario es miembro activo, incluye su rol.")
    @GetMapping("/api/gyms/{gymId}/info")
    public ResponseEntity<ApiResponse<GymInfoDto>> getGymInfo(
            @PathVariable Long gymId) {
        return ResponseEntity.ok(ApiResponse.success(gymService.getGymInfo(gymId)));
    }

    @Operation(summary = "[ADMIN] Actualizar configuración del gimnasio", description = "Actualiza nombre, descripción, logo y/o ciclo de rutina. Solo se modifican los campos enviados (null = sin cambio). El slug no es editable.")
    @PreAuthorize("@gymAccess.hasRole(#gymId, 'ADMIN')")
    @PutMapping("/api/gyms/{gymId}/admin/settings")
    public ResponseEntity<ApiResponse<GymInfoDto>> updateSettings(
            @PathVariable Long gymId,
            @RequestBody @Valid UpdateGymRequest request) {
        return ResponseEntity.ok(ApiResponse.success(gymService.updateSettings(gymId, request)));
    }

    @Operation(summary = "Buscar gimnasios", description = "Búsqueda pública por nombre. No requiere autenticación.")
    @GetMapping("/api/gyms/search")
    public ResponseEntity<ApiResponse<List<GymInfoDto>>> searchGyms(
            @RequestParam String q) {
        return ResponseEntity.ok(ApiResponse.success(gymService.searchGyms(q)));
    }
}
