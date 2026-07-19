package com.devsync.monolith.project.service;

import com.devsync.common.dto.PageResponse;
import com.devsync.common.exception.BadRequestException;
import com.devsync.common.exception.DuplicateResourceException;
import com.devsync.common.exception.ResourceNotFoundException;
import com.devsync.monolith.auth.entity.User;
import com.devsync.monolith.auth.repository.UserRepository;
import com.devsync.monolith.project.dto.*;
import com.devsync.monolith.project.entity.Project;
import com.devsync.monolith.project.entity.ProjectMember;
import com.devsync.monolith.project.repository.ProjectMemberRepository;
import com.devsync.monolith.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private static final Logger log = LoggerFactory.getLogger(ProjectService.class);
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    @Transactional
    public ProjectResponse create(CreateProjectRequest request, UUID orgId) {
        if (orgId == null) {
            String currentUserIdStr = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
            if (currentUserIdStr != null && !currentUserIdStr.equals("anonymousUser")) {
                UUID currentUserId = UUID.fromString(currentUserIdStr);
                User currentUser = userRepository.findByIdAndDeletedFalse(currentUserId).orElse(null);
                if (currentUser != null) {
                    orgId = currentUser.getOrganizationId();
                }
            }
        }
        Project project = Project.builder()
                .organizationId(orgId)
                .name(request.getName())
                .description(request.getDescription())
                .teamId(request.getTeamId())
                .build();
        project = projectRepository.save(project);
        log.info("Project created: {} in org {}", project.getName(), orgId);
        return toResponse(project);
    }

    public ProjectResponse getById(UUID id) {
        Project project = projectRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        return toResponse(project);
    }

    @Transactional
    public ProjectResponse update(UUID id, UpdateProjectRequest request) {
        Project project = projectRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        if (request.getName() != null) project.setName(request.getName());
        if (request.getDescription() != null) project.setDescription(request.getDescription());
        project = projectRepository.save(project);
        return toResponse(project);
    }

    @Transactional
    public ProjectResponse updateStatus(UUID id, UpdateProjectStatusRequest request) {
        Project project = projectRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        if (!project.getStatus().canTransitionTo(request.getStatus())) {
            throw new BadRequestException("Invalid status transition from " + project.getStatus() + " to " + request.getStatus());
        }
        project.setStatus(request.getStatus());
        project = projectRepository.save(project);
        return toResponse(project);
    }

    @Transactional
    public void delete(UUID id) {
        Project project = projectRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        project.softDelete();
        projectRepository.save(project);
        log.info("Project soft-deleted: {}", id);
    }

    public PageResponse<ProjectResponse> listByOrganization(UUID orgId, Pageable pageable) {
        if (orgId == null) {
            String currentUserIdStr = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
            if (currentUserIdStr != null && !currentUserIdStr.equals("anonymousUser")) {
                UUID currentUserId = UUID.fromString(currentUserIdStr);
                User currentUser = userRepository.findByIdAndDeletedFalse(currentUserId).orElse(null);
                if (currentUser != null) {
                    orgId = currentUser.getOrganizationId();
                }
            }
        }
        Page<Project> page = projectRepository.findByOrganizationIdAndDeletedFalse(orgId, pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    @Transactional
    public ProjectMemberResponse addMember(UUID projectId, AddProjectMemberRequest request) {
        projectRepository.findByIdAndDeletedFalse(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
        User user = userRepository.findByIdAndDeletedFalse(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));
        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, request.getUserId())) {
            throw new DuplicateResourceException("ProjectMember", "userId", request.getUserId());
        }
        ProjectMember member = ProjectMember.builder()
                .projectId(projectId)
                .userId(request.getUserId())
                .role(request.getRole() != null ? request.getRole() : "MEMBER")
                .build();
        member = projectMemberRepository.save(member);
        return toMemberResponse(member, user);
    }

    @Transactional
    public void removeMember(UUID projectId, UUID userId) {
        projectMemberRepository.deleteByProjectIdAndUserId(projectId, userId);
    }

    public PageResponse<ProjectMemberResponse> getMembers(UUID projectId, Pageable pageable) {
        Page<ProjectMember> page = projectMemberRepository.findByProjectId(projectId, pageable);
        return PageResponse.of(page.map(pm -> {
            User user = userRepository.findByIdAndDeletedFalse(pm.getUserId()).orElse(null);
            return toMemberResponse(pm, user);
        }));
    }

    private ProjectResponse toResponse(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .organizationId(project.getOrganizationId())
                .name(project.getName())
                .description(project.getDescription())
                .status(project.getStatus())
                .teamId(project.getTeamId())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .memberCount((int) projectMemberRepository.countByProjectId(project.getId()))
                .build();
    }

    private ProjectMemberResponse toMemberResponse(ProjectMember pm, User user) {
        return ProjectMemberResponse.builder()
                .id(pm.getId())
                .projectId(pm.getProjectId())
                .userId(pm.getUserId())
                .email(user != null ? user.getEmail() : null)
                .firstName(user != null ? user.getFirstName() : null)
                .lastName(user != null ? user.getLastName() : null)
                .role(pm.getRole())
                .build();
    }
}
