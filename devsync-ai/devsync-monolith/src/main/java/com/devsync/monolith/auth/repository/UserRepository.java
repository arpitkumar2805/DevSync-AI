package com.devsync.monolith.auth.repository;

import com.devsync.monolith.auth.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmailAndDeletedFalse(String email);

    boolean existsByEmailAndDeletedFalse(String email);

    Optional<User> findByIdAndDeletedFalse(UUID id);

    Page<User> findByOrganizationIdAndDeletedFalse(UUID organizationId, Pageable pageable);

    List<User> findByOrganizationIdAndDeletedFalse(UUID organizationId);

    long countByOrganizationIdAndDeletedFalse(UUID organizationId);
}
