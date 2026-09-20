package com.devsync.orguser.settings.service;

import com.devsync.orguser.settings.dto.SettingRequest;
import com.devsync.orguser.settings.dto.SettingResponse;
import com.devsync.orguser.settings.entity.Setting;
import com.devsync.orguser.settings.repository.SettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SettingService {

    private final SettingRepository settingRepository;

    @Transactional
    public SettingResponse saveOrgSetting(UUID orgId, SettingRequest request) {
        Setting setting = settingRepository.findByOrgIdAndKeyAndDeletedFalse(orgId, request.getKey())
                .orElseGet(() -> Setting.builder().orgId(orgId).key(request.getKey()).build());

        setting.setValue(request.getValue());
        setting = settingRepository.save(setting);
        return toResponse(setting);
    }

    public List<SettingResponse> getOrgSettings(UUID orgId) {
        return settingRepository.findByOrgIdAndDeletedFalse(orgId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public SettingResponse saveUserSetting(UUID userId, SettingRequest request) {
        Setting setting = settingRepository.findByUserIdAndKeyAndDeletedFalse(userId, request.getKey())
                .orElseGet(() -> Setting.builder().userId(userId).key(request.getKey()).build());

        setting.setValue(request.getValue());
        setting = settingRepository.save(setting);
        return toResponse(setting);
    }

    public List<SettingResponse> getUserSettings(UUID userId) {
        return settingRepository.findByUserIdAndDeletedFalse(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private SettingResponse toResponse(Setting setting) {
        return SettingResponse.builder()
                .id(setting.getId())
                .orgId(setting.getOrgId())
                .userId(setting.getUserId())
                .key(setting.getKey())
                .value(setting.getValue())
                .build();
    }
}
