package com.sgg.tenancy.service;

import com.sgg.common.exception.BusinessException;
import com.sgg.common.exception.ResourceNotFoundException;
import com.sgg.common.exception.TenantViolationException;
import com.sgg.identity.entity.User;
import com.sgg.identity.repository.UserRepository;
import com.sgg.tenancy.dto.GymMemberDto;
import com.sgg.tenancy.dto.MembershipDto;
import com.sgg.tenancy.entity.Gym;
import com.sgg.tenancy.entity.GymMember;
import com.sgg.tenancy.entity.MemberRole;
import com.sgg.tenancy.entity.MembershipStatus;
import com.sgg.tenancy.repository.GymMemberRepository;
import com.sgg.tenancy.repository.GymRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MembershipService {

    private final GymMemberRepository gymMemberRepository;
    private final GymRepository gymRepository;
    private final UserRepository userRepository;

    public GymMemberDto requestJoin(Long userId, Long gymId) {
        gymRepository.findById(gymId)
                .orElseThrow(() -> new ResourceNotFoundException("Gym not found: " + gymId));

        // Regla de negocio: solo puede existir una solicitud activa/pendiente por gym
        Optional<GymMember> existing = gymMemberRepository
                .findByUserIdAndGymIdAndStatusIn(userId, gymId,
                        List.of(MembershipStatus.PENDING, MembershipStatus.ACTIVE));

        if (existing.isPresent()) {
            MembershipStatus status = existing.get().getStatus();
            throw new BusinessException(
                    status == MembershipStatus.PENDING
                            ? "Ya tenés una solicitud pendiente en este gimnasio"
                            : "Ya tenés membresía activa en este gimnasio");
        }

        GymMember member = new GymMember();
        member.setUserId(userId);
        member.setGymId(gymId);
        member.setRole(MemberRole.MEMBER);
        member.setStatus(MembershipStatus.PENDING);

        GymMember saved = gymMemberRepository.save(member);
        log.info("Solicitud de membresía creada: userId={}, gymId={}", userId, gymId);

        return toDto(saved);
    }

    public GymMemberDto approve(Long gymId, Long memberId, LocalDateTime expiresAt) {
        GymMember member = gymMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found: " + memberId));

        validateTenant(member, gymId);

        if (member.getStatus() != MembershipStatus.PENDING) {
            throw new BusinessException("Solo se pueden aprobar solicitudes en estado PENDING");
        }

        member.setStatus(MembershipStatus.ACTIVE);
        member.setMembershipExpiresAt(expiresAt);

        log.info("Membresía aprobada: memberId={}, gymId={}, expiresAt={}", memberId, gymId, expiresAt);
        return toDto(gymMemberRepository.save(member));
    }

    public GymMemberDto reject(Long gymId, Long memberId) {
        GymMember member = gymMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found: " + memberId));

        validateTenant(member, gymId);

        if (member.getStatus() != MembershipStatus.PENDING) {
            throw new BusinessException("Solo se pueden rechazar solicitudes en estado PENDING");
        }

        member.setStatus(MembershipStatus.REJECTED);
        return toDto(gymMemberRepository.save(member));
    }

    public GymMemberDto block(Long gymId, Long memberId) {
        GymMember member = gymMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found: " + memberId));

        validateTenant(member, gymId);
        member.setStatus(MembershipStatus.BLOCKED);
        return toDto(gymMemberRepository.save(member));
    }

    public GymMemberDto updateExpiration(Long gymId, Long memberId, LocalDateTime expiresAt) {
        GymMember member = gymMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found: " + memberId));

        validateTenant(member, gymId);

        if (member.getStatus() != MembershipStatus.ACTIVE) {
            throw new BusinessException("Solo se puede modificar la expiración de membresías activas");
        }

        member.setMembershipExpiresAt(expiresAt);
        return toDto(gymMemberRepository.save(member));
    }

    @Transactional(readOnly = true)
    public List<GymMemberDto> listMembers(Long gymId) {
        return gymMemberRepository.findByGymId(gymId).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Retorna todas las membresías del usuario (sin filtro por gym).
     * Usado por la app móvil para mostrar los gimnasios del usuario.
     */
    @Transactional(readOnly = true)
    public List<MembershipDto> getUserMemberships(Long userId) {
        return gymMemberRepository
                .findByUserIdAndStatusIn(userId,
                        List.of(MembershipStatus.ACTIVE, MembershipStatus.PENDING))
                .stream()
                .map(this::toMembershipDto)
                .toList();
    }

    private void validateTenant(GymMember member, Long gymId) {
        if (!member.getGymId().equals(gymId)) {
            throw new TenantViolationException(
                    "Member " + member.getId() + " no pertenece al gym " + gymId);
        }
    }

    private GymMemberDto toDto(GymMember member) {
        User user = userRepository.findById(member.getUserId()).orElse(null);
        return new GymMemberDto(
                member.getId(),
                member.getUserId(),
                member.getGymId(),
                user != null ? user.getFullName() : "Unknown",
                user != null ? user.getEmail() : "",
                member.getRole().name(),
                member.getStatus().name(),
                member.getMembershipExpiresAt(),
                member.getCreatedAt()
        );
    }

    private MembershipDto toMembershipDto(GymMember member) {
        Gym gym = gymRepository.findById(member.getGymId()).orElse(null);
        boolean expired = member.getMembershipExpiresAt() != null &&
                          member.getMembershipExpiresAt().isBefore(LocalDateTime.now());
        return new MembershipDto(
                member.getId(),
                member.getGymId(),
                gym != null ? gym.getName() : "Unknown",
                gym != null ? gym.getSlug() : "",
                member.getRole().name(),
                member.getStatus().name(),
                member.getMembershipExpiresAt(),
                expired
        );
    }
}
