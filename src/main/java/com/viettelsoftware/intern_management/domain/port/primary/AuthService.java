package com.viettelsoftware.intern_management.domain.port.primary;

import com.viettelsoftware.intern_management.domain.dto.UserDTO;

public interface AuthService {

    void register(UserDTO userDTO);

    String login(String usernameOrEmail, String password);
}
