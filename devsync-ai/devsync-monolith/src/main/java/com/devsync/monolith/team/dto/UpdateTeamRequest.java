package com.devsync.monolith.team.dto;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UpdateTeamRequest {
    private String name;
    private String description;
}
