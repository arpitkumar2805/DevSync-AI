package com.devsync.monolith.task.controller;

import com.devsync.common.dto.ApiResponse;
import com.devsync.common.dto.PageResponse;
import com.devsync.monolith.task.dto.*;
import com.devsync.monolith.task.service.TaskService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Task management endpoints")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @Operation(summary = "Create a new task")
    public ResponseEntity<ApiResponse<TaskResponse>> create(@Valid @RequestBody CreateTaskRequest request) {
        UUID reporterId = getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Task created", taskService.create(request, reporterId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID")
    public ResponseEntity<ApiResponse<TaskResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Task found", taskService.getById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update task")
    public ResponseEntity<ApiResponse<TaskResponse>> update(@PathVariable UUID id, @Valid @RequestBody UpdateTaskRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Task updated", taskService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete task")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        taskService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Task deleted", null));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update task status")
    public ResponseEntity<ApiResponse<TaskResponse>> updateStatus(@PathVariable UUID id, @Valid @RequestBody UpdateTaskStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Status updated", taskService.updateStatus(id, request)));
    }

    @PutMapping("/{id}/assign")
    @Operation(summary = "Assign task to user")
    public ResponseEntity<ApiResponse<TaskResponse>> assign(@PathVariable UUID id, @Valid @RequestBody AssignTaskRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Task assigned", taskService.assignTask(id, request)));
    }

    @GetMapping
    @Operation(summary = "List tasks by project, sprint, or assignee")
    public ResponseEntity<ApiResponse<PageResponse<TaskResponse>>> list(
            @RequestParam(required = false) UUID projectId,
            @RequestParam(required = false) UUID sprintId,
            @RequestParam(required = false) UUID assigneeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PageResponse<TaskResponse> result;
        if (sprintId != null) {
            result = taskService.listBySprint(sprintId, pageRequest);
        } else if (assigneeId != null) {
            result = taskService.listByAssignee(assigneeId, pageRequest);
        } else if (projectId != null) {
            result = taskService.listByProject(projectId, pageRequest);
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("At least one of projectId, sprintId, or assigneeId is required"));
        }
        return ResponseEntity.ok(ApiResponse.success("Tasks retrieved", result));
    }

    @GetMapping("/{id}/subtasks")
    @Operation(summary = "Get subtasks of a task")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getSubtasks(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Subtasks retrieved", taskService.getSubtasks(id)));
    }

    @PostMapping("/{id}/dependencies")
    @Operation(summary = "Add task dependency")
    public ResponseEntity<ApiResponse<TaskDependencyResponse>> addDependency(@PathVariable UUID id, @Valid @RequestBody AddDependencyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Dependency added", taskService.addDependency(id, request)));
    }

    @DeleteMapping("/{id}/dependencies/{depTaskId}")
    @Operation(summary = "Remove task dependency")
    public ResponseEntity<ApiResponse<Void>> removeDependency(@PathVariable UUID id, @PathVariable UUID depTaskId) {
        taskService.removeDependency(id, depTaskId);
        return ResponseEntity.ok(ApiResponse.success("Dependency removed", null));
    }

    @GetMapping("/{id}/dependencies")
    @Operation(summary = "Get task dependencies")
    public ResponseEntity<ApiResponse<List<TaskDependencyResponse>>> getDependencies(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Dependencies retrieved", taskService.getDependencies(id)));
    }

    private UUID getCurrentUserId() {
        return UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
    }
}
