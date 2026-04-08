package com.viettelsoftware.intern_management.domain.service;

import com.viettelsoftware.intern_management.domain.dto.UserDTO;
import com.viettelsoftware.intern_management.domain.dto.UserSearchCriteria;
import com.viettelsoftware.intern_management.domain.entity.RoleEntity;
import com.viettelsoftware.intern_management.domain.port.primary.UserService;
import com.viettelsoftware.intern_management.domain.port.secondary.PasswordPort;
import com.viettelsoftware.intern_management.domain.port.secondary.RolePort;
import com.viettelsoftware.intern_management.domain.port.secondary.UserPort;
import com.viettelsoftware.intern_management.shared.enumeration.domain.RoleKey;
import com.viettelsoftware.intern_management.shared.enumeration.domain.StatusKey;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserPort userPort;
    private final PasswordPort passwordPort;
    private final RolePort rolePort;

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> searchUsers(UserSearchCriteria criteria, Pageable pageable) {
        return userPort.search(normalizeCriteria(criteria), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUser(UUID id) {
        return userPort.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    }

    @Override
    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        if (userDTO == null) {
            throw new IllegalArgumentException("User payload must not be null");
        }

        String username = normalizeRequired(userDTO.getUsername(), "Username must not be blank");
        String email = normalizeRequired(userDTO.getEmail(), "Email must not be blank");
        String fullName = normalizeRequired(userDTO.getFullName(), "Full name must not be blank");
        String rawPassword = normalizeRequired(userDTO.getPasswordHash(), "Password must not be blank");

        if (userPort.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }
        if (userPort.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }

        Set<String> normalizedRoles = normalizeRoles(userDTO.getRoleCodes());
        ensureRoles(normalizedRoles);

        UserDTO toSave = UserDTO.builder()
                .username(username)
                .email(email)
                .fullName(fullName)
                .status(normalizeStatus(userDTO.getStatus()))
                .roleCodes(normalizedRoles)
                // Raw password is carried here and encoded before persistence.
                .passwordHash(passwordPort.encode(rawPassword))
                .build();

        return userPort.save(toSave);
    }

    @Override
    @Transactional
    public UserDTO updateUser(UUID id, UserDTO userDTO) {
        UserDTO existing = getUser(id);
        if (userDTO == null) {
            return existing;
        }

        if (userDTO.getUsername() != null) {
            String username = normalizeRequired(userDTO.getUsername(), "Username must not be blank");
            userPort.findByUsername(username)
                    .filter(found -> !id.equals(found.getId()))
                    .ifPresent(found -> {
                        throw new IllegalArgumentException("Username already exists: " + username);
                    });
            existing.setUsername(username);
        }

        if (userDTO.getEmail() != null) {
            String email = normalizeRequired(userDTO.getEmail(), "Email must not be blank");
            userPort.findByEmail(email)
                    .filter(found -> !id.equals(found.getId()))
                    .ifPresent(found -> {
                        throw new IllegalArgumentException("Email already exists: " + email);
                    });
            existing.setEmail(email);
        }

        if (userDTO.getFullName() != null) {
            existing.setFullName(normalizeRequired(userDTO.getFullName(), "Full name must not be blank"));
        }

        if (userDTO.getStatus() != null) {
            existing.setStatus(normalizeStatus(userDTO.getStatus()));
        }

        if (userDTO.getRoleCodes() != null && !userDTO.getRoleCodes().isEmpty()) {
            Set<String> normalizedRoles = normalizeRoles(userDTO.getRoleCodes());
            ensureRoles(normalizedRoles);
            existing.setRoleCodes(normalizedRoles);
        }

        if (userDTO.getPasswordHash() != null && !userDTO.getPasswordHash().isBlank()) {
            existing.setPasswordHash(passwordPort.encode(userDTO.getPasswordHash().trim()));
        }

        return userPort.save(existing);
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        getUser(id);
        userPort.deleteById(id);
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return StatusKey.ACTIVE.getCode();
        }
        return status.trim().toUpperCase();
    }

    private String normalizeRequired(String value, String errorMessage) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(errorMessage);
        }
        return value.trim();
    }

    private Set<String> normalizeRoles(Set<String> roleCodes) {
        if (roleCodes == null) {
            return Set.of();
        }
        Set<String> normalized = roleCodes.stream()
                .filter(code -> code != null && !code.isBlank())
                .map(code -> code.trim().toUpperCase())
                .collect(Collectors.toSet());

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("At least one role is required");
        }

        return normalized;
    }

    private void ensureRoles(Set<String> roleCodes) {
        roleCodes.forEach(roleCode -> {
            RoleKey roleKey = RoleKey.fromValue(roleCode);
            rolePort.findByCode(roleCode)
                    .orElseGet(() -> rolePort.save(RoleEntity.builder()
                            .code(roleCode)
                            .name(roleCode)
                            .description(roleKey.getDescription())
                            .status(StatusKey.ACTIVE.getCode())
                            .build()));
        });
    }

    private UserSearchCriteria normalizeCriteria(UserSearchCriteria criteria) {
        UserSearchCriteria normalized = criteria == null ? new UserSearchCriteria() : criteria;

        if (normalized.getUsername() != null) {
            normalized.setUsername(normalized.getUsername().trim());
        }
        if (normalized.getEmail() != null) {
            normalized.setEmail(normalized.getEmail().trim());
        }
        if (normalized.getStatus() != null && !normalized.getStatus().isBlank()) {
            normalized.setStatus(normalizeStatus(normalized.getStatus()));
        }
        if (normalized.getRoleCode() != null) {
            normalized.setRoleCode(normalized.getRoleCode().trim().toUpperCase());
        }

        return normalized;
    }
}

