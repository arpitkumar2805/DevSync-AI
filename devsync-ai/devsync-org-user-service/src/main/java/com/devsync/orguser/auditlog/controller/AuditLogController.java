package com.devsync.orguser.auditlog.controller;

import com.devsync.common.dto.ApiResponse;
import com.devsync.common.dto.PageResponse;
import com.devsync.orguser.auditlog.dto.AuditLogResponse;
import com.devsync.orguser.auditlog.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "System security and audit logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @Operation(summary = "List all audit logs (Admin only)")
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        // In a real implementation, you would restrict this to SUPER_ADMIN
        PageResponse<AuditLogResponse> result = auditLogService.listAllLogs(
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
                
        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved", result));
    }
}
