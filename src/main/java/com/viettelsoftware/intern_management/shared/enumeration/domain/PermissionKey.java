package com.viettelsoftware.intern_management.shared.enumeration.domain;

public enum PermissionKey {
    USER_READ("USER_READ", "Read users"),
    USER_WRITE("USER_WRITE", "Create or update users"),
    ROLE_READ("ROLE_READ", "Read roles"),
    ROLE_WRITE("ROLE_WRITE", "Create or update roles"),
    INTERN_READ("INTERN_READ", "Read intern data"),
    INTERN_WRITE("INTERN_WRITE", "Create or update intern data"),
    REPORT_READ("REPORT_READ", "Read reports");

    private final String code;
    private final String description;

    PermissionKey(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}

