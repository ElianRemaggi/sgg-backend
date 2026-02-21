package com.sgg.tenancy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateMemberRoleRequest {

    @NotBlank
    @Pattern(regexp = "^(MEMBER|COACH|ADMIN|ADMIN_COACH)$",
             message = "role debe ser MEMBER, COACH, ADMIN o ADMIN_COACH")
    private String role;
}
