package com.hcmus.mentor.backend.controller.mapper;

import com.hcmus.mentor.backend.controller.payload.response.groups.GroupDetailResponse;
import com.hcmus.mentor.backend.domain.Group;
import com.hcmus.mentor.backend.domain.GroupUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface GroupMapper {
    GroupMapper INSTANCE = Mappers.getMapper(GroupMapper.class);

    @Mapping(target = "groupCategory", source = "group.groupCategory.name")
    @Mapping(target = "role", source = "groupUsers", qualifiedByName = "getRole")
    @Mapping(target = "pinned", source = "group", qualifiedByName = "setPinned")
    @Mapping(target = "mentors", source = "group", qualifiedByName = "setMentor")
    @Mapping(target = "mentees", source = "group", qualifiedByName = "setMentee")
    @Mapping(target = "permissions", source = "group.groupCategory.permissions")
    GroupDetailResponse groupToGroupDetailResponse(Group group, String userId);

    @Named("getRole")
    default String getRole(Group group, String userId) {
        return group.getGroupUsers().stream()
                .filter(groupUser -> groupUser.getUser().getId().equals(userId))
                .findFirst()
                .map(groupUser -> groupUser.isMentor() ? "MENTOR" : "MENTEE")
                .orElse(null);
    }

    @Named("setPinned")
    default boolean setPinned(Group group, String userId){
        return group.getGroupUsers().stream()
                .filter(groupUser -> groupUser.getUser().getId().equals(userId))
                .findFirst()
                .map(GroupUser::isPinned)
                .orElse(false);
    }

    @Named("setMentor")
    default List<String> setMentor(Group group){
        return group.getGroupUsers().stream()
                .filter(GroupUser::isMentor)
                .map(groupUser -> groupUser.getUser().getId())
                .toList();
    }

    @Named("setMentee")
    default List<String> setMentee(Group group){
        return group.getGroupUsers().stream()
                .filter(groupUser -> !groupUser.isMentor())
                .map(groupUser -> groupUser.getUser().getId())
                .toList();
    }


}
