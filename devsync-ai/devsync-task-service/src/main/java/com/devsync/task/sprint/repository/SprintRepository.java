package com.devsync.task.sprint.repository;

import com.devsync.common.enums.SprintStatus;
import com.devsync.task.sprint.entity.Sprint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SprintRepository extends JpaRepository<Sprint, UUID> {
    Page<Sprint> findByProjectIdAndDeletedFalse(UUID projectId, Pageable pageable);
    Optional<Sprint> findByIdAndDeletedFalse(UUID id);
    Optional<Sprint> findByProjectIdAndStatusAndDeletedFalse(UUID projectId, SprintStatus status);
    List<Sprint> findByProjectIdAndDeletedFalseOrderByCreatedAtDesc(UUID projectId);
}
