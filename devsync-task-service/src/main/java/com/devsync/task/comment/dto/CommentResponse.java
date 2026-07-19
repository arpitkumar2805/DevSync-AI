package com.devsync.task.comment.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CommentResponse {
    private UUID id;
    private UUID taskId;
    private UUID userId;
    private String userEmail;
    private String userFirstName;
    private String userLastName;
    private String content;
    private UUID parentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
