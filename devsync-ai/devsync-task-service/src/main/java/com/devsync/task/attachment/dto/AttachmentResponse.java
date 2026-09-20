package com.devsync.task.attachment.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AttachmentResponse {
    private UUID id;
    private UUID taskId;
    private UUID userId;
    private String userEmail;
    private String fileName;
    private String fileUrl;
    private Long fileSize;
    private String contentType;
    private LocalDateTime createdAt;
}
