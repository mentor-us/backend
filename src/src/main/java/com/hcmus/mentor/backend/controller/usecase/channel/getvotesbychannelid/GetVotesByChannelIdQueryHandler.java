package com.hcmus.mentor.backend.controller.usecase.channel.getvotesbychannelid;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.ForbiddenException;
import com.hcmus.mentor.backend.controller.payload.response.users.ShortProfile;
import com.hcmus.mentor.backend.controller.payload.response.votes.VoteDetailResponse;
import com.hcmus.mentor.backend.domain.Vote;
import com.hcmus.mentor.backend.domain.dto.ChoiceDto;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.repository.VoteRepository;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import com.hcmus.mentor.backend.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class GetVotesByChannelIdQueryHandler implements Command.Handler<GetVotesByChannelIdQuery, List<VoteDetailResponse>> {

    private final LoggedUserAccessor loggedUserAccessor;
    private final PermissionService permissionService;
    private final VoteRepository voteRepository;
    private final UserRepository userRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<VoteDetailResponse> handle(GetVotesByChannelIdQuery query) {
        if (!permissionService.isUserInChannel(query.getId(), loggedUserAccessor.getCurrentUserId())) {
            throw new ForbiddenException("Bạn không có quyền truy cập");
        }

        return voteRepository.findByGroupIdOrderByCreatedDateDesc(query.getId()).stream()
                .map(this::fulfillChoices)
                .toList();
    }

    public VoteDetailResponse fulfillChoices(Vote vote) {
        ShortProfile creator = userRepository.findShortProfile(vote.getCreatorId());
        List<VoteDetailResponse.ChoiceDetail> choices = vote.getChoices().stream()
                .map(this::fulfillChoice)
                .filter(Objects::nonNull)
                .sorted((c1, c2) -> c2.getVoters().size() - c1.getVoters().size())
                .toList();
        return VoteDetailResponse.from(vote, creator, choices);
    }

    public VoteDetailResponse.ChoiceDetail fulfillChoice(ChoiceDto choice) {
        if (choice == null) {
            return null;
        }
        List<ShortProfile> voters = userRepository.findByIds(choice.getVoters());
        return VoteDetailResponse.ChoiceDetail.from(choice, voters);
    }
}
