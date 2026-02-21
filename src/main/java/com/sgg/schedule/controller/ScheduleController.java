package com.sgg.schedule.controller;

import com.sgg.common.dto.ApiResponse;
import com.sgg.schedule.dto.CreateScheduleRequest;
import com.sgg.schedule.dto.ScheduleActivityDto;
import com.sgg.schedule.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Schedule", description = "Horarios de actividades del gimnasio")
@RestController
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @Operation(summary = "Ver horarios del gimnasio", description = "Lista todas las actividades activas del gimnasio. Acceso público.")
    @GetMapping("/api/gyms/{gymId}/schedule")
    public ResponseEntity<ApiResponse<List<ScheduleActivityDto>>> list(
            @PathVariable Long gymId) {
        return ResponseEntity.ok(ApiResponse.success(scheduleService.listActive(gymId)));
    }

    @Operation(summary = "[ADMIN] Crear actividad", description = "Agrega una nueva actividad al horario del gimnasio. dayOfWeek: 1=Lunes … 7=Domingo.")
    @PreAuthorize("@gymAccess.hasRole(#gymId, 'ADMIN')")
    @PostMapping("/api/gyms/{gymId}/admin/schedule")
    public ResponseEntity<ApiResponse<ScheduleActivityDto>> create(
            @PathVariable Long gymId,
            @RequestBody @Valid CreateScheduleRequest request) {
        return ResponseEntity.status(201)
                .body(ApiResponse.success(scheduleService.create(gymId, request)));
    }

    @Operation(summary = "[ADMIN] Eliminar actividad", description = "Desactiva una actividad del horario (soft delete: isActive = false).")
    @PreAuthorize("@gymAccess.hasRole(#gymId, 'ADMIN')")
    @DeleteMapping("/api/gyms/{gymId}/admin/schedule/{activityId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long gymId,
            @PathVariable Long activityId) {
        scheduleService.delete(gymId, activityId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
