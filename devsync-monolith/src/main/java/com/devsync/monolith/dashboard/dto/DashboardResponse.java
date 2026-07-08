package com.devsync.monolith.dashboard.dto;

import lombok.*;

import java.util.Map;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DashboardResponse {
    private UUID projectId;
    private long totalTasks;
    private long completedTasks;
    private long pendingTasks;
    
    // Key: Status (TODO, IN_PROGRESS, etc.), Value: Count
    private Map<String, Long> taskDistributionByStatus;
    
    // Key: Priority (HIGH, LOW, etc.), Value: Count
    private Map<String, Long> taskDistributionByPriority;
    
    // Key: Assignee UUID string, Value: Count
    private Map<String, Long> taskDistributionByAssignee;

    // Optional sprint-specific metrics
    private UUID activeSprintId;
    private Integer sprintTotalStoryPoints;
    private Integer sprintCompletedStoryPoints;
}
