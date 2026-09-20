package com.devsync.monolith.activitylog.service;

import com.devsync.common.dto.PageResponse;
import com.devsync.monolith.activitylog.dto.ActivityLogResponse;
import com.devsync.monolith.activitylog.entity.ActivityLog;
import com.devsync.monolith.activitylog.repository.ActivityLogRepository;
import com.devsync.monolith.auth.entity.User;
import com.devsync.monolith.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;

    @Transactional
    public void logActivity(String entityType, UUID entityId, String action, String changes, UUID userId) {
        ActivityLog log = ActivityLog.builder()
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .changes(changes)
                .userId(userId)
                .build();
        activityLogRepository.save(log);
    }

    public PageResponse<ActivityLogResponse> getEntityActivity(String entityType, UUID entityId, Pageable pageable) {
        Page<ActivityLog> logs = activityLogRepository.findByEntityTypeAndEntityIdAndDeletedFalse(entityType, entityId, pageable);
        return PageResponse.of(logs.map(log -> {
            User user = userRepository.findByIdAndDeletedFalse(log.getUserId()).orElse(null);
            return toResponse(log, user);
        }));
    }

    private ActivityLogResponse toResponse(ActivityLog log, User user) {
        return ActivityLogResponse.builder()
                .id(log.getId())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .action(log.getAction())
                .changes(log.getChanges())
                .userId(log.getUserId())
                .userEmail(user != null ? user.getEmail() : null)
                .createdAt(log.getCreatedAt())
                .build();
    }
}
