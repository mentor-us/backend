package com.hcmus.mentor.backend.controller.payload.response.tasks;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskDetailResponseGroup {
    private String id;
    private String name;

    public static TaskDetailResponseGroup from(com.hcmus.mentor.backend.domain.Group group) {
        return TaskDetailResponseGroup.builder().id(group.getId()).name(group.getName()).build();
    }
}
