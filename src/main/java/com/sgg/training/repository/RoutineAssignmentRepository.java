package com.sgg.training.repository;

import com.sgg.training.entity.RoutineAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RoutineAssignmentRepository extends JpaRepository<RoutineAssignment, Long> {
    List<RoutineAssignment> findByMemberUserIdAndGymId(Long memberUserId, Long gymId);

    Optional<RoutineAssignment> findFirstByMemberUserIdAndGymIdAndStartsAtLessThanEqualAndEndsAtGreaterThanEqual(
            Long memberUserId, Long gymId, LocalDate today1, LocalDate today2);
}
