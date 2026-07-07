package com.devsync.monolith.org.controller;

import com.devsync.common.dto.ApiResponse;
import com.devsync.common.dto.PageResponse;
import com.devsync.monolith.org.dto.*;
import com.devsync.monolith.org.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orgs")
@RequiredArgsConstructor
@Tag(name = "Organizations", description = "Organization management endpoints")
public class OrganizationController {

    private final OrganizationService organizationService;

    @PostMapping
    @Operation(summary = "Create a new organization")
    public ResponseEntity<ApiResponse<OrgResponse>> create(@Valid @RequestBody CreateOrgRequest request) {
        OrgResponse response = organizationService.create(request, getCurrentUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Organization created", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get organization by ID")
    public ResponseEntity<ApiResponse<OrgResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Organization found", organizationService.getById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update organization")
    public ResponseEntity<ApiResponse<OrgResponse>> update(@PathVariable UUID id, @Valid @RequestBody UpdateOrgRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Organization updated", organizationService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete organization")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        organizationService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Organization deleted", null));
    }

    @GetMapping
    @Operation(summary = "List all organizations")
    public ResponseEntity<ApiResponse<PageResponse<OrgResponse>>> listAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success("Organizations retrieved",
                organizationService.listAll(PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @PostMapping("/{id}/invite")
    @Operation(summary = "Invite a member to organization")
    public ResponseEntity<ApiResponse<Void>> inviteMember(@PathVariable UUID id, @Valid @RequestBody InviteMemberRequest request) {
        organizationService.inviteMember(id, request);
        return ResponseEntity.ok(ApiResponse.success("Member invited", null));
    }

    private UUID getCurrentUserId() {
        return UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
    }
}
