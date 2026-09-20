package com.devsync.monolith.user.dto;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UpdateUserRequest {
    private String firstName;
    private String lastName;
    private String avatarUrl;
}
