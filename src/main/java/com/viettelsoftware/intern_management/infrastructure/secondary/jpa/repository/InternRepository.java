package com.viettelsoftware.intern_management.infrastructure.secondary.jpa.repository;

import com.viettelsoftware.intern_management.domain.entity.InternEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InternRepository extends BaseJPARepository<InternEntity, UUID> {

    Optional<InternEntity> findByUserId(UUID userId);

    Optional<InternEntity> findByCompanyAccount(String companyAccount);

    Optional<InternEntity> findByIdentityNumber(String identityNumber);
}

