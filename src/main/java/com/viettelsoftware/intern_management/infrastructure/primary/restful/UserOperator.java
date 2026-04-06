package com.viettelsoftware.intern_management.infrastructure.primary.restful;

import com.viettelsoftware.intern_management.domain.dto.UserDTO;
import com.viettelsoftware.intern_management.domain.dto.UserSearchCriteria;
import com.viettelsoftware.intern_management.domain.port.primary.UserService;
import com.viettelsoftware.intern_management.infrastructure.primary.restful.request.UserSearchRequest;
import com.viettelsoftware.intern_management.infrastructure.primary.restful.request.UpdateUserRequest;
import com.viettelsoftware.intern_management.infrastructure.primary.restful.response.PageResponse;
import com.viettelsoftware.intern_management.infrastructure.primary.restful.response.UserResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("${application.path.base.private}/users")
public class UserOperator {

    private final UserService userService;

    public UserOperator(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<UserResponse> searchUsers(@Valid @RequestBody(required = false) UserSearchRequest request) {
        UserSearchRequest safeRequest = request == null ? new UserSearchRequest() : request;
        return PageResponse.of(userService.searchUsers(toCriteria(safeRequest), safeRequest.toPageable()), this::toResponse);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getUser(@PathVariable UUID id) {
        return toResponse(userService.getUser(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse updateUser(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest request) {
        return toResponse(userService.updateUser(id, toUpdateDTO(request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
    }


    private UserDTO toUpdateDTO(UpdateUserRequest request) {
        return UserDTO.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .fullName(request.getFullName())
                .status(request.getStatus())
                .roleCodes(request.getRoleCodes())
                // Raw password is carried here and encoded in the domain service.
                .passwordHash(request.getPassword())
                .build();
    }

    private UserSearchCriteria toCriteria(UserSearchRequest request) {
        return UserSearchCriteria.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .status(request.getStatus())
                .roleCode(request.getRoleCode())
                .build();
    }

    private UserResponse toResponse(UserDTO dto) {
        return UserResponse.builder()
                .id(dto.getId())
                .username(dto.getUsername())
                .email(dto.getEmail())
                .fullName(dto.getFullName())
                .status(dto.getStatus())
                .lastLoginAt(dto.getLastLoginAt())
                .roleCodes(dto.getRoleCodes())
                .permissionCodes(dto.getPermissionCodes())
                .createdAt(dto.getCreatedAt())
                .createdBy(dto.getCreatedBy())
                .updatedAt(dto.getUpdatedAt())
                .updatedBy(dto.getUpdatedBy())
                .build();
    }
}

