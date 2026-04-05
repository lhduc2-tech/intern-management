package com.viettelsoftware.intern_management.shared.enumeration.domain;

public enum RoleKey {
    ADMIN("ADMIN", "Administrator"),
    HR("HR", "Human resources"),
    MANAGER("MANAGER", "Manager"),
    MENTOR("MENTOR", "Mentor"),
    INTERN("INTERN", "Intern");

    private final String code;
    private final String description;

    RoleKey(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static RoleKey fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Role is required");
        }
        for (RoleKey roleKey : values()) {
            if (roleKey.code.equalsIgnoreCase(value.trim())) {
                return roleKey;
            }
        }
        throw new IllegalArgumentException("Unsupported role: " + value);
    }
}

