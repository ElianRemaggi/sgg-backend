package com.sgg.tenancy.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GymMemberDto {
    private Long id;
    private Long userId;
    private Long gymId;
    private String fullName;
    private String email;
    private String avatarUrl;
    private String role;
    private String status;
    private LocalDateTime membershipExpiresAt;
    private LocalDateTime createdAt;
    private Long assignedCoachId;
    private String assignedCoachName;
}
