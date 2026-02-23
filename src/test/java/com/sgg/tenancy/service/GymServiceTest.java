package com.sgg.tenancy.service;

import com.sgg.common.exception.BusinessException;
import com.sgg.common.exception.ResourceNotFoundException;
import com.sgg.common.security.TenantContext;
import com.sgg.tenancy.dto.CreateGymRequest;
import com.sgg.tenancy.dto.GymInfoDto;
import com.sgg.tenancy.dto.UpdateGymRequest;
import com.sgg.tenancy.entity.Gym;
import com.sgg.tenancy.entity.GymMember;
import com.sgg.tenancy.entity.MemberRole;
import com.sgg.tenancy.entity.MembershipStatus;
import com.sgg.tenancy.repository.GymMemberRepository;
import com.sgg.tenancy.repository.GymRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GymServiceTest {

    @Mock
    private GymRepository gymRepository;

    @Mock
    private GymMemberRepository gymMemberRepository;

    @InjectMocks
    private GymService gymService;

    private Gym gym;

    @BeforeEach
    void setUp() {
        gym = new Gym();
        gym.setId(10L);
        gym.setName("Fitness Center");
        gym.setSlug("fitness-center");
        gym.setDescription("Best gym");
        gym.setRoutineCycle(Gym.RoutineCycle.WEEKLY);
        gym.setOwnerUserId(1L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    // --- createGym ---

    @Test
    void createGym_success_whenSlugIsAvailable() {
        CreateGymRequest request = new CreateGymRequest();
        request.setName("Fitness Center");
        request.setSlug("fitness-center");
        request.setRoutineCycle("WEEKLY");

        when(gymRepository.existsBySlug("fitness-center")).thenReturn(false);
        when(gymRepository.save(any(Gym.class))).thenReturn(gym);
        when(gymMemberRepository.save(any(GymMember.class))).thenReturn(new GymMember());

        GymInfoDto result = gymService.createGym(1L, request);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Fitness Center");
        assertThat(result.getUserRole()).isEqualTo(MemberRole.ADMIN_COACH.name());
        verify(gymRepository).save(any(Gym.class));
        verify(gymMemberRepository).save(any(GymMember.class));
    }

    @Test
    void createGym_throwsBusinessException_whenSlugAlreadyExists() {
        CreateGymRequest request = new CreateGymRequest();
        request.setName("Another Gym");
        request.setSlug("fitness-center");

        when(gymRepository.existsBySlug("fitness-center")).thenReturn(true);

        assertThatThrownBy(() -> gymService.createGym(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("fitness-center");
    }

    // --- getGymInfo ---

    @Test
    void getGymInfo_returnsDto_withUserRole_whenUserIsMember() {
        TenantContext.setUserId(1L);

        GymMember member = new GymMember();
        member.setUserId(1L);
        member.setGymId(10L);
        member.setRole(MemberRole.MEMBER);
        member.setStatus(MembershipStatus.ACTIVE);

        when(gymRepository.findById(10L)).thenReturn(Optional.of(gym));
        when(gymMemberRepository.findByUserIdAndGymIdAndStatusIn(eq(1L), eq(10L), any()))
                .thenReturn(Optional.of(member));
        when(gymMemberRepository.countByGymIdAndStatus(10L, MembershipStatus.ACTIVE)).thenReturn(5L);
        when(gymMemberRepository.countByGymIdAndRoleInAndStatus(eq(10L), any(), eq(MembershipStatus.ACTIVE))).thenReturn(2L);

        GymInfoDto result = gymService.getGymInfo(10L);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getUserRole()).isEqualTo("MEMBER");
        assertThat(result.getMemberCount()).isEqualTo(5);
        assertThat(result.getCoachCount()).isEqualTo(2);
    }

    @Test
    void getGymInfo_returnsDto_withNullRole_whenNoUserInContext() {
        TenantContext.clear(); // no userId set

        when(gymRepository.findById(10L)).thenReturn(Optional.of(gym));
        when(gymMemberRepository.countByGymIdAndStatus(10L, MembershipStatus.ACTIVE)).thenReturn(3L);
        when(gymMemberRepository.countByGymIdAndRoleInAndStatus(eq(10L), any(), eq(MembershipStatus.ACTIVE))).thenReturn(1L);

        GymInfoDto result = gymService.getGymInfo(10L);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getUserRole()).isNull();
        assertThat(result.getMemberCount()).isEqualTo(3);
    }

    @Test
    void getGymInfo_throwsResourceNotFoundException_whenGymNotFound() {
        when(gymRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gymService.getGymInfo(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- searchGyms ---

    @Test
    void searchGyms_returnsMatchingList() {
        when(gymRepository.findByNameContainingIgnoreCase("fit"))
                .thenReturn(List.of(gym));

        List<GymInfoDto> results = gymService.searchGyms("fit");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getSlug()).isEqualTo("fitness-center");
    }

    @Test
    void searchGyms_returnsEmptyList_whenNoMatch() {
        when(gymRepository.findByNameContainingIgnoreCase("xyz")).thenReturn(List.of());

        List<GymInfoDto> results = gymService.searchGyms("xyz");

        assertThat(results).isEmpty();
    }

    // --- updateSettings ---

    @Test
    void updateSettings_updatesOnlyProvidedFields() {
        UpdateGymRequest request = new UpdateGymRequest();
        request.setName("New Name");
        request.setRoutineCycle("MONTHLY");

        when(gymRepository.findById(10L)).thenReturn(Optional.of(gym));
        when(gymRepository.save(any(Gym.class))).thenAnswer(i -> i.getArgument(0));

        GymInfoDto result = gymService.updateSettings(10L, request);

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getRoutineCycle()).isEqualTo("MONTHLY");
        assertThat(result.getSlug()).isEqualTo("fitness-center"); // slug no cambia
        verify(gymRepository).save(any(Gym.class));
    }

    @Test
    void updateSettings_throwsResourceNotFoundException_whenGymNotFound() {
        UpdateGymRequest request = new UpdateGymRequest();
        request.setName("Any");

        when(gymRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gymService.updateSettings(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateSettings_doesNotModifyFields_whenNullsProvided() {
        UpdateGymRequest request = new UpdateGymRequest(); // todos null

        when(gymRepository.findById(10L)).thenReturn(Optional.of(gym));
        when(gymRepository.save(any(Gym.class))).thenAnswer(i -> i.getArgument(0));

        GymInfoDto result = gymService.updateSettings(10L, request);

        assertThat(result.getName()).isEqualTo("Fitness Center");
        assertThat(result.getRoutineCycle()).isEqualTo("WEEKLY");
    }
}
