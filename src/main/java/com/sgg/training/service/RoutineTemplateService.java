package com.sgg.training.service;

import com.sgg.common.exception.ResourceNotFoundException;
import com.sgg.common.security.TenantContext;
import com.sgg.training.dto.*;
import com.sgg.training.entity.RoutineTemplate;
import com.sgg.training.entity.TemplateBlock;
import com.sgg.training.entity.TemplateExercise;
import com.sgg.training.repository.RoutineTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Transactional
public class RoutineTemplateService {

    private final RoutineTemplateRepository templateRepository;

    public RoutineTemplateDto create(Long gymId, Long createdBy, CreateTemplateRequest request) {
        RoutineTemplate template = new RoutineTemplate();
        template.setGymId(gymId);
        template.setName(request.getName());
        template.setDescription(request.getDescription());
        template.setCreatedBy(createdBy);

        AtomicInteger blockOrder = new AtomicInteger(0);
        for (CreateTemplateRequest.BlockRequest blockReq : request.getBlocks()) {
            TemplateBlock block = new TemplateBlock();
            block.setTemplate(template);
            block.setName(blockReq.getName());
            block.setDayNumber(blockReq.getDayNumber());
            block.setSortOrder(blockReq.getSortOrder() != null ? blockReq.getSortOrder() : blockOrder.getAndIncrement());

            AtomicInteger exOrder = new AtomicInteger(0);
            for (CreateTemplateRequest.ExerciseRequest exReq : blockReq.getExercises()) {
                TemplateExercise exercise = new TemplateExercise();
                exercise.setBlock(block);
                exercise.setName(exReq.getName());
                exercise.setSets(exReq.getSets());
                exercise.setReps(exReq.getReps());
                exercise.setRestSeconds(exReq.getRestSeconds());
                exercise.setNotes(exReq.getNotes());
                exercise.setSortOrder(exReq.getSortOrder() != null ? exReq.getSortOrder() : exOrder.getAndIncrement());
                block.getExercises().add(exercise);
            }
            template.getBlocks().add(block);
        }

        return toDto(templateRepository.save(template));
    }

    @Transactional(readOnly = true)
    public List<RoutineTemplateDto> listByGym(Long gymId) {
        return templateRepository.findByGymId(gymId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public RoutineTemplateDto getById(Long templateId) {
        return toDto(templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found: " + templateId)));
    }

    public void delete(Long templateId) {
        RoutineTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found: " + templateId));
        templateRepository.delete(template);
    }

    public RoutineTemplateDto toDto(RoutineTemplate t) {
        List<TemplateBlockDto> blocks = t.getBlocks().stream()
                .map(b -> new TemplateBlockDto(
                        b.getId(), b.getName(), b.getDayNumber(), b.getSortOrder(),
                        b.getExercises().stream()
                                .map(e -> new TemplateExerciseDto(
                                        e.getId(), e.getName(), e.getSets(), e.getReps(),
                                        e.getRestSeconds(), e.getNotes(), e.getSortOrder()))
                                .toList()))
                .toList();
        return new RoutineTemplateDto(t.getId(), t.getName(), t.getDescription(),
                t.getCreatedBy(), blocks, t.getCreatedAt());
    }
}
