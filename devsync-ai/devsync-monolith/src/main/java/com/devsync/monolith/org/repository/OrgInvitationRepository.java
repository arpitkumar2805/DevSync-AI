package com.devsync.monolith.org.repository;

import com.devsync.monolith.org.entity.OrgInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrgInvitationRepository extends JpaRepository<OrgInvitation, UUID> {
    Optional<OrgInvitation> findByEmailAndStatusAndDeletedFalse(String email, String status);
    Optional<OrgInvitation> findByEmailAndDeletedFalse(String email);
    boolean existsByEmailAndStatusAndDeletedFalse(String email, String status);
}
