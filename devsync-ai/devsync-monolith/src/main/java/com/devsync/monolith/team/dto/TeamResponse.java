package com.devsync.monolith.team.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TeamResponse {
    private UUID id;
    private UUID organizationId;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private int memberCount;
}
