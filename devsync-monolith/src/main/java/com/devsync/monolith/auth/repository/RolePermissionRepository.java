package com.devsync.monolith.auth.repository;

import com.devsync.monolith.auth.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, UUID> {
    List<RolePermission> findByRoleId(UUID roleId);
    boolean existsByRoleIdAndPermissionId(UUID roleId, UUID permissionId);
}
