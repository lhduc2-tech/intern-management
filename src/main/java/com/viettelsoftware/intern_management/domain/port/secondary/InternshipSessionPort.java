package com.viettelsoftware.intern_management.domain.port.secondary;

import com.viettelsoftware.intern_management.domain.dto.InternshipSessionDTO;
import com.viettelsoftware.intern_management.domain.dto.InternshipSessionSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface InternshipSessionPort {

    Page<InternshipSessionDTO> search(InternshipSessionSearchCriteria criteria, Pageable pageable);

    Optional<InternshipSessionDTO> findById(UUID id);

    InternshipSessionDTO save(InternshipSessionDTO internshipSessionDTO);

    InternshipSessionDTO update(InternshipSessionDTO internshipSessionDTO);

    void deleteById(UUID id);
}

