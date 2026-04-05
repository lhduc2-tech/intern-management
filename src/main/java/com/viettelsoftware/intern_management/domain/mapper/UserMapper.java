package com.viettelsoftware.intern_management.domain.mapper;

import com.viettelsoftware.intern_management.domain.dto.UserDTO;
import com.viettelsoftware.intern_management.domain.entity.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper extends BaseMapper<UserEntity, UserDTO> {
}
