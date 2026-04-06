package com.viettelsoftware.intern_management.domain.service;

import com.viettelsoftware.intern_management.domain.dto.AttendanceDTO;
import com.viettelsoftware.intern_management.domain.dto.AttendanceSearchCriteria;
import com.viettelsoftware.intern_management.domain.port.primary.AttendanceService;
import com.viettelsoftware.intern_management.domain.port.secondary.AttendancePort;
import com.viettelsoftware.intern_management.domain.port.secondary.InternPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendancePort attendancePort;
    private final InternPort internPort;

    @Override
    @Transactional(readOnly = true)
    public Page<AttendanceDTO> searchAttendances(AttendanceSearchCriteria criteria, Pageable pageable) {
        AttendanceSearchCriteria normalized = criteria == null ? new AttendanceSearchCriteria() : criteria;
        if (normalized.getFromDate() != null && normalized.getToDate() != null && normalized.getFromDate().isAfter(normalized.getToDate())) {
            throw new IllegalArgumentException("fromDate must be before or equal to toDate");
        }
        return attendancePort.search(normalized, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceDTO getAttendance(UUID id) {
        return attendancePort.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Attendance not found: " + id));
    }

    @Override
    @Transactional
    public AttendanceDTO createAttendance(AttendanceDTO attendanceDTO) {
        if (attendanceDTO == null) {
            throw new IllegalArgumentException("Attendance payload must not be null");
        }

        AttendanceDTO normalized = normalizeForSave(attendanceDTO);
        validateInternExists(normalized.getInternId());
        ensureUniqueInternDate(normalized.getInternId(), normalized.getDate(), null);
        return attendancePort.save(normalized);
    }

    @Override
    @Transactional
    public AttendanceDTO updateAttendance(UUID id, AttendanceDTO attendanceDTO) {
        AttendanceDTO existing = getAttendance(id);
        merge(existing, attendanceDTO);
        validateInternExists(existing.getInternId());
        ensureUniqueInternDate(existing.getInternId(), existing.getDate(), id);
        return attendancePort.update(existing);
    }

    @Override
    @Transactional
    public void deleteAttendance(UUID id) {
        getAttendance(id);
        attendancePort.deleteById(id);
    }

    private AttendanceDTO normalizeForSave(AttendanceDTO attendanceDTO) {
        if (attendanceDTO.getInternId() == null) {
            throw new IllegalArgumentException("internId is required");
        }
        if (attendanceDTO.getDate() == null) {
            throw new IllegalArgumentException("date is required");
        }

        attendanceDTO.setId(null);
        if (attendanceDTO.getNote() != null) {
            attendanceDTO.setNote(attendanceDTO.getNote().trim());
        }
        return attendanceDTO;
    }

    private void validateInternExists(UUID internId) {
        if (internId == null) {
            throw new IllegalArgumentException("internId is required");
        }
        internPort.findById(internId)
                .orElseThrow(() -> new IllegalArgumentException("Intern not found: " + internId));
    }

    private void ensureUniqueInternDate(UUID internId, java.time.LocalDate date, UUID currentId) {
        attendancePort.findByInternIdAndDate(internId, date)
                .filter(found -> currentId == null || !currentId.equals(found.getId()))
                .ifPresent(found -> {
                    throw new IllegalArgumentException("Attendance already exists for intern and date");
                });
    }

    private void merge(AttendanceDTO target, AttendanceDTO source) {
        if (source == null) {
            return;
        }
        if (source.getInternId() != null) {
            target.setInternId(source.getInternId());
        }
        if (source.getDate() != null) {
            target.setDate(source.getDate());
        }
        if (source.getNote() != null) {
            target.setNote(source.getNote().trim());
        }
    }
}

