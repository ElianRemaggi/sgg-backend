package com.sgg.schedule.repository;

import com.sgg.schedule.entity.ScheduleActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleActivityRepository extends JpaRepository<ScheduleActivity, Long> {
    List<ScheduleActivity> findByGymIdAndIsActiveTrue(Long gymId);
    List<ScheduleActivity> findByGymId(Long gymId);
}
