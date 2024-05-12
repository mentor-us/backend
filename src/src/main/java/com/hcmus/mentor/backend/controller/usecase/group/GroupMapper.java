package com.hcmus.mentor.backend.controller.usecase.group;

import com.hcmus.mentor.backend.controller.usecase.group.common.GroupDetailDto;
import com.hcmus.mentor.backend.controller.usecase.group.getgroupworkspace.GroupWorkspaceDto;
import com.hcmus.mentor.backend.controller.usecase.group.getgroupworkspace.WorkspaceChannelDto;
import com.hcmus.mentor.backend.controller.usecase.group.searchowngroups.GroupHomepageDto;
import com.hcmus.mentor.backend.domain.BaseDomain;
import com.hcmus.mentor.backend.domain.Channel;
import com.hcmus.mentor.backend.domain.Group;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class GroupMapper {

    public GroupMapper(ModelMapper modelMapper) {
        var mapIdConverter = new Converter<List<BaseDomain>, List<String>>() {
            public List<String> convert(MappingContext<List<BaseDomain>, List<String>> ctx) {
                if (ctx.getSource() == null) {
                    return Collections.emptyList();
                }

                return ctx.getSource().stream().map(BaseDomain::getId).toList();
            }
        };

        modelMapper.createTypeMap(Group.class, GroupWorkspaceDto.class).addMappings(mapper -> {
            mapper.map(src -> src.getGroupCategory().getName(), GroupWorkspaceDto::setGroupCategory);
            mapper.map(src -> src.getGroupCategory().getPermissions(), GroupWorkspaceDto::setPermissions);
            mapper.using(mapIdConverter).map(Group::getMentees, GroupWorkspaceDto::setMentees);
            mapper.using(mapIdConverter).map(Group::getMentors, GroupWorkspaceDto::setMentors);
            mapper.skip(GroupWorkspaceDto::setRole);
        });
        modelMapper.createTypeMap(Channel.class, WorkspaceChannelDto.class);
        modelMapper.createTypeMap(Group.class, GroupDetailDto.class).addMappings(mapper -> {
            mapper.map(src -> src.getCreator().getId(), GroupDetailDto::setCreatorId);
            mapper.map(src -> src.getDefaultChannel().getId(), GroupDetailDto::setDefaultChannelId);
            mapper.map(src -> src.getGroupCategory().getId(), GroupDetailDto::setGroupCategory);
            mapper.map(src -> src.getLastMessage().getContent(), GroupDetailDto::setLastMessage);
            mapper.map(src -> src.getLastMessage().getId(), GroupDetailDto::setLastMessageId);
            mapper.using(mapIdConverter).map(Group::getMembers, GroupDetailDto::setMembers);
            mapper.using(mapIdConverter).map(Group::getMentees, GroupDetailDto::setMentees);
            mapper.using(mapIdConverter).map(Group::getMentors, GroupDetailDto::setMentors);
        });
        modelMapper.createTypeMap(Group.class, GroupHomepageDto.class).addMappings(mapper -> {
            mapper.map(src -> src.getGroupCategory().getId(), GroupHomepageDto::setGroupCategory);
            mapper.map(src -> src.getDefaultChannel().getId(), GroupHomepageDto::setDefaultChannelId);
            mapper.using(mapIdConverter).map(Group::getMentees, GroupHomepageDto::setMentees);
            mapper.using(mapIdConverter).map(Group::getMentors, GroupHomepageDto::setMentors);
            mapper.skip(GroupHomepageDto::setRole);
            mapper.skip(GroupHomepageDto::setPinned);
        });
    }
}
