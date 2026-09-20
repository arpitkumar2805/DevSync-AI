package com.devsync.monolith.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AssignRoleRequest {
    @NotBlank(message = "Role name is required")
    private String roleName;
}
