package com.devsync.monolith.sprint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CreateSprintRequest {
    @NotNull(message = "Project ID is required")
    private UUID projectId;
    @NotBlank(message = "Sprint name is required")
    private String name;
    private String goal;
    private LocalDate startDate;
    private LocalDate endDate;
}
