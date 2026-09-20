package com.devsync.monolith.settings.repository;

import com.devsync.monolith.settings.entity.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SettingRepository extends JpaRepository<Setting, UUID> {
    List<Setting> findByOrgIdAndDeletedFalse(UUID orgId);
    List<Setting> findByUserIdAndDeletedFalse(UUID userId);
    Optional<Setting> findByOrgIdAndKeyAndDeletedFalse(UUID orgId, String key);
    Optional<Setting> findByUserIdAndKeyAndDeletedFalse(UUID userId, String key);
}
