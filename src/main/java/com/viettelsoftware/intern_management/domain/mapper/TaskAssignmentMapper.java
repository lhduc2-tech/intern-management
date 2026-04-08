package com.viettelsoftware.intern_management.domain.mapper;

import com.viettelsoftware.intern_management.domain.dto.TaskAssignmentDTO;
import com.viettelsoftware.intern_management.domain.entity.TaskAssignmentEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TaskAssignmentMapper extends BaseMapper<TaskAssignmentEntity, TaskAssignmentDTO> {

    @Override
    @BeanMapping(builder = @org.mapstruct.Builder(disableBuilder = true))
    TaskAssignmentEntity toEntity(TaskAssignmentDTO dto);
}

