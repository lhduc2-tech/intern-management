package com.viettelsoftware.intern_management.domain.port.secondary;

import com.viettelsoftware.intern_management.domain.dto.InternDTO;
import com.viettelsoftware.intern_management.domain.dto.InternSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("unused")
public interface InternPort {

    Page<InternDTO> search(InternSearchCriteria criteria, Pageable pageable);

    Optional<InternDTO> findById(UUID id);

    Optional<InternDTO> findByUserId(UUID userId);

    Optional<InternDTO> findByCompanyAccount(String companyAccount);

    Optional<InternDTO> findByIdentityNumber(String identityNumber);

    InternDTO save(InternDTO internDTO);

    InternDTO update(InternDTO internDTO);

    void deleteById(UUID id);
}

