package com.viettelsoftware.intern_management.infrastructure.secondary.jpa.repository;

import com.viettelsoftware.intern_management.domain.entity.RoleEntity;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends BaseJPARepository<RoleEntity, UUID> {
    Optional<RoleEntity> findByCode(String code);

    boolean existsByCode(String code);

    List<RoleEntity> findByCodeIn(Collection<String> codes);
}

