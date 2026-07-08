package com.devsync.monolith.comment.repository;

import com.devsync.monolith.comment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {
    Page<Comment> findByTaskIdAndDeletedFalse(UUID taskId, Pageable pageable);
    Optional<Comment> findByIdAndDeletedFalse(UUID id);
    List<Comment> findByTaskIdAndParentIdIsNullAndDeletedFalse(UUID taskId);
    List<Comment> findByParentIdAndDeletedFalse(UUID parentId);
    long countByTaskIdAndDeletedFalse(UUID taskId);
}
