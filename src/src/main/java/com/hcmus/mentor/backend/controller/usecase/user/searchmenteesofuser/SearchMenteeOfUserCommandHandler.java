package com.hcmus.mentor.backend.controller.usecase.user.searchmenteesofuser;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import com.hcmus.mentor.backend.util.MappingUtil;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SearchMenteeOfUserCommandHandler implements Command.Handler<SearchMenteesOfUserCommand, SearchMenteesOfUserResult> {

    private final UserRepository userRepository;
    private final Logger logger = LoggerFactory.getLogger(SearchMenteeOfUserCommandHandler.class);
    private final ModelMapper modelMapper;
    private final LoggedUserAccessor loggedUserAccessor;

    @Override
    public SearchMenteesOfUserResult handle(SearchMenteesOfUserCommand command) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();
        var pageMentees = userRepository.findAllMenteeOfUserId(
                currentUserId,
                Optional.ofNullable(command.getEmail()).map(e -> "%" + e.toLowerCase() + "%").orElse(null),
                PageRequest.of(command.getPage(), command.getPageSize()));
        var data = pageMentees.getContent().stream()
                .map(mentee -> modelMapper.map(mentee, ShortMenteeProfile.class))
                .toList();
        var result = new SearchMenteesOfUserResult();
        result.setData(data);
        MappingUtil.mapPageQueryMetadata(pageMentees, result);
        return result;
    }
}