package com.devsync.monolith.user.service;

import com.devsync.common.dto.PageResponse;
import com.devsync.common.exception.ResourceNotFoundException;
import com.devsync.monolith.auth.entity.User;
import com.devsync.monolith.auth.repository.UserRepository;
import com.devsync.monolith.user.dto.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;

    public UserResponse getById(UUID id) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return toResponse(user);
    }

    public UserResponse getCurrentUser(UUID userId) {
        return getById(userId);
    }

    @Transactional
    public UserResponse update(UUID id, UpdateUserRequest request) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getAvatarUrl() != null) user.setAvatarUrl(request.getAvatarUrl());

        user = userRepository.save(user);
        return toResponse(user);
    }

    @Transactional
    public UserResponse assignRole(UUID id, AssignRoleRequest request) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        user.setRoleName(request.getRoleName());
        user = userRepository.save(user);
        log.info("Role {} assigned to user {}", request.getRoleName(), id);
        return toResponse(user);
    }

    @Transactional
    public void deactivate(UUID id) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        user.setActive(false);
        userRepository.save(user);
        log.info("User deactivated: {}", id);
    }

    @Transactional
    public void reactivate(UUID id) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        user.setActive(true);
        userRepository.save(user);
        log.info("User reactivated: {}", id);
    }

    public PageResponse<UserResponse> listByOrganization(UUID orgId, Pageable pageable) {
        Page<User> page = userRepository.findByOrganizationIdAndDeletedFalse(orgId, pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    @Transactional
    public void delete(UUID id) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        user.softDelete();
        userRepository.save(user);
        log.info("User soft-deleted: {}", id);
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .avatarUrl(user.getAvatarUrl())
                .roleName(user.getRoleName())
                .organizationId(user.getOrganizationId())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
