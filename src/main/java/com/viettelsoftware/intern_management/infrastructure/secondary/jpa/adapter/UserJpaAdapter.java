package com.viettelsoftware.intern_management.infrastructure.secondary.jpa.adapter;

import com.viettelsoftware.intern_management.domain.dto.UserDTO;
import com.viettelsoftware.intern_management.domain.entity.UserEntity;
import com.viettelsoftware.intern_management.domain.entity.RoleEntity;
import com.viettelsoftware.intern_management.domain.mapper.UserMapper;
import com.viettelsoftware.intern_management.domain.port.secondary.UserPort;
import com.viettelsoftware.intern_management.infrastructure.secondary.jpa.repository.RoleRepository;
import com.viettelsoftware.intern_management.infrastructure.secondary.jpa.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class UserJpaAdapter extends BaseJPAAdapterVer2<UserEntity, UserDTO, UUID, UserMapper, UserRepository> implements UserPort {

    private final RoleRepository roleRepository;

    public UserJpaAdapter(UserRepository repository, UserMapper mapper, RoleRepository roleRepository) {
        super(repository, mapper);
        this.roleRepository = roleRepository;
    }

    @Override
    public Optional<UserDTO> findByUsername(String username) {
        return repository.findByUsername(username).map(mapper::toDto);
    }

    @Override
    public Optional<UserDTO> findByEmail(String email) {
        return repository.findByEmail(email).map(mapper::toDto);
    }

    @Override
    public boolean existsByUsername(String username) {
        return repository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    @Transactional
    public UserDTO save(UserDTO userDTO) {
        UserEntity entity = mapper.toEntity(userDTO);
        entity.setRoles(resolveRoles(userDTO.getRoleCodes()));
        return mapper.toDto(repository.save(entity));
    }

    private Set<RoleEntity> resolveRoles(Set<String> roleCodes) {
        if (roleCodes == null || roleCodes.isEmpty()) {
            return new HashSet<>();
        }

        return roleCodes.stream()
                .map(code -> roleRepository.findByCode(code)
                        .orElseThrow(() -> new IllegalStateException("Role not found: " + code)))
                .collect(Collectors.toSet());
    }
}
