package com.devsync.orguser.settings.dto;

import lombok.*;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SettingResponse {
    private UUID id;
    private UUID orgId;
    private UUID userId;
    private String key;
    private String value;
}
