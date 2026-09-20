package com.devsync.monolith.auditlog.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AuditLogResponse {
    private UUID id;
    private UUID userId;
    private String userEmail;
    private String action;
    private String entityType;
    private UUID entityId;
    private String ipAddress;
    private String details;
    private LocalDateTime createdAt;
}
