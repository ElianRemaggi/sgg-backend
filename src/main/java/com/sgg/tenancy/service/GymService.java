package com.sgg.tenancy.service;

import com.sgg.common.exception.BusinessException;
import com.sgg.common.exception.ResourceNotFoundException;
import com.sgg.common.security.TenantContext;
import com.sgg.tenancy.dto.CreateGymRequest;
import com.sgg.tenancy.dto.GymInfoDto;
import com.sgg.tenancy.entity.Gym;
import com.sgg.tenancy.entity.GymMember;
import com.sgg.tenancy.entity.MemberRole;
import com.sgg.tenancy.entity.MembershipStatus;
import com.sgg.tenancy.repository.GymMemberRepository;
import com.sgg.tenancy.repository.GymRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class GymService {

    private final GymRepository gymRepository;
    private final GymMemberRepository gymMemberRepository;

    public GymInfoDto createGym(Long ownerUserId, CreateGymRequest request) {
        if (gymRepository.existsBySlug(request.getSlug())) {
            throw new BusinessException("El slug '" + request.getSlug() + "' ya está en uso");
        }

        Gym gym = new Gym();
        gym.setName(request.getName());
        gym.setSlug(request.getSlug());
        gym.setDescription(request.getDescription());
        gym.setLogoUrl(request.getLogoUrl());
        gym.setOwnerUserId(ownerUserId);
        gym.setRoutineCycle(Gym.RoutineCycle.valueOf(
                request.getRoutineCycle() != null ? request.getRoutineCycle() : "WEEKLY"));

        Gym saved = gymRepository.save(gym);

        // El creador queda automáticamente como ADMIN_COACH con membresía activa
        GymMember ownerMember = new GymMember();
        ownerMember.setUserId(ownerUserId);
        ownerMember.setGymId(saved.getId());
        ownerMember.setRole(MemberRole.ADMIN_COACH);
        ownerMember.setStatus(MembershipStatus.ACTIVE);
        gymMemberRepository.save(ownerMember);

        return toDto(saved, MemberRole.ADMIN_COACH.name());
    }

    @Transactional(readOnly = true)
    public GymInfoDto getGymInfo(Long gymId) {
        Gym gym = gymRepository.findById(gymId)
                .orElseThrow(() -> new ResourceNotFoundException("Gym not found: " + gymId));

        Long userId = TenantContext.getUserId();
        String userRole = null;

        if (userId != null) {
            userRole = gymMemberRepository
                    .findByUserIdAndGymIdAndStatusIn(userId, gymId,
                            List.of(MembershipStatus.ACTIVE))
                    .map(m -> m.getRole().name())
                    .orElse(null);
        }

        return toDto(gym, userRole);
    }

    @Transactional(readOnly = true)
    public List<GymInfoDto> searchGyms(String query) {
        return gymRepository.findByNameContainingIgnoreCase(query).stream()
                .map(g -> toDto(g, null))
                .toList();
    }

    private GymInfoDto toDto(Gym gym, String userRole) {
        return new GymInfoDto(
                gym.getId(),
                gym.getName(),
                gym.getSlug(),
                gym.getDescription(),
                gym.getLogoUrl(),
                gym.getRoutineCycle().name(),
                userRole
        );
    }
}
