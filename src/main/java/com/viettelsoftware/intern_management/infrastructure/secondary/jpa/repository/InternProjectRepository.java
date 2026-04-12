package com.viettelsoftware.intern_management.infrastructure.secondary.jpa.repository;

import com.viettelsoftware.intern_management.domain.entity.InternProjectEntity;
import com.viettelsoftware.intern_management.domain.entity.InternProjectId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface InternProjectRepository extends JpaRepository<InternProjectEntity, InternProjectId> {

    List<InternProjectEntity> findByInternId(UUID internId);

    List<InternProjectEntity> findByInternIdIn(Collection<UUID> internIds);

    void deleteByInternId(UUID internId);
}

