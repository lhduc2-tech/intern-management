package com.viettelsoftware.intern_management.infrastructure.secondary.jpa.adapter;

import com.viettelsoftware.intern_management.domain.entity.RoleEntity;
import com.viettelsoftware.intern_management.domain.port.secondary.RolePort;
import com.viettelsoftware.intern_management.infrastructure.secondary.jpa.repository.RoleRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RoleJpaAdapter implements RolePort {

    private final RoleRepository roleRepository;

    public RoleJpaAdapter(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public Optional<RoleEntity> findByCode(String code) {
        return roleRepository.findByCode(code);
    }

    @Override
    public RoleEntity save(RoleEntity roleEntity) {
        return roleRepository.save(roleEntity);
    }
}

