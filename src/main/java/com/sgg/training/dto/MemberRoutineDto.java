package com.sgg.training.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class MemberRoutineDto {
    private Long assignmentId;
    private String templateName;
    private String templateDescription;
    private LocalDate startsAt;
    private LocalDate endsAt;
    private boolean expired;
    private List<TemplateBlockDto> blocks;
}
