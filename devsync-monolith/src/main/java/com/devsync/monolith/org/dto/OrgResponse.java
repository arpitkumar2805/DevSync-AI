package com.devsync.monolith.org.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OrgResponse {
    private UUID id;
    private String name;
    private String slug;
    private String description;
    private String subscriptionTier;
    private LocalDateTime createdAt;
}
