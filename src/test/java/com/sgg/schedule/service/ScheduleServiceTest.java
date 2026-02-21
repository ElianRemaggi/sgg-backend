package com.sgg.schedule.service;

import com.sgg.common.exception.ResourceNotFoundException;
import com.sgg.schedule.dto.CreateScheduleRequest;
import com.sgg.schedule.dto.ScheduleActivityDto;
import com.sgg.schedule.entity.ScheduleActivity;
import com.sgg.schedule.repository.ScheduleActivityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock
    private ScheduleActivityRepository activityRepository;

    @InjectMocks
    private ScheduleService scheduleService;

    private static final Long GYM_ID = 10L;
    private static final Long ACTIVITY_ID = 20L;

    private ScheduleActivity activity;

    @BeforeEach
    void setUp() {
        activity = new ScheduleActivity();
        activity.setId(ACTIVITY_ID);
        activity.setGymId(GYM_ID);
        activity.setName("Spinning");
        activity.setDescription("Clase de spinning");
        activity.setDayOfWeek(1);
        activity.setStartTime(LocalTime.of(9, 0));
        activity.setEndTime(LocalTime.of(10, 0));
        activity.setIsActive(true);
    }

    // --- create ---

    @Test
    void create_success_setsGymIdAndIsActiveTrue() {
        CreateScheduleRequest request = new CreateScheduleRequest();
        request.setName("Spinning");
        request.setDescription("Clase de spinning");
        request.setDayOfWeek(1);
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(10, 0));

        when(activityRepository.save(any(ScheduleActivity.class))).thenReturn(activity);

        ScheduleActivityDto result = scheduleService.create(GYM_ID, request);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Spinning");
        assertThat(result.getIsActive()).isTrue();
        verify(activityRepository).save(argThat(a -> a.getGymId().equals(GYM_ID) && a.getIsActive()));
    }

    // --- delete ---

    @Test
    void delete_success_setsIsActiveFalse() {
        when(activityRepository.findById(ACTIVITY_ID)).thenReturn(Optional.of(activity));
        when(activityRepository.save(activity)).thenReturn(activity);

        scheduleService.delete(GYM_ID, ACTIVITY_ID);

        assertThat(activity.getIsActive()).isFalse();
        verify(activityRepository).save(activity);
    }

    @Test
    void delete_throws_whenActivityNotFound() {
        when(activityRepository.findById(ACTIVITY_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> scheduleService.delete(GYM_ID, ACTIVITY_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_throws_whenActivityBelongsToDifferentGym() {
        activity.setGymId(999L); // pertenece a otro gym
        when(activityRepository.findById(ACTIVITY_ID)).thenReturn(Optional.of(activity));

        assertThatThrownBy(() -> scheduleService.delete(GYM_ID, ACTIVITY_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("gym");
    }

    // --- listActive ---

    @Test
    void listActive_returnsOnlyActiveActivities() {
        when(activityRepository.findByGymIdAndIsActiveTrue(GYM_ID)).thenReturn(List.of(activity));

        List<ScheduleActivityDto> result = scheduleService.listActive(GYM_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Spinning");
        assertThat(result.get(0).getDayOfWeek()).isEqualTo(1);
    }

    @Test
    void listActive_returnsEmptyList_whenNoActiveActivities() {
        when(activityRepository.findByGymIdAndIsActiveTrue(GYM_ID)).thenReturn(List.of());

        List<ScheduleActivityDto> result = scheduleService.listActive(GYM_ID);

        assertThat(result).isEmpty();
    }
}
