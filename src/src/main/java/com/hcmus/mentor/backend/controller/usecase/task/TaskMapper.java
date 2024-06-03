package com.hcmus.mentor.backend.controller.usecase.task;

import com.hcmus.mentor.backend.controller.payload.response.tasks.TaskAssigneeResponse;
import com.hcmus.mentor.backend.controller.payload.response.tasks.TaskMessageResponse;
import com.hcmus.mentor.backend.controller.payload.response.tasks.TaskResponse;
import com.hcmus.mentor.backend.controller.usecase.common.NewEventDto;
import com.hcmus.mentor.backend.controller.usecase.task.common.NewTaskDto;
import com.hcmus.mentor.backend.domain.Assignee;
import com.hcmus.mentor.backend.domain.Task;
import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.service.EventType;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;


@Component
public class TaskMapper {

    public TaskMapper(ModelMapper modelMapper) {

        var mappingAssignee = new Converter<List<Assignee>, List<TaskAssigneeResponse>>() {
            public List<TaskAssigneeResponse> convert(MappingContext<List<Assignee>, List<TaskAssigneeResponse>> context) {
                if (context.getSource() == null || context.getSource().size() == 0) {
                    return List.of();
                }

                return context.getSource().stream().map(src -> modelMapper.map(src, TaskAssigneeResponse.class)).toList();
            }
        };

        var mappingImageUrl = new Converter<String, String>() {
            public String convert(MappingContext<String, String> context) {
                return context.getSource().equals("https://graph.microsoft.com/v1.0/me/photo/$value") ? null : context.getSource();
            }
        };

        modelMapper.createTypeMap(Task.class, TaskMessageResponse.class).addMappings(mapper -> {
            mapper.map(src -> Optional.ofNullable(src.getAssigner()).map(User::getId).orElse(null), TaskMessageResponse::setAssignerId);
            mapper.using(mappingAssignee).map(Task::getAssignees, TaskMessageResponse::setAssignees);
        });

        modelMapper.createTypeMap(Assignee.class, TaskAssigneeResponse.class).addMappings(mapper -> {
            mapper.map(src -> Optional.ofNullable(src.getUser()).map(User::getId).orElse(null), TaskAssigneeResponse::setId);
            mapper.map(src -> Optional.ofNullable(src.getUser()).map(User::getName).orElse(null), TaskAssigneeResponse::setName);
            mapper.map(src -> Optional.ofNullable(src.getUser()).map(User::getEmail).orElse(null), TaskAssigneeResponse::setEmail);
            mapper.using(mappingImageUrl).map(src -> Optional.ofNullable(src.getUser()).map(User::getImageUrl).orElse(null), TaskAssigneeResponse::setImageUrl);
        });

        modelMapper.emptyTypeMap(Task.class, NewTaskDto.class).addMappings(mapper -> {
            mapper.skip(NewTaskDto::setStatus);
            mapper.skip(NewTaskDto::setCreatedDate);
            mapper.skip(NewTaskDto::setDeadline);
        }).implicitMappings();

        modelMapper.createTypeMap(NewTaskDto.class, NewEventDto.class).addMappings(mapper -> {
            mapper.map(src -> src.getGroup().getName(), NewEventDto::setGroupName);
            mapper.map(src -> src.getAssigner().getName(), NewEventDto::setUser);
            mapper.map(src -> EventType.TASK, NewEventDto::setType);
            mapper.map(NewTaskDto::getCreatedDate, NewEventDto::setTimeStart);
            mapper.map(NewTaskDto::getDeadline, NewEventDto::setTimeEnd);
            mapper.map(NewTaskDto::getDeadline, NewEventDto::setDeadline);
        });

        modelMapper.createTypeMap(Task.class, TaskResponse.class).addMappings(mapper->{
            mapper.map(src->Optional.ofNullable(src.getParentTask()).map(Task::getId).orElse(null), TaskResponse::setParentTask);
            mapper.skip(TaskResponse::setStatus);
        });
    }
}