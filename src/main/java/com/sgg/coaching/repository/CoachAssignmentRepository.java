package com.sgg.coaching.repository;

import com.sgg.coaching.entity.CoachAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CoachAssignmentRepository extends JpaRepository<CoachAssignment, Long> {
    List<CoachAssignment> findByGymIdAndCoachUserIdAndUnassignedAtIsNull(Long gymId, Long coachUserId);
    List<CoachAssignment> findByGymIdAndUnassignedAtIsNull(Long gymId);
    Optional<CoachAssignment> findByGymIdAndCoachUserIdAndMemberUserIdAndUnassignedAtIsNull(
            Long gymId, Long coachUserId, Long memberUserId);
}
