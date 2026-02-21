package com.sgg.tracking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ProgressDto {
    private Long assignmentId;
    private String templateName;
    private int totalExercises;
    private int completedExercises;
    private int percentComplete;
    private List<BlockProgressDto> blockProgress;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class BlockProgressDto {
        private Long blockId;
        private String blockName;
        private int totalExercises;
        private int completedExercises;
        private List<Long> completedExerciseIds;
    }
}
