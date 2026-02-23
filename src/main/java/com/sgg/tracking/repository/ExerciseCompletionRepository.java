package com.sgg.tracking.repository;

import com.sgg.tracking.entity.ExerciseCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ExerciseCompletionRepository extends JpaRepository<ExerciseCompletion, Long> {

    Optional<ExerciseCompletion> findByAssignmentIdAndExerciseIdAndUserId(
            Long assignmentId, Long exerciseId, Long userId);

    List<ExerciseCompletion> findByAssignmentIdAndUserIdAndIsCompleted(
            Long assignmentId, Long userId, Boolean isCompleted);

    List<ExerciseCompletion> findByAssignmentIdAndUserId(Long assignmentId, Long userId);

    @Query("SELECT MAX(ec.completedAt) FROM ExerciseCompletion ec WHERE ec.assignmentId = :assignmentId AND ec.isCompleted = true")
    Optional<LocalDateTime> findLastActivityByAssignmentId(@Param("assignmentId") Long assignmentId);
}
