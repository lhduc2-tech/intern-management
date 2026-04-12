package com.viettelsoftware.intern_management.domain.mapper;

import com.viettelsoftware.intern_management.domain.dto.InternDTO;
import com.viettelsoftware.intern_management.domain.entity.InternEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InternMapper extends BaseMapper<InternEntity, InternDTO> {

	@Override
	@Mapping(target = "projectIds", ignore = true)
	@Mapping(target = "fullName", ignore = true)
	InternDTO toDto(InternEntity entity);

	@Override
	@BeanMapping(builder = @org.mapstruct.Builder(disableBuilder = true))
	InternEntity toEntity(InternDTO dto);
}

