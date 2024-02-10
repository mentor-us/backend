package com.hcmus.mentor.backend.domain.dto;

import com.hcmus.mentor.backend.domain.constant.TaskStatus;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssigneeDto implements Serializable {

    private String userId;

    @Builder.Default
    private TaskStatus status = TaskStatus.TO_DO;
}
