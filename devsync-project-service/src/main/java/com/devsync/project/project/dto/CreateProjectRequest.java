package com.devsync.project.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CreateProjectRequest {
    @NotBlank(message = "Project name is required")
    @Size(max = 200, message = "Name must be at most 200 characters")
    private String name;
    private String description;
    private UUID teamId;
}
