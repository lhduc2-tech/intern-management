package com.viettelsoftware.intern_management.domain.port.secondary;

import com.viettelsoftware.intern_management.domain.dto.UserDTO;

import java.util.Optional;

public interface UserPort {

    Optional<UserDTO> findByUsername(String username);

    Optional<UserDTO> findByEmail(String email);

    UserDTO save(UserDTO userDTO);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
