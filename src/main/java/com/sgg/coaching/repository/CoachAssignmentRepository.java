package com.sgg.coaching.repository;

import com.sgg.coaching.entity.CoachAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CoachAssignmentRepository extends JpaRepository<CoachAssignment, Long> {
    List<CoachAssignment> findByGymIdAndCoachUserIdAndUnassignedAtIsNull(Long gymId, Long coachUserId);
    List<CoachAssignment> findByGymIdAndUnassignedAtIsNull(Long gymId);
    Optional<CoachAssignment> findByGymIdAndCoachUserIdAndMemberUserIdAndUnassignedAtIsNull(
            Long gymId, Long coachUserId, Long memberUserId);

    @Query("SELECT ca FROM CoachAssignment ca WHERE ca.gymId = :gymId AND ca.unassignedAt IS NULL")
    List<CoachAssignment> findActiveByGymId(@Param("gymId") Long gymId);
}
