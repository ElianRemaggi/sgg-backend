package com.sgg.coaching.controller;

import com.sgg.coaching.dto.AssignCoachRequest;
import com.sgg.coaching.dto.CoachAssignmentDto;
import com.sgg.coaching.service.CoachAssignmentService;
import com.sgg.common.dto.ApiResponse;
import com.sgg.common.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Coach Assignments", description = "Asignación y gestión de coaches a miembros del gimnasio")
@RestController
@RequiredArgsConstructor
public class CoachAssignmentController {

    private final CoachAssignmentService coachAssignmentService;

    @Operation(summary = "[ADMIN] Asignar coach", description = "Asigna un coach a un miembro. No puede existir asignación activa duplicada.")
    @PreAuthorize("@gymAccess.hasRole(#gymId, 'ADMIN')")
    @PostMapping("/api/gyms/{gymId}/admin/coach-assignments")
    public ResponseEntity<ApiResponse<CoachAssignmentDto>> assign(
            @PathVariable Long gymId,
            @RequestBody @Valid AssignCoachRequest request) {
        return ResponseEntity.status(201)
                .body(ApiResponse.success(coachAssignmentService.assign(gymId, request)));
    }

    @Operation(summary = "[ADMIN] Desasignar coach", description = "Finaliza una asignación activa. Registra la fecha de desasignación.")
    @PreAuthorize("@gymAccess.hasRole(#gymId, 'ADMIN')")
    @DeleteMapping("/api/gyms/{gymId}/admin/coach-assignments/{assignmentId}")
    public ResponseEntity<ApiResponse<Void>> unassign(
            @PathVariable Long gymId,
            @PathVariable Long assignmentId) {
        coachAssignmentService.unassign(gymId, assignmentId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "[ADMIN] Listar asignaciones", description = "Lista todas las asignaciones activas del gimnasio.")
    @PreAuthorize("@gymAccess.hasRole(#gymId, 'ADMIN')")
    @GetMapping("/api/gyms/{gymId}/admin/coach-assignments")
    public ResponseEntity<ApiResponse<List<CoachAssignmentDto>>> listAll(
            @PathVariable Long gymId) {
        return ResponseEntity.ok(
                ApiResponse.success(coachAssignmentService.listByGym(gymId)));
    }

    @Operation(summary = "[COACH] Mis asignaciones", description = "Lista los miembros asignados al coach autenticado en este gimnasio.")
    @PreAuthorize("@gymAccess.hasRole(#gymId, 'COACH')")
    @GetMapping("/api/gyms/{gymId}/coach/my-assignments")
    public ResponseEntity<ApiResponse<List<CoachAssignmentDto>>> myAssignments(
            @PathVariable Long gymId,
            @CurrentUser Long userId) {
        return ResponseEntity.ok(
                ApiResponse.success(coachAssignmentService.listByCoach(gymId, userId)));
    }
}
