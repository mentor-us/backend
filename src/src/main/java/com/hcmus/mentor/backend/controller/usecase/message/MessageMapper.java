package com.hcmus.mentor.backend.controller.usecase.message;

import com.hcmus.mentor.backend.controller.payload.FileModel;
import com.hcmus.mentor.backend.controller.payload.response.messages.MessageDetailResponse;
import com.hcmus.mentor.backend.controller.payload.response.messages.MessageResponse;
import com.hcmus.mentor.backend.domain.Channel;
import com.hcmus.mentor.backend.domain.File;
import com.hcmus.mentor.backend.domain.Message;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MessageMapper {

    public MessageMapper(ModelMapper modelMapper) {

        modelMapper.emptyTypeMap(Message.class, MessageDetailResponse.class).addMappings(mapper -> {
                    mapper.skip(MessageDetailResponse::setVote);
                    mapper.skip(MessageDetailResponse::setFile);
                    mapper.skip(MessageDetailResponse::setTask);
                    mapper.skip(MessageDetailResponse::setMeeting);
                    mapper.skip(MessageDetailResponse::setTotalReaction);
                    mapper.skip(MessageDetailResponse::setReactions);
                    mapper.map(src -> Optional.ofNullable(src.getChannel()).map(Channel::getId).orElse(null), MessageDetailResponse::setGroupId);
                })
                .implicitMappings();

        modelMapper.createTypeMap(Message.class, MessageResponse.class).addMappings(mapper -> {
            mapper.map(src -> Optional.ofNullable(src.getChannel()).map(Channel::getId).orElse(null), MessageResponse::setGroupId);
            mapper.skip(MessageResponse::setFile);
        });

        modelMapper.createTypeMap(File.class, FileModel.class);
    }
}