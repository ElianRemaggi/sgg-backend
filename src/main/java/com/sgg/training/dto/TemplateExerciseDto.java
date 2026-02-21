package com.sgg.training.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class TemplateExerciseDto {
    private Long id;
    private String name;
    private Integer sets;
    private String reps;
    private Integer restSeconds;
    private String notes;
    private Integer sortOrder;
}
