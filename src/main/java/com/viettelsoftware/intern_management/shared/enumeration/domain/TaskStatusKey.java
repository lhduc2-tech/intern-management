package com.viettelsoftware.intern_management.shared.enumeration.domain;

public enum TaskStatusKey {
    TODO("TODO");

    private final String code;

    TaskStatusKey(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}

