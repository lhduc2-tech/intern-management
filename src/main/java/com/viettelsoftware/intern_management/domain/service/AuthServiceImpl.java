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

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserPort userPort;
    private final PasswordPort passwordPort;
    private final JwtProviderPort jwtProviderPort;

    @Override
    public void register(String username, String email, String password) {
        if (userPort.existsByUsername(username)) {
            throw new AuthException("Username already exists", AuthErrorKey.USERNAME_ALREADY_EXISTS.getValue());
        }
        if (userPort.existsByEmail(email)) {
            throw new AuthException("Email already exists", AuthErrorKey.EMAIL_ALREADY_EXISTS.getValue());
        }

        UserDTO userDTO = UserDTO.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordPort.encode(password))
                .build();

        userPort.save(userDTO);
    }

    @Override
    public String login(String usernameOrEmail, String password) {
        UserDTO userDTO = userPort.findByUsername(usernameOrEmail)
                .orElseGet(() -> userPort.findByEmail(usernameOrEmail)
                        .orElseThrow(() -> new AuthException("Invalid credentials", AuthErrorKey.INVALID_CREDENTIALS.getValue())));

        if (!passwordPort.matches(password, userDTO.getPasswordHash())) {
            throw new AuthException("Invalid credentials", AuthErrorKey.INVALID_CREDENTIALS.getValue());
        }

        return jwtProviderPort.generateToken(userDTO);
    }
}
