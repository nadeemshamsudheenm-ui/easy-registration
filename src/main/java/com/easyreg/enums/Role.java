package com.easyreg.enums;

/**
 * Roles for role-based authentication and dashboard access.
 */
public enum Role {
    STUDENT("Student"),
    ACCOUNTS("Accounts Department"),
    TRANSPORT("Transport Department"),
    LAB_ASSISTANT("Laboratory Assistant"),
    HOD("Head of Department");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
