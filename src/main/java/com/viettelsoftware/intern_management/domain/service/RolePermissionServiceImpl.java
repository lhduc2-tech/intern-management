package com.viettelsoftware.intern_management.domain.service;

import com.viettelsoftware.intern_management.domain.dto.PermissionDTO;
import com.viettelsoftware.intern_management.domain.dto.RoleDTO;
import com.viettelsoftware.intern_management.domain.entity.PermissionEntity;
import com.viettelsoftware.intern_management.domain.entity.RoleEntity;
import com.viettelsoftware.intern_management.domain.mapper.PermissionMapper;
import com.viettelsoftware.intern_management.domain.mapper.RoleMapper;
import com.viettelsoftware.intern_management.domain.port.primary.RolePermissionService;
import com.viettelsoftware.intern_management.domain.port.secondary.RolePermissionPort;
import com.viettelsoftware.intern_management.shared.enumeration.domain.RoleKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RolePermissionServiceImpl implements RolePermissionService {

    private final RolePermissionPort rolePermissionPort;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;

    @Override
    @Transactional(readOnly = true)
    public List<RoleDTO> getRoles() {
        return rolePermissionPort.findAllRoles().stream().map(roleMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RoleDTO getRole(String roleCode) {
        String normalizedCode = normalizeCode(roleCode);
        validateSupportedRole(normalizedCode);
        RoleEntity roleEntity = rolePermissionPort.findRoleByCode(normalizedCode)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + normalizedCode));
        return roleMapper.toDto(roleEntity);
    }

    @Override
    @Transactional
    public RoleDTO createRole(RoleDTO roleDTO) {
        String normalizedCode = normalizeCode(roleDTO.getCode());
        validateSupportedRole(normalizedCode);

        if (rolePermissionPort.findRoleByCode(normalizedCode).isPresent()) {
            throw new IllegalArgumentException("Role already exists: " + normalizedCode);
        }

        RoleKey roleKey = RoleKey.fromValue(normalizedCode);
        RoleEntity roleEntity = RoleEntity.builder()
                .code(normalizedCode)
                .name(normalizeName(roleDTO.getName(), normalizedCode))
                .description(roleDTO.getDescription() != null ? roleDTO.getDescription() : roleKey.getDescription())
                .status("ACTIVE")
                .build();

        return roleMapper.toDto(rolePermissionPort.saveRole(roleEntity));
    }

    @Override
    @Transactional
    public RoleDTO updateRolePermissions(String roleCode, Set<String> permissionCodes) {
        String normalizedCode = normalizeCode(roleCode);
        validateSupportedRole(normalizedCode);

        RoleEntity roleEntity = rolePermissionPort.findRoleByCode(normalizedCode)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + normalizedCode));

        Set<String> normalizedPermissions = normalizeCodes(permissionCodes);
        Set<PermissionEntity> permissionEntities = rolePermissionPort.findPermissionsByCodeIn(normalizedPermissions);

        if (permissionEntities.size() != normalizedPermissions.size()) {
            Set<String> existing = permissionEntities.stream().map(PermissionEntity::getCode).collect(Collectors.toSet());
            Set<String> missing = new HashSet<>(normalizedPermissions);
            missing.removeAll(existing);
            throw new IllegalArgumentException("Permission not found: " + String.join(", ", missing));
        }

        roleEntity.setPermissions(permissionEntities);
        return roleMapper.toDto(rolePermissionPort.saveRole(roleEntity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionDTO> getPermissions() {
        return rolePermissionPort.findAllPermissions().stream().map(permissionMapper::toDto).toList();
    }

    @Override
    @Transactional
    public PermissionDTO createPermission(PermissionDTO permissionDTO) {
        String normalizedCode = normalizeCode(permissionDTO.getCode());
        if (rolePermissionPort.existsPermissionByCode(normalizedCode)) {
            throw new IllegalArgumentException("Permission already exists: " + normalizedCode);
        }

        PermissionEntity permissionEntity = PermissionEntity.builder()
                .code(normalizedCode)
                .description(permissionDTO.getDescription())
                .build();

        return permissionMapper.toDto(rolePermissionPort.savePermission(permissionEntity));
    }

    @Override
    @Transactional
    public PermissionDTO updatePermission(PermissionDTO permissionDTO) {
        String normalizedCode = normalizeCode(permissionDTO.getCode());
        PermissionEntity permissionEntity = rolePermissionPort.findPermissionByCode(normalizedCode)
                .orElseThrow(() -> new IllegalArgumentException("Permission not found: " + normalizedCode));

        permissionEntity.setDescription(permissionDTO.getDescription());
        return permissionMapper.toDto(rolePermissionPort.savePermission(permissionEntity));
    }

    private String normalizeCode(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Code must not be blank");
        }
        return input.trim().toUpperCase();
    }

    private Set<String> normalizeCodes(Set<String> input) {
        if (input == null) {
            return Set.of();
        }
        return input.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(this::normalizeCode)
                .collect(Collectors.toSet());
    }

    private void validateSupportedRole(String roleCode) {
        RoleKey.fromValue(roleCode);
    }

    private String normalizeName(String input, String fallback) {
        if (input == null || input.isBlank()) {
            return fallback;
        }
        return input.trim();
    }
}

