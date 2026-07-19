package com.devsync.project.project.dto;

import lombok.*;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ProjectMemberResponse {
    private UUID id;
    private UUID projectId;
    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
}
