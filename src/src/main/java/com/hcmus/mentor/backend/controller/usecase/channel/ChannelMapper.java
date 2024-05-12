package com.hcmus.mentor.backend.controller.usecase.channel;

import com.hcmus.mentor.backend.controller.usecase.channel.common.ChannelDetailDto;
import com.hcmus.mentor.backend.domain.Channel;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class ChannelMapper {

    public ChannelMapper(ModelMapper modelMapper) {
        modelMapper.createTypeMap(Channel.class, ChannelDetailDto.class).addMappings(mapper -> {
            mapper.map(src -> src.getGroup().getId(), ChannelDetailDto::setParentId);
            mapper.map(src -> src.getLastMessage().getContent(), ChannelDetailDto::setLastMessage);
            mapper.map(src -> src.getCreator().getId(), ChannelDetailDto::setCreator);
            mapper.skip(ChannelDetailDto::setGroupCategory);
            mapper.skip(ChannelDetailDto::setPermissions);
            mapper.skip(ChannelDetailDto::setRole);
        });
    }
}
