package com.viettelsoftware.intern_management.domain.mapper;

import com.viettelsoftware.intern_management.domain.dto.InternshipSessionDTO;
import com.viettelsoftware.intern_management.domain.entity.InternshipSessionEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InternshipSessionMapper extends BaseMapper<InternshipSessionEntity, InternshipSessionDTO> {

    @Override
    @BeanMapping(builder = @org.mapstruct.Builder(disableBuilder = true))
    InternshipSessionEntity toEntity(InternshipSessionDTO dto);
}

