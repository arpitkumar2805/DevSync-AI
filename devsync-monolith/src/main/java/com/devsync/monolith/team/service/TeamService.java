package com.devsync.monolith.team.service;

import com.devsync.common.dto.PageResponse;
import com.devsync.common.enums.TeamMemberRole;
import com.devsync.common.exception.DuplicateResourceException;
import com.devsync.common.exception.ResourceNotFoundException;
import com.devsync.monolith.auth.entity.User;
import com.devsync.monolith.auth.repository.UserRepository;
import com.devsync.monolith.team.dto.*;
import com.devsync.monolith.team.entity.Team;
import com.devsync.monolith.team.entity.TeamMember;
import com.devsync.monolith.team.repository.TeamMemberRepository;
import com.devsync.monolith.team.repository.TeamRepository;
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
public class TeamService {

    private static final Logger log = LoggerFactory.getLogger(TeamService.class);
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;

    @Transactional
    public TeamResponse create(CreateTeamRequest request, UUID orgId) {
        Team team = Team.builder()
                .organizationId(orgId)
                .name(request.getName())
                .description(request.getDescription())
                .build();
        team = teamRepository.save(team);
        log.info("Team created: {} in org {}", team.getName(), orgId);
        return toResponse(team);
    }

    public TeamResponse getById(UUID id) {
        Team team = teamRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", id));
        return toResponse(team);
    }

    @Transactional
    public TeamResponse update(UUID id, UpdateTeamRequest request) {
        Team team = teamRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", id));
        if (request.getName() != null) team.setName(request.getName());
        if (request.getDescription() != null) team.setDescription(request.getDescription());
        team = teamRepository.save(team);
        return toResponse(team);
    }

    @Transactional
    public void delete(UUID id) {
        Team team = teamRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", id));
        team.softDelete();
        teamRepository.save(team);
        log.info("Team soft-deleted: {}", id);
    }

    public PageResponse<TeamResponse> listByOrganization(UUID orgId, Pageable pageable) {
        Page<Team> page = teamRepository.findByOrganizationIdAndDeletedFalse(orgId, pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    @Transactional
    public TeamMemberResponse addMember(UUID teamId, AddTeamMemberRequest request) {
        teamRepository.findByIdAndDeletedFalse(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", teamId));
        User user = userRepository.findByIdAndDeletedFalse(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));
        if (teamMemberRepository.existsByTeamIdAndUserId(teamId, request.getUserId())) {
            throw new DuplicateResourceException("TeamMember", "userId", request.getUserId());
        }
        TeamMember member = TeamMember.builder()
                .teamId(teamId)
                .userId(request.getUserId())
                .role(request.getRole() != null ? request.getRole() : TeamMemberRole.MEMBER)
                .build();
        member = teamMemberRepository.save(member);
        return toMemberResponse(member, user);
    }

    @Transactional
    public void removeMember(UUID teamId, UUID userId) {
        teamMemberRepository.deleteByTeamIdAndUserId(teamId, userId);
    }

    public PageResponse<TeamMemberResponse> getMembers(UUID teamId, Pageable pageable) {
        Page<TeamMember> page = teamMemberRepository.findByTeamId(teamId, pageable);
        return PageResponse.of(page.map(tm -> {
            User user = userRepository.findByIdAndDeletedFalse(tm.getUserId()).orElse(null);
            return toMemberResponse(tm, user);
        }));
    }

    private TeamResponse toResponse(Team team) {
        return TeamResponse.builder()
                .id(team.getId())
                .organizationId(team.getOrganizationId())
                .name(team.getName())
                .description(team.getDescription())
                .createdAt(team.getCreatedAt())
                .memberCount((int) teamMemberRepository.countByTeamId(team.getId()))
                .build();
    }

    private TeamMemberResponse toMemberResponse(TeamMember tm, User user) {
        return TeamMemberResponse.builder()
                .id(tm.getId())
                .teamId(tm.getTeamId())
                .userId(tm.getUserId())
                .email(user != null ? user.getEmail() : null)
                .firstName(user != null ? user.getFirstName() : null)
                .lastName(user != null ? user.getLastName() : null)
                .role(tm.getRole())
                .build();
    }
}
