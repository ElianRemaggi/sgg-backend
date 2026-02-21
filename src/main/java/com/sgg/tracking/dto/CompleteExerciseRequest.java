package com.sgg.tracking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompleteExerciseRequest {
    @NotNull
    private Long exerciseId;
    @NotNull
    private Long assignmentId;
}
