package com.devsync.monolith.org.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CreateOrgRequest {
    @NotBlank(message = "Organization name is required")
    @Size(max = 100, message = "Name must be at most 100 characters")
    private String name;
    private String description;
}
