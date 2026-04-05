package com.viettelsoftware.intern_management.infrastructure.primary.restful;

import com.viettelsoftware.intern_management.domain.dto.PermissionDTO;
import com.viettelsoftware.intern_management.domain.dto.RoleManagementDTO;
import com.viettelsoftware.intern_management.domain.port.primary.RolePermissionService;
import com.viettelsoftware.intern_management.infrastructure.primary.restful.request.CreatePermissionRequest;
import com.viettelsoftware.intern_management.infrastructure.primary.restful.request.UpdatePermissionRequest;
import com.viettelsoftware.intern_management.infrastructure.primary.restful.request.UpdateRolePermissionsRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${application.path.base.private}/rbac")
public class RolePermissionOperator {

    private final RolePermissionService rolePermissionService;

    public RolePermissionOperator(RolePermissionService rolePermissionService) {
        this.rolePermissionService = rolePermissionService;
    }

    @GetMapping("/roles")
    @PreAuthorize("isAuthenticated()")
    public List<RoleManagementDTO> getRoles() {
        return rolePermissionService.getRoles();
    }

    @GetMapping("/roles/{roleCode}")
    @PreAuthorize("isAuthenticated()")
    public RoleManagementDTO getRole(@PathVariable String roleCode) {
        return rolePermissionService.getRole(roleCode);
    }

    @PutMapping("/roles/{roleCode}/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public RoleManagementDTO updateRolePermissions(
            @PathVariable String roleCode,
            @Valid @RequestBody UpdateRolePermissionsRequest request
    ) {
        return rolePermissionService.updateRolePermissions(roleCode, request.getPermissionCodes());
    }

    @GetMapping("/permissions")
    @PreAuthorize("isAuthenticated()")
    public List<PermissionDTO> getPermissions() {
        return rolePermissionService.getPermissions();
    }

    @PostMapping("/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public PermissionDTO createPermission(@Valid @RequestBody CreatePermissionRequest request) {
        return rolePermissionService.createPermission(toPermissionDTO(request));
    }

    @PutMapping("/permissions/{permissionCode}")
    @PreAuthorize("hasRole('ADMIN')")
    public PermissionDTO updatePermission(
            @PathVariable String permissionCode,
            @Valid @RequestBody UpdatePermissionRequest request
    ) {
        return rolePermissionService.updatePermission(toPermissionDTO(permissionCode, request));
    }

    private PermissionDTO toPermissionDTO(CreatePermissionRequest request) {
        return PermissionDTO.builder()
                .code(request.getCode())
                .description(request.getDescription())
                .build();
    }

    private PermissionDTO toPermissionDTO(String permissionCode, UpdatePermissionRequest request) {
        return PermissionDTO.builder()
                .code(permissionCode)
                .description(request.getDescription())
                .build();
    }
}

