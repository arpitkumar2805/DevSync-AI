package com.devsync.task.activitylog.entity;

import com.devsync.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Table(name = "activity_logs")
@SQLRestriction("deleted = false")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ActivityLog extends BaseEntity {

    @Column(name = "entity_type", nullable = false)
    private String entityType; // TASK, PROJECT, SPRINT

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Column(nullable = false)
    private String action; // e.g. CREATED, UPDATED, STATUS_CHANGED

    @Column(columnDefinition = "TEXT")
    private String changes; // JSON string representing old -> new values

    @Column(name = "user_id", nullable = false)
    private UUID userId; // The user who performed the action
}
