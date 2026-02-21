package com.sgg.training.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "template_exercises")
@Getter
@Setter
@NoArgsConstructor
public class TemplateExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "block_id", nullable = false)
    private TemplateBlock block;

    @Column(nullable = false)
    private String name;  // "Press banca"

    private Integer sets;
    private String reps;          // "10-12" o "Al fallo"
    private Integer restSeconds;
    private String notes;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;
}
