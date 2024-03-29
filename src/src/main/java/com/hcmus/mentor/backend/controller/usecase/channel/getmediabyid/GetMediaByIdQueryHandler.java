package com.hcmus.mentor.backend.controller.usecase.channel.getmediabyid;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.exception.ForbiddenException;
import com.hcmus.mentor.backend.controller.payload.response.ShortMediaMessage;
import com.hcmus.mentor.backend.controller.payload.response.users.ProfileResponse;
import com.hcmus.mentor.backend.domain.Message;
import com.hcmus.mentor.backend.repository.ChannelRepository;
import com.hcmus.mentor.backend.repository.MessageRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handler for {@link GetMediaByIdQuery}.
 */
@Component
@RequiredArgsConstructor
public class GetMediaByIdQueryHandler implements Command.Handler<GetMediaByIdQuery, List<ShortMediaMessage>> {

    private final LoggedUserAccessor loggedUserAccessor;
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ShortMediaMessage> handle(GetMediaByIdQuery query) {
        var userId = loggedUserAccessor.getCurrentUserId();
        List<String> senderIds;

        var channel = channelRepository.findById(query.getId()).orElseThrow(() -> new DomainException("Không tìm thấy kênh"));

        if (channelRepository.existsByIdAndUserIdsContains(query.getId(), userId)) {
            throw new ForbiddenException("Không thể xem media trong kênh này");
        }

        senderIds = channel.getUserIds();

        Map<String, ProfileResponse> senders = userRepository.findAllByIdIn(senderIds).stream()
                .collect(Collectors.toMap(ProfileResponse::getId, sender -> sender, (sender1, sender2) -> sender2));

        List<Message> mediaMessages = messageRepository.findByGroupIdAndTypeInAndStatusInOrderByCreatedDateDesc(
                query.getId(),
                Arrays.asList(Message.Type.IMAGE, Message.Type.FILE),
                Arrays.asList(Message.Status.SENT, Message.Status.EDITED));

        List<ShortMediaMessage> media = new ArrayList<>();
        mediaMessages.forEach(message -> {
            ProfileResponse sender = senders.getOrDefault(message.getSenderId(), null);

            if (Message.Type.IMAGE.equals(message.getType())) {
                List<ShortMediaMessage> images = message.getImages().stream()
                        .map(url -> ShortMediaMessage.builder()
                                .id(message.getId())
                                .sender(sender)
                                .imageUrl(url)
                                .type(message.getType())
                                .createdDate(message.getCreatedDate())
                                .build())
                        .toList();
                media.addAll(images);
            }

            if (Message.Type.FILE.equals(message.getType())) {
                ShortMediaMessage file = ShortMediaMessage.builder()
                        .id(message.getId())
                        .sender(sender)
                        .file(message.getFile())
                        .type(message.getType())
                        .createdDate(message.getCreatedDate())
                        .build();
                media.add(file);
            }
        });

        return media;
    }
}
