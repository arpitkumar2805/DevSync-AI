package com.devsync.monolith.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateProjectRequest {

    @NotBlank(message = "Prompt cannot be empty")
    private String prompt;

    @NotNull(message = "Organization ID is required")
    private UUID organizationId;
}
