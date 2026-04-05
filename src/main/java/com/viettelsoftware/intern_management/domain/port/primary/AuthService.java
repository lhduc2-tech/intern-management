package com.viettelsoftware.intern_management.domain.port.primary;

public interface AuthService {

    void register(String username, String email, String password);

    String login(String usernameOrEmail, String password);
}
