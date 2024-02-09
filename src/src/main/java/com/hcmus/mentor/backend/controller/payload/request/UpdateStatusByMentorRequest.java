package com.hcmus.mentor.backend.controller.payload.request;

import com.hcmus.mentor.backend.domain.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class UpdateStatusByMentorRequest {
    private String emailUserAssigned;
    private TaskStatus status;
}
