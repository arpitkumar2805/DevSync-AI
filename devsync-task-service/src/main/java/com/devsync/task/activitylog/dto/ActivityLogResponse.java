package com.devsync.task.activitylog.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ActivityLogResponse {
    private UUID id;
    private String entityType;
    private UUID entityId;
    private String action;
    private String changes;
    private UUID userId;
    private String userEmail;
    private LocalDateTime createdAt;
}
