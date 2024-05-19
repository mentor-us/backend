package com.hcmus.mentor.backend.controller.usecase.channel.getmediabychannelid;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.exception.ForbiddenException;
import com.hcmus.mentor.backend.controller.payload.FileModel;
import com.hcmus.mentor.backend.controller.payload.response.ShortMediaMessage;
import com.hcmus.mentor.backend.controller.payload.response.users.ProfileResponse;
import com.hcmus.mentor.backend.domain.Message;
import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.repository.ChannelRepository;
import com.hcmus.mentor.backend.repository.MessageRepository;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Handler for {@link GetMediaByChannelIdQuery}.
 */
@Component
@RequiredArgsConstructor
public class GetMediaByChannelIdQueryHandler implements Command.Handler<GetMediaByChannelIdQuery, List<ShortMediaMessage>> {

    private final LoggedUserAccessor loggedUserAccessor;
    private final ModelMapper modelMapper;
    private final ChannelRepository channelRepository;
    private final MessageRepository messageRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ShortMediaMessage> handle(GetMediaByChannelIdQuery query) {
        var userId = loggedUserAccessor.getCurrentUserId();

        var channel = channelRepository.findById(query.getId()).orElseThrow(() -> new DomainException("Không tìm thấy kênh"));

        if (Boolean.FALSE.equals(channelRepository.existsByIdAndUserId(query.getId(), userId))) {
            throw new ForbiddenException("Không thể xem media trong kênh này");
        }

        Map<String, ProfileResponse> senders = channel.getUsers().stream()
                .collect(Collectors.toMap(User::getId, user -> modelMapper.map(user, ProfileResponse.class)));

        List<Message> messages = messageRepository.findByChannelIdAndTypeInAndStatusInOrderByCreatedDateDesc(
                query.getId(),
                Arrays.asList(Message.Type.IMAGE, Message.Type.FILE),
                Arrays.asList(Message.Status.SENT, Message.Status.EDITED));

        List<ShortMediaMessage> media = new ArrayList<>();
        messages.forEach(message -> {
            var sender = senders.getOrDefault(message.getSender().getId(), null);

            if (Message.Type.IMAGE.equals(message.getType())) {
                var images = mapToImageMedia(message);
                images.forEach(image -> image.setSender(sender));

                media.addAll(images);
            }

            if (Message.Type.FILE.equals(message.getType())) {
                var file = mapToFileMedia(message);
                file.setSender(sender);

                media.add(file);
            }
        });

        return media;
    }

    private List<ShortMediaMessage> mapToImageMedia(Message message) {
        return message.getImages().stream()
                .map(image -> {
                    var media = modelMapper.map(image, ShortMediaMessage.class);
                    media.setId(message.getId());
                    media.setImageUrl(image);
                    media.setType(Message.Type.IMAGE);
                    media.setCreatedDate(message.getCreatedDate());

                    return media;
                })
                .toList();
    }

    private ShortMediaMessage mapToFileMedia(Message message) {
        var media = modelMapper.map(message, ShortMediaMessage.class);
        media.setId(message.getId());
        media.setFile(new FileModel(message.getFile()));
        media.setType(Message.Type.FILE);
        media.setCreatedDate(message.getCreatedDate());

        return media;
    }
}