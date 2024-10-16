package com.hcmus.mentor.backend.controller.usecase.group.serachgroups;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.usecase.common.pagination.PageQueryFilter;
import com.hcmus.mentor.backend.controller.usecase.group.common.GroupDetailDto;
import com.hcmus.mentor.backend.domain.constant.GroupStatus;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchGroupsQuery extends PageQueryFilter implements Command<Page<GroupDetailDto>> {

    private String name;

    @Email(message = "Email không hợp lệ")
    @Length(max = 50, message = "Độ dài email không quá 50 ký tự")
    private String mentorEmail;

    @Email(message = "Email không hợp lệ")
    @Length(max = 50, message = "Độ dài email không quá 50 ký tự")
    private String menteeEmail;
    private String groupCategory;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime timeStart1;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime timeEnd1;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime timeStart2;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime timeEnd2;

    private GroupStatus status;
}
