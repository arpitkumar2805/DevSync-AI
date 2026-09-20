package com.devsync.monolith.dashboard.controller;

import com.devsync.common.dto.ApiResponse;
import com.devsync.monolith.dashboard.dto.DashboardResponse;
import com.devsync.monolith.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Project analytics and metrics")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Get project dashboard metrics")
    public ResponseEntity<ApiResponse<DashboardResponse>> getProjectDashboard(@PathVariable UUID projectId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Dashboard metrics retrieved",
                dashboardService.getProjectDashboard(projectId)
        ));
    }
}
