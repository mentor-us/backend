package vn.edu.hcmus.mentor.controller.usecase.channel.getvotesbychannelid;

import an.awesome.pipelinr.Command;
import vn.edu.hcmus.mentor.controller.exception.ForbiddenException;
import vn.edu.hcmus.mentor.controller.payload.response.votes.VoteDetailResponse;
import vn.edu.hcmus.mentor.domain.Vote;
import vn.edu.hcmus.mentor.repository.UserRepository;
import vn.edu.hcmus.mentor.repository.VoteRepository;
import vn.edu.hcmus.mentor.security.principal.LoggedUserAccessor;
import vn.edu.hcmus.mentor.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetVotesByChannelIdQueryHandler implements Command.Handler<GetVotesByChannelIdQuery, List<VoteDetailResponse>> {

    private final LoggedUserAccessor loggedUserAccessor;
    private final PermissionService permissionService;
    private final VoteRepository voteRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<VoteDetailResponse> handle(GetVotesByChannelIdQuery query) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();

        if (!permissionService.isMemberInChannel(query.getId(), currentUserId)) {
            throw new ForbiddenException("Bạn không có quyền truy cập");
        }

        return voteRepository.findByGroupIdOrderByCreatedDateDesc(query.getId()).stream()
                .map(this::fulfillChoices)
                .toList();
    }

    public VoteDetailResponse fulfillChoices(Vote vote) {
//        ShortProfile creator = new ShortProfile(vote.getCreator());
//        List<VoteDetailResponse.ChoiceDetail> choices = vote.getChoices().stream()
//                .map(this::fulfillChoice)
//                .filter(Objects::nonNull)
//                .sorted((c1, c2) -> c2.getVoters().size() - c1.getVoters().size())
//                .toList();
//        return VoteDetailResponse.from(vote, creator, choices);
        return modelMapper.map(vote, VoteDetailResponse.class);
    }

//    public VoteDetailResponse.ChoiceDetail fulfillChoice(Choice choice) {
//        if (choice == null) {
//            return null;
//        }
//        List<ShortProfile> voters = choice.getVoters().stream().map(u->modelMapper.map(u, ShortProfile.class)).toList();
//        return VoteDetailResponse.ChoiceDetail.from(choice, voters);
//    }
}