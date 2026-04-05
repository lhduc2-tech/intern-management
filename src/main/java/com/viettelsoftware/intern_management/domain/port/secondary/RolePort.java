package com.viettelsoftware.intern_management.domain.port.secondary;

import com.viettelsoftware.intern_management.domain.entity.RoleEntity;

import java.util.Optional;

@SuppressWarnings("unused")
public interface RolePort {

    Optional<RoleEntity> findByCode(String code);

    RoleEntity save(RoleEntity roleEntity);
}

