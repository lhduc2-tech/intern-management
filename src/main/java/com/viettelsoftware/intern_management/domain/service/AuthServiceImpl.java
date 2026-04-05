package com.viettelsoftware.intern_management.domain.service;

import com.viettelsoftware.intern_management.domain.dto.UserDTO;
import com.viettelsoftware.intern_management.domain.port.primary.AuthService;
import com.viettelsoftware.intern_management.domain.port.secondary.JwtProviderPort;
import com.viettelsoftware.intern_management.domain.port.secondary.PasswordPort;
import com.viettelsoftware.intern_management.domain.port.secondary.UserPort;
import com.viettelsoftware.intern_management.exception.domain.AuthException;
import com.viettelsoftware.intern_management.shared.enumeration.domain.AuthErrorKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserPort userPort;
    private final PasswordPort passwordPort;
    private final JwtProviderPort jwtProviderPort;

    @Override
    @Transactional
    public void register(UserDTO userDTO) {
        if (userPort.existsByUsername(userDTO.getUsername())) {
            throw new AuthException("Username already exists", AuthErrorKey.USERNAME_ALREADY_EXISTS.getValue());
        }
        if (userPort.existsByEmail(userDTO.getEmail())) {
            throw new AuthException("Email already exists", AuthErrorKey.EMAIL_ALREADY_EXISTS.getValue());
        }

        UserDTO userToSave = UserDTO.builder()
                .username(userDTO.getUsername())
                .email(userDTO.getEmail())
                .fullName(userDTO.getFullName())
                .status("ACTIVE")
                .passwordHash(passwordPort.encode(userDTO.getPasswordHash()))
                .build();

        userPort.save(userToSave);
    }

    @Override
    @Transactional
    public String login(String usernameOrEmail, String password) {
        UserDTO userDTO = userPort.findByUsername(usernameOrEmail)
                .orElseGet(() -> userPort.findByEmail(usernameOrEmail)
                        .orElseThrow(() -> new AuthException("Invalid credentials", AuthErrorKey.INVALID_CREDENTIALS.getValue())));

        if (!passwordPort.matches(password, userDTO.getPasswordHash())) {
            throw new AuthException("Invalid credentials", AuthErrorKey.INVALID_CREDENTIALS.getValue());
        }

        userDTO.setLastLoginAt(OffsetDateTime.now());
        userPort.save(userDTO);

        return jwtProviderPort.generateToken(userDTO);
    }
}
