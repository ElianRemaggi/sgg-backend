package com.sgg.tracking.controller;

import com.sgg.common.dto.ApiResponse;
import com.sgg.common.security.CurrentUser;
import com.sgg.tracking.dto.CompleteExerciseRequest;
import com.sgg.tracking.dto.ProgressDto;
import com.sgg.tracking.service.TrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Tracking", description = "Registro de ejercicios completados y progreso de rutina")
@RestController
@RequiredArgsConstructor
public class TrackingController {

    private final TrackingService trackingService;

    @Operation(summary = "[MEMBER] Marcar ejercicio completado", description = "Registra un ejercicio como completado. Si ya existe el registro, lo reactiva.")
    @PostMapping("/api/gyms/{gymId}/member/tracking/complete")
    public ResponseEntity<ApiResponse<Void>> complete(
            @PathVariable Long gymId,
            @CurrentUser Long userId,
            @RequestBody @Valid CompleteExerciseRequest request) {
        trackingService.complete(gymId, userId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "[MEMBER] Desmarcar ejercicio (undo)", description = "Revierte el estado de un ejercicio a no completado.")
    @PostMapping("/api/gyms/{gymId}/member/tracking/undo")
    public ResponseEntity<ApiResponse<Void>> undo(
            @PathVariable Long gymId,
            @CurrentUser Long userId,
            @RequestBody @Valid CompleteExerciseRequest request) {
        trackingService.undo(userId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "[MEMBER] Progreso de rutina", description = "Retorna el porcentaje de progreso y el detalle por bloque de la asignación especificada.")
    @GetMapping("/api/gyms/{gymId}/member/tracking/progress/{assignmentId}")
    public ResponseEntity<ApiResponse<ProgressDto>> getProgress(
            @PathVariable Long gymId,
            @PathVariable Long assignmentId,
            @CurrentUser Long userId) {
        return ResponseEntity.ok(
                ApiResponse.success(trackingService.getProgress(gymId, userId, assignmentId)));
    }

    @Operation(summary = "[COACH] Progreso de un miembro", description = "Retorna el progreso de la rutina activa de un miembro específico.")
    @PreAuthorize("@gymAccess.hasRole(#gymId, 'COACH')")
    @GetMapping("/api/gyms/{gymId}/coach/members/{memberId}/progress")
    public ResponseEntity<ApiResponse<ProgressDto>> getMemberProgress(
            @PathVariable Long gymId,
            @PathVariable Long memberId,
            @CurrentUser Long userId) {
        return ResponseEntity.ok(
                ApiResponse.success(trackingService.getProgressByMember(gymId, userId, memberId)));
    }
}
