package com.hcmus.mentor.backend.controller.usecase.grade.getshareinfobyuserid;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.usecase.grade.common.GradeUserDto;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.service.impl.GradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class GetShareInfoByUserQueryHandler implements Command.Handler<GetShareInfoByUserQuery, GradeUserDto> {

    private final UserRepository userRepository;
    private final GradeService gradeService;

    @Override
    @Transactional(readOnly = true)
    public GradeUserDto handle(GetShareInfoByUserQuery command) {
        var user = userRepository.findById(command.getUserId())
                .orElseThrow(() -> new DomainException("Không tìm thấy user với id: " + command.getUserId()));

        return gradeService.mapToGradeUserDto(user);
    }
}