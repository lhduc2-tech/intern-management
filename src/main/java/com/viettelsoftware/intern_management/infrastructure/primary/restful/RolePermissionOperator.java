package com.viettelsoftware.intern_management.infrastructure.primary.restful;

import com.viettelsoftware.intern_management.domain.dto.PermissionDTO;
import com.viettelsoftware.intern_management.domain.dto.RoleDTO;
import com.viettelsoftware.intern_management.domain.port.primary.RolePermissionService;
import com.viettelsoftware.intern_management.infrastructure.primary.restful.request.CreateRoleRequest;
import com.viettelsoftware.intern_management.infrastructure.primary.restful.request.CreatePermissionRequest;
import com.viettelsoftware.intern_management.infrastructure.primary.restful.request.UpdatePermissionRequest;
import com.viettelsoftware.intern_management.infrastructure.primary.restful.request.UpdateRolePermissionsRequest;
import com.viettelsoftware.intern_management.infrastructure.primary.restful.response.PermissionResponse;
import com.viettelsoftware.intern_management.infrastructure.primary.restful.response.RoleResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${application.path.base.private}/rbac")
@RequiredArgsConstructor
public class RolePermissionOperator {

    private final RolePermissionService rolePermissionService;

    @GetMapping("/roles")
    @PreAuthorize("isAuthenticated()")
    public List<RoleResponse> getRoles() {
        return rolePermissionService.getRoles().stream().map(this::toRoleResponse).toList();
    }

    @GetMapping("/roles/{roleCode}")
    @PreAuthorize("isAuthenticated()")
    public RoleResponse getRole(@PathVariable String roleCode) {
        return toRoleResponse(rolePermissionService.getRole(roleCode));
    }

    @PostMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public RoleResponse createRole(@Valid @RequestBody CreateRoleRequest request) {
        return toRoleResponse(rolePermissionService.createRole(toRoleDTO(request)));
    }

    @PutMapping("/roles/{roleCode}/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public RoleResponse updateRolePermissions(
            @PathVariable String roleCode,
            @Valid @RequestBody UpdateRolePermissionsRequest request
    ) {
        return toRoleResponse(rolePermissionService.updateRolePermissions(roleCode, request.getPermissionCodes()));
    }

    @GetMapping("/permissions")
    @PreAuthorize("isAuthenticated()")
    public List<PermissionResponse> getPermissions() {
        return rolePermissionService.getPermissions().stream().map(this::toPermissionResponse).toList();
    }

    @PostMapping("/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public PermissionResponse createPermission(@Valid @RequestBody CreatePermissionRequest request) {
        return toPermissionResponse(rolePermissionService.createPermission(toPermissionDTO(request)));
    }

    @PutMapping("/permissions/{permissionCode}")
    @PreAuthorize("hasRole('ADMIN')")
    public PermissionResponse updatePermission(
            @PathVariable String permissionCode,
            @Valid @RequestBody UpdatePermissionRequest request
    ) {
        return toPermissionResponse(rolePermissionService.updatePermission(toPermissionDTO(permissionCode, request)));
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

    private RoleDTO toRoleDTO(CreateRoleRequest request) {
        return RoleDTO.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .build();
    }

    private RoleResponse toRoleResponse(RoleDTO dto) {
        return RoleResponse.builder()
                .code(dto.getCode())
                .name(dto.getName())
                .description(dto.getDescription())
                .status(dto.getStatus())
                .permissionCodes(dto.getPermissionCodes())
                .build();
    }

    private PermissionResponse toPermissionResponse(PermissionDTO dto) {
        return PermissionResponse.builder()
                .code(dto.getCode())
                .description(dto.getDescription())
                .build();
    }
}

