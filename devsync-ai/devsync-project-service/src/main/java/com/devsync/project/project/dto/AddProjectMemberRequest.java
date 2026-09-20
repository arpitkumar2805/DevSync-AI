package com.devsync.project.project.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AddProjectMemberRequest {
    @NotNull(message = "User ID is required")
    private UUID userId;
    private String role;
}
