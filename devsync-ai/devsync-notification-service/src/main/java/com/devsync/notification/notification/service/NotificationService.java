package com.devsync.notification.notification.service;

import com.devsync.common.dto.PageResponse;
import com.devsync.common.exception.AccessDeniedException;
import com.devsync.common.exception.ResourceNotFoundException;
import com.devsync.notification.notification.dto.NotificationResponse;
import com.devsync.notification.notification.entity.Notification;
import com.devsync.notification.notification.repository.NotificationRepository;
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
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final NotificationRepository notificationRepository;

    @Transactional
    public NotificationResponse createNotification(UUID userId, String type, String title, String message, String entityType, UUID entityId) {
        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .read(false)
                .entityType(entityType)
                .entityId(entityId)
                .build();
        notification = notificationRepository.save(notification);
        log.info("Created {} notification for user {}", type, userId);
        return toResponse(notification);
    }

    public PageResponse<NotificationResponse> listUserNotifications(UUID userId, boolean unreadOnly, Pageable pageable) {
        Page<Notification> page;
        if (unreadOnly) {
            page = notificationRepository.findByUserIdAndReadFalseAndDeletedFalse(userId, pageable);
        } else {
            page = notificationRepository.findByUserIdAndDeletedFalse(userId, pageable);
        }
        return PageResponse.of(page.map(this::toResponse));
    }

    @Transactional
    public NotificationResponse markAsRead(UUID id, UUID userId) {
        Notification notification = notificationRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));

        if (!notification.getUserId().equals(userId)) {
            throw new AccessDeniedException("Access denied");
        }

        notification.setRead(true);
        notification = notificationRepository.save(notification);
        return toResponse(notification);
    }

    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .read(notification.isRead())
                .entityType(notification.getEntityType())
                .entityId(notification.getEntityId())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
