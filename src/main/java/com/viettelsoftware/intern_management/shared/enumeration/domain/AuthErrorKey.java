package com.viettelsoftware.intern_management.shared.enumeration.domain;

public enum AuthErrorKey {

    USERNAME_ALREADY_EXISTS("USERNAME_ALREADY_EXISTS"),
    EMAIL_ALREADY_EXISTS("EMAIL_ALREADY_EXISTS"),
    INVALID_CREDENTIALS("INVALID_CREDENTIALS");

    private final String value;

    AuthErrorKey(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
