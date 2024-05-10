package com.hcmus.mentor.backend.controller.usecase.group.searchgroup;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.domain.Group;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchGroupsQuery implements Command<Page<Group>> {

    private int page = 0;
    private int size = 25;
    private String type = "";
}
