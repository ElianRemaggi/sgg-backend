package com.sgg.training.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class AssignRoutineRequest {
    @NotNull
    private Long templateId;
    @NotNull
    private Long memberUserId;
    @NotNull
    private LocalDate startsAt;
    @NotNull
    private LocalDate endsAt;
}
