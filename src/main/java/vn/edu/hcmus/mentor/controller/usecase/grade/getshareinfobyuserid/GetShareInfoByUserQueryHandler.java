package vn.edu.hcmus.mentor.controller.usecase.grade.getshareinfobyuserid;

import an.awesome.pipelinr.Command;
import vn.edu.hcmus.mentor.controller.exception.DomainException;
import vn.edu.hcmus.mentor.controller.usecase.grade.common.GradeUserDto;
import vn.edu.hcmus.mentor.repository.UserRepository;
import vn.edu.hcmus.mentor.service.impl.GradeService;
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