package com.devsync.monolith.project.repository;

import com.devsync.monolith.project.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
    Page<Project> findByOrganizationIdAndDeletedFalse(UUID orgId, Pageable pageable);
    Optional<Project> findByIdAndDeletedFalse(UUID id);
    long countByOrganizationIdAndDeletedFalse(UUID orgId);

    @Query("SELECT p.id FROM Project p WHERE p.organizationId = :orgId AND p.deleted = false")
    List<UUID> findIdsByOrganizationId(UUID orgId);
}
