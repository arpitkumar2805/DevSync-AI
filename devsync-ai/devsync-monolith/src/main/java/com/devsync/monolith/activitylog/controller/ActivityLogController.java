package com.devsync.monolith.activitylog.controller;

import com.devsync.common.dto.ApiResponse;
import com.devsync.common.dto.PageResponse;
import com.devsync.monolith.activitylog.dto.ActivityLogResponse;
import com.devsync.monolith.activitylog.service.ActivityLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/activity")
@RequiredArgsConstructor
@Tag(name = "Activity Logs", description = "Timeline and activity tracking")
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    @GetMapping
    @Operation(summary = "Get activity logs for a specific entity")
    public ResponseEntity<ApiResponse<PageResponse<ActivityLogResponse>>> getActivity(
            @RequestParam String entityType,
            @RequestParam UUID entityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        PageResponse<ActivityLogResponse> result = activityLogService.getEntityActivity(
                entityType, entityId, PageRequest.of(page, size, Sort.by("createdAt").descending()));
                
        return ResponseEntity.ok(ApiResponse.success("Activity logs retrieved", result));
    }
}
