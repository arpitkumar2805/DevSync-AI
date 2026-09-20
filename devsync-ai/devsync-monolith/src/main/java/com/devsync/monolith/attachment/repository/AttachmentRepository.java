package com.devsync.monolith.attachment.repository;

import com.devsync.monolith.attachment.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {
    List<Attachment> findByTaskIdAndDeletedFalse(UUID taskId);
    Optional<Attachment> findByIdAndDeletedFalse(UUID id);
}
