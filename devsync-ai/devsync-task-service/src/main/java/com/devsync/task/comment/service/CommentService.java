package com.devsync.task.comment.service;

import com.devsync.common.dto.PageResponse;
import com.devsync.common.exception.AccessDeniedException;
import com.devsync.common.exception.ResourceNotFoundException;
import com.devsync.task.comment.dto.*;
import com.devsync.task.comment.entity.Comment;
import com.devsync.task.comment.repository.CommentRepository;
import com.devsync.task.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService {

    private static final Logger log = LoggerFactory.getLogger(CommentService.class);
    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;

    @Transactional
    public CommentResponse create(UUID taskId, CreateCommentRequest request, UUID userId) {
        taskRepository.findByIdAndDeletedFalse(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        Comment comment = Comment.builder()
                .taskId(taskId)
                .userId(userId)
                .content(request.getContent())
                .parentId(request.getParentId())
                .build();
        comment = commentRepository.save(comment);
        log.info("Comment created on task {} by user {}", taskId, userId);
        return toResponse(comment);
    }

    public CommentResponse getById(UUID id) {
        Comment comment = commentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", id));
        return toResponse(comment);
    }

    @Transactional
    public CommentResponse update(UUID id, UpdateCommentRequest request, UUID userId) {
        Comment comment = commentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", id));

        if (!comment.getUserId().equals(userId)) {
            throw new AccessDeniedException("You can only edit your own comments");
        }

        comment.setContent(request.getContent());
        comment = commentRepository.save(comment);
        return toResponse(comment);
    }

    @Transactional
    public void delete(UUID id, UUID userId) {
        Comment comment = commentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", id));

        if (!comment.getUserId().equals(userId)) {
            throw new AccessDeniedException("You can only delete your own comments");
        }

        comment.softDelete();
        commentRepository.save(comment);
        log.info("Comment soft-deleted: {}", id);
    }

    public PageResponse<CommentResponse> listByTask(UUID taskId, Pageable pageable) {
        Page<Comment> page = commentRepository.findByTaskIdAndDeletedFalse(taskId, pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    // userEmail/userFirstName/userLastName are intentionally left null: user profiles live in
    // devsync-org-user-service's own database and must be resolved via that service, not joined locally.
    private CommentResponse toResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .taskId(comment.getTaskId())
                .userId(comment.getUserId())
                .content(comment.getContent())
                .parentId(comment.getParentId())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
