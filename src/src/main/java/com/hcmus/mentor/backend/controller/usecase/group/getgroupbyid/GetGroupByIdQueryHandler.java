package com.hcmus.mentor.backend.controller.usecase.group.getgroupbyid;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.usecase.group.searchgroup.GroupDetailDto;
import com.hcmus.mentor.backend.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Handler for {@link GetGroupByIdQueryHandler}.
 */
@Component
@RequiredArgsConstructor
public class GetGroupByIdQueryHandler implements Command.Handler<GetGroupByIdQuery, GroupDetailDto> {

    private final ModelMapper modelMapper;
    private final GroupRepository groupRepository;

    @Override
    public GroupDetailDto handle(GetGroupByIdQuery query) {
        var group = groupRepository.findById(query.getId()).orElseThrow(() -> new DomainException("Không tìm thấy nhóm với id " + query.getId()));

        return modelMapper.map(group, GroupDetailDto.class);
    }
}