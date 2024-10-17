package vn.edu.hcmus.mentor.controller.usecase.vote.DoVoting;

import an.awesome.pipelinr.Command;
import vn.edu.hcmus.mentor.repository.ChoiceRepository;
import vn.edu.hcmus.mentor.repository.UserRepository;
import vn.edu.hcmus.mentor.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DoVotingCommandHandler implements Command.Handler<DoVotingCommand, Void> {
    private final ChoiceRepository choiceRepository;
    private final UserRepository userRepository;
    private final VoteRepository voteRepository;

    /**
     * @param command command to do voting.
     * @return void.
     */
    @Override
    public Void handle(final DoVotingCommand command) {
        var vote = voteRepository.findById(command.getVoteId()).orElse(null);
        if (vote == null) {
            return null;
        }

        var voter = userRepository.findById(command.getVoterId()).orElse(null);
        if (voter == null) {
            return null;
        }

        var choices = vote.getChoices();
        choices.forEach(choice -> {
            if (command.getChoiceIds().contains(choice.getId())) {
                choice.getVoters().add(voter);
            }
        });
        voteRepository.save(vote);
        return null;
    }
}