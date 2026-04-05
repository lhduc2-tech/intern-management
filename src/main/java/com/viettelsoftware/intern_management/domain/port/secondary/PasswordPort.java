package com.viettelsoftware.intern_management.domain.port.secondary;

public interface PasswordPort {

    String encode(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}
