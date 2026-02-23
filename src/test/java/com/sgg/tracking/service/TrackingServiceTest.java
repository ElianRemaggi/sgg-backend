package com.sgg.tracking.service;

import com.sgg.common.exception.ResourceNotFoundException;
import com.sgg.tracking.dto.CompleteExerciseRequest;
import com.sgg.tracking.dto.ProgressDto;
import com.sgg.tracking.entity.ExerciseCompletion;
import com.sgg.tracking.repository.ExerciseCompletionRepository;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrackingServiceTest {

    @Mock
    private ExerciseCompletionRepository completionRepository;

    @Mock
    private RoutineAssignmentRepository assignmentRepository;

    @Mock
    private RoutineTemplateRepository templateRepository;

    @InjectMocks
    private TrackingService trackingService;

    private static final Long GYM_ID = 10L;
    private static final Long USER_ID = 1L;
    private static final Long ASSIGNMENT_ID = 50L;
    private static final Long TEMPLATE_ID = 1L;
    private static final Long EXERCISE_1_ID = 101L;
    private static final Long EXERCISE_2_ID = 102L;
    private static final Long EXERCISE_3_ID = 103L;

    private RoutineAssignment assignment;
    private RoutineTemplate template;

    @BeforeEach
    void setUp() {
        assignment = new RoutineAssignment();
        assignment.setId(ASSIGNMENT_ID);
        assignment.setGymId(GYM_ID);
        assignment.setTemplateId(TEMPLATE_ID);
        assignment.setMemberUserId(USER_ID);
        assignment.setStartsAt(LocalDate.now().minusDays(3));
        assignment.setEndsAt(LocalDate.now().plusDays(27));

        TemplateExercise ex1 = new TemplateExercise();
        ex1.setId(EXERCISE_1_ID);
        ex1.setName("Press banca");
        ex1.setSets(3);
        ex1.setReps("10");
        ex1.setSortOrder(0);

        TemplateExercise ex2 = new TemplateExercise();
        ex2.setId(EXERCISE_2_ID);
        ex2.setName("Fondos");
        ex2.setSets(3);
        ex2.setReps("12");
        ex2.setSortOrder(1);

        TemplateBlock block1 = new TemplateBlock();
        block1.setId(201L);
        block1.setName("Día 1 — Pecho");
        block1.setDayNumber(1);
        block1.setSortOrder(0);
        block1.setExercises(new ArrayList<>(List.of(ex1, ex2)));

        TemplateExercise ex3 = new TemplateExercise();
        ex3.setId(EXERCISE_3_ID);
        ex3.setName("Sentadilla");
        ex3.setSets(4);
        ex3.setReps("8");
        ex3.setSortOrder(0);

        TemplateBlock block2 = new TemplateBlock();
        block2.setId(202L);
        block2.setName("Día 2 — Piernas");
        block2.setDayNumber(2);
        block2.setSortOrder(1);
        block2.setExercises(new ArrayList<>(List.of(ex3)));

        template = new RoutineTemplate();
        template.setId(TEMPLATE_ID);
        template.setGymId(GYM_ID);
        template.setName("Full Body");
        template.setCreatedBy(2L);
        template.setBlocks(new ArrayList<>(List.of(block1, block2)));
    }

    // --- complete ---

    @Test
    void complete_createsNewCompletion_whenDoesNotExist() {
        CompleteExerciseRequest request = new CompleteExerciseRequest();
        request.setAssignmentId(ASSIGNMENT_ID);
        request.setExerciseId(EXERCISE_1_ID);

        when(completionRepository.findByAssignmentIdAndExerciseIdAndUserId(ASSIGNMENT_ID, EXERCISE_1_ID, USER_ID))
                .thenReturn(Optional.empty());

        ExerciseCompletion saved = new ExerciseCompletion();
        saved.setId(1L);
        saved.setAssignmentId(ASSIGNMENT_ID);
        saved.setExerciseId(EXERCISE_1_ID);
        saved.setUserId(USER_ID);
        saved.setGymId(GYM_ID);
        saved.setIsCompleted(true);

        when(completionRepository.save(any(ExerciseCompletion.class))).thenReturn(saved);

        ExerciseCompletion result = trackingService.complete(GYM_ID, USER_ID, request);

        assertThat(result.getIsCompleted()).isTrue();
        verify(completionRepository).save(any(ExerciseCompletion.class));
    }

    @Test
    void complete_reactivatesExistingCompletion_whenAlreadyExists() {
        CompleteExerciseRequest request = new CompleteExerciseRequest();
        request.setAssignmentId(ASSIGNMENT_ID);
        request.setExerciseId(EXERCISE_1_ID);

        ExerciseCompletion existing = new ExerciseCompletion();
        existing.setId(5L);
        existing.setIsCompleted(false);

        when(completionRepository.findByAssignmentIdAndExerciseIdAndUserId(ASSIGNMENT_ID, EXERCISE_1_ID, USER_ID))
                .thenReturn(Optional.of(existing));
        when(completionRepository.save(existing)).thenReturn(existing);

        ExerciseCompletion result = trackingService.complete(GYM_ID, USER_ID, request);

        assertThat(result.getIsCompleted()).isTrue();
        verify(completionRepository).save(existing);
    }

    // --- undo ---

    @Test
    void undo_setsIsCompleted_toFalse_whenCompletionExists() {
        CompleteExerciseRequest request = new CompleteExerciseRequest();
        request.setAssignmentId(ASSIGNMENT_ID);
        request.setExerciseId(EXERCISE_1_ID);

        ExerciseCompletion existing = new ExerciseCompletion();
        existing.setId(5L);
        existing.setIsCompleted(true);

        when(completionRepository.findByAssignmentIdAndExerciseIdAndUserId(ASSIGNMENT_ID, EXERCISE_1_ID, USER_ID))
                .thenReturn(Optional.of(existing));
        when(completionRepository.save(existing)).thenReturn(existing);

        trackingService.undo(USER_ID, request);

        assertThat(existing.getIsCompleted()).isFalse();
        verify(completionRepository).save(existing);
    }

    @Test
    void undo_doesNothing_whenCompletionDoesNotExist() {
        CompleteExerciseRequest request = new CompleteExerciseRequest();
        request.setAssignmentId(ASSIGNMENT_ID);
        request.setExerciseId(EXERCISE_1_ID);

        when(completionRepository.findByAssignmentIdAndExerciseIdAndUserId(ASSIGNMENT_ID, EXERCISE_1_ID, USER_ID))
                .thenReturn(Optional.empty());

        trackingService.undo(USER_ID, request);

        verify(completionRepository, never()).save(any());
    }

    // --- getProgress ---

    @Test
    void getProgress_calculatesPercentage_correctly() {
        // 2 de 3 ejercicios completados → 67%
        ExerciseCompletion c1 = new ExerciseCompletion();
        c1.setExerciseId(EXERCISE_1_ID);

        ExerciseCompletion c2 = new ExerciseCompletion();
        c2.setExerciseId(EXERCISE_3_ID);

        LocalDateTime lastActivity = LocalDateTime.now().minusHours(1);

        when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(assignment));
        when(templateRepository.findById(TEMPLATE_ID)).thenReturn(Optional.of(template));
        when(completionRepository.findByAssignmentIdAndUserIdAndIsCompleted(ASSIGNMENT_ID, USER_ID, true))
                .thenReturn(List.of(c1, c2));
        when(completionRepository.findLastActivityByAssignmentId(ASSIGNMENT_ID))
                .thenReturn(Optional.of(lastActivity));

        ProgressDto result = trackingService.getProgress(GYM_ID, USER_ID, ASSIGNMENT_ID);

        assertThat(result.getTotalExercises()).isEqualTo(3);
        assertThat(result.getCompletedExercises()).isEqualTo(2);
        assertThat(result.getPercentComplete()).isEqualTo(67);
        assertThat(result.getBlockProgress()).hasSize(2);
        assertThat(result.getLastActivityAt()).isEqualTo(lastActivity);
        // Verify percentComplete is set on BlockProgressDto
        assertThat(result.getBlockProgress().get(0).getPercentComplete()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void getProgress_returns0Percent_whenNothingCompleted() {
        when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(assignment));
        when(templateRepository.findById(TEMPLATE_ID)).thenReturn(Optional.of(template));
        when(completionRepository.findByAssignmentIdAndUserIdAndIsCompleted(ASSIGNMENT_ID, USER_ID, true))
                .thenReturn(List.of());
        when(completionRepository.findLastActivityByAssignmentId(ASSIGNMENT_ID))
                .thenReturn(Optional.empty());

        ProgressDto result = trackingService.getProgress(GYM_ID, USER_ID, ASSIGNMENT_ID);

        assertThat(result.getTotalExercises()).isEqualTo(3);
        assertThat(result.getCompletedExercises()).isEqualTo(0);
        assertThat(result.getPercentComplete()).isEqualTo(0);
        assertThat(result.getLastActivityAt()).isNull();
    }

    @Test
    void getProgress_throws_whenAssignmentNotFound() {
        when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trackingService.getProgress(GYM_ID, USER_ID, ASSIGNMENT_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(String.valueOf(ASSIGNMENT_ID));
    }

    // --- getProgressByMember ---

    @Test
    void getProgressByMember_success_whenActivRoutineExists() {
        ExerciseCompletion c1 = new ExerciseCompletion();
        c1.setExerciseId(EXERCISE_1_ID);

        when(assignmentRepository.findActiveByMemberUserIdAndGymId(eq(USER_ID), eq(GYM_ID), any(LocalDate.class)))
                .thenReturn(Optional.of(assignment));
        when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(assignment));
        when(templateRepository.findById(TEMPLATE_ID)).thenReturn(Optional.of(template));
        when(completionRepository.findByAssignmentIdAndUserIdAndIsCompleted(ASSIGNMENT_ID, USER_ID, true))
                .thenReturn(List.of(c1));
        when(completionRepository.findLastActivityByAssignmentId(ASSIGNMENT_ID))
                .thenReturn(Optional.empty());

        ProgressDto result = trackingService.getProgressByMember(GYM_ID, 2L, USER_ID);

        assertThat(result).isNotNull();
        assertThat(result.getAssignmentId()).isEqualTo(ASSIGNMENT_ID);
        assertThat(result.getCompletedExercises()).isEqualTo(1);
    }

    @Test
    void getProgressByMember_throws_whenNoActiveRoutine() {
        when(assignmentRepository.findActiveByMemberUserIdAndGymId(eq(USER_ID), eq(GYM_ID), any(LocalDate.class)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> trackingService.getProgressByMember(GYM_ID, 2L, USER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(String.valueOf(USER_ID));
    }
}
