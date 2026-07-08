package com.devsync.monolith.notification.repository;

import com.devsync.monolith.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Page<Notification> findByUserIdAndDeletedFalse(UUID userId, Pageable pageable);
    Page<Notification> findByUserIdAndReadFalseAndDeletedFalse(UUID userId, Pageable pageable);
    Optional<Notification> findByIdAndDeletedFalse(UUID id);
}
