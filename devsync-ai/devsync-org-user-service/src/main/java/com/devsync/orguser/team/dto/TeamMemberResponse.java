package com.devsync.orguser.team.dto;

import com.devsync.common.enums.TeamMemberRole;
import lombok.*;

import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TeamMemberResponse {
    private UUID id;
    private UUID teamId;
    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private TeamMemberRole role;
}
