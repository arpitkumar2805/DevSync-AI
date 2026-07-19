package com.devsync.project.project.dto;

import com.devsync.common.enums.ProjectStatus;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ProjectResponse {
    private UUID id;
    private UUID organizationId;
    private String name;
    private String description;
    private ProjectStatus status;
    private UUID teamId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int memberCount;
}
