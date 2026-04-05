package com.viettelsoftware.intern_management.domain.port.primary;

import com.viettelsoftware.intern_management.domain.dto.PermissionDTO;
import com.viettelsoftware.intern_management.domain.dto.RoleManagementDTO;

import java.util.List;
import java.util.Set;

public interface RolePermissionService {

    List<RoleManagementDTO> getRoles();

    RoleManagementDTO getRole(String roleCode);

    RoleManagementDTO updateRolePermissions(String roleCode, Set<String> permissionCodes);

    List<PermissionDTO> getPermissions();

    PermissionDTO createPermission(PermissionDTO permissionDTO);

    PermissionDTO updatePermission(PermissionDTO permissionDTO);
}

