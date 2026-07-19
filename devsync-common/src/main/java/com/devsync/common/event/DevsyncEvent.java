package com.devsync.common.event;

import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DevsyncEvent implements Serializable {
    private String eventType;
    private UUID entityId;
    private String entityType;
    private UUID actorUserId;
    private String actorEmail;
    private UUID organizationId;
    private String title;
    private String message;
    private LocalDateTime timestamp;
}
