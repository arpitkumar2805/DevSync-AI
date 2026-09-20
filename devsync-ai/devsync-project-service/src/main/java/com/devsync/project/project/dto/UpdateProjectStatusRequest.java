package com.devsync.project.project.dto;

import com.devsync.common.enums.ProjectStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UpdateProjectStatusRequest {
    @NotNull(message = "Status is required")
    private ProjectStatus status;
}
