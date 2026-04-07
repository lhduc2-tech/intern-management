package com.viettelsoftware.intern_management.infrastructure.secondary.jpa.repository;

import com.viettelsoftware.intern_management.domain.entity.InternshipSessionEntity;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InternshipSessionRepository extends BaseJPARepository<InternshipSessionEntity, UUID> {
}

