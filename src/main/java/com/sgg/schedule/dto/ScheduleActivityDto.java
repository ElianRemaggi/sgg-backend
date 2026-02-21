package com.sgg.schedule.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ScheduleActivityDto {
    private Long id;
    private String name;
    private String description;
    private Integer dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean isActive;
}
