package com.viettelsoftware.intern_management.infrastructure.secondary.jpa.repository;

import com.viettelsoftware.intern_management.domain.entity.TaskAssignmentEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskAssignmentRepository extends BaseJPARepository<TaskAssignmentEntity, UUID> {

	Optional<TaskAssignmentEntity> findByTaskIdAndInternId(UUID taskId, UUID internId);
}

