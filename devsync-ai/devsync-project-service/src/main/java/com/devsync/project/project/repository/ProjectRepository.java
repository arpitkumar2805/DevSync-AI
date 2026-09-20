package com.devsync.project.project.repository;

import com.devsync.project.project.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
    Page<Project> findByOrganizationIdAndDeletedFalse(UUID orgId, Pageable pageable);
    Optional<Project> findByIdAndDeletedFalse(UUID id);
    long countByOrganizationIdAndDeletedFalse(UUID orgId);
}
