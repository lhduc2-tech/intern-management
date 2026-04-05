package com.viettelsoftware.intern_management.domain.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
@Builder
public class RoleManagementDTO {

    private String code;

    private String name;

    private String description;

    private String status;

    private Set<String> permissionCodes;
}

