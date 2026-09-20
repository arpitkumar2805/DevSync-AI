package com.devsync.task.sprint.dto;

import com.devsync.common.enums.SprintStatus;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SprintResponse {
    private UUID id;
    private UUID projectId;
    private String name;
    private String goal;
    private LocalDate startDate;
    private LocalDate endDate;
    private SprintStatus status;
    private LocalDateTime createdAt;
    private int taskCount;
    private int completedTaskCount;
}
