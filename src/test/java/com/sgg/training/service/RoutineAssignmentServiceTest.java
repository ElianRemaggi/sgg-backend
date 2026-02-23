package com.sgg.training.service;

import com.sgg.common.exception.BusinessException;
import com.sgg.common.exception.ResourceNotFoundException;
import com.sgg.identity.entity.User;
import com.sgg.identity.repository.UserRepository;
import com.sgg.training.dto.AssignRoutineRequest;
import com.sgg.training.dto.MemberRoutineDto;
import com.sgg.training.dto.RoutineAssignmentDto;
import com.sgg.training.entity.RoutineAssignment;
import com.sgg.training.entity.RoutineTemplate;
import com.sgg.training.entity.TemplateBlock;
import com.sgg.training.entity.TemplateExercise;
import com.sgg.training.repository.RoutineAssignmentRepository;
import com.sgg.training.repository.RoutineTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class RoutineAssignmentServiceTest {

    @Mock
    private RoutineAssignmentRepository assignmentRepository;

    @Mock
    private RoutineTemplateRepository templateRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RoutineAssignmentService routineAssignmentService;

    private static final Long GYM_ID = 10L;
    private static final Long MEMBER_ID = 3L;
    private static final Long COACH_ID = 2L;
    private static final Long TEMPLATE_ID = 1L;
    private static final Long ASSIGNMENT_ID = 50L;

    private RoutineTemplate template;
    private RoutineAssignment assignment;

    @BeforeEach
    void setUp() {
        TemplateExercise exercise = new TemplateExercise();
        exercise.setId(101L);
        exercise.setName("Press banca");
        exercise.setSets(3);
        exercise.setReps("10");
        exercise.setSortOrder(0);

        TemplateBlock block = new TemplateBlock();
        block.setId(201L);
        block.setName("Día 1 — Pecho");
        block.setDayNumber(1);
        block.setSortOrder(0);
        block.setExercises(new ArrayList<>(List.of(exercise)));

        template = new RoutineTemplate();
        template.setId(TEMPLATE_ID);
        template.setGymId(GYM_ID);
        template.setName("Rutina Full Body");
        template.setDescription("Rutina completa");
        template.setCreatedBy(COACH_ID);
        template.setBlocks(new ArrayList<>(List.of(block)));

        assignment = new RoutineAssignment();
        assignment.setId(ASSIGNMENT_ID);
        assignment.setGymId(GYM_ID);
        assignment.setTemplateId(TEMPLATE_ID);
        assignment.setMemberUserId(MEMBER_ID);
        assignment.setAssignedBy(COACH_ID);
        assignment.setStartsAt(LocalDate.now().minusDays(5));
        assignment.setEndsAt(LocalDate.now().plusDays(25));

        User memberUser = new User();
        memberUser.setId(MEMBER_ID);
        memberUser.setFullName("Miembro Prueba");
        memberUser.setEmail("miembro@test.com");
        lenient().when(userRepository.findById(MEMBER_ID)).thenReturn(Optional.of(memberUser));
    }

    // --- assign ---

    @Test
    void assign_success_whenDatesAreValid() {
        AssignRoutineRequest request = new AssignRoutineRequest();
        request.setTemplateId(TEMPLATE_ID);
        request.setMemberUserId(MEMBER_ID);
        request.setStartsAt(LocalDate.now());
        request.setEndsAt(LocalDate.now().plusDays(30));

        when(templateRepository.findById(TEMPLATE_ID)).thenReturn(Optional.of(template));
        when(assignmentRepository.save(any(RoutineAssignment.class))).thenReturn(assignment);

        RoutineAssignmentDto result = routineAssignmentService.assign(GYM_ID, COACH_ID, request);

        assertThat(result).isNotNull();
        assertThat(result.getTemplateName()).isEqualTo("Rutina Full Body");
        assertThat(result.getMemberName()).isEqualTo("Miembro Prueba");
        verify(assignmentRepository).save(any(RoutineAssignment.class));
    }

    @Test
    void assign_throws_whenEndDateBeforeStartDate() {
        AssignRoutineRequest request = new AssignRoutineRequest();
        request.setTemplateId(TEMPLATE_ID);
        request.setMemberUserId(MEMBER_ID);
        request.setStartsAt(LocalDate.now().plusDays(10));
        request.setEndsAt(LocalDate.now()); // endsAt < startsAt

        assertThatThrownBy(() -> routineAssignmentService.assign(GYM_ID, COACH_ID, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("posterior");
    }

    @Test
    void assign_throws_whenTemplateNotFound() {
        AssignRoutineRequest request = new AssignRoutineRequest();
        request.setTemplateId(99L);
        request.setMemberUserId(MEMBER_ID);
        request.setStartsAt(LocalDate.now());
        request.setEndsAt(LocalDate.now().plusDays(30));

        when(templateRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> routineAssignmentService.assign(GYM_ID, COACH_ID, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- getActiveRoutineForMember ---

    @Test
    void getActiveRoutineForMember_returnsDto_whenActiveRoutineExists() {
        when(assignmentRepository
                .findFirstByMemberUserIdAndGymIdAndStartsAtLessThanEqualAndEndsAtGreaterThanEqual(
                        eq(MEMBER_ID), eq(GYM_ID), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Optional.of(assignment));
        when(templateRepository.findById(TEMPLATE_ID)).thenReturn(Optional.of(template));

        MemberRoutineDto result = routineAssignmentService.getActiveRoutineForMember(GYM_ID, MEMBER_ID);

        assertThat(result).isNotNull();
        assertThat(result.getTemplateName()).isEqualTo("Rutina Full Body");
        assertThat(result.getBlocks()).hasSize(1);
    }

    @Test
    void getActiveRoutineForMember_throwsResourceNotFoundException_whenNoActiveRoutine() {
        when(assignmentRepository
                .findFirstByMemberUserIdAndGymIdAndStartsAtLessThanEqualAndEndsAtGreaterThanEqual(
                        eq(MEMBER_ID), eq(GYM_ID), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> routineAssignmentService.getActiveRoutineForMember(GYM_ID, MEMBER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("rutina activa");
    }

    // --- getAssignmentsForMember ---

    @Test
    void getAssignmentsForMember_returnsAllAssignments() {
        when(assignmentRepository.findByMemberUserIdAndGymId(MEMBER_ID, GYM_ID))
                .thenReturn(List.of(assignment));
        when(templateRepository.findById(TEMPLATE_ID)).thenReturn(Optional.of(template));

        List<RoutineAssignmentDto> result = routineAssignmentService.getAssignmentsForMember(GYM_ID, MEMBER_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMemberUserId()).isEqualTo(MEMBER_ID);
        assertThat(result.get(0).getTemplateName()).isEqualTo("Rutina Full Body");
        assertThat(result.get(0).getMemberName()).isEqualTo("Miembro Prueba");
    }

    @Test
    void getAssignmentsForMember_returnsEmptyList_whenNone() {
        when(assignmentRepository.findByMemberUserIdAndGymId(MEMBER_ID, GYM_ID))
                .thenReturn(List.of());

        List<RoutineAssignmentDto> result = routineAssignmentService.getAssignmentsForMember(GYM_ID, MEMBER_ID);

        assertThat(result).isEmpty();
    }
}
