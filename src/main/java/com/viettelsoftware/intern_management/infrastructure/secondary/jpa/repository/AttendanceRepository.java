package com.viettelsoftware.intern_management.infrastructure.secondary.jpa.repository;

import com.viettelsoftware.intern_management.domain.entity.AttendanceEntity;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceRepository extends BaseJPARepository<AttendanceEntity, UUID> {

    Optional<AttendanceEntity> findByInternIdAndDate(UUID internId, LocalDate date);
}

