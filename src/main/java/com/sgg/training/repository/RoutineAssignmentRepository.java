package com.sgg.training.repository;

import com.sgg.training.entity.RoutineAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RoutineAssignmentRepository extends JpaRepository<RoutineAssignment, Long> {
    List<RoutineAssignment> findByMemberUserIdAndGymId(Long memberUserId, Long gymId);

    Optional<RoutineAssignment> findFirstByMemberUserIdAndGymIdAndStartsAtLessThanEqualAndEndsAtGreaterThanEqual(
            Long memberUserId, Long gymId, LocalDate today1, LocalDate today2);

    @Query("SELECT ra FROM RoutineAssignment ra WHERE ra.memberUserId = :memberUserId AND ra.gymId = :gymId AND ra.startsAt <= :today AND ra.endsAt >= :today")
    Optional<RoutineAssignment> findActiveByMemberUserIdAndGymId(
            @Param("memberUserId") Long memberUserId,
            @Param("gymId") Long gymId,
            @Param("today") LocalDate today);
}
