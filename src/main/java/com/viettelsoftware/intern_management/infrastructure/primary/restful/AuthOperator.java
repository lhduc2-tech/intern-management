package com.viettelsoftware.intern_management.infrastructure.primary.restful;

import com.viettelsoftware.intern_management.domain.dto.UserDTO;
import com.viettelsoftware.intern_management.domain.port.primary.AuthService;
import com.viettelsoftware.intern_management.infrastructure.primary.restful.request.LoginRequest;
import com.viettelsoftware.intern_management.infrastructure.primary.restful.request.RegisterRequest;
import com.viettelsoftware.intern_management.infrastructure.primary.restful.response.AuthResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
@RestController
@RequestMapping("${application.path.base.public}/auth")
public class AuthOperator {

    private final AuthService authService;

    public AuthOperator(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(toUserDTO(request));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request.getUsernameOrEmail(), request.getPassword());
        return ResponseEntity.ok(AuthResponse.builder().accessToken(token).build());
    }

    private UserDTO toUserDTO(RegisterRequest request) {
        return UserDTO.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .fullName(request.getFullName())
                // Raw password is carried here and encoded in the domain service.
                .passwordHash(request.getPassword())
                .build();
    }
}
