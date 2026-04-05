package com.viettelsoftware.intern_management.infrastructure.primary.restful.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePermissionRequest extends BasePermissionRequest {

    @NotBlank(message = "Permission code must not be blank")
    private String code;
}
