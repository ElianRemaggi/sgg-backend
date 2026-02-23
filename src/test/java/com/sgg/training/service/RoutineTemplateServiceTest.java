package com.sgg.training.service;

import com.sgg.common.exception.ResourceNotFoundException;
import com.sgg.identity.entity.User;
import com.sgg.identity.repository.UserRepository;
import com.sgg.training.dto.CreateTemplateRequest;
import com.sgg.training.dto.RoutineTemplateDto;
import com.sgg.training.entity.RoutineTemplate;
import com.sgg.training.repository.RoutineTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class RoutineTemplateServiceTest {

    @Mock
    private RoutineTemplateRepository templateRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RoutineTemplateService routineTemplateService;

    private static final Long GYM_ID = 10L;
    private static final Long COACH_ID = 2L;
    private static final Long TEMPLATE_ID = 1L;

    private RoutineTemplate savedTemplate;

    @BeforeEach
    void setUp() {
        savedTemplate = new RoutineTemplate();
        savedTemplate.setId(TEMPLATE_ID);
        savedTemplate.setGymId(GYM_ID);
        savedTemplate.setName("Rutina A");
        savedTemplate.setDescription("Descripción rutina A");
        savedTemplate.setCreatedBy(COACH_ID);
        savedTemplate.setBlocks(new ArrayList<>());

        User coachUser = new User();
        coachUser.setId(COACH_ID);
        coachUser.setFullName("Coach Juan");
        coachUser.setEmail("juan@gym.com");
        lenient().when(userRepository.findById(COACH_ID)).thenReturn(Optional.of(coachUser));
    }

    // --- create ---

    @Test
    void create_success_withBlocksAndExercises() {
        CreateTemplateRequest.ExerciseRequest exReq = new CreateTemplateRequest.ExerciseRequest();
        exReq.setName("Press banca");
        exReq.setSets(4);
        exReq.setReps("8-10");

        CreateTemplateRequest.BlockRequest blockReq = new CreateTemplateRequest.BlockRequest();
        blockReq.setName("Día 1 — Pecho");
        blockReq.setDayNumber(1);
        blockReq.setExercises(List.of(exReq));

        CreateTemplateRequest request = new CreateTemplateRequest();
        request.setName("Rutina A");
        request.setDescription("Descripción");
        request.setBlocks(List.of(blockReq));

        when(templateRepository.save(any(RoutineTemplate.class))).thenAnswer(inv -> {
            RoutineTemplate t = inv.getArgument(0);
            t.setId(TEMPLATE_ID);
            return t;
        });

        RoutineTemplateDto result = routineTemplateService.create(GYM_ID, COACH_ID, request);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Rutina A");
        assertThat(result.getBlocks()).hasSize(1);
        assertThat(result.getBlocks().get(0).getExercises()).hasSize(1);
        assertThat(result.getBlocks().get(0).getExercises().get(0).getName()).isEqualTo("Press banca");
        verify(templateRepository).save(any(RoutineTemplate.class));
    }

    @Test
    void create_assignsGymIdAndCreatedBy() {
        CreateTemplateRequest.ExerciseRequest exReq = new CreateTemplateRequest.ExerciseRequest();
        exReq.setName("Sentadilla");
        exReq.setSets(3);
        exReq.setReps("12");

        CreateTemplateRequest.BlockRequest blockReq = new CreateTemplateRequest.BlockRequest();
        blockReq.setName("Bloque piernas");
        blockReq.setDayNumber(2);
        blockReq.setExercises(List.of(exReq));

        CreateTemplateRequest request = new CreateTemplateRequest();
        request.setName("Rutina Piernas");
        request.setBlocks(List.of(blockReq));

        when(templateRepository.save(any(RoutineTemplate.class))).thenAnswer(inv -> {
            RoutineTemplate t = inv.getArgument(0);
            t.setId(TEMPLATE_ID);
            return t;
        });

        routineTemplateService.create(GYM_ID, COACH_ID, request);

        verify(templateRepository).save(argThat(t ->
                t.getGymId().equals(GYM_ID) && t.getCreatedBy().equals(COACH_ID)
        ));
    }

    // --- listByGym ---

    @Test
    void listByGym_returnsAllTemplatesOfGym() {
        when(templateRepository.findByGymId(GYM_ID)).thenReturn(List.of(savedTemplate));

        List<RoutineTemplateDto> result = routineTemplateService.listByGym(GYM_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Rutina A");
    }

    @Test
    void listByGym_returnsEmptyList_whenNoTemplates() {
        when(templateRepository.findByGymId(GYM_ID)).thenReturn(List.of());

        List<RoutineTemplateDto> result = routineTemplateService.listByGym(GYM_ID);

        assertThat(result).isEmpty();
    }

    // --- getById ---

    @Test
    void getById_returnsDto_whenTemplateExists() {
        when(templateRepository.findById(TEMPLATE_ID)).thenReturn(Optional.of(savedTemplate));

        RoutineTemplateDto result = routineTemplateService.getById(TEMPLATE_ID);

        assertThat(result.getId()).isEqualTo(TEMPLATE_ID);
        assertThat(result.getName()).isEqualTo("Rutina A");
    }

    @Test
    void getById_throwsResourceNotFoundException_whenNotFound() {
        when(templateRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> routineTemplateService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // --- delete ---

    @Test
    void delete_success_deletesTemplate() {
        when(templateRepository.findById(TEMPLATE_ID)).thenReturn(Optional.of(savedTemplate));

        routineTemplateService.delete(TEMPLATE_ID);

        verify(templateRepository).delete(savedTemplate);
    }

    @Test
    void delete_throwsResourceNotFoundException_whenNotFound() {
        when(templateRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> routineTemplateService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
