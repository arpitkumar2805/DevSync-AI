package com.devsync.monolith.team.dto;

import com.devsync.common.enums.TeamMemberRole;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AddTeamMemberRequest {
    @NotNull(message = "User ID is required")
    private UUID userId;
    private TeamMemberRole role;
}
