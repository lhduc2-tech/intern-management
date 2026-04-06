package com.viettelsoftware.intern_management.infrastructure.primary.restful.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class UserSearchRequest extends BaseRequest {

    private String username;

    private String email;

    private String status;

    private String roleCode;
}

