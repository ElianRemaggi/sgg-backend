package com.sgg.training.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class TemplateBlockDto {
    private Long id;
    private String name;
    private Integer dayNumber;
    private Integer sortOrder;
    private List<TemplateExerciseDto> exercises;
}
