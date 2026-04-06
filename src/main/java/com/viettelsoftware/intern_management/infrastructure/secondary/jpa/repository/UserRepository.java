package com.viettelsoftware.intern_management.infrastructure.secondary.jpa.repository;

import com.viettelsoftware.intern_management.domain.entity.UserEntity;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends BaseJPARepository<UserEntity, UUID> {
    @Override
    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    List<UserEntity> findAll();

    @Override
    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<UserEntity> findById(UUID id);

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<UserEntity> findByUsername(String username);

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<UserEntity> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
