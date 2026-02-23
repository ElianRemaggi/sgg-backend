package com.sgg.schedule.service;

import com.sgg.common.exception.ResourceNotFoundException;
import com.sgg.schedule.dto.CreateScheduleRequest;
import com.sgg.schedule.dto.ScheduleActivityDto;
import com.sgg.schedule.entity.ScheduleActivity;
import com.sgg.schedule.repository.ScheduleActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleService {

    private final ScheduleActivityRepository activityRepository;

    public ScheduleActivityDto create(Long gymId, CreateScheduleRequest request) {
        ScheduleActivity activity = new ScheduleActivity();
        activity.setGymId(gymId);
        activity.setName(request.getName());
        activity.setDescription(request.getDescription());
        activity.setDayOfWeek(request.getDayOfWeek());
        activity.setStartTime(request.getStartTime());
        activity.setEndTime(request.getEndTime());
        activity.setIsActive(true);
        activity.setInstructor(request.getInstructor());
        activity.setMaxCapacity(request.getMaxCapacity());
        return toDto(activityRepository.save(activity));
    }

    public void delete(Long gymId, Long activityId) {
        ScheduleActivity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found: " + activityId));
        if (!activity.getGymId().equals(gymId)) {
            throw new ResourceNotFoundException("Activity not found in this gym");
        }
        activity.setIsActive(false);
        activityRepository.save(activity);
    }

    @Transactional(readOnly = true)
    public List<ScheduleActivityDto> listActive(Long gymId) {
        return activityRepository.findByGymIdAndIsActiveTrue(gymId).stream()
                .map(this::toDto)
                .toList();
    }

    private ScheduleActivityDto toDto(ScheduleActivity a) {
        return new ScheduleActivityDto(
                a.getId(), a.getName(), a.getDescription(),
                a.getDayOfWeek(), a.getStartTime(), a.getEndTime(), a.getIsActive(),
                a.getInstructor(), a.getMaxCapacity());
    }
}
