package com.hcmus.mentor.backend.controller.usecase.channel;

import com.hcmus.mentor.backend.controller.payload.response.ShortMediaMessage;
import com.hcmus.mentor.backend.controller.usecase.channel.common.ChannelDetailDto;
import com.hcmus.mentor.backend.controller.usecase.channel.common.ChannelForwardDto;
import com.hcmus.mentor.backend.domain.Channel;
import com.hcmus.mentor.backend.domain.Message;
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
        modelMapper.createTypeMap(Message.class, ShortMediaMessage.class).addMappings(mapper -> {
        });

        modelMapper.createTypeMap(Channel.class, ChannelForwardDto.class).addMappings(mapper -> {
            mapper.skip(ChannelForwardDto::setName);
        });
    }
}