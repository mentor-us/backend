package com.hcmus.mentor.backend.payload.response.tasks;

import com.hcmus.mentor.backend.entity.Task;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class TaskAssignerResponse {
    private String id;
    private String name;
    private String imageUrl;
}
