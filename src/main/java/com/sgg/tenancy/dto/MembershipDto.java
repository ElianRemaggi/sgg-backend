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
public class MembershipDto {
    private Long membershipId;
    private Long gymId;
    private String gymName;
    private String gymSlug;
    private String role;
    private String status;
    private LocalDateTime membershipExpiresAt;
    private boolean expired;
}
