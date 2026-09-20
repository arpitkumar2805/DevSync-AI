package com.devsync.orguser.settings.entity;

import com.devsync.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Table(name = "settings", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"org_id", "setting_key"}),
        @UniqueConstraint(columnNames = {"user_id", "setting_key"})
})
@SQLRestriction("deleted = false")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Setting extends BaseEntity {

    @Column(name = "org_id")
    private UUID orgId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "setting_key", nullable = false)
    private String key;

    @Column(name = "setting_value", nullable = false)
    private String value;
}
