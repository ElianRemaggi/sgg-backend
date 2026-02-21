package com.sgg.training.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateTemplateRequest {

    @NotBlank
    private String name;

    private String description;

    @NotEmpty
    private List<BlockRequest> blocks;

    @Getter
    @Setter
    public static class BlockRequest {
        @NotBlank
        private String name;
        @NotNull
        private Integer dayNumber;
        private Integer sortOrder = 0;
        @NotEmpty
        private List<ExerciseRequest> exercises;
    }

    @Getter
    @Setter
    public static class ExerciseRequest {
        @NotBlank
        private String name;
        private Integer sets;
        private String reps;
        private Integer restSeconds;
        private String notes;
        private Integer sortOrder = 0;
    }
}
