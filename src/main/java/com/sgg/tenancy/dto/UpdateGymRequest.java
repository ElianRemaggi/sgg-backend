package com.sgg.tenancy.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateGymRequest {

    @Size(min = 1, message = "El nombre no puede estar vacío")
    private String name;

    private String description;

    private String logoUrl;

    @Pattern(regexp = "^(WEEKLY|MONTHLY)$", message = "routineCycle debe ser WEEKLY o MONTHLY")
    private String routineCycle;
}
