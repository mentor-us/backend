package vn.edu.hcmus.mentor.controller.payload.response.tasks;

import vn.edu.hcmus.mentor.domain.User;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskDetailResponseAssigner {
    private String id;
    private String name;
    private String imageUrl;

    public static TaskDetailResponseAssigner from(User user) {
        return TaskDetailResponseAssigner.builder()
                .id(user.getId())
                .name(user.getName())
                .imageUrl(user.getImageUrl())
                .build();
    }
}
