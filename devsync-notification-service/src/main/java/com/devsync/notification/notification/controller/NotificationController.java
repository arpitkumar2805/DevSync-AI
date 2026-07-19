package com.devsync.notification.notification.controller;

import com.devsync.common.dto.ApiResponse;
import com.devsync.common.dto.PageResponse;
import com.devsync.notification.notification.dto.NotificationResponse;
import com.devsync.notification.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "User notification endpoints")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "List current user's notifications")
    public ResponseEntity<ApiResponse<PageResponse<NotificationResponse>>> listMyNotifications(
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        UUID userId = getCurrentUserId();
        PageResponse<NotificationResponse> result = notificationService.listUserNotifications(
                userId, unreadOnly, PageRequest.of(page, size, Sort.by("createdAt").descending()));
                
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved", result));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(@PathVariable UUID id) {
        UUID userId = getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", notificationService.markAsRead(id, userId)));
    }

    private UUID getCurrentUserId() {
        return UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
    }
}
