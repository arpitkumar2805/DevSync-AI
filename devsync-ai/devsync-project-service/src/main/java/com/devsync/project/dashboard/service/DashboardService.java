package com.devsync.project.dashboard.service;

import com.devsync.common.exception.ResourceNotFoundException;
import com.devsync.project.dashboard.dto.DashboardResponse;
import com.devsync.project.project.entity.Project;
import com.devsync.project.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProjectRepository projectRepository;

    // Task data lives in devsync-task-service's own database. In a real deployment this method
    // should call that service (Feign/REST) to fetch task metrics; until that client exists,
    // it returns zeroed task metrics so the endpoint stays functional for project-level data.
    @Cacheable(value = "projectDashboard", key = "#projectId")
    public DashboardResponse getProjectDashboard(UUID projectId) {
        projectRepository.findByIdAndDeletedFalse(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        return DashboardResponse.builder()
                .projectId(projectId)
                .totalTasks(0)
                .completedTasks(0)
                .pendingTasks(0)
                .taskDistributionByStatus(Collections.emptyMap())
                .taskDistributionByPriority(Collections.emptyMap())
                .taskDistributionByAssignee(Collections.emptyMap())
                .build();
    }
}
