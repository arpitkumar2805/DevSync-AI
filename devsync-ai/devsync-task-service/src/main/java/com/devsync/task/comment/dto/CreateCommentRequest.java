package com.devsync.task.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CreateCommentRequest {
    @NotBlank(message = "Content is required")
    @Size(max = 5000, message = "Content must be at most 5000 characters")
    private String content;
    private UUID parentId;
}
