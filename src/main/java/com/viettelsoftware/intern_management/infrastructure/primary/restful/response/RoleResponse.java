package com.viettelsoftware.intern_management.infrastructure.primary.restful.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {

    private String code;

    private String name;

    private String description;

    private String status;

    @Builder.Default
    private Set<String> permissionCodes = new HashSet<>();
}

