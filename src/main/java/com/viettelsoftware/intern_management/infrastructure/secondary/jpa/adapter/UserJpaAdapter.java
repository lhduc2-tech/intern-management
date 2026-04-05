package com.viettelsoftware.intern_management.infrastructure.secondary.jpa.adapter;

import com.viettelsoftware.intern_management.domain.dto.UserDTO;
import com.viettelsoftware.intern_management.domain.entity.UserEntity;
import com.viettelsoftware.intern_management.domain.mapper.UserMapper;
import com.viettelsoftware.intern_management.domain.port.secondary.UserPort;
import com.viettelsoftware.intern_management.infrastructure.secondary.jpa.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class UserJpaAdapter extends BaseJPAAdapterVer2<UserEntity, UserDTO, UUID, UserMapper, UserRepository> implements UserPort {

    public UserJpaAdapter(UserRepository repository, UserMapper mapper) {
        super(repository, mapper);
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
}
