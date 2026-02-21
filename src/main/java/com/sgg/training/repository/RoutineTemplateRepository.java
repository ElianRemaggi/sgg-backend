package com.sgg.training.repository;

import com.sgg.training.entity.RoutineTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoutineTemplateRepository extends JpaRepository<RoutineTemplate, Long> {
    List<RoutineTemplate> findByGymId(Long gymId);
}
