package com.devsync.orguser.team.repository;

import com.devsync.orguser.team.entity.TeamMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, UUID> {
    List<TeamMember> findByTeamId(UUID teamId);
    Page<TeamMember> findByTeamId(UUID teamId, Pageable pageable);
    List<TeamMember> findByUserId(UUID userId);
    boolean existsByTeamIdAndUserId(UUID teamId, UUID userId);

    @Modifying
    @Transactional
    void deleteByTeamIdAndUserId(UUID teamId, UUID userId);

    long countByTeamId(UUID teamId);
}
