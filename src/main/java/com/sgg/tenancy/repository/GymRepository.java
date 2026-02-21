package com.sgg.tenancy.repository;

import com.sgg.tenancy.entity.Gym;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GymRepository extends JpaRepository<Gym, Long> {
    Optional<Gym> findBySlug(String slug);
    List<Gym> findByNameContainingIgnoreCase(String name);
    boolean existsBySlug(String slug);
}
