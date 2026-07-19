package com.devsync.task.attachment.service;

import com.devsync.common.exception.ResourceNotFoundException;
import com.devsync.task.attachment.dto.AttachmentResponse;
import com.devsync.task.attachment.entity.Attachment;
import com.devsync.task.attachment.repository.AttachmentRepository;
import com.devsync.task.auth.entity.User;
import com.devsync.task.auth.repository.UserRepository;
import com.devsync.task.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private static final Logger log = LoggerFactory.getLogger(AttachmentService.class);
    private final AttachmentRepository attachmentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public AttachmentResponse uploadAttachment(UUID taskId, UUID userId, MultipartFile file) {
        taskRepository.findByIdAndDeletedFalse(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        String storedFileName = fileStorageService.storeFile(file);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/attachments/download/")
                .path(storedFileName)
                .toUriString();

        Attachment attachment = Attachment.builder()
                .taskId(taskId)
                .userId(userId)
                .fileName(file.getOriginalFilename())
                .fileUrl(fileDownloadUri)
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .build();
        attachment = attachmentRepository.save(attachment);

        log.info("Attachment uploaded for task {}: {}", taskId, attachment.getFileName());
        return toResponse(attachment, user);
    }

    public List<AttachmentResponse> getAttachmentsByTaskId(UUID taskId) {
        return attachmentRepository.findByTaskIdAndDeletedFalse(taskId).stream()
                .map(attachment -> {
                    User user = userRepository.findByIdAndDeletedFalse(attachment.getUserId()).orElse(null);
                    return toResponse(attachment, user);
                })
                .collect(Collectors.toList());
    }

    public Resource loadAttachmentAsResource(String fileName) {
        return fileStorageService.loadFileAsResource(fileName);
    }

    @Transactional
    public void deleteAttachment(UUID id, UUID userId) {
        Attachment attachment = attachmentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment", "id", id));
        
        // MVP: Only the user who uploaded can delete (or project admin, but skipping complex roles check for MVP)
        if (!attachment.getUserId().equals(userId)) {
            throw new com.devsync.common.exception.AccessDeniedException("You can only delete your own attachments");
        }
        
        attachment.softDelete();
        attachmentRepository.save(attachment);
        
        // Optionally delete the physical file too, but keeping it for soft-delete semantics is fine for now.
        log.info("Attachment soft-deleted: {}", id);
    }

    private AttachmentResponse toResponse(Attachment attachment, User user) {
        return AttachmentResponse.builder()
                .id(attachment.getId())
                .taskId(attachment.getTaskId())
                .userId(attachment.getUserId())
                .userEmail(user != null ? user.getEmail() : null)
                .fileName(attachment.getFileName())
                .fileUrl(attachment.getFileUrl())
                .fileSize(attachment.getFileSize())
                .contentType(attachment.getContentType())
                .createdAt(attachment.getCreatedAt())
                .build();
    }
}
