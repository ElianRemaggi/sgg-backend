package com.sgg.training.service;

import com.sgg.common.exception.BusinessException;
import com.sgg.common.exception.ResourceNotFoundException;
import com.sgg.training.dto.AssignRoutineRequest;
import com.sgg.training.dto.MemberRoutineDto;
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

    public RoutineAssignment assign(Long gymId, Long assignedBy, AssignRoutineRequest request) {
        if (request.getEndsAt().isBefore(request.getStartsAt())) {
            throw new BusinessException("La fecha de fin debe ser posterior a la de inicio");
        }

        templateRepository.findById(request.getTemplateId())
                .orElseThrow(() -> new ResourceNotFoundException("Template not found: " + request.getTemplateId()));

        RoutineAssignment assignment = new RoutineAssignment();
        assignment.setGymId(gymId);
        assignment.setTemplateId(request.getTemplateId());
        assignment.setMemberUserId(request.getMemberUserId());
        assignment.setAssignedBy(assignedBy);
        assignment.setStartsAt(request.getStartsAt());
        assignment.setEndsAt(request.getEndsAt());

        return assignmentRepository.save(assignment);
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
    public List<RoutineAssignment> getAssignmentsForMember(Long gymId, Long memberUserId) {
        return assignmentRepository.findByMemberUserIdAndGymId(memberUserId, gymId);
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
