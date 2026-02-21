package com.sgg.coaching.service;

import com.sgg.coaching.dto.AssignCoachRequest;
import com.sgg.coaching.dto.CoachAssignmentDto;
import com.sgg.coaching.entity.CoachAssignment;
import com.sgg.coaching.repository.CoachAssignmentRepository;
import com.sgg.common.exception.BusinessException;
import com.sgg.common.exception.ResourceNotFoundException;
import com.sgg.identity.entity.User;
import com.sgg.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CoachAssignmentService {

    private final CoachAssignmentRepository coachAssignmentRepository;
    private final UserRepository userRepository;

    public CoachAssignmentDto assign(Long gymId, AssignCoachRequest request) {
        // Validar que no existe asignación activa
        coachAssignmentRepository
                .findByGymIdAndCoachUserIdAndMemberUserIdAndUnassignedAtIsNull(
                        gymId, request.getCoachUserId(), request.getMemberUserId())
                .ifPresent(a -> {
                    throw new BusinessException("Ya existe una asignación activa entre este coach y miembro");
                });

        CoachAssignment assignment = new CoachAssignment();
        assignment.setGymId(gymId);
        assignment.setCoachUserId(request.getCoachUserId());
        assignment.setMemberUserId(request.getMemberUserId());

        return toDto(coachAssignmentRepository.save(assignment));
    }

    public void unassign(Long gymId, Long assignmentId) {
        CoachAssignment assignment = coachAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));

        if (!assignment.getGymId().equals(gymId)) {
            throw new BusinessException("Assignment no pertenece a este gym");
        }

        assignment.setUnassignedAt(LocalDateTime.now());
        coachAssignmentRepository.save(assignment);
    }

    @Transactional(readOnly = true)
    public List<CoachAssignmentDto> listByGym(Long gymId) {
        return coachAssignmentRepository
                .findByGymIdAndUnassignedAtIsNull(gymId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CoachAssignmentDto> listByCoach(Long gymId, Long coachUserId) {
        return coachAssignmentRepository
                .findByGymIdAndCoachUserIdAndUnassignedAtIsNull(gymId, coachUserId).stream()
                .map(this::toDto)
                .toList();
    }

    private CoachAssignmentDto toDto(CoachAssignment a) {
        User coach = userRepository.findById(a.getCoachUserId()).orElse(null);
        User member = userRepository.findById(a.getMemberUserId()).orElse(null);
        return new CoachAssignmentDto(
                a.getId(), a.getGymId(),
                a.getCoachUserId(), coach != null ? coach.getFullName() : "Unknown",
                a.getMemberUserId(), member != null ? member.getFullName() : "Unknown",
                a.getAssignedAt(), a.getUnassignedAt()
        );
    }
}
