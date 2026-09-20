package com.devsync.auth.repository;

import com.devsync.auth.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    Optional<Organization> findBySlugAndDeletedFalse(String slug);
    boolean existsBySlugAndDeletedFalse(String slug);
}
