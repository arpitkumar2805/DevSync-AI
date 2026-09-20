package com.devsync.orguser.settings.controller;

import com.devsync.common.dto.ApiResponse;
import com.devsync.orguser.settings.dto.SettingRequest;
import com.devsync.orguser.settings.dto.SettingResponse;
import com.devsync.orguser.settings.service.SettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
@Tag(name = "Settings", description = "Organization and user settings")
public class SettingController {

    private final SettingService settingService;

    @GetMapping("/org/{orgId}")
    @Operation(summary = "Get organization settings")
    public ResponseEntity<ApiResponse<List<SettingResponse>>> getOrgSettings(@PathVariable UUID orgId) {
        return ResponseEntity.ok(ApiResponse.success("Organization settings retrieved", settingService.getOrgSettings(orgId)));
    }

    @PutMapping("/org/{orgId}")
    @Operation(summary = "Save organization setting")
    public ResponseEntity<ApiResponse<SettingResponse>> saveOrgSetting(
            @PathVariable UUID orgId,
            @Valid @RequestBody SettingRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Organization setting saved", settingService.saveOrgSetting(orgId, request)));
    }

    @GetMapping("/user")
    @Operation(summary = "Get current user settings")
    public ResponseEntity<ApiResponse<List<SettingResponse>>> getUserSettings() {
        UUID userId = getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("User settings retrieved", settingService.getUserSettings(userId)));
    }

    @PutMapping("/user")
    @Operation(summary = "Save user setting")
    public ResponseEntity<ApiResponse<SettingResponse>> saveUserSetting(
            @Valid @RequestBody SettingRequest request) {
        UUID userId = getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("User setting saved", settingService.saveUserSetting(userId, request)));
    }

    private UUID getCurrentUserId() {
        return UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
    }
}
