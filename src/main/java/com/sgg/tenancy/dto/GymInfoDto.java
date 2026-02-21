package com.sgg.tenancy.dto;

import com.sgg.tenancy.entity.Gym;
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
    private String userRole; // rol del usuario autenticado en este gym
}
