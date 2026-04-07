package com.viettelsoftware.intern_management.domain.service;

import com.viettelsoftware.intern_management.domain.dto.InternshipSessionDTO;
import com.viettelsoftware.intern_management.domain.dto.InternshipSessionSearchCriteria;
import com.viettelsoftware.intern_management.domain.port.primary.InternshipSessionService;
import com.viettelsoftware.intern_management.domain.port.secondary.InternshipSessionPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class InternshipSessionServiceImpl implements InternshipSessionService {

    private final InternshipSessionPort internshipSessionPort;

    public InternshipSessionServiceImpl(InternshipSessionPort internshipSessionPort) {
        this.internshipSessionPort = internshipSessionPort;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InternshipSessionDTO> searchInternshipSessions(InternshipSessionSearchCriteria criteria, Pageable pageable) {
        InternshipSessionSearchCriteria normalized = criteria == null ? new InternshipSessionSearchCriteria() : criteria;
        if (normalized.getName() != null) {
            normalized.setName(normalized.getName().trim());
        }
        validateDateRange(normalized.getStartFrom(), normalized.getStartTo(), "startFrom must be before or equal to startTo");
        validateDateRange(normalized.getEndFrom(), normalized.getEndTo(), "endFrom must be before or equal to endTo");
        return internshipSessionPort.search(normalized, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public InternshipSessionDTO getInternshipSession(UUID id) {
        return internshipSessionPort.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Internship session not found: " + id));
    }

    @Override
    @Transactional
    public InternshipSessionDTO createInternshipSession(InternshipSessionDTO internshipSessionDTO) {
        if (internshipSessionDTO == null) {
            throw new IllegalArgumentException("Internship session payload must not be null");
        }
        InternshipSessionDTO toSave = InternshipSessionDTO.builder()
                .name(normalizeName(internshipSessionDTO.getName()))
                .startDate(internshipSessionDTO.getStartDate())
                .endDate(internshipSessionDTO.getEndDate())
                .build();
        validateDateRange(toSave.getStartDate(), toSave.getEndDate(), "startDate must be before or equal to endDate");
        return internshipSessionPort.save(toSave);
    }

    @Override
    @Transactional
    public InternshipSessionDTO updateInternshipSession(UUID id, InternshipSessionDTO internshipSessionDTO) {
        InternshipSessionDTO existing = getInternshipSession(id);
        merge(existing, internshipSessionDTO);
        validateDateRange(existing.getStartDate(), existing.getEndDate(), "startDate must be before or equal to endDate");
        return internshipSessionPort.update(existing);
    }

    @Override
    @Transactional
    public void deleteInternshipSession(UUID id) {
        getInternshipSession(id);
        internshipSessionPort.deleteById(id);
    }

    private void merge(InternshipSessionDTO target, InternshipSessionDTO source) {
        if (source == null) {
            return;
        }
        if (source.getName() != null) {
            target.setName(normalizeName(source.getName()));
        }
        if (source.getStartDate() != null) {
            target.setStartDate(source.getStartDate());
        }
        if (source.getEndDate() != null) {
            target.setEndDate(source.getEndDate());
        }
    }

    private String normalizeName(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Internship session name must not be blank");
        }
        return value.trim();
    }

    private void validateDateRange(LocalDate from, LocalDate to, String message) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException(message);
        }
    }
}


