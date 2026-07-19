package com.devsync.project.dashboard.service;

import com.devsync.common.enums.TaskStatus;
import com.devsync.common.exception.ResourceNotFoundException;
import com.devsync.project.dashboard.dto.DashboardResponse;
import com.devsync.project.project.entity.Project;
import com.devsync.project.project.repository.ProjectRepository;
import com.devsync.project.task.entity.Task;
import com.devsync.project.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

    @Cacheable(value = "projectDashboard", key = "#projectId")
    public DashboardResponse getProjectDashboard(UUID projectId) {
        Project project = projectRepository.findByIdAndDeletedFalse(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        List<Task> allTasks = taskRepository.findByProjectIdAndDeletedFalse(projectId);

        long totalTasks = allTasks.size();
        long completedTasks = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();
        long pendingTasks = totalTasks - completedTasks;

        Map<String, Long> byStatus = allTasks.stream()
                .collect(Collectors.groupingBy(t -> t.getStatus().name(), Collectors.counting()));

        Map<String, Long> byPriority = allTasks.stream()
                .collect(Collectors.groupingBy(t -> t.getPriority().name(), Collectors.counting()));

        Map<String, Long> byAssignee = allTasks.stream()
                .filter(t -> t.getAssigneeId() != null)
                .collect(Collectors.groupingBy(t -> t.getAssigneeId().toString(), Collectors.counting()));

        return DashboardResponse.builder()
                .projectId(projectId)
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .pendingTasks(pendingTasks)
                .taskDistributionByStatus(byStatus)
                .taskDistributionByPriority(byPriority)
                .taskDistributionByAssignee(byAssignee)
                .build();
    }
}
