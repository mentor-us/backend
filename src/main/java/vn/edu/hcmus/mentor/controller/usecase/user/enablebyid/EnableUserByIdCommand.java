package vn.edu.hcmus.mentor.controller.usecase.user.enablebyid;

import an.awesome.pipelinr.Command;
import vn.edu.hcmus.mentor.service.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnableUserByIdCommand implements Command<UserDto> {

    private String id;
}
