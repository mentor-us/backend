package com.hcmus.mentor.backend.controller.mapper;

import com.hcmus.mentor.backend.controller.payload.response.groups.GroupDetailResponse;
import com.hcmus.mentor.backend.domain.Group;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface GroupMapper {
    GroupMapper INSTANCE = Mappers.getMapper(GroupMapper.class);

    @Mapping(target = "mentors", expression = "java(group.getMentors().stream().map(user -> user.getId()).toList())")
    @Mapping(target = "mentees", expression = "java(group.getMentees().stream().map(user -> user.getId()).toList())")
    @Mapping(target = "groupCategory", source = "group.groupCategory.name")

    GroupDetailResponse groupToGroupDetailResponse(Group group);
}
