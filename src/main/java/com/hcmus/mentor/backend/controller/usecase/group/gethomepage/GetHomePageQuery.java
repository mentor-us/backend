package com.hcmus.mentor.backend.controller.usecase.group.gethomepage;

import an.awesome.pipelinr.Command;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class GetHomePageQuery implements Command<HomePageDto> {

}
