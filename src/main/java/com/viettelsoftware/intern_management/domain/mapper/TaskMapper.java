package com.viettelsoftware.intern_management.domain.mapper;

import com.viettelsoftware.intern_management.domain.dto.TaskDTO;
import com.viettelsoftware.intern_management.domain.entity.TaskEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TaskMapper extends BaseMapper<TaskEntity, TaskDTO> {

    @Override
    @BeanMapping(builder = @org.mapstruct.Builder(disableBuilder = true))
    TaskEntity toEntity(TaskDTO dto);
}

