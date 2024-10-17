package vn.edu.hcmus.mentor.backend.controller.payload.response.tasks;

import lombok.*;
import vn.edu.hcmus.mentor.backend.domain.Group;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskDetailResponseGroup {
    private String id;
    private String name;

    public static TaskDetailResponseGroup from(Group group) {
        return TaskDetailResponseGroup.builder().id(group.getId()).name(group.getName()).build();
    }
}
