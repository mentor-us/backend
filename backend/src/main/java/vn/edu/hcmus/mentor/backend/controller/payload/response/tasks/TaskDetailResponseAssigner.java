package vn.edu.hcmus.mentor.backend.controller.payload.response.tasks;

import vn.edu.hcmus.mentor.backend.domain.User;
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
