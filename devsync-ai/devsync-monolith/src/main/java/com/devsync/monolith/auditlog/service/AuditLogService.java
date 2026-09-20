package com.devsync.monolith.auditlog.service;

import com.devsync.common.dto.PageResponse;
import com.devsync.monolith.auditlog.dto.AuditLogResponse;
import com.devsync.monolith.auditlog.entity.AuditLog;
import com.devsync.monolith.auditlog.repository.AuditLogRepository;
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
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Transactional
    public void logAudit(UUID userId, String action, String entityType, UUID entityId, String ipAddress, String details) {
        AuditLog log = AuditLog.builder()
                .userId(userId)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .ipAddress(ipAddress)
                .details(details)
                .build();
        auditLogRepository.save(log);
    }

    public PageResponse<AuditLogResponse> listAllLogs(Pageable pageable) {
        Page<AuditLog> logs = auditLogRepository.findByDeletedFalse(pageable);
        return PageResponse.of(logs.map(log -> {
            User user = log.getUserId() != null ? userRepository.findByIdAndDeletedFalse(log.getUserId()).orElse(null) : null;
            return toResponse(log, user);
        }));
    }

    private AuditLogResponse toResponse(AuditLog log, User user) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .userId(log.getUserId())
                .userEmail(user != null ? user.getEmail() : null)
                .action(log.getAction())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .ipAddress(log.getIpAddress())
                .details(log.getDetails())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
