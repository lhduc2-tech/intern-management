package com.viettelsoftware.intern_management.domain.port.secondary;

import com.viettelsoftware.intern_management.domain.dto.AttendanceDTO;
import com.viettelsoftware.intern_management.domain.dto.AttendanceSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface AttendancePort {

    Page<AttendanceDTO> search(AttendanceSearchCriteria criteria, Pageable pageable);

    Optional<AttendanceDTO> findById(UUID id);

    Optional<AttendanceDTO> findByInternIdAndDate(UUID internId, LocalDate date);

    AttendanceDTO save(AttendanceDTO attendanceDTO);

    AttendanceDTO update(AttendanceDTO attendanceDTO);

    void deleteById(UUID id);
}

