package com.devsync.project.project.entity;

import com.devsync.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "project_members", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"project_id", "user_id"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProjectMember extends BaseEntity {

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    @Builder.Default
    private String role = "MEMBER";
}
