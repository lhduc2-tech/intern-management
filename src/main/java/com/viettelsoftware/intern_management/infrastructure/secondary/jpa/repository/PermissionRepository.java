package com.viettelsoftware.intern_management.infrastructure.secondary.jpa.repository;

import com.viettelsoftware.intern_management.domain.entity.PermissionEntity;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PermissionRepository extends BaseJPARepository<PermissionEntity, UUID> {
    Optional<PermissionEntity> findByCode(String code);

    boolean existsByCode(String code);

    List<PermissionEntity> findByCodeIn(Collection<String> codes);
}

