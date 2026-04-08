package com.viettelsoftware.intern_management.shared.enumeration.domain;

public enum StatusKey {
    ACTIVE("ACTIVE");

    private final String code;

    StatusKey(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}

