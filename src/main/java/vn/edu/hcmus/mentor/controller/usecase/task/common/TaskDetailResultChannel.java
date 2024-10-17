package vn.edu.hcmus.mentor.controller.usecase.task.common;

import vn.edu.hcmus.mentor.domain.Channel;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskDetailResultChannel {
    private String id;
    private String name;

    public static TaskDetailResultChannel from(Channel channel) {
        return TaskDetailResultChannel.builder()
                .id(channel.getId())
                .name(channel.getName())
                .build();
    }
}
