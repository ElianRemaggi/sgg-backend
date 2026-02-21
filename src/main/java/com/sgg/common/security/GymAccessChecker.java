package com.sgg.common.security;

import com.sgg.tenancy.entity.MemberRole;
import com.sgg.tenancy.entity.MembershipStatus;
import com.sgg.tenancy.repository.GymMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Bean usado en expresiones @PreAuthorize para verificar roles por gimnasio.
 *
 * Uso: @PreAuthorize("@gymAccess.hasRole(#gymId, 'ADMIN')")
 */
@Component("gymAccess")
@RequiredArgsConstructor
public class GymAccessChecker {

    private final GymMemberRepository gymMemberRepository;

    /**
     * Verifica que el usuario actual tiene al menos uno de los roles indicados en el gym.
     * ADMIN_COACH tiene permisos de tanto ADMIN como COACH.
     */
    public boolean hasRole(Long gymId, String... roles) {
        Long userId = TenantContext.getUserId();
        if (userId == null) return false;

        return gymMemberRepository
                .findByUserIdAndGymIdAndStatusIn(userId, gymId, List.of(MembershipStatus.ACTIVE))
                .map(member -> {
                    MemberRole memberRole = member.getRole();
                    for (String role : roles) {
                        if (memberRole.name().equals(role)) return true;
                        // ADMIN_COACH tiene permisos de ambos roles
                        if (memberRole == MemberRole.ADMIN_COACH &&
                                (role.equals("ADMIN") || role.equals("COACH"))) return true;
                    }
                    return false;
                })
                .orElse(false);
    }

    /**
     * Verifica que el usuario tiene membresía activa en el gym.
     */
    public boolean isMemberActive(Long gymId) {
        Long userId = TenantContext.getUserId();
        if (userId == null) return false;

        return gymMemberRepository
                .existsByUserIdAndGymIdAndStatus(userId, gymId, MembershipStatus.ACTIVE);
    }

    /**
     * Verifica que la membresía no esté vencida.
     * Membresías sin fecha de vencimiento se consideran permanentes.
     */
    public boolean isMembershipNotExpired(Long gymId) {
        Long userId = TenantContext.getUserId();
        if (userId == null) return false;

        return gymMemberRepository
                .findByUserIdAndGymIdAndStatusIn(userId, gymId, List.of(MembershipStatus.ACTIVE))
                .map(m -> m.getMembershipExpiresAt() == null ||
                          m.getMembershipExpiresAt().isAfter(LocalDateTime.now()))
                .orElse(false);
    }
}
