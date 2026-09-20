package com.devsync.orguser.user.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserResponse {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String avatarUrl;
    private String roleName;
    private UUID organizationId;
    private boolean active;
    private LocalDateTime createdAt;
}
