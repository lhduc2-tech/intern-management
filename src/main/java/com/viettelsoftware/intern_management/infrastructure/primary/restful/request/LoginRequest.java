package com.viettelsoftware.intern_management.infrastructure.primary.restful.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Username or email must not be blank")
    private String usernameOrEmail;

    @NotBlank(message = "Password must not be blank")
    private String password;
}
