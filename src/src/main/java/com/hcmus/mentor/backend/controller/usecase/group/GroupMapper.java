package com.hcmus.mentor.backend.controller.usecase.group;

import com.hcmus.mentor.backend.controller.usecase.common.mapper.MapperConverter;
import com.hcmus.mentor.backend.controller.usecase.group.common.*;
import com.hcmus.mentor.backend.domain.Channel;
import com.hcmus.mentor.backend.domain.Group;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class GroupMapper {

    public GroupMapper(ModelMapper modelMapper) {

        modelMapper.createTypeMap(Group.class, GroupWorkspaceDto.class).addMappings(mapper -> {
            mapper.map(src -> src.getGroupCategory().getName(), GroupWorkspaceDto::setGroupCategory);
            mapper.map(src -> src.getGroupCategory().getPermissions(), GroupWorkspaceDto::setPermissions);
            mapper.map(src -> src.getDefaultChannel().getId(), GroupWorkspaceDto::setDefaultChannelId);
            mapper.using(MapperConverter.mapIdConverter()).map(Group::getMentees, GroupWorkspaceDto::setMentees);
            mapper.using(MapperConverter.mapIdConverter()).map(Group::getMentors, GroupWorkspaceDto::setMentors);
            mapper.skip(GroupWorkspaceDto::setRole);
        });

        modelMapper.createTypeMap(Channel.class, WorkspaceChannelDto.class).addMappings(mapper -> {
            mapper.map(src -> src.getGroup().getId(), WorkspaceChannelDto::setParentId);
        });

        modelMapper.createTypeMap(Group.class, BasicGroupDto.class).addMappings(mapper -> {
            mapper.map(src -> src.getCreator().getId(), BasicGroupDto::setCreatorId);
            mapper.map(src -> src.getGroupCategory().getName(), BasicGroupDto::setGroupCategory);
        });

        modelMapper.createTypeMap(Group.class, GroupDetailDto.class).addMappings(mapper -> {
            mapper.map(src -> src.getCreator().getId(), GroupDetailDto::setCreatorId);
            mapper.map(src -> src.getDefaultChannel().getId(), GroupDetailDto::setDefaultChannelId);
            mapper.map(src -> src.getGroupCategory().getId(), GroupDetailDto::setGroupCategory);
            mapper.map(src -> src.getLastMessage().getContent(), GroupDetailDto::setLastMessage);
            mapper.map(src -> src.getLastMessage().getId(), GroupDetailDto::setLastMessageId);
            mapper.using(MapperConverter.mapIdConverter()).map(Group::getMembers, GroupDetailDto::setMembers);
            mapper.using(MapperConverter.mapIdConverter()).map(Group::getMentees, GroupDetailDto::setMentees);
            mapper.using(MapperConverter.mapIdConverter()).map(Group::getMentors, GroupDetailDto::setMentors);
            mapper.skip(GroupDetailDto::setPermissions);
        });

        modelMapper.createTypeMap(Group.class, GroupHomepageDto.class).addMappings(mapper -> {
            mapper.map(src -> src.getGroupCategory().getName(), GroupHomepageDto::setGroupCategory);
            mapper.map(src -> src.getDefaultChannel().getId(), GroupHomepageDto::setDefaultChannelId);
            mapper.using(MapperConverter.mapIdConverter()).map(Group::getMentees, GroupHomepageDto::setMentees);
            mapper.using(MapperConverter.mapIdConverter()).map(Group::getMentors, GroupHomepageDto::setMentors);
            mapper.skip(GroupHomepageDto::setRole);
            mapper.skip(GroupHomepageDto::setPinned);
        });

        modelMapper.createTypeMap(Group.class, GroupForwardDto.class);
    }
}
