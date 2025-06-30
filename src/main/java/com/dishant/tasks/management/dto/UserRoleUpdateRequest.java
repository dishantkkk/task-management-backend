package com.dishant.tasks.management.dto;

import com.dishant.tasks.management.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserRoleUpdateRequest {
    private Role role;
}

