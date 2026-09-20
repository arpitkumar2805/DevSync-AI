package com.devsync.task.task.dto;

import com.devsync.common.enums.Priority;
import com.devsync.common.enums.TaskStatus;
import com.devsync.common.enums.TaskType;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TaskResponse {
    private UUID id;
    private UUID projectId;
    private UUID sprintId;
    private UUID parentId;
    private UUID assigneeId;
    private UUID reporterId;
    private String title;
    private String description;
    private TaskType type;
    private TaskStatus status;
    private Priority priority;
    private Integer storyPoints;
    private LocalDate dueDate;
    private String labels;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int subtaskCount;
}
