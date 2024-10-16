package com.hcmus.mentor.backend.controller.usecase.group.getallgroups;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.usecase.common.pagination.PageQueryFilter;
import com.hcmus.mentor.backend.controller.usecase.group.common.BasicGroupDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetAllGroupsQuery extends PageQueryFilter implements Command<Page<BasicGroupDto>> {

    private String type = "";
}
