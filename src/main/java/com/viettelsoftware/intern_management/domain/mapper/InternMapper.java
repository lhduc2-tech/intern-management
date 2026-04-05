package com.viettelsoftware.intern_management.domain.mapper;

import com.viettelsoftware.intern_management.domain.dto.InternDTO;
import com.viettelsoftware.intern_management.domain.entity.InternEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InternMapper extends BaseMapper<InternEntity, InternDTO> {
}

