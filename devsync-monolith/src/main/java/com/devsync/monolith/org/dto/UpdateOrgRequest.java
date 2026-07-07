package com.devsync.monolith.org.dto;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UpdateOrgRequest {
    private String name;
    private String description;
    private String subscriptionTier;
}
