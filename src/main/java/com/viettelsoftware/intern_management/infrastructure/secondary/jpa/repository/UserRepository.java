package com.viettelsoftware.intern_management.infrastructure.secondary.jpa.repository;

import com.viettelsoftware.intern_management.domain.entity.UserEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends BaseJPARepository<UserEntity, UUID> {
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
