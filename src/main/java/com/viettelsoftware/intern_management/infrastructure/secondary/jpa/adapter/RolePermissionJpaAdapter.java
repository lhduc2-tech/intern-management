package com.viettelsoftware.intern_management.infrastructure.secondary.jpa.adapter;

import com.viettelsoftware.intern_management.domain.entity.PermissionEntity;
import com.viettelsoftware.intern_management.domain.entity.RoleEntity;
import com.viettelsoftware.intern_management.domain.port.secondary.RolePermissionPort;
import com.viettelsoftware.intern_management.infrastructure.secondary.jpa.repository.PermissionRepository;
import com.viettelsoftware.intern_management.infrastructure.secondary.jpa.repository.RoleRepository;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RolePermissionJpaAdapter implements RolePermissionPort {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RolePermissionJpaAdapter(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    public List<RoleEntity> findAllRoles() {
        return roleRepository.findAll().stream()
                .sorted(Comparator.comparing(RoleEntity::getCode))
                .toList();
    }

    @Override
    public Optional<RoleEntity> findRoleByCode(String code) {
        return roleRepository.findByCode(code);
    }

    @Override
    public RoleEntity saveRole(RoleEntity roleEntity) {
        return roleRepository.save(roleEntity);
    }

    @Override
    public List<PermissionEntity> findAllPermissions() {
        return permissionRepository.findAll().stream()
                .sorted(Comparator.comparing(PermissionEntity::getCode))
                .toList();
    }

    @Override
    public Optional<PermissionEntity> findPermissionByCode(String code) {
        return permissionRepository.findByCode(code);
    }

    @Override
    public boolean existsPermissionByCode(String code) {
        return permissionRepository.existsByCode(code);
    }

    @Override
    public PermissionEntity savePermission(PermissionEntity permissionEntity) {
        return permissionRepository.save(permissionEntity);
    }

    @Override
    public Set<PermissionEntity> findPermissionsByCodeIn(Set<String> codes) {
        return permissionRepository.findByCodeIn(codes).stream().collect(Collectors.toSet());
    }
}

