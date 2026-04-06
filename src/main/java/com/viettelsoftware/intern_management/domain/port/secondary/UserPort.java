package com.viettelsoftware.intern_management.domain.port.secondary;

import com.viettelsoftware.intern_management.domain.dto.UserDTO;
import com.viettelsoftware.intern_management.domain.dto.UserSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface UserPort {

    Optional<UserDTO> findByUsername(String username);

    Optional<UserDTO> findByEmail(String email);

    Optional<UserDTO> findById(UUID id);

    Page<UserDTO> search(UserSearchCriteria criteria, Pageable pageable);

    UserDTO save(UserDTO userDTO);

    void deleteById(UUID id);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
