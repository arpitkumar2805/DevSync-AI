package com.devsync.orguser.org.repository;

import com.devsync.orguser.org.entity.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    Optional<Organization> findBySlugAndDeletedFalse(String slug);
    boolean existsBySlugAndDeletedFalse(String slug);
    Optional<Organization> findByIdAndDeletedFalse(UUID id);
    Page<Organization> findByDeletedFalse(Pageable pageable);
}
