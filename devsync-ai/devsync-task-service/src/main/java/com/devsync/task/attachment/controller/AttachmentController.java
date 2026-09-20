package com.devsync.task.attachment.controller;

import com.devsync.common.dto.ApiResponse;
import com.devsync.task.attachment.dto.AttachmentResponse;
import com.devsync.task.attachment.service.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Attachments", description = "File attachment endpoints")
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping("/tasks/{taskId}/attachments")
    @Operation(summary = "Upload an attachment to a task")
    public ResponseEntity<ApiResponse<AttachmentResponse>> uploadFile(
            @PathVariable UUID taskId,
            @RequestParam("file") MultipartFile file) {
        
        UUID userId = getCurrentUserId();
        AttachmentResponse response = attachmentService.uploadAttachment(taskId, userId, file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("File uploaded successfully", response));
    }

    @GetMapping("/tasks/{taskId}/attachments")
    @Operation(summary = "List attachments for a task")
    public ResponseEntity<ApiResponse<List<AttachmentResponse>>> getAttachments(@PathVariable UUID taskId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Attachments retrieved",
                attachmentService.getAttachmentsByTaskId(taskId)
        ));
    }

    @DeleteMapping("/attachments/{id}")
    @Operation(summary = "Delete an attachment")
    public ResponseEntity<ApiResponse<Void>> deleteAttachment(@PathVariable UUID id) {
        UUID userId = getCurrentUserId();
        attachmentService.deleteAttachment(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Attachment deleted", null));
    }

    @GetMapping("/attachments/download/{fileName:.+}")
    @Operation(summary = "Download a file")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        Resource resource = attachmentService.loadAttachmentAsResource(fileName);
        
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            contentType = "application/octet-stream";
        }
        
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    private UUID getCurrentUserId() {
        return UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
    }
}
