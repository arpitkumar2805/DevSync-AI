package com.devsync.monolith.ai.controller;

import com.devsync.common.dto.ApiResponse;
import com.devsync.monolith.ai.service.AIService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI Assistant", description = "AI-powered Agile project management features")
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
        return ResponseEntity.ok(ApiResponse.success("Success", aiService.chat(request.getMessage(), getCurrentUserId())));
    }

    private UUID getCurrentUserId() {
        return UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
    }
}

class ChatRequest {
    private String message;
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
