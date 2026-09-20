package com.devsync.monolith.ai.entity;

import com.devsync.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Table(name = "ai_request_logs")
@SQLRestriction("deleted = false")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AIRequestLog extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String feature; // e.g. SPRINT_SUMMARY, ESTIMATE_POINTS

    @Column(name = "entity_type")
    private String entityType; // e.g. TASK, SPRINT, PROJECT

    @Column(name = "entity_id")
    private UUID entityId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String prompt;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String response;

    @Column(nullable = false)
    private String model;

    @Column(name = "tokens_used")
    private Integer tokensUsed;

    @Column(name = "latency_ms")
    private Long latencyMs;
}
