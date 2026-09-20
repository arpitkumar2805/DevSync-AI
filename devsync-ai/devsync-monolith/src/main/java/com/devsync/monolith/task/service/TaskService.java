package com.devsync.monolith.task.service;

import com.devsync.common.dto.PageResponse;
import com.devsync.common.exception.AccessDeniedException;
import com.devsync.common.exception.BadRequestException;
import com.devsync.common.exception.DuplicateResourceException;
import com.devsync.common.exception.ResourceNotFoundException;
import com.devsync.monolith.auth.entity.User;
import com.devsync.monolith.auth.repository.UserRepository;
import com.devsync.monolith.project.entity.Project;
import com.devsync.monolith.project.repository.ProjectRepository;
import com.devsync.monolith.task.dto.*;
import com.devsync.monolith.task.entity.Task;
import com.devsync.monolith.task.entity.TaskDependency;
import com.devsync.monolith.task.repository.TaskDependencyRepository;
import com.devsync.monolith.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);
    private final TaskRepository taskRepository;
    private final TaskDependencyRepository taskDependencyRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Transactional
    public TaskResponse create(CreateTaskRequest request, UUID reporterId) {
        requireOwnedProject(request.getProjectId());

        Task task = Task.builder()
                .projectId(request.getProjectId())
                .sprintId(request.getSprintId())
                .parentId(request.getParentId())
                .reporterId(reporterId)
                .title(request.getTitle())
                .description(request.getDescription())
                .type(request.getType())
                .priority(request.getPriority() != null ? request.getPriority() : com.devsync.common.enums.Priority.MEDIUM)
                .storyPoints(request.getStoryPoints())
                .dueDate(request.getDueDate())
                .labels(request.getLabels())
                .build();
        task = taskRepository.save(task);
        log.info("Task created: {} in project {}", task.getTitle(), request.getProjectId());
        return toResponse(task);
    }

    public TaskResponse getById(UUID id) {
        Task task = requireOwnedTask(id);
        return toResponse(task);
    }

    @Transactional
    public TaskResponse update(UUID id, UpdateTaskRequest request) {
        Task task = requireOwnedTask(id);

        if (request.getTitle() != null) task.setTitle(request.getTitle());
        if (request.getDescription() != null) task.setDescription(request.getDescription());
        if (request.getPriority() != null) task.setPriority(request.getPriority());
        if (request.getStoryPoints() != null) task.setStoryPoints(request.getStoryPoints());
        if (request.getDueDate() != null) task.setDueDate(request.getDueDate());
        if (request.getLabels() != null) task.setLabels(request.getLabels());

        task = taskRepository.save(task);
        return toResponse(task);
    }

    @Transactional
    public TaskResponse updateStatus(UUID id, UpdateTaskStatusRequest request) {
        Task task = requireOwnedTask(id);

        if (!task.getStatus().canTransitionTo(request.getStatus())) {
            throw new BadRequestException("Invalid status transition from " + task.getStatus() + " to " + request.getStatus());
        }

        task.setStatus(request.getStatus());
        task = taskRepository.save(task);
        log.info("Task {} status changed to {}", id, request.getStatus());
        return toResponse(task);
    }

    @Transactional
    public TaskResponse assignTask(UUID id, AssignTaskRequest request) {
        Task task = requireOwnedTask(id);
        task.setAssigneeId(request.getAssigneeId());
        task = taskRepository.save(task);
        return toResponse(task);
    }

    @Transactional
    public void delete(UUID id) {
        Task task = requireOwnedTask(id);
        task.softDelete();
        taskRepository.save(task);
        log.info("Task soft-deleted: {}", id);
    }

    public PageResponse<TaskResponse> listByProject(UUID projectId, Pageable pageable) {
        requireOwnedProject(projectId);
        Page<Task> page = taskRepository.findByProjectIdAndDeletedFalse(projectId, pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    public PageResponse<TaskResponse> listBySprint(UUID sprintId, Pageable pageable) {
        Page<Task> page = taskRepository.findBySprintIdAndDeletedFalse(sprintId, pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    public PageResponse<TaskResponse> listByAssignee(UUID assigneeId, Pageable pageable) {
        Page<Task> page = taskRepository.findByAssigneeIdAndDeletedFalse(assigneeId, pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    /**
     * Lists tasks with no explicit filter, scoped to the calling user's own organization
     * (via their projects) rather than every task in the system, to avoid leaking other
     * tenants' data.
     */
    public PageResponse<TaskResponse> listAll(Pageable pageable) {
        UUID orgId = currentUserOrgId();
        List<UUID> projectIds = orgId != null
                ? projectRepository.findIdsByOrganizationId(orgId)
                : Collections.emptyList();
        if (projectIds.isEmpty()) {
            return PageResponse.of(Page.empty(pageable));
        }
        Page<Task> page = taskRepository.findByProjectIdInAndDeletedFalse(projectIds, pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    @Transactional
    public TaskDependencyResponse addDependency(UUID taskId, AddDependencyRequest request) {
        if (taskId.equals(request.getDependsOnTaskId())) {
            throw new BadRequestException("A task cannot depend on itself");
        }
        if (taskDependencyRepository.existsByTaskIdAndDependsOnTaskId(taskId, request.getDependsOnTaskId())) {
            throw new DuplicateResourceException("TaskDependency", "dependsOnTaskId", request.getDependsOnTaskId());
        }

        TaskDependency dep = TaskDependency.builder()
                .taskId(taskId)
                .dependsOnTaskId(request.getDependsOnTaskId())
                .dependencyType(request.getDependencyType() != null ? request.getDependencyType() : "BLOCKS")
                .build();
        dep = taskDependencyRepository.save(dep);

        return TaskDependencyResponse.builder()
                .id(dep.getId())
                .taskId(dep.getTaskId())
                .dependsOnTaskId(dep.getDependsOnTaskId())
                .dependencyType(dep.getDependencyType())
                .build();
    }

    @Transactional
    public void removeDependency(UUID taskId, UUID dependsOnTaskId) {
        taskDependencyRepository.deleteByTaskIdAndDependsOnTaskId(taskId, dependsOnTaskId);
    }

    public List<TaskDependencyResponse> getDependencies(UUID taskId) {
        return taskDependencyRepository.findByTaskId(taskId).stream()
                .map(dep -> TaskDependencyResponse.builder()
                        .id(dep.getId())
                        .taskId(dep.getTaskId())
                        .dependsOnTaskId(dep.getDependsOnTaskId())
                        .dependencyType(dep.getDependencyType())
                        .build())
                .collect(Collectors.toList());
    }

    public List<TaskResponse> getSubtasks(UUID parentId) {
        return taskRepository.findByParentIdAndDeletedFalse(parentId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Loads a project and verifies it belongs to the calling user's organization,
     * so one tenant cannot create/list tasks against another tenant's project.
     */
    private Project requireOwnedProject(UUID projectId) {
        Project project = projectRepository.findByIdAndDeletedFalse(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
        UUID callerOrgId = currentUserOrgId();
        if (callerOrgId == null || !callerOrgId.equals(project.getOrganizationId())) {
            throw new AccessDeniedException("You do not have access to this project");
        }
        return project;
    }

    /**
     * Loads a task and verifies its project belongs to the calling user's organization,
     * so one tenant cannot read/modify another tenant's task by guessing its ID.
     */
    private Task requireOwnedTask(UUID id) {
        Task task = taskRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));
        requireOwnedProject(task.getProjectId());
        return task;
    }

    private UUID currentUserOrgId() {
        String currentUserIdStr = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        if (currentUserIdStr == null || currentUserIdStr.equals("anonymousUser")) {
            return null;
        }
        UUID currentUserId = UUID.fromString(currentUserIdStr);
        User currentUser = userRepository.findByIdAndDeletedFalse(currentUserId).orElse(null);
        return currentUser != null ? currentUser.getOrganizationId() : null;
    }

    private TaskResponse toResponse(Task task) {
        int subtaskCount = taskRepository.findByParentIdAndDeletedFalse(task.getId()).size();
        return TaskResponse.builder()
                .id(task.getId())
                .projectId(task.getProjectId())
                .sprintId(task.getSprintId())
                .parentId(task.getParentId())
                .assigneeId(task.getAssigneeId())
                .reporterId(task.getReporterId())
                .title(task.getTitle())
                .description(task.getDescription())
                .type(task.getType())
                .status(task.getStatus())
                .priority(task.getPriority())
                .storyPoints(task.getStoryPoints())
                .dueDate(task.getDueDate())
                .labels(task.getLabels())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .subtaskCount(subtaskCount)
                .build();
    }
}
