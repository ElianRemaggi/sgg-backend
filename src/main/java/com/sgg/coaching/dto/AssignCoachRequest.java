package com.sgg.coaching.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignCoachRequest {
    @NotNull
    private Long coachUserId;
    @NotNull
    private Long memberUserId;
}
