package com.viettelsoftware.intern_management.wire.security;

import com.viettelsoftware.intern_management.domain.port.secondary.PasswordPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordPortImpl implements PasswordPort {

    private final PasswordEncoder passwordEncoder;

    public PasswordPortImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
