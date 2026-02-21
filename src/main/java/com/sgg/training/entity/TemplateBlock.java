package com.sgg.training.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "template_blocks")
@Getter
@Setter
@NoArgsConstructor
public class TemplateBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private RoutineTemplate template;

    @Column(nullable = false)
    private String name;  // "Día 1 — Pecho y Tríceps"

    @Column(name = "day_number", nullable = false)
    private Integer dayNumber;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @OneToMany(mappedBy = "block", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<TemplateExercise> exercises = new ArrayList<>();
}
