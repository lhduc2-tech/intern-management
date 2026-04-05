package com.viettelsoftware.intern_management.domain.port.secondary;

import com.viettelsoftware.intern_management.domain.dto.UserDTO;

public interface JwtProviderPort {

    String generateToken(UserDTO userDTO);
}
