package com.hcmus.mentor.backend.payload.request;

import com.hcmus.mentor.backend.entity.Task;
import lombok.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class AddTaskRequest {
    private String title;
    private String description;
    private Date deadline;
    private List<String> userIds = new ArrayList<>();
    private String parentTask;
    private String groupId;
}
