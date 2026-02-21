package com.sgg.tenancy.controller;

import com.sgg.common.dto.ApiResponse;
import com.sgg.common.security.CurrentUser;
import com.sgg.tenancy.dto.ApproveMemberRequest;
import com.sgg.tenancy.dto.GymMemberDto;
import com.sgg.tenancy.dto.MembershipDto;
import com.sgg.tenancy.dto.UpdateMemberRoleRequest;
import com.sgg.tenancy.entity.MemberRole;
import com.sgg.tenancy.service.MembershipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Memberships", description = "Solicitudes de membresía y gestión de miembros por el admin")
@RestController
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipService membershipService;

    // --- Endpoints para members (app móvil) ---

    @Operation(summary = "Solicitar membresía", description = "El usuario autenticado solicita unirse al gimnasio. Solo puede existir una solicitud PENDING o ACTIVE por vez.")
    @PostMapping("/api/gyms/{gymId}/join-request")
    public ResponseEntity<ApiResponse<GymMemberDto>> requestJoin(
            @PathVariable Long gymId,
            @CurrentUser Long userId) {
        return ResponseEntity.status(201)
                .body(ApiResponse.success(membershipService.requestJoin(userId, gymId)));
    }

    @Operation(summary = "Mis membresías", description = "Lista todos los gimnasios donde el usuario tiene membresía ACTIVE o PENDING.")
    @GetMapping("/api/users/me/memberships")
    public ResponseEntity<ApiResponse<List<MembershipDto>>> getMyMemberships(
            @CurrentUser Long userId) {
        return ResponseEntity.ok(
                ApiResponse.success(membershipService.getUserMemberships(userId)));
    }

    // --- Endpoints para admins (panel web) ---

    @Operation(summary = "[ADMIN] Listar coaches", description = "Retorna los miembros con rol COACH o ADMIN_COACH que tienen membresía activa.")
    @PreAuthorize("@gymAccess.hasRole(#gymId, 'ADMIN')")
    @GetMapping("/api/gyms/{gymId}/admin/coaches")
    public ResponseEntity<ApiResponse<List<GymMemberDto>>> listCoaches(
            @PathVariable Long gymId) {
        return ResponseEntity.ok(
                ApiResponse.success(membershipService.listCoaches(gymId)));
    }

    @Operation(summary = "[ADMIN] Cambiar rol de miembro", description = "Actualiza el rol de un miembro activo. Roles válidos: MEMBER, COACH, ADMIN, ADMIN_COACH.")
    @PreAuthorize("@gymAccess.hasRole(#gymId, 'ADMIN')")
    @PutMapping("/api/gyms/{gymId}/admin/members/{memberId}/role")
    public ResponseEntity<ApiResponse<GymMemberDto>> updateRole(
            @PathVariable Long gymId,
            @PathVariable Long memberId,
            @RequestBody @Valid UpdateMemberRoleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                membershipService.updateRole(gymId, memberId, MemberRole.valueOf(request.getRole()))));
    }

    @Operation(summary = "[ADMIN] Listar miembros", description = "Retorna todos los miembros del gimnasio con su estado y rol. Requiere rol ADMIN.")
    @PreAuthorize("@gymAccess.hasRole(#gymId, 'ADMIN')")
    @GetMapping("/api/gyms/{gymId}/admin/members")
    public ResponseEntity<ApiResponse<List<GymMemberDto>>> listMembers(
            @PathVariable Long gymId) {
        return ResponseEntity.ok(
                ApiResponse.success(membershipService.listMembers(gymId)));
    }

    @Operation(summary = "[ADMIN] Aprobar miembro", description = "Aprueba una solicitud PENDING. Opcionalmente establece fecha de vencimiento de membresía.")
    @PreAuthorize("@gymAccess.hasRole(#gymId, 'ADMIN')")
    @PutMapping("/api/gyms/{gymId}/admin/members/{memberId}/approve")
    public ResponseEntity<ApiResponse<GymMemberDto>> approve(
            @PathVariable Long gymId,
            @PathVariable Long memberId,
            @RequestBody @Valid ApproveMemberRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                membershipService.approve(gymId, memberId, request.getExpiresAt())));
    }

    @Operation(summary = "[ADMIN] Rechazar miembro", description = "Rechaza una solicitud PENDING.")
    @PreAuthorize("@gymAccess.hasRole(#gymId, 'ADMIN')")
    @PutMapping("/api/gyms/{gymId}/admin/members/{memberId}/reject")
    public ResponseEntity<ApiResponse<GymMemberDto>> reject(
            @PathVariable Long gymId,
            @PathVariable Long memberId) {
        return ResponseEntity.ok(
                ApiResponse.success(membershipService.reject(gymId, memberId)));
    }

    @Operation(summary = "[ADMIN] Bloquear miembro", description = "Bloquea un miembro activo o pendiente, impidiendo su acceso.")
    @PreAuthorize("@gymAccess.hasRole(#gymId, 'ADMIN')")
    @PutMapping("/api/gyms/{gymId}/admin/members/{memberId}/block")
    public ResponseEntity<ApiResponse<GymMemberDto>> block(
            @PathVariable Long gymId,
            @PathVariable Long memberId) {
        return ResponseEntity.ok(
                ApiResponse.success(membershipService.block(gymId, memberId)));
    }

    @Operation(summary = "[ADMIN] Actualizar vencimiento", description = "Modifica la fecha de vencimiento de una membresía ACTIVE.")
    @PreAuthorize("@gymAccess.hasRole(#gymId, 'ADMIN')")
    @PutMapping("/api/gyms/{gymId}/admin/members/{memberId}/expiration")
    public ResponseEntity<ApiResponse<GymMemberDto>> updateExpiration(
            @PathVariable Long gymId,
            @PathVariable Long memberId,
            @RequestBody ApproveMemberRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                membershipService.updateExpiration(gymId, memberId, request.getExpiresAt())));
    }
}
