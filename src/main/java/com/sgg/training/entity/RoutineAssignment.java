package com.sgg.training.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Filter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "routine_assignments")
@Filter(name = "tenantFilter", condition = "gym_id = :gymId")
@Getter
@Setter
@NoArgsConstructor
public class RoutineAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "gym_id", nullable = false)
    private Long gymId;

    @Column(name = "template_id", nullable = false)
    private Long templateId;

    @Column(name = "member_user_id", nullable = false)
    private Long memberUserId;

    @Column(name = "assigned_by", nullable = false)
    private Long assignedBy;

    @Column(name = "starts_at", nullable = false)
    private LocalDate startsAt;

    @Column(name = "ends_at", nullable = false)
    private LocalDate endsAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
