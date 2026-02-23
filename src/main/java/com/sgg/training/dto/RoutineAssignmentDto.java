package com.sgg.training.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoutineAssignmentDto {
    private Long id;
    private Long gymId;
    private Long templateId;
    private String templateName;
    private Long memberUserId;
    private String memberName;
    private String memberAvatarUrl;
    private Long assignedBy;
    private LocalDate startsAt;
    private LocalDate endsAt;
}
