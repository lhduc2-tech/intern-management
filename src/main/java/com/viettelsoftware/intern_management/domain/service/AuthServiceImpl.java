package com.viettelsoftware.intern_management.domain.service;

import com.viettelsoftware.intern_management.domain.dto.UserDTO;
import com.viettelsoftware.intern_management.domain.entity.RoleEntity;
import com.viettelsoftware.intern_management.domain.port.primary.AuthService;
import com.viettelsoftware.intern_management.domain.port.secondary.JwtProviderPort;
import com.viettelsoftware.intern_management.domain.port.secondary.PasswordPort;
import com.viettelsoftware.intern_management.domain.port.secondary.RolePort;
import com.viettelsoftware.intern_management.domain.port.secondary.UserPort;
import com.viettelsoftware.intern_management.exception.domain.AuthException;
import com.viettelsoftware.intern_management.shared.enumeration.domain.AuthErrorKey;
import com.viettelsoftware.intern_management.shared.enumeration.domain.RoleKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserPort userPort;
    private final PasswordPort passwordPort;
    private final JwtProviderPort jwtProviderPort;
    private final RolePort rolePort;

    @Override
    @Transactional
    public void register(UserDTO userDTO) {
        if (userPort.existsByUsername(userDTO.getUsername())) {
            throw new AuthException("Username already exists", AuthErrorKey.USERNAME_ALREADY_EXISTS.getValue());
        }
        if (userPort.existsByEmail(userDTO.getEmail())) {
            throw new AuthException("Email already exists", AuthErrorKey.EMAIL_ALREADY_EXISTS.getValue());
        }

        Set<String> roleCodes = normalizeRoles(userDTO.getRoleCodes());
        if (roleCodes.isEmpty()) {
            throw new AuthException("Role must not be blank", AuthErrorKey.INVALID_ROLE.getValue());
        }
        roleCodes.forEach(roleCode -> {
            try {
                RoleKey.fromValue(roleCode);
            } catch (IllegalArgumentException ex) {
                throw new AuthException("Invalid role: " + roleCode, AuthErrorKey.INVALID_ROLE.getValue());
            }
            rolePort.findByCode(roleCode).orElseGet(() -> rolePort.save(RoleEntity.builder()
                    .code(roleCode)
                    .name(roleCode)
                    .description(RoleKey.fromValue(roleCode).getDescription())
                    .status("ACTIVE")
                    .build()));
        });

        UserDTO userToSave = UserDTO.builder()
                .username(userDTO.getUsername())
                .email(userDTO.getEmail())
                .fullName(userDTO.getFullName())
                .status("ACTIVE")
                .roleCodes(roleCodes)
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

    private Set<String> normalizeRoles(Set<String> roleCodes) {
        if (roleCodes == null) {
            return Set.of();
        }

        return roleCodes.stream()
                .filter(code -> code != null && !code.isBlank())
                .map(code -> code.trim().toUpperCase())
                .collect(Collectors.toSet());
    }
}
