package com.devsync.orguser.org.service;

import com.devsync.common.dto.PageResponse;
import com.devsync.common.exception.DuplicateResourceException;
import com.devsync.common.exception.ResourceNotFoundException;
import com.devsync.orguser.user.entity.User;
import com.devsync.orguser.user.repository.UserRepository;
import com.devsync.orguser.org.dto.*;
import com.devsync.orguser.org.entity.Organization;
import com.devsync.orguser.org.repository.OrganizationRepository;
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
public class OrganizationService {

    private static final Logger log = LoggerFactory.getLogger(OrganizationService.class);
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;

    @Transactional
    public OrgResponse create(CreateOrgRequest request, UUID creatorUserId) {
        String slug = request.getName().toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-");

        if (organizationRepository.existsBySlugAndDeletedFalse(slug)) {
            slug = slug + "-" + UUID.randomUUID().toString().substring(0, 4);
        }

        Organization org = Organization.builder()
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .subscriptionTier("FREE")
                .build();
        org = organizationRepository.save(org);

        // Assign creator as ORG_ADMIN
        User creator = userRepository.findByIdAndDeletedFalse(creatorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", creatorUserId));
        creator.setOrganizationId(org.getId());
        creator.setRoleName("ORG_ADMIN");
        userRepository.save(creator);

        log.info("Organization created: {} by user {}", org.getName(), creatorUserId);
        return toResponse(org);
    }

    public OrgResponse getById(UUID id) {
        Organization org = organizationRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", id));
        return toResponse(org);
    }

    @Transactional
    public OrgResponse update(UUID id, UpdateOrgRequest request) {
        Organization org = organizationRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", id));

        if (request.getName() != null) org.setName(request.getName());
        if (request.getDescription() != null) org.setDescription(request.getDescription());
        if (request.getSubscriptionTier() != null) org.setSubscriptionTier(request.getSubscriptionTier());

        org = organizationRepository.save(org);
        return toResponse(org);
    }

    @Transactional
    public void delete(UUID id) {
        Organization org = organizationRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", id));
        org.softDelete();
        organizationRepository.save(org);
        log.info("Organization soft-deleted: {}", id);
    }

    public PageResponse<OrgResponse> listAll(Pageable pageable) {
        Page<Organization> page = organizationRepository.findByDeletedFalse(pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    @Transactional
    public void inviteMember(UUID orgId, InviteMemberRequest request) {
        organizationRepository.findByIdAndDeletedFalse(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", orgId));

        User user = userRepository.findByEmailAndDeletedFalse(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        if (user.getOrganizationId() != null && user.getOrganizationId().equals(orgId)) {
            throw new DuplicateResourceException("User", "organization", orgId);
        }

        user.setOrganizationId(orgId);
        user.setRoleName(request.getRoleName());
        userRepository.save(user);
        log.info("User {} invited to org {} with role {}", user.getEmail(), orgId, request.getRoleName());
    }

    private OrgResponse toResponse(Organization org) {
        return OrgResponse.builder()
                .id(org.getId())
                .name(org.getName())
                .slug(org.getSlug())
                .description(org.getDescription())
                .subscriptionTier(org.getSubscriptionTier())
                .createdAt(org.getCreatedAt())
                .build();
    }
}
