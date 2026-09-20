package com.devsync.monolith.org.entity;

import com.devsync.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import java.util.UUID;

@Entity
@Table(name = "org_invitations")
@SQLRestriction("deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrgInvitation extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "role_name", nullable = false)
    private String roleName;

    @Column(nullable = false)
    @Builder.Default
    private String status = "PENDING"; // PENDING, ACCEPTED
}
