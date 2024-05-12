package com.hcmus.mentor.backend.controller.usecase.group.searchowngroups;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.usecase.common.pagination.PageQueryFilter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

/**
 * Command to find own groups.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchOwnGroupsQuery extends PageQueryFilter implements Command<Page<GroupHomepageDto>> {

    private String userId;
    private String isMentor = "";
}
