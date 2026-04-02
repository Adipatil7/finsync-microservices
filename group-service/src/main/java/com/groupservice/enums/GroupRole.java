package com.groupservice.enums;

import com.groupservice.exception.InvalidRoleException;

public enum GroupRole {

    ADMIN,
    MEMBER;

    public static GroupRole fromString(String role) {
        if (role == null || role.isBlank()) {
            return MEMBER;
        }
        try {
            return GroupRole.valueOf(role.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new InvalidRoleException("Invalid role: '" + role + "'. Allowed values: ADMIN, MEMBER");
        }
    }
}
