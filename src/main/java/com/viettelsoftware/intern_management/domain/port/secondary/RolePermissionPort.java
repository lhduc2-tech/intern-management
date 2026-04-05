package com.viettelsoftware.intern_management.domain.port.secondary;

import com.viettelsoftware.intern_management.domain.entity.PermissionEntity;
import com.viettelsoftware.intern_management.domain.entity.RoleEntity;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RolePermissionPort {

    List<RoleEntity> findAllRoles();

    Optional<RoleEntity> findRoleByCode(String code);

    RoleEntity saveRole(RoleEntity roleEntity);

    List<PermissionEntity> findAllPermissions();

    Optional<PermissionEntity> findPermissionByCode(String code);

    boolean existsPermissionByCode(String code);

    PermissionEntity savePermission(PermissionEntity permissionEntity);

    Set<PermissionEntity> findPermissionsByCodeIn(Set<String> codes);
}

