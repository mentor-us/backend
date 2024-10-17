package vn.edu.hcmus.mentor.backend.controller.usecase.message;

import vn.edu.hcmus.mentor.backend.controller.payload.FileModel;
import vn.edu.hcmus.mentor.backend.controller.payload.response.messages.MessageDetailResponse;
import vn.edu.hcmus.mentor.backend.controller.payload.response.messages.MessageResponse;
import vn.edu.hcmus.mentor.backend.domain.Channel;
import vn.edu.hcmus.mentor.backend.domain.File;
import vn.edu.hcmus.mentor.backend.domain.Message;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MessageMapper {

    public MessageMapper(ModelMapper modelMapper) {

        modelMapper.createTypeMap(File.class, FileModel.class);

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

        modelMapper.emptyTypeMap(Message.class, MessageResponse.class).addMappings(mapper -> {
            mapper.map(src -> Optional.ofNullable(src.getChannel()).map(Channel::getId).orElse(null), MessageResponse::setGroupId);
            mapper.skip(MessageResponse::setFile);
        })
                .implicitMappings();

    }
}