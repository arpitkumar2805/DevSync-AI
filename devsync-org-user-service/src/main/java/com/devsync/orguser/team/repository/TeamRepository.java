package com.devsync.orguser.team.repository;

import com.devsync.orguser.team.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeamRepository extends JpaRepository<Team, UUID> {
    Page<Team> findByOrganizationIdAndDeletedFalse(UUID orgId, Pageable pageable);
    Optional<Team> findByIdAndDeletedFalse(UUID id);
}
