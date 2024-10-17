package vn.edu.hcmus.mentor.controller.usecase.channel;

import vn.edu.hcmus.mentor.controller.payload.response.ShortMediaMessage;
import vn.edu.hcmus.mentor.controller.usecase.channel.common.ChannelDetailDto;
import vn.edu.hcmus.mentor.controller.usecase.channel.common.ChannelForwardDto;
import vn.edu.hcmus.mentor.domain.Channel;
import vn.edu.hcmus.mentor.domain.Message;
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
            mapper.skip(ChannelDetailDto::setTimeStart);
            mapper.skip(ChannelDetailDto::setTimeEnd);
        });
        modelMapper.createTypeMap(Message.class, ShortMediaMessage.class).addMappings(mapper -> {
        });

        modelMapper.createTypeMap(Channel.class, ChannelForwardDto.class).addMappings(mapper -> {
            mapper.skip(ChannelForwardDto::setName);
        });
    }
}