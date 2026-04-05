package com.viettelsoftware.intern_management.domain.mapper;

import com.viettelsoftware.intern_management.domain.dto.RoleManagementDTO;
import com.viettelsoftware.intern_management.domain.entity.PermissionEntity;
import com.viettelsoftware.intern_management.domain.entity.RoleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface RoleMapper extends BaseMapper<RoleEntity, RoleManagementDTO> {

    @Override
    @Mapping(target = "permissionCodes", source = "permissions", qualifiedByName = "mapPermissionCodes")
    RoleManagementDTO toDto(RoleEntity entity);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    RoleEntity toEntity(RoleManagementDTO dto);

    @Named("mapPermissionCodes")
    default Set<String> mapPermissionCodes(Set<PermissionEntity> permissions) {
        if (permissions == null) {
            return Collections.emptySet();
        }
        return permissions.stream()
                .map(PermissionEntity::getCode)
                .collect(Collectors.toCollection(HashSet::new));
    }
}

