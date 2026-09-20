package com.devsync.monolith.notification.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class NotificationResponse {
    private UUID id;
    private UUID userId;
    private String type;
    private String title;
    private String message;
    private boolean read;
    private String entityType;
    private UUID entityId;
    private LocalDateTime createdAt;
}
