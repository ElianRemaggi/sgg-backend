package com.sgg.tenancy.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GymInfoDto {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String logoUrl;
    private String routineCycle;
    private String userRole;   // rol del usuario autenticado en este gym
    private int memberCount;   // COUNT de gym_members con status ACTIVE
    private int coachCount;    // COUNT de gym_members con rol COACH o ADMIN_COACH y status ACTIVE
}
