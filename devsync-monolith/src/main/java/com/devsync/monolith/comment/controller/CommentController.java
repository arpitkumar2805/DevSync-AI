package com.devsync.monolith.comment.controller;

import com.devsync.common.dto.ApiResponse;
import com.devsync.common.dto.PageResponse;
import com.devsync.monolith.comment.dto.*;
import com.devsync.monolith.comment.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tasks/{taskId}/comments")
@RequiredArgsConstructor
@Tag(name = "Comments", description = "Task comment endpoints")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    @Operation(summary = "Add a comment to a task")
    public ResponseEntity<ApiResponse<CommentResponse>> create(
            @PathVariable UUID taskId,
            @Valid @RequestBody CreateCommentRequest request) {
        UUID userId = getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Comment added", commentService.create(taskId, request, userId)));
    }

    @GetMapping
    @Operation(summary = "List comments for a task")
    public ResponseEntity<ApiResponse<PageResponse<CommentResponse>>> list(
            @PathVariable UUID taskId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success("Comments retrieved",
                commentService.listByTask(taskId, PageRequest.of(page, size, Sort.by("createdAt").ascending()))));
    }

    @PutMapping("/{commentId}")
    @Operation(summary = "Update a comment")
    public ResponseEntity<ApiResponse<CommentResponse>> update(
            @PathVariable UUID taskId,
            @PathVariable UUID commentId,
            @Valid @RequestBody UpdateCommentRequest request) {
        UUID userId = getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Comment updated", commentService.update(commentId, request, userId)));
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "Delete a comment")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID taskId,
            @PathVariable UUID commentId) {
        UUID userId = getCurrentUserId();
        commentService.delete(commentId, userId);
        return ResponseEntity.ok(ApiResponse.success("Comment deleted", null));
    }

    private UUID getCurrentUserId() {
        return UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
    }
}
