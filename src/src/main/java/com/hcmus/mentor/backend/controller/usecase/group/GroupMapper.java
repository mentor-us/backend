package com.hcmus.mentor.backend.controller.usecase.group;

import com.hcmus.mentor.backend.controller.usecase.group.getgroupworkspace.GetGroupWorkspaceResult;
import com.hcmus.mentor.backend.controller.usecase.group.getgroupworkspace.WorkspaceChannelDto;
import com.hcmus.mentor.backend.domain.Channel;
import com.hcmus.mentor.backend.domain.Group;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class GroupMapper {

    public GroupMapper(ModelMapper modelMapper) {
        modelMapper.createTypeMap(Group.class, GetGroupWorkspaceResult.class).addMappings(mapper -> {
            mapper.skip(GetGroupWorkspaceResult::setMentors);
            mapper.skip(GetGroupWorkspaceResult::setMentees);
        });
        modelMapper.createTypeMap(Channel.class, WorkspaceChannelDto.class);
    }
}
