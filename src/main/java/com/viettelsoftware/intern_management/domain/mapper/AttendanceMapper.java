package com.viettelsoftware.intern_management.domain.mapper;

import com.viettelsoftware.intern_management.domain.dto.AttendanceDTO;
import com.viettelsoftware.intern_management.domain.entity.AttendanceEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AttendanceMapper extends BaseMapper<AttendanceEntity, AttendanceDTO> {

    @Override
    @BeanMapping(builder = @org.mapstruct.Builder(disableBuilder = true))
    AttendanceEntity toEntity(AttendanceDTO dto);
}

