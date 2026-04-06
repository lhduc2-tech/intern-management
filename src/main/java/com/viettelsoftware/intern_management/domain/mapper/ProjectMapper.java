package com.viettelsoftware.intern_management.domain.mapper;

import com.viettelsoftware.intern_management.domain.dto.ProjectDTO;
import com.viettelsoftware.intern_management.domain.entity.ProjectEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProjectMapper extends BaseMapper<ProjectEntity, ProjectDTO> {

    @Override
    @BeanMapping(builder = @org.mapstruct.Builder(disableBuilder = true))
    ProjectEntity toEntity(ProjectDTO dto);
}

