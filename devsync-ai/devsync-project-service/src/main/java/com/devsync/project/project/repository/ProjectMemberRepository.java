package com.devsync.project.project.repository;

import com.devsync.project.project.entity.ProjectMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, UUID> {
    List<ProjectMember> findByProjectId(UUID projectId);
    Page<ProjectMember> findByProjectId(UUID projectId, Pageable pageable);
    boolean existsByProjectIdAndUserId(UUID projectId, UUID userId);

    @Modifying
    @Transactional
    void deleteByProjectIdAndUserId(UUID projectId, UUID userId);

    long countByProjectId(UUID projectId);
}
