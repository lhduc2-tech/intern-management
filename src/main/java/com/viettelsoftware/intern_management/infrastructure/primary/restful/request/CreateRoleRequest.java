package com.viettelsoftware.intern_management.infrastructure.primary.restful.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateRoleRequest {

    @NotBlank(message = "Role code must not be blank")
    private String code;

    @Size(max = 100, message = "Role name must not exceed 100 characters")
    private String name;

    private String description;
}

