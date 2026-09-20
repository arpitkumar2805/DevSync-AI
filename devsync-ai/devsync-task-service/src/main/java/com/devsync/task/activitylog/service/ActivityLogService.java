package com.devsync.task.activitylog.service;

import com.devsync.common.dto.PageResponse;
import com.devsync.task.activitylog.dto.ActivityLogResponse;
import com.devsync.task.activitylog.entity.ActivityLog;
import com.devsync.task.activitylog.repository.ActivityLogRepository;
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
        return PageResponse.of(logs.map(this::toResponse));
    }

    // userEmail is intentionally left null: user profiles live in devsync-org-user-service's
    // own database and must be resolved via that service, not joined locally.
    private ActivityLogResponse toResponse(ActivityLog log) {
        return ActivityLogResponse.builder()
                .id(log.getId())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .action(log.getAction())
                .changes(log.getChanges())
                .userId(log.getUserId())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
