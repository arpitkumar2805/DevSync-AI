package com.devsync.orguser.user.repository;

import com.devsync.orguser.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmailAndDeletedFalse(String email);
    Optional<User> findByIdAndDeletedFalse(UUID id);
    boolean existsByEmailAndDeletedFalse(String email);
    Page<User> findByOrganizationIdAndDeletedFalse(UUID orgId, Pageable pageable);
}
