package com.devsync.task.task.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AddDependencyRequest {
    @NotNull(message = "Depends-on task ID is required")
    private UUID dependsOnTaskId;
    private String dependencyType;
}
