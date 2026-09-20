package com.devsync.orguser.team.controller;

import com.devsync.common.dto.ApiResponse;
import com.devsync.common.dto.PageResponse;
import com.devsync.common.util.JwtUtil;
import com.devsync.orguser.team.dto.*;
import com.devsync.orguser.team.service.TeamService;
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
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
@Tag(name = "Teams", description = "Team management endpoints")
public class TeamController {

    private final TeamService teamService;
    private final JwtUtil jwtUtil;

    @PostMapping
    @Operation(summary = "Create a new team")
    public ResponseEntity<ApiResponse<TeamResponse>> create(
            @Valid @RequestBody CreateTeamRequest request,
            @RequestHeader("Authorization") String authHeader) {
        UUID orgId = extractOrgId(authHeader);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Team created", teamService.create(request, orgId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get team by ID")
    public ResponseEntity<ApiResponse<TeamResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Team found", teamService.getById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update team")
    public ResponseEntity<ApiResponse<TeamResponse>> update(@PathVariable UUID id, @Valid @RequestBody UpdateTeamRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Team updated", teamService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete team")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        teamService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Team deleted", null));
    }

    @GetMapping
    @Operation(summary = "List teams by organization")
    public ResponseEntity<ApiResponse<PageResponse<TeamResponse>>> list(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        UUID orgId = extractOrgId(authHeader);
        return ResponseEntity.ok(ApiResponse.success("Teams retrieved",
                teamService.listByOrganization(orgId, PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @PostMapping("/{id}/members")
    @Operation(summary = "Add member to team")
    public ResponseEntity<ApiResponse<TeamMemberResponse>> addMember(@PathVariable UUID id, @Valid @RequestBody AddTeamMemberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Member added", teamService.addMember(id, request)));
    }

    @DeleteMapping("/{id}/members/{userId}")
    @Operation(summary = "Remove member from team")
    public ResponseEntity<ApiResponse<Void>> removeMember(@PathVariable UUID id, @PathVariable UUID userId) {
        teamService.removeMember(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Member removed", null));
    }

    @GetMapping("/{id}/members")
    @Operation(summary = "List team members")
    public ResponseEntity<ApiResponse<PageResponse<TeamMemberResponse>>> getMembers(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success("Members retrieved",
                teamService.getMembers(id, PageRequest.of(page, size))));
    }

    private UUID extractOrgId(String authHeader) {
        String token = authHeader.substring(7);
        return jwtUtil.extractOrgId(token);
    }
}
