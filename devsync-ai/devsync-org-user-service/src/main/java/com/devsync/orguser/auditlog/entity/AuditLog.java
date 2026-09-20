package com.devsync.orguser.auditlog.entity;

import com.devsync.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@SQLRestriction("deleted = false")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog extends BaseEntity {

    @Column(name = "user_id")
    private UUID userId; // nullable for anonymous/system actions

    @Column(nullable = false)
    private String action; // e.g. LOGIN, LOGOUT, ROLE_CHANGED, DELETED

    @Column(name = "entity_type")
    private String entityType;

    @Column(name = "entity_id")
    private UUID entityId;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(columnDefinition = "TEXT")
    private String details;
}
