package com.devsync.orguser.settings.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SettingRequest {
    @NotBlank(message = "Setting key is required")
    private String key;
    
    @NotBlank(message = "Setting value is required")
    private String value;
}
