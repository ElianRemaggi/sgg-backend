package com.sgg.training.controller;

import com.sgg.common.dto.ApiResponse;
import com.sgg.common.security.CurrentUser;
import com.sgg.training.dto.AssignRoutineRequest;
import com.sgg.training.dto.MemberRoutineDto;
import com.sgg.training.entity.RoutineAssignment;
import com.sgg.training.service.RoutineAssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Routine Assignments", description = "Asignación de plantillas de rutina a miembros")
@RestController
@RequiredArgsConstructor
public class RoutineAssignmentController {

    private final RoutineAssignmentService assignmentService;

    @Operation(summary = "[COACH] Asignar rutina", description = "Asigna una plantilla de rutina a un miembro con fechas de vigencia. La fecha de fin debe ser posterior a la de inicio.")
    @PreAuthorize("@gymAccess.hasRole(#gymId, 'COACH')")
    @PostMapping("/api/gyms/{gymId}/coach/routine-assignments")
    public ResponseEntity<ApiResponse<RoutineAssignment>> assign(
            @PathVariable Long gymId,
            @CurrentUser Long userId,
            @RequestBody @Valid AssignRoutineRequest request) {
        return ResponseEntity.status(201)
                .body(ApiResponse.success(assignmentService.assign(gymId, userId, request)));
    }

    @Operation(summary = "[MEMBER] Mi rutina activa", description = "Retorna la plantilla de rutina vigente hoy para el miembro autenticado.")
    @GetMapping("/api/gyms/{gymId}/member/routine")
    public ResponseEntity<ApiResponse<MemberRoutineDto>> getMyRoutine(
            @PathVariable Long gymId,
            @CurrentUser Long userId) {
        return ResponseEntity.ok(
                ApiResponse.success(assignmentService.getActiveRoutineForMember(gymId, userId)));
    }

    @Operation(summary = "[COACH] Rutina activa de un miembro", description = "Retorna la rutina vigente de un miembro específico.")
    @PreAuthorize("@gymAccess.hasRole(#gymId, 'COACH')")
    @GetMapping("/api/gyms/{gymId}/coach/members/{memberId}/routine")
    public ResponseEntity<ApiResponse<MemberRoutineDto>> getMemberRoutine(
            @PathVariable Long gymId,
            @PathVariable Long memberId) {
        return ResponseEntity.ok(
                ApiResponse.success(assignmentService.getActiveRoutineForMember(gymId, memberId)));
    }
}
