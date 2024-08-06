package com.hcmus.mentor.backend.controller.payload.request.tasks;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class UpdateTaskRequest {

    @Length(min = 1, max = 255, message = "Tiêu đề không được để trống và tối đa 255 ký tự")
    private String title;

    @Length(min = 1, max = 255, message = "Tiêu đề không được để trống và tối đa 255 ký tự")
    private String description;
    private Date deadline;
    private List<String> userIds = new ArrayList<>();
    private String parentTask;
}