package com.sgg.tracking.service;

import com.sgg.common.exception.ResourceNotFoundException;
import com.sgg.tracking.dto.CompleteExerciseRequest;
import com.sgg.tracking.dto.ProgressDto;
import com.sgg.tracking.entity.ExerciseCompletion;
import com.sgg.tracking.repository.ExerciseCompletionRepository;
import com.sgg.training.entity.RoutineAssignment;
import com.sgg.training.entity.RoutineTemplate;
import com.sgg.training.repository.RoutineAssignmentRepository;
import com.sgg.training.repository.RoutineTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TrackingService {

    private final ExerciseCompletionRepository completionRepository;
    private final RoutineAssignmentRepository assignmentRepository;
    private final RoutineTemplateRepository templateRepository;

    /**
     * Marca un ejercicio como completado.
     * Si ya existe un registro, lo reactiva (is_completed = true).
     */
    public ExerciseCompletion complete(Long gymId, Long userId, CompleteExerciseRequest request) {
        ExerciseCompletion completion = completionRepository
                .findByAssignmentIdAndExerciseIdAndUserId(
                        request.getAssignmentId(), request.getExerciseId(), userId)
                .orElseGet(() -> {
                    ExerciseCompletion newCompletion = new ExerciseCompletion();
                    newCompletion.setAssignmentId(request.getAssignmentId());
                    newCompletion.setExerciseId(request.getExerciseId());
                    newCompletion.setGymId(gymId);
                    newCompletion.setUserId(userId);
                    return newCompletion;
                });

        completion.setIsCompleted(true);
        return completionRepository.save(completion);
    }

    /**
     * Desmarca un ejercicio (undo). Setea is_completed = false.
     */
    public void undo(Long userId, CompleteExerciseRequest request) {
        completionRepository
                .findByAssignmentIdAndExerciseIdAndUserId(
                        request.getAssignmentId(), request.getExerciseId(), userId)
                .ifPresent(c -> {
                    c.setIsCompleted(false);
                    completionRepository.save(c);
                });
    }

    /**
     * Retorna el progreso del usuario en una asignación específica.
     */
    @Transactional(readOnly = true)
    public ProgressDto getProgress(Long gymId, Long userId, Long assignmentId) {
        RoutineAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found: " + assignmentId));

        RoutineTemplate template = templateRepository.findById(assignment.getTemplateId())
                .orElseThrow(() -> new ResourceNotFoundException("Template not found"));

        List<ExerciseCompletion> completions = completionRepository
                .findByAssignmentIdAndUserIdAndIsCompleted(assignmentId, userId, true);

        Set<Long> completedIds = completions.stream()
                .map(ExerciseCompletion::getExerciseId)
                .collect(Collectors.toSet());

        List<ProgressDto.BlockProgressDto> blockProgressList = template.getBlocks().stream()
                .map(block -> {
                    List<Long> completedInBlock = block.getExercises().stream()
                            .map(e -> e.getId())
                            .filter(completedIds::contains)
                            .toList();
                    int blockTotal = block.getExercises().size();
                    int blockCompleted = completedInBlock.size();
                    int blockPercent = blockTotal > 0
                            ? (int) Math.round((blockCompleted * 100.0) / blockTotal)
                            : 0;
                    return new ProgressDto.BlockProgressDto(
                            block.getId(),
                            block.getName(),
                            blockTotal,
                            blockCompleted,
                            blockPercent,
                            completedInBlock
                    );
                })
                .toList();

        int totalExercises = template.getBlocks().stream()
                .mapToInt(b -> b.getExercises().size()).sum();
        int completedExercises = completedIds.size();
        int percent = totalExercises > 0
                ? (int) Math.round((completedExercises * 100.0) / totalExercises)
                : 0;

        LocalDateTime lastActivityAt = completionRepository
                .findLastActivityByAssignmentId(assignmentId)
                .orElse(null);

        return new ProgressDto(
                assignmentId,
                template.getName(),
                totalExercises,
                completedExercises,
                percent,
                blockProgressList,
                lastActivityAt
        );
    }

    /**
     * Retorna el progreso de la rutina activa de un miembro (vista de coach).
     */
    @Transactional(readOnly = true)
    public ProgressDto getProgressByMember(Long gymId, Long coachUserId, Long memberUserId) {
        LocalDate today = LocalDate.now();
        RoutineAssignment assignment = assignmentRepository
                .findActiveByMemberUserIdAndGymId(memberUserId, gymId, today)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No hay rutina activa para el miembro: " + memberUserId));
        return getProgress(gymId, memberUserId, assignment.getId());
    }
}
