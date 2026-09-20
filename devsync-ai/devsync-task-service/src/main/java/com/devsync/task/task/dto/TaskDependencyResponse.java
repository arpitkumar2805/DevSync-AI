package com.devsync.task.task.dto;

import lombok.*;

import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TaskDependencyResponse {
    private UUID id;
    private UUID taskId;
    private UUID dependsOnTaskId;
    private String dependencyType;
}
