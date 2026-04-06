package com.viettelsoftware.intern_management.infrastructure.secondary.jpa.adapter;

import com.viettelsoftware.intern_management.domain.dto.UserDTO;
import com.viettelsoftware.intern_management.domain.dto.UserSearchCriteria;
import com.viettelsoftware.intern_management.domain.entity.UserEntity;
import com.viettelsoftware.intern_management.domain.entity.RoleEntity;
import com.viettelsoftware.intern_management.domain.mapper.UserMapper;
import com.viettelsoftware.intern_management.domain.port.secondary.UserPort;
import com.viettelsoftware.intern_management.infrastructure.secondary.jpa.repository.RoleRepository;
import com.viettelsoftware.intern_management.infrastructure.secondary.jpa.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
    public Optional<UserDTO> findById(UUID id) {
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    public Page<UserDTO> search(UserSearchCriteria criteria, Pageable pageable) {
        Specification<UserEntity> specification = buildSpecification(criteria);
        return repository.findAll(specification, pageable).map(mapper::toDto);
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
        UserEntity saved = repository.saveAndFlush(entity);
        return repository.findById(saved.getId())
                .map(mapper::toDto)
                .orElseGet(() -> mapper.toDto(saved));
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        repository.deleteById(id);
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

    private Specification<UserEntity> buildSpecification(UserSearchCriteria criteria) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();

            if (criteria == null) {
                return predicate;
            }
            if (criteria.getUsername() != null && !criteria.getUsername().isBlank()) {
                predicate = cb.and(predicate, cb.like(cb.lower(root.get("username")), "%" + criteria.getUsername().trim().toLowerCase() + "%"));
            }
            if (criteria.getEmail() != null && !criteria.getEmail().isBlank()) {
                predicate = cb.and(predicate, cb.like(cb.lower(root.get("email")), "%" + criteria.getEmail().trim().toLowerCase() + "%"));
            }
            if (criteria.getStatus() != null && !criteria.getStatus().isBlank()) {
                predicate = cb.and(predicate, cb.equal(cb.upper(root.get("status")), criteria.getStatus().trim().toUpperCase()));
            }
            if (criteria.getRoleCode() != null && !criteria.getRoleCode().isBlank()) {
                predicate = cb.and(predicate, cb.equal(cb.upper(root.join("roles").get("code")), criteria.getRoleCode().trim().toUpperCase()));
                query.distinct(true);
            }

            return predicate;
        };
    }
}
