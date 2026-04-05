package com.viettelsoftware.intern_management.domain.port.primary;

import com.viettelsoftware.intern_management.domain.dto.InternDTO;
import com.viettelsoftware.intern_management.domain.dto.InternSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface InternService {

    Page<InternDTO> searchInterns(InternSearchCriteria criteria, Pageable pageable);

    InternDTO getIntern(UUID id);

    InternDTO createIntern(InternDTO internDTO, String fullName);

    InternDTO updateIntern(UUID id, InternDTO internDTO);

    void deleteIntern(UUID id);
}

