package com.hcmus.mentor.backend.controller.usecase.vote.createvote;

import an.awesome.pipelinr.Command;
import com.corundumstudio.socketio.SocketIOServer;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.exception.ForbiddenException;
import com.hcmus.mentor.backend.controller.payload.response.messages.MessageDetailResponse;
import com.hcmus.mentor.backend.controller.payload.response.messages.MessageResponse;
import com.hcmus.mentor.backend.controller.payload.response.users.ProfileResponse;
import com.hcmus.mentor.backend.controller.usecase.vote.common.VoteResult;
import com.hcmus.mentor.backend.domain.Choice;
import com.hcmus.mentor.backend.domain.Message;
import com.hcmus.mentor.backend.domain.Vote;
import com.hcmus.mentor.backend.repository.ChannelRepository;
import com.hcmus.mentor.backend.repository.ChoiceRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.repository.VoteRepository;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import com.hcmus.mentor.backend.service.GroupService;
import com.hcmus.mentor.backend.service.MessageService;
import com.hcmus.mentor.backend.service.NotificationService;
import com.hcmus.mentor.backend.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateVoteCommandHandler implements Command.Handler<CreateVoteCommand, VoteResult> {

    private final Logger logger = LoggerFactory.getLogger(CreateVoteCommandHandler.class);
    private final ModelMapper modelMapper;
    private final LoggedUserAccessor loggedUserAccessor;
    private final UserRepository userRepository;
    private final PermissionService permissionService;
    private final ChannelRepository channelRepository;
    private final VoteRepository voteRepository;
    private final ChoiceRepository choiceRepository;
    private final MessageService messageService;
    private final SocketIOServer socketServer;
    private final NotificationService notificationService;
    private final GroupService groupService;

    @Override
    public VoteResult handle(CreateVoteCommand command) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();

        var sender = userRepository.findById(currentUserId).orElseThrow(() -> new DomainException("User not found."));
        if (!permissionService.isMemberInChannel(command.getGroupId(), currentUserId)) {
            throw new ForbiddenException("user not in channel");
        }

        var channel = channelRepository.findById(command.getGroupId()).orElseThrow(() -> new DomainException("Không tìm thấy kênh"));

        Vote newVote = modelMapper.map(command, Vote.class);
        newVote.setGroup(channel);
        newVote.setCreator(sender);

        newVote = voteRepository.save(newVote);

        var choices = command.getChoices().stream()
                .map(choice -> modelMapper.map(choice, Choice.class))
                .toList();

        choiceRepository.saveAll(choices);

        Message message = messageService.saveVoteMessage(newVote);
        MessageDetailResponse response = MessageDetailResponse.from(MessageResponse.from(message, ProfileResponse.from(sender)), newVote);
        socketServer.getRoomOperations(command.getGroupId()).sendEvent("receive_message", response);

        notificationService.sendNewVoteNotification(newVote.getGroup().getId(), newVote);
        groupService.pingGroup(newVote.getGroup().getId());

        return modelMapper.map(newVote, VoteResult.class);
    }
}
