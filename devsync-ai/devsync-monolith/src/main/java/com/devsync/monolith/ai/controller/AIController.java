package com.devsync.monolith.ai.controller;

import com.devsync.common.dto.ApiResponse;
import com.devsync.monolith.ai.service.AIService;
import com.devsync.monolith.ai.dto.GenerateProjectRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI Assistant", description = "AI-powered Agile project management features")
@Slf4j
public class AIController {

    private final AIService aiService;

    @PostMapping("/sprint-summary/{sprintId}")
    @Operation(summary = "Generate sprint summary")
    public ResponseEntity<ApiResponse<String>> generateSprintSummary(@PathVariable UUID sprintId) {
        return ResponseEntity.ok(ApiResponse.success("Success", aiService.generateSprintSummary(sprintId, getCurrentUserId())));
    }

    @PostMapping("/estimate-points/{taskId}")
    @Operation(summary = "Auto-estimate story points")
    public ResponseEntity<ApiResponse<String>> estimatePoints(@PathVariable UUID taskId) {
        return ResponseEntity.ok(ApiResponse.success("Success", aiService.estimatePoints(taskId, getCurrentUserId())));
    }

    @PostMapping("/explain-bug/{taskId}")
    @Operation(summary = "Explain bug and suggest fixes")
    public ResponseEntity<ApiResponse<String>> explainBug(@PathVariable UUID taskId) {
        return ResponseEntity.ok(ApiResponse.success("Success", aiService.explainBug(taskId, getCurrentUserId())));
    }

    @PostMapping("/release-notes/{sprintId}")
    @Operation(summary = "Generate sprint release notes")
    public ResponseEntity<ApiResponse<String>> generateReleaseNotes(@PathVariable UUID sprintId) {
        return ResponseEntity.ok(ApiResponse.success("Success", aiService.generateReleaseNotes(sprintId, getCurrentUserId())));
    }

    @GetMapping("/project-health/{projectId}")
    @Operation(summary = "Assess project health")
    public ResponseEntity<ApiResponse<String>> projectHealth(@PathVariable UUID projectId) {
        return ResponseEntity.ok(ApiResponse.success("Success", aiService.projectHealth(projectId, getCurrentUserId())));
    }

    @PostMapping("/chat")
    @Operation(summary = "Chat with AI assistant")
    public ResponseEntity<ApiResponse<String>> chat(@RequestBody ChatRequest request) {
        log.info("[DEBUG STAGE 1] AI request received. Message: '{}', ProjectId: {}", request.getMessage(), request.getProjectId());
        log.info("[DEBUG STAGE 2] Controller entered.");
        String response = aiService.chat(request.getMessage(), request.getProjectId(), getCurrentUserId());
        log.info("[DEBUG STAGE 11] Response returned to frontend.");
        return ResponseEntity.ok(ApiResponse.success("Success", response));
    }

    @GetMapping("/deadline-risk/{projectId}")
    @Operation(summary = "Assess deadline risk")
    public ResponseEntity<ApiResponse<String>> deadlineRisk(@PathVariable UUID projectId) {
        return ResponseEntity.ok(ApiResponse.success("Success", aiService.deadlineRisk(projectId, getCurrentUserId())));
    }

    @PostMapping("/generate-docs/{projectId}")
    @Operation(summary = "Generate project documentation")
    public ResponseEntity<ApiResponse<String>> generateDocs(@PathVariable UUID projectId) {
        return ResponseEntity.ok(ApiResponse.success("Success", aiService.generateDocs(projectId, getCurrentUserId())));
    }

    @PostMapping("/task-priority/{projectId}")
    @Operation(summary = "Prioritize project tasks")
    public ResponseEntity<ApiResponse<String>> taskPriority(@PathVariable UUID projectId) {
        return ResponseEntity.ok(ApiResponse.success("Success", aiService.taskPriority(projectId, getCurrentUserId())));
    }


    @PostMapping("/generate-project")
    @Operation(summary = "Automatically generate a project, sprints, and tasks from a prompt")
    public ResponseEntity<ApiResponse<String>> generateProject(@Valid @RequestBody GenerateProjectRequest request) {
        log.info("[AI CONTROLLER] Project auto-generation request received for prompt: '{}'", request.getPrompt());
        String result = aiService.generateProjectFromPrompt(request.getPrompt(), request.getOrganizationId(), getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success("Project generated successfully", result));
    }

    private UUID getCurrentUserId() {
        return UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
    }
}

class ChatRequest {
    private String message;
    private UUID projectId;
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public UUID getProjectId() { return projectId; }
    public void setProjectId(UUID projectId) { this.projectId = projectId; }
}
