package com.devsync.task.task.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AssignTaskRequest {
    @NotNull(message = "Assignee ID is required")
    private UUID assigneeId;
}
