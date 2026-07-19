package com.devsync.task.sprint.controller;

import com.devsync.common.dto.ApiResponse;
import com.devsync.common.dto.PageResponse;
import com.devsync.task.sprint.dto.*;
import com.devsync.task.sprint.service.SprintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sprints")
@RequiredArgsConstructor
@Tag(name = "Sprints", description = "Sprint lifecycle management")
public class SprintController {

    private final SprintService sprintService;

    @PostMapping
    @Operation(summary = "Create a new sprint")
    public ResponseEntity<ApiResponse<SprintResponse>> create(@Valid @RequestBody CreateSprintRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Sprint created", sprintService.create(request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get sprint by ID")
    public ResponseEntity<ApiResponse<SprintResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Sprint found", sprintService.getById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update sprint (only PLANNED)")
    public ResponseEntity<ApiResponse<SprintResponse>> update(@PathVariable UUID id, @Valid @RequestBody UpdateSprintRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Sprint updated", sprintService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete sprint (only PLANNED)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        sprintService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Sprint deleted", null));
    }

    @GetMapping
    @Operation(summary = "List sprints by project")
    public ResponseEntity<ApiResponse<PageResponse<SprintResponse>>> list(
            @RequestParam UUID projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success("Sprints retrieved",
                sprintService.listByProject(projectId, PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "Start a sprint")
    public ResponseEntity<ApiResponse<SprintResponse>> start(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Sprint started", sprintService.startSprint(id)));
    }

    @PostMapping("/{id}/close")
    @Operation(summary = "Close a sprint")
    public ResponseEntity<ApiResponse<SprintResponse>> close(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Sprint closed", sprintService.closeSprint(id)));
    }
}
