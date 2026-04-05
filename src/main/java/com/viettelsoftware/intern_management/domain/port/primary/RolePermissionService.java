package com.viettelsoftware.intern_management.domain.port.primary;

import com.viettelsoftware.intern_management.domain.dto.PermissionDTO;
import com.viettelsoftware.intern_management.domain.dto.RoleDTO;

import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
public interface RolePermissionService {

    List<RoleDTO> getRoles();

    RoleDTO getRole(String roleCode);

    RoleDTO createRole(RoleDTO roleDTO);

    RoleDTO updateRolePermissions(String roleCode, Set<String> permissionCodes);

    List<PermissionDTO> getPermissions();

    PermissionDTO createPermission(PermissionDTO permissionDTO);

    PermissionDTO updatePermission(PermissionDTO permissionDTO);
}

