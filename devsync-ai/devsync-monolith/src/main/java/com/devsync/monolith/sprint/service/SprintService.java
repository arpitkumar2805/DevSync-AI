package com.devsync.monolith.sprint.service;

import com.devsync.common.dto.PageResponse;
import com.devsync.common.enums.SprintStatus;
import com.devsync.common.exception.BadRequestException;
import com.devsync.common.exception.ResourceNotFoundException;
import com.devsync.monolith.project.repository.ProjectRepository;
import com.devsync.monolith.sprint.dto.*;
import com.devsync.monolith.sprint.entity.Sprint;
import com.devsync.monolith.sprint.repository.SprintRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SprintService {

    private static final Logger log = LoggerFactory.getLogger(SprintService.class);
    private final SprintRepository sprintRepository;
    private final ProjectRepository projectRepository;

    @Transactional
    public SprintResponse create(CreateSprintRequest request) {
        projectRepository.findByIdAndDeletedFalse(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", request.getProjectId()));

        if (request.getEndDate() != null && request.getStartDate() != null
                && request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date must be after start date");
        }

        Sprint sprint = Sprint.builder()
                .projectId(request.getProjectId())
                .name(request.getName())
                .goal(request.getGoal())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();
        sprint = sprintRepository.save(sprint);
        log.info("Sprint created: {} for project {}", sprint.getName(), request.getProjectId());
        return toResponse(sprint);
    }

    public SprintResponse getById(UUID id) {
        Sprint sprint = sprintRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint", "id", id));
        return toResponse(sprint);
    }

    @Transactional
    public SprintResponse update(UUID id, UpdateSprintRequest request) {
        Sprint sprint = sprintRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint", "id", id));

        if (sprint.getStatus() != SprintStatus.PLANNED) {
            throw new BadRequestException("Can only update sprints in PLANNED status");
        }

        if (request.getName() != null) sprint.setName(request.getName());
        if (request.getGoal() != null) sprint.setGoal(request.getGoal());
        if (request.getStartDate() != null) sprint.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) sprint.setEndDate(request.getEndDate());

        sprint = sprintRepository.save(sprint);
        return toResponse(sprint);
    }

    @Transactional
    public void delete(UUID id) {
        Sprint sprint = sprintRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint", "id", id));

        if (sprint.getStatus() != SprintStatus.PLANNED) {
            throw new BadRequestException("Can only delete sprints in PLANNED status");
        }

        sprint.softDelete();
        sprintRepository.save(sprint);
        log.info("Sprint soft-deleted: {}", id);
    }

    public PageResponse<SprintResponse> listByProject(UUID projectId, Pageable pageable) {
        Page<Sprint> page = sprintRepository.findByProjectIdAndDeletedFalse(projectId, pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    @Transactional
    public SprintResponse startSprint(UUID id) {
        Sprint sprint = sprintRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint", "id", id));

        if (sprint.getStatus() != SprintStatus.PLANNED) {
            throw new BadRequestException("Can only start sprints in PLANNED status");
        }

        // Check no other active sprint for same project
        sprintRepository.findByProjectIdAndStatusAndDeletedFalse(sprint.getProjectId(), SprintStatus.ACTIVE)
                .ifPresent(s -> {
                    throw new BadRequestException("Project already has an active sprint: " + s.getName());
                });

        sprint.setStatus(SprintStatus.ACTIVE);
        if (sprint.getStartDate() == null) {
            sprint.setStartDate(LocalDate.now());
        }
        sprint = sprintRepository.save(sprint);
        log.info("Sprint started: {}", sprint.getName());
        return toResponse(sprint);
    }

    @Transactional
    public SprintResponse closeSprint(UUID id) {
        Sprint sprint = sprintRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint", "id", id));

        if (sprint.getStatus() != SprintStatus.ACTIVE) {
            throw new BadRequestException("Can only close sprints in ACTIVE status");
        }

        sprint.setStatus(SprintStatus.CLOSED);
        if (sprint.getEndDate() == null) {
            sprint.setEndDate(LocalDate.now());
        }
        sprint = sprintRepository.save(sprint);
        log.info("Sprint closed: {}", sprint.getName());
        return toResponse(sprint);
    }

    private SprintResponse toResponse(Sprint sprint) {
        return SprintResponse.builder()
                .id(sprint.getId())
                .projectId(sprint.getProjectId())
                .name(sprint.getName())
                .goal(sprint.getGoal())
                .startDate(sprint.getStartDate())
                .endDate(sprint.getEndDate())
                .status(sprint.getStatus())
                .createdAt(sprint.getCreatedAt())
                .taskCount(0)
                .completedTaskCount(0)
                .build();
    }
}
