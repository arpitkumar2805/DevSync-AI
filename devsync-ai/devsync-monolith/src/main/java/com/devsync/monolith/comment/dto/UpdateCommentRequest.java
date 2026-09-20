package com.devsync.monolith.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UpdateCommentRequest {
    @NotBlank(message = "Content is required")
    @Size(max = 5000, message = "Content must be at most 5000 characters")
    private String content;
}
