package vn.edu.hcmus.mentor.controller.usecase.vote.createvote;

import an.awesome.pipelinr.Command;
import com.corundumstudio.socketio.SocketIOServer;
import vn.edu.hcmus.mentor.controller.exception.DomainException;
import vn.edu.hcmus.mentor.controller.exception.ForbiddenException;
import vn.edu.hcmus.mentor.controller.payload.response.messages.MessageDetailResponse;
import vn.edu.hcmus.mentor.controller.usecase.vote.ChoiceMapper;
import vn.edu.hcmus.mentor.controller.usecase.vote.common.VoteResult;
import vn.edu.hcmus.mentor.domain.Choice;
import vn.edu.hcmus.mentor.domain.Message;
import vn.edu.hcmus.mentor.domain.Vote;
import vn.edu.hcmus.mentor.repository.ChannelRepository;
import vn.edu.hcmus.mentor.repository.ChoiceRepository;
import vn.edu.hcmus.mentor.repository.UserRepository;
import vn.edu.hcmus.mentor.repository.VoteRepository;
import vn.edu.hcmus.mentor.security.principal.LoggedUserAccessor;
import vn.edu.hcmus.mentor.service.GroupService;
import vn.edu.hcmus.mentor.service.MessageService;
import vn.edu.hcmus.mentor.service.NotificationService;
import vn.edu.hcmus.mentor.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    private final ChoiceMapper choiceMapper;

    @Override
    @Transactional
    public VoteResult handle(CreateVoteCommand command) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();
        var sender = userRepository.findById(currentUserId)
                .orElseThrow(() -> new DomainException("User not found."));
        if (!permissionService.isMemberInChannel(command.getGroupId(), currentUserId)) {
            throw new ForbiddenException("user not in channel");
        }

        var channel = channelRepository.findById(command.getGroupId())
                .orElseThrow(() -> new DomainException("Không tìm thấy kênh"));

        Vote vote = modelMapper.map(command, Vote.class);
        vote.setGroup(channel);
        vote.setCreator(sender);

        var choices = command.getChoices().stream()
                .map(c -> {
                    var choice = Choice.builder()
                            .creator(sender)
                            .name(c.getName())
                            .vote(vote)
                            .build();

                    return choice;
                })
                .toList();
        vote.setChoices(choices);

        voteRepository.save(vote);

        Message message = messageService.saveVoteMessage(vote);
        MessageDetailResponse response = messageService.mappingToMessageDetailResponse(message, currentUserId);
        socketServer.getRoomOperations(command.getGroupId()).sendEvent("receive_message", response);

        notificationService.sendForNote(vote.getGroup().getId(), vote);
        groupService.pingGroup(vote.getGroup().getId());

        return modelMapper.map(vote, VoteResult.class);
    }
}