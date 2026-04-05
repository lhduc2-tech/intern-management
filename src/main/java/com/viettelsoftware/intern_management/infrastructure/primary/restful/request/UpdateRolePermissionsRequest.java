package com.viettelsoftware.intern_management.infrastructure.primary.restful.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class UpdateRolePermissionsRequest {

    @NotNull(message = "Permission codes must not be null")
    private Set<String> permissionCodes;
}

