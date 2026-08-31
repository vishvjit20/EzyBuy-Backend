package com.vj.ezybuy.users.dto;

import com.vj.ezybuy.users.entity.Role;

import java.util.UUID;

public record ChangeRoleRequest(
        UUID userId,
        Role role
) {
}