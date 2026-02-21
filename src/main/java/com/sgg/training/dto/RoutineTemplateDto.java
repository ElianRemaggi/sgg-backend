package com.sgg.training.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RoutineTemplateDto {
    private Long id;
    private String name;
    private String description;
    private Long createdBy;
    private List<TemplateBlockDto> blocks;
    private LocalDateTime createdAt;
}
