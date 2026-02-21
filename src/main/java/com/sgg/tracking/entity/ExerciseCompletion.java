package com.sgg.tracking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Filter;

import java.time.LocalDateTime;

@Entity
@Table(name = "exercise_completions")
@Filter(name = "tenantFilter", condition = "gym_id = :gymId")
@Getter
@Setter
@NoArgsConstructor
public class ExerciseCompletion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "assignment_id", nullable = false)
    private Long assignmentId;

    @Column(name = "exercise_id", nullable = false)
    private Long exerciseId;

    @Column(name = "gym_id", nullable = false)
    private Long gymId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = true;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        completedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
