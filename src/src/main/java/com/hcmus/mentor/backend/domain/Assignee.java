package com.hcmus.mentor.backend.domain;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Assignee implements Serializable {

    private String userId;

    @Builder.Default
    private TaskStatus status = TaskStatus.TO_DO;
}
