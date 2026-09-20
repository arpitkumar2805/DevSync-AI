package com.devsync.monolith.comment.service;

import com.devsync.common.dto.PageResponse;
import com.devsync.common.exception.AccessDeniedException;
import com.devsync.common.exception.ResourceNotFoundException;
import com.devsync.monolith.auth.entity.User;
import com.devsync.monolith.auth.repository.UserRepository;
import com.devsync.monolith.comment.dto.*;
import com.devsync.monolith.comment.entity.Comment;
import com.devsync.monolith.comment.repository.CommentRepository;
import com.devsync.monolith.task.repository.TaskRepository;
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
    private final UserRepository userRepository;

    @Transactional
    public CommentResponse create(UUID taskId, CreateCommentRequest request, UUID userId) {
        taskRepository.findByIdAndDeletedFalse(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Comment comment = Comment.builder()
                .taskId(taskId)
                .userId(userId)
                .content(request.getContent())
                .parentId(request.getParentId())
                .build();
        comment = commentRepository.save(comment);
        log.info("Comment created on task {} by user {}", taskId, userId);
        return toResponse(comment, user);
    }

    public CommentResponse getById(UUID id) {
        Comment comment = commentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", id));
        User user = userRepository.findByIdAndDeletedFalse(comment.getUserId()).orElse(null);
        return toResponse(comment, user);
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

        User user = userRepository.findByIdAndDeletedFalse(comment.getUserId()).orElse(null);
        return toResponse(comment, user);
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
        return PageResponse.of(page.map(comment -> {
            User user = userRepository.findByIdAndDeletedFalse(comment.getUserId()).orElse(null);
            return toResponse(comment, user);
        }));
    }

    private CommentResponse toResponse(Comment comment, User user) {
        return CommentResponse.builder()
                .id(comment.getId())
                .taskId(comment.getTaskId())
                .userId(comment.getUserId())
                .userEmail(user != null ? user.getEmail() : null)
                .userFirstName(user != null ? user.getFirstName() : null)
                .userLastName(user != null ? user.getLastName() : null)
                .content(comment.getContent())
                .parentId(comment.getParentId())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
