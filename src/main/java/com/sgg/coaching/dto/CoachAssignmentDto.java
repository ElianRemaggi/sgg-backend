package com.sgg.coaching.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CoachAssignmentDto {
    private Long id;
    private Long gymId;
    private Long coachUserId;
    private String coachName;
    private Long memberUserId;
    private String memberName;
    private LocalDateTime assignedAt;
    private LocalDateTime unassignedAt;
}
