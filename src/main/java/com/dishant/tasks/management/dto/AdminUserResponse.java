package com.dishant.tasks.management.dto;

import com.dishant.tasks.management.model.Role;
import com.dishant.tasks.management.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AdminUserResponse {
    private Long id;
    private String username;
    private String email;
    private Role role;
    private boolean enabled;

    public static AdminUserResponse from(User user) {
        return AdminUserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .build();
    }
}
