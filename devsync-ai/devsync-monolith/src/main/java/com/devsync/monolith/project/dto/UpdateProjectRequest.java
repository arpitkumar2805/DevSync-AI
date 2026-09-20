package com.devsync.monolith.project.dto;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UpdateProjectRequest {
    private String name;
    private String description;
}
