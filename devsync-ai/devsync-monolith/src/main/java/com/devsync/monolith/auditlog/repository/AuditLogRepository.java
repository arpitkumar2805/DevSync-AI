package com.devsync.monolith.auditlog.repository;

import com.devsync.monolith.auditlog.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    Page<AuditLog> findByUserIdAndDeletedFalse(UUID userId, Pageable pageable);
    Page<AuditLog> findByDeletedFalse(Pageable pageable);
}
