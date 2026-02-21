package com.sgg.tenancy.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ApproveMemberRequest {
    private LocalDateTime expiresAt; // null = sin vencimiento
}
