package com.sgg.tenancy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateGymRequest {

    @NotBlank
    private String name;

    @NotBlank
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must be lowercase letters, numbers and hyphens only")
    private String slug;

    private String description;
    private String logoUrl;
    private String routineCycle = "WEEKLY";
}
