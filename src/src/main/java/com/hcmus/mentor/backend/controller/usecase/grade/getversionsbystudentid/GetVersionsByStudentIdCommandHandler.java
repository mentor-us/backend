package com.hcmus.mentor.backend.controller.usecase.grade.getversionsbystudentid;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.usecase.grade.common.GradeVersionDto;
import com.hcmus.mentor.backend.repository.GradeVersionRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetVersionsByStudentIdCommandHandler implements Command.Handler<GetVersionsByStudentIdCommand, List<GradeVersionDto>> {

    private final GradeVersionRepository gradeVersionRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<GradeVersionDto> handle(GetVersionsByStudentIdCommand command) {
        var gradeVersions = gradeVersionRepository.findByUserIdOrderByCreatedDateDesc(command.getStudentId());

        return gradeVersions.stream().map(gradeVersion -> modelMapper.map(gradeVersion, GradeVersionDto.class)).toList();
    }
}