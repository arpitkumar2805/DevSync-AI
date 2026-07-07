package com.devsync.monolith.team.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CreateTeamRequest {
    @NotBlank(message = "Team name is required")
    private String name;
    private String description;
}
