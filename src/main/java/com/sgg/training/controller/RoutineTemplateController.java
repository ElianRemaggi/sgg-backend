package com.sgg.training.controller;

import com.sgg.common.dto.ApiResponse;
import com.sgg.common.security.CurrentUser;
import com.sgg.training.dto.CreateTemplateRequest;
import com.sgg.training.dto.RoutineTemplateDto;
import com.sgg.training.service.RoutineTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Routine Templates", description = "Plantillas de rutina creadas por coaches (bloques + ejercicios)")
@RestController
@RequiredArgsConstructor
public class RoutineTemplateController {

    private final RoutineTemplateService templateService;

    @Operation(summary = "[COACH] Crear template", description = "Crea una plantilla de rutina con bloques y ejercicios. Requiere rol COACH.")
    @PreAuthorize("@gymAccess.hasRole(#gymId, 'COACH')")
    @PostMapping("/api/gyms/{gymId}/coach/templates")
    public ResponseEntity<ApiResponse<RoutineTemplateDto>> create(
            @PathVariable Long gymId,
            @CurrentUser Long userId,
            @RequestBody @Valid CreateTemplateRequest request) {
        return ResponseEntity.status(201)
                .body(ApiResponse.success(templateService.create(gymId, userId, request)));
    }

    @Operation(summary = "[COACH] Listar templates", description = "Retorna todas las plantillas del gimnasio.")
    @PreAuthorize("@gymAccess.hasRole(#gymId, 'COACH')")
    @GetMapping("/api/gyms/{gymId}/coach/templates")
    public ResponseEntity<ApiResponse<List<RoutineTemplateDto>>> list(
            @PathVariable Long gymId) {
        return ResponseEntity.ok(ApiResponse.success(templateService.listByGym(gymId)));
    }

    @Operation(summary = "[COACH] Obtener template", description = "Retorna el detalle completo de una plantilla.")
    @PreAuthorize("@gymAccess.hasRole(#gymId, 'COACH')")
    @GetMapping("/api/gyms/{gymId}/coach/templates/{templateId}")
    public ResponseEntity<ApiResponse<RoutineTemplateDto>> getById(
            @PathVariable Long gymId,
            @PathVariable Long templateId) {
        return ResponseEntity.ok(ApiResponse.success(templateService.getById(templateId)));
    }

    @Operation(summary = "[COACH] Eliminar template", description = "Elimina una plantilla de rutina.")
    @PreAuthorize("@gymAccess.hasRole(#gymId, 'COACH')")
    @DeleteMapping("/api/gyms/{gymId}/coach/templates/{templateId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long gymId,
            @PathVariable Long templateId) {
        templateService.delete(templateId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
