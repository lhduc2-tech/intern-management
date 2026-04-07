package com.viettelsoftware.intern_management.domain.port.primary;

import com.viettelsoftware.intern_management.domain.dto.InternshipSessionDTO;
import com.viettelsoftware.intern_management.domain.dto.InternshipSessionSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface InternshipSessionService {

    Page<InternshipSessionDTO> searchInternshipSessions(InternshipSessionSearchCriteria criteria, Pageable pageable);

    InternshipSessionDTO getInternshipSession(UUID id);

    InternshipSessionDTO createInternshipSession(InternshipSessionDTO internshipSessionDTO);

    InternshipSessionDTO updateInternshipSession(UUID id, InternshipSessionDTO internshipSessionDTO);

    void deleteInternshipSession(UUID id);
}

