package com.viettelsoftware.intern_management.domain.port.primary;

import com.viettelsoftware.intern_management.domain.dto.AttendanceDTO;
import com.viettelsoftware.intern_management.domain.dto.AttendanceSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AttendanceService {

    Page<AttendanceDTO> searchAttendances(AttendanceSearchCriteria criteria, Pageable pageable);

    AttendanceDTO getAttendance(UUID id);

    AttendanceDTO createAttendance(AttendanceDTO attendanceDTO);

    AttendanceDTO updateAttendance(UUID id, AttendanceDTO attendanceDTO);

    void deleteAttendance(UUID id);
}

