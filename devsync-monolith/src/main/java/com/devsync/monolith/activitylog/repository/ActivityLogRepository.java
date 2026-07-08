package com.devsync.monolith.activitylog.repository;

import com.devsync.monolith.activitylog.entity.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {
    Page<ActivityLog> findByEntityTypeAndEntityIdAndDeletedFalse(String entityType, UUID entityId, Pageable pageable);
}
