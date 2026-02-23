package com.sgg.tenancy.service;

import com.sgg.coaching.entity.CoachAssignment;
import com.sgg.coaching.repository.CoachAssignmentRepository;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MembershipServiceTest {

    @Mock
    private GymMemberRepository gymMemberRepository;

    @Mock
    private GymRepository gymRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CoachAssignmentRepository coachAssignmentRepository;

    @InjectMocks
    private MembershipService membershipService;

    private static final Long GYM_ID = 10L;
    private static final Long USER_ID = 1L;
    private static final Long MEMBER_ID = 100L;

    private Gym gym;
    private User user;
    private GymMember pendingMember;
    private GymMember activeMember;

    @BeforeEach
    void setUp() {
        gym = new Gym();
        gym.setId(GYM_ID);
        gym.setName("Test Gym");
        gym.setSlug("test-gym");
        gym.setRoutineCycle(Gym.RoutineCycle.WEEKLY);
        gym.setOwnerUserId(99L);

        user = new User();
        user.setId(USER_ID);
        user.setFullName("Test User");
        user.setEmail("test@example.com");

        pendingMember = new GymMember();
        pendingMember.setId(MEMBER_ID);
        pendingMember.setUserId(USER_ID);
        pendingMember.setGymId(GYM_ID);
        pendingMember.setRole(MemberRole.MEMBER);
        pendingMember.setStatus(MembershipStatus.PENDING);

        activeMember = new GymMember();
        activeMember.setId(MEMBER_ID);
        activeMember.setUserId(USER_ID);
        activeMember.setGymId(GYM_ID);
        activeMember.setRole(MemberRole.MEMBER);
        activeMember.setStatus(MembershipStatus.ACTIVE);
    }

    // --- requestJoin ---

    @Test
    void requestJoin_success_whenNoExistingMembership() {
        when(gymRepository.findById(GYM_ID)).thenReturn(Optional.of(gym));
        when(gymMemberRepository.findByUserIdAndGymIdAndStatusIn(eq(USER_ID), eq(GYM_ID), any()))
                .thenReturn(Optional.empty());
        when(gymMemberRepository.save(any(GymMember.class))).thenReturn(pendingMember);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        GymMemberDto result = membershipService.requestJoin(USER_ID, GYM_ID);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("PENDING");
        verify(gymMemberRepository).save(any(GymMember.class));
    }

    @Test
    void requestJoin_throws_whenGymNotFound() {
        when(gymRepository.findById(GYM_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> membershipService.requestJoin(USER_ID, GYM_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void requestJoin_throws_whenPendingMembershipExists() {
        when(gymRepository.findById(GYM_ID)).thenReturn(Optional.of(gym));
        when(gymMemberRepository.findByUserIdAndGymIdAndStatusIn(eq(USER_ID), eq(GYM_ID), any()))
                .thenReturn(Optional.of(pendingMember));

        assertThatThrownBy(() -> membershipService.requestJoin(USER_ID, GYM_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("pendiente");
    }

    @Test
    void requestJoin_throws_whenActiveMembershipExists() {
        when(gymRepository.findById(GYM_ID)).thenReturn(Optional.of(gym));
        when(gymMemberRepository.findByUserIdAndGymIdAndStatusIn(eq(USER_ID), eq(GYM_ID), any()))
                .thenReturn(Optional.of(activeMember));

        assertThatThrownBy(() -> membershipService.requestJoin(USER_ID, GYM_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("activa");
    }

    // --- approve ---

    @Test
    void approve_success_whenMemberIsPending() {
        when(gymMemberRepository.findById(MEMBER_ID)).thenReturn(Optional.of(pendingMember));
        when(gymMemberRepository.save(pendingMember)).thenReturn(pendingMember);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        LocalDateTime expiresAt = LocalDateTime.now().plusMonths(1);
        GymMemberDto result = membershipService.approve(GYM_ID, MEMBER_ID, expiresAt);

        assertThat(result).isNotNull();
        assertThat(pendingMember.getStatus()).isEqualTo(MembershipStatus.ACTIVE);
        verify(gymMemberRepository).save(pendingMember);
    }

    @Test
    void approve_throws_whenMemberIsNotPending() {
        when(gymMemberRepository.findById(MEMBER_ID)).thenReturn(Optional.of(activeMember));

        assertThatThrownBy(() -> membershipService.approve(GYM_ID, MEMBER_ID, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("PENDING");
    }

    @Test
    void approve_throws_whenTenantViolation() {
        pendingMember.setGymId(999L); // pertenece a otro gym
        when(gymMemberRepository.findById(MEMBER_ID)).thenReturn(Optional.of(pendingMember));

        assertThatThrownBy(() -> membershipService.approve(GYM_ID, MEMBER_ID, null))
                .isInstanceOf(TenantViolationException.class);
    }

    @Test
    void approve_throws_whenMemberNotFound() {
        when(gymMemberRepository.findById(MEMBER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> membershipService.approve(GYM_ID, MEMBER_ID, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- reject ---

    @Test
    void reject_success_whenMemberIsPending() {
        when(gymMemberRepository.findById(MEMBER_ID)).thenReturn(Optional.of(pendingMember));
        when(gymMemberRepository.save(pendingMember)).thenReturn(pendingMember);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        GymMemberDto result = membershipService.reject(GYM_ID, MEMBER_ID);

        assertThat(result).isNotNull();
        assertThat(pendingMember.getStatus()).isEqualTo(MembershipStatus.REJECTED);
    }

    @Test
    void reject_throws_whenMemberIsNotPending() {
        when(gymMemberRepository.findById(MEMBER_ID)).thenReturn(Optional.of(activeMember));

        assertThatThrownBy(() -> membershipService.reject(GYM_ID, MEMBER_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("PENDING");
    }

    // --- block ---

    @Test
    void block_success_setsStatusToBlocked() {
        when(gymMemberRepository.findById(MEMBER_ID)).thenReturn(Optional.of(activeMember));
        when(gymMemberRepository.save(activeMember)).thenReturn(activeMember);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        GymMemberDto result = membershipService.block(GYM_ID, MEMBER_ID);

        assertThat(result).isNotNull();
        assertThat(activeMember.getStatus()).isEqualTo(MembershipStatus.BLOCKED);
    }

    // --- listMembers ---

    @Test
    void listMembers_returnsAllMembersOfGym() {
        when(coachAssignmentRepository.findActiveByGymId(GYM_ID)).thenReturn(List.of());
        when(gymMemberRepository.findByGymId(GYM_ID)).thenReturn(List.of(pendingMember, activeMember));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        List<GymMemberDto> result = membershipService.listMembers(GYM_ID);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getAvatarUrl()).isNull(); // user has no avatarUrl set
    }

    @Test
    void listMembers_includesAssignedCoachInfo_whenCoachAssigned() {
        CoachAssignment coachAssignment = new CoachAssignment();
        coachAssignment.setCoachUserId(99L);
        coachAssignment.setMemberUserId(USER_ID);
        coachAssignment.setGymId(GYM_ID);

        User coachUser = new User();
        coachUser.setId(99L);
        coachUser.setFullName("Coach Pedro");
        coachUser.setEmail("pedro@gym.com");

        when(coachAssignmentRepository.findActiveByGymId(GYM_ID)).thenReturn(List.of(coachAssignment));
        when(gymMemberRepository.findByGymId(GYM_ID)).thenReturn(List.of(activeMember));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userRepository.findById(99L)).thenReturn(Optional.of(coachUser));

        List<GymMemberDto> result = membershipService.listMembers(GYM_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAssignedCoachId()).isEqualTo(99L);
        assertThat(result.get(0).getAssignedCoachName()).isEqualTo("Coach Pedro");
    }

    // --- listCoaches ---

    @Test
    void listCoaches_returnsOnlyCoachAndAdminCoachRoles() {
        GymMember coach = new GymMember();
        coach.setId(200L);
        coach.setUserId(2L);
        coach.setGymId(GYM_ID);
        coach.setRole(MemberRole.COACH);
        coach.setStatus(MembershipStatus.ACTIVE);

        User coachUser = new User();
        coachUser.setId(2L);
        coachUser.setFullName("Coach Ana");
        coachUser.setEmail("ana@gym.com");

        when(gymMemberRepository.findByGymIdAndRoleInAndStatus(eq(GYM_ID), any(), eq(MembershipStatus.ACTIVE)))
                .thenReturn(List.of(coach));
        when(userRepository.findById(2L)).thenReturn(Optional.of(coachUser));

        List<GymMemberDto> result = membershipService.listCoaches(GYM_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRole()).isEqualTo("COACH");
    }

    // --- updateRole ---

    @Test
    void updateRole_success_promotesActiveMemberToCoach() {
        when(gymMemberRepository.findById(MEMBER_ID)).thenReturn(Optional.of(activeMember));
        when(gymMemberRepository.save(activeMember)).thenReturn(activeMember);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        GymMemberDto result = membershipService.updateRole(GYM_ID, MEMBER_ID, MemberRole.COACH);

        assertThat(activeMember.getRole()).isEqualTo(MemberRole.COACH);
        assertThat(result).isNotNull();
        verify(gymMemberRepository).save(activeMember);
    }

    @Test
    void updateRole_throws_whenMemberIsNotActive() {
        when(gymMemberRepository.findById(MEMBER_ID)).thenReturn(Optional.of(pendingMember));

        assertThatThrownBy(() -> membershipService.updateRole(GYM_ID, MEMBER_ID, MemberRole.COACH))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ACTIVE");
    }

    @Test
    void updateRole_throws_whenTenantViolation() {
        activeMember.setGymId(999L);
        when(gymMemberRepository.findById(MEMBER_ID)).thenReturn(Optional.of(activeMember));

        assertThatThrownBy(() -> membershipService.updateRole(GYM_ID, MEMBER_ID, MemberRole.COACH))
                .isInstanceOf(TenantViolationException.class);
    }

    @Test
    void updateRole_throws_whenMemberNotFound() {
        when(gymMemberRepository.findById(MEMBER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> membershipService.updateRole(GYM_ID, MEMBER_ID, MemberRole.COACH))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- getUserMemberships ---

    @Test
    void getUserMemberships_returnsActiveAndPendingMemberships() {
        when(gymMemberRepository.findByUserIdAndStatusIn(eq(USER_ID), any()))
                .thenReturn(List.of(activeMember));
        when(gymRepository.findById(GYM_ID)).thenReturn(Optional.of(gym));

        List<MembershipDto> result = membershipService.getUserMemberships(USER_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getGymId()).isEqualTo(GYM_ID);
    }
}
