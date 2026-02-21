package com.sgg.coaching.service;

import com.sgg.coaching.dto.AssignCoachRequest;
import com.sgg.coaching.dto.CoachAssignmentDto;
import com.sgg.coaching.entity.CoachAssignment;
import com.sgg.coaching.repository.CoachAssignmentRepository;
import com.sgg.common.exception.BusinessException;
import com.sgg.common.exception.ResourceNotFoundException;
import com.sgg.identity.entity.User;
import com.sgg.identity.repository.UserRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CoachAssignmentServiceTest {

    @Mock
    private CoachAssignmentRepository coachAssignmentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CoachAssignmentService coachAssignmentService;

    private static final Long GYM_ID = 10L;
    private static final Long COACH_ID = 2L;
    private static final Long MEMBER_ID = 3L;
    private static final Long ASSIGNMENT_ID = 50L;

    private CoachAssignment assignment;
    private User coach;
    private User member;

    @BeforeEach
    void setUp() {
        assignment = new CoachAssignment();
        assignment.setId(ASSIGNMENT_ID);
        assignment.setGymId(GYM_ID);
        assignment.setCoachUserId(COACH_ID);
        assignment.setMemberUserId(MEMBER_ID);
        assignment.setAssignedAt(LocalDateTime.now());

        coach = new User();
        coach.setId(COACH_ID);
        coach.setFullName("Coach Ana");
        coach.setEmail("ana@gym.com");

        member = new User();
        member.setId(MEMBER_ID);
        member.setFullName("Member Bob");
        member.setEmail("bob@gym.com");
    }

    // --- assign ---

    @Test
    void assign_success_whenNoActiveAssignmentExists() {
        AssignCoachRequest request = new AssignCoachRequest();
        request.setCoachUserId(COACH_ID);
        request.setMemberUserId(MEMBER_ID);

        when(coachAssignmentRepository
                .findByGymIdAndCoachUserIdAndMemberUserIdAndUnassignedAtIsNull(GYM_ID, COACH_ID, MEMBER_ID))
                .thenReturn(Optional.empty());
        when(coachAssignmentRepository.save(any(CoachAssignment.class))).thenReturn(assignment);
        when(userRepository.findById(COACH_ID)).thenReturn(Optional.of(coach));
        when(userRepository.findById(MEMBER_ID)).thenReturn(Optional.of(member));

        CoachAssignmentDto result = coachAssignmentService.assign(GYM_ID, request);

        assertThat(result).isNotNull();
        assertThat(result.getCoachName()).isEqualTo("Coach Ana");
        assertThat(result.getMemberName()).isEqualTo("Member Bob");
        verify(coachAssignmentRepository).save(any(CoachAssignment.class));
    }

    @Test
    void assign_throws_whenActiveAssignmentAlreadyExists() {
        AssignCoachRequest request = new AssignCoachRequest();
        request.setCoachUserId(COACH_ID);
        request.setMemberUserId(MEMBER_ID);

        when(coachAssignmentRepository
                .findByGymIdAndCoachUserIdAndMemberUserIdAndUnassignedAtIsNull(GYM_ID, COACH_ID, MEMBER_ID))
                .thenReturn(Optional.of(assignment));

        assertThatThrownBy(() -> coachAssignmentService.assign(GYM_ID, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("asignación activa");
    }

    // --- unassign ---

    @Test
    void unassign_success_setsUnassignedAt() {
        when(coachAssignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(assignment));
        when(coachAssignmentRepository.save(assignment)).thenReturn(assignment);

        coachAssignmentService.unassign(GYM_ID, ASSIGNMENT_ID);

        assertThat(assignment.getUnassignedAt()).isNotNull();
        verify(coachAssignmentRepository).save(assignment);
    }

    @Test
    void unassign_throws_whenAssignmentBelongsToDifferentGym() {
        assignment.setGymId(999L);
        when(coachAssignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(assignment));

        assertThatThrownBy(() -> coachAssignmentService.unassign(GYM_ID, ASSIGNMENT_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("gym");
    }

    @Test
    void unassign_throws_whenAssignmentNotFound() {
        when(coachAssignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> coachAssignmentService.unassign(GYM_ID, ASSIGNMENT_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- listByGym ---

    @Test
    void listByGym_returnsActiveAssignments() {
        when(coachAssignmentRepository.findByGymIdAndUnassignedAtIsNull(GYM_ID))
                .thenReturn(List.of(assignment));
        when(userRepository.findById(COACH_ID)).thenReturn(Optional.of(coach));
        when(userRepository.findById(MEMBER_ID)).thenReturn(Optional.of(member));

        List<CoachAssignmentDto> result = coachAssignmentService.listByGym(GYM_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getGymId()).isEqualTo(GYM_ID);
    }

    // --- listByCoach ---

    @Test
    void listByCoach_returnsAssignmentsForCoach() {
        when(coachAssignmentRepository.findByGymIdAndCoachUserIdAndUnassignedAtIsNull(GYM_ID, COACH_ID))
                .thenReturn(List.of(assignment));
        when(userRepository.findById(COACH_ID)).thenReturn(Optional.of(coach));
        when(userRepository.findById(MEMBER_ID)).thenReturn(Optional.of(member));

        List<CoachAssignmentDto> result = coachAssignmentService.listByCoach(GYM_ID, COACH_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCoachUserId()).isEqualTo(COACH_ID);
    }
}
