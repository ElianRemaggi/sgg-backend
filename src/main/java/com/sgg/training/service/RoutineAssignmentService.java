package com.sgg.training.service;

import com.sgg.common.exception.BusinessException;
import com.sgg.common.exception.ResourceNotFoundException;
import com.sgg.identity.entity.User;
import com.sgg.identity.repository.UserRepository;
import com.sgg.training.dto.AssignRoutineRequest;
import com.sgg.training.dto.MemberRoutineDto;
import com.sgg.training.dto.RoutineAssignmentDto;
import com.sgg.training.dto.TemplateBlockDto;
import com.sgg.training.dto.TemplateExerciseDto;
import com.sgg.training.entity.RoutineAssignment;
import com.sgg.training.entity.RoutineTemplate;
import com.sgg.training.entity.TemplateBlock;
import com.sgg.training.repository.RoutineAssignmentRepository;
import com.sgg.training.repository.RoutineTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RoutineAssignmentService {

    private final RoutineAssignmentRepository assignmentRepository;
    private final RoutineTemplateRepository templateRepository;
    private final UserRepository userRepository;

    public RoutineAssignmentDto assign(Long gymId, Long assignedBy, AssignRoutineRequest request) {
        if (request.getEndsAt().isBefore(request.getStartsAt())) {
            throw new BusinessException("La fecha de fin debe ser posterior a la de inicio");
        }

        RoutineTemplate template = templateRepository.findById(request.getTemplateId())
                .orElseThrow(() -> new ResourceNotFoundException("Template not found: " + request.getTemplateId()));

        RoutineAssignment assignment = new RoutineAssignment();
        assignment.setGymId(gymId);
        assignment.setTemplateId(request.getTemplateId());
        assignment.setMemberUserId(request.getMemberUserId());
        assignment.setAssignedBy(assignedBy);
        assignment.setStartsAt(request.getStartsAt());
        assignment.setEndsAt(request.getEndsAt());

        RoutineAssignment saved = assignmentRepository.save(assignment);
        User member = userRepository.findById(request.getMemberUserId()).orElse(null);
        return toDto(saved, template, member);
    }

    /**
     * Retorna la rutina activa del miembro (la que está vigente hoy).
     * Si no tiene rutina activa, lanza ResourceNotFoundException.
     */
    @Transactional(readOnly = true)
    public MemberRoutineDto getActiveRoutineForMember(Long gymId, Long memberUserId) {
        LocalDate today = LocalDate.now();

        RoutineAssignment assignment = assignmentRepository
                .findFirstByMemberUserIdAndGymIdAndStartsAtLessThanEqualAndEndsAtGreaterThanEqual(
                        memberUserId, gymId, today, today)
                .orElseThrow(() -> new ResourceNotFoundException("No hay rutina activa asignada"));

        RoutineTemplate template = templateRepository.findById(assignment.getTemplateId())
                .orElseThrow(() -> new ResourceNotFoundException("Template not found"));

        boolean expired = assignment.getEndsAt().isBefore(today);

        List<TemplateBlockDto> blocks = template.getBlocks().stream()
                .map(this::blockToDto)
                .toList();

        return new MemberRoutineDto(
                assignment.getId(),
                template.getName(),
                template.getDescription(),
                assignment.getStartsAt(),
                assignment.getEndsAt(),
                expired,
                blocks
        );
    }

    @Transactional(readOnly = true)
    public List<RoutineAssignmentDto> getAssignmentsForMember(Long gymId, Long memberUserId) {
        return assignmentRepository.findByMemberUserIdAndGymId(memberUserId, gymId).stream()
                .map(a -> {
                    RoutineTemplate t = templateRepository.findById(a.getTemplateId()).orElse(null);
                    User member = userRepository.findById(a.getMemberUserId()).orElse(null);
                    return toDto(a, t, member);
                })
                .toList();
    }

    private RoutineAssignmentDto toDto(RoutineAssignment a, RoutineTemplate template, User member) {
        return new RoutineAssignmentDto(
                a.getId(),
                a.getGymId(),
                a.getTemplateId(),
                template != null ? template.getName() : null,
                a.getMemberUserId(),
                member != null ? member.getFullName() : null,
                member != null ? member.getAvatarUrl() : null,
                a.getAssignedBy(),
                a.getStartsAt(),
                a.getEndsAt()
        );
    }

    private TemplateBlockDto blockToDto(TemplateBlock b) {
        List<TemplateExerciseDto> exercises = b.getExercises().stream()
                .map(e -> new TemplateExerciseDto(
                        e.getId(), e.getName(), e.getSets(), e.getReps(),
                        e.getRestSeconds(), e.getNotes(), e.getSortOrder()))
                .toList();
        return new TemplateBlockDto(b.getId(), b.getName(), b.getDayNumber(), b.getSortOrder(), exercises);
    }
}
