package com.hcmus.mentor.backend.controller.usecase.group;

import com.hcmus.mentor.backend.controller.usecase.group.getgroupworkspace.GetGroupWorkspaceResult;
import com.hcmus.mentor.backend.controller.usecase.group.getgroupworkspace.WorkspaceChannelDto;
import com.hcmus.mentor.backend.controller.usecase.group.common.GroupDetailDto;
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

        modelMapper.createTypeMap(Group.class, GetGroupWorkspaceResult.class).addMappings(mapper -> {
            mapper.skip(GetGroupWorkspaceResult::setMentors);
            mapper.skip(GetGroupWorkspaceResult::setMentees);
        });
        modelMapper.createTypeMap(Channel.class, WorkspaceChannelDto.class);
        modelMapper.createTypeMap(Group.class, GroupDetailDto.class).addMappings(mapper -> {
            mapper.map(src -> src.getCreator().getId(), GroupDetailDto::setCreatorId);
            mapper.map(src -> src.getDefaultChannel().getId(), GroupDetailDto::setDefaultChannelId);
            mapper.using(mapIdConverter).map(Group::getMembers, GroupDetailDto::setMembers);
            mapper.map(src -> src.getGroupCategory().getId(), GroupDetailDto::setGroupCategory);
            mapper.map(src -> src.getLastMessage().getContent(), GroupDetailDto::setLastMessage);
            mapper.map(src -> src.getLastMessage().getId(), GroupDetailDto::setLastMessageId);
            mapper.using(mapIdConverter).map(Group::getMembers, GroupDetailDto::setMembers);
            mapper.using(mapIdConverter).map(Group::getMentees, GroupDetailDto::setMentees);
            mapper.using(mapIdConverter).map(Group::getMentors, GroupDetailDto::setMentors);
        });
    }
}
