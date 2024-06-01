package com.hcmus.mentor.backend.controller.usecase.group.gethomepage;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.controller.payload.response.groups.GroupHomepageResponse;
import com.hcmus.mentor.backend.controller.usecase.common.NewEventDto;
import com.hcmus.mentor.backend.controller.usecase.meeting.common.NewMeetingDto;
import com.hcmus.mentor.backend.controller.usecase.task.common.NewTaskDto;
import com.hcmus.mentor.backend.domain.Assignee;
import com.hcmus.mentor.backend.domain.Channel;
import com.hcmus.mentor.backend.domain.constant.TaskStatus;
import com.hcmus.mentor.backend.repository.ChannelRepository;
import com.hcmus.mentor.backend.repository.MeetingRepository;
import com.hcmus.mentor.backend.repository.TaskRepository;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import com.hcmus.mentor.backend.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class GetHomePageQueryHandler implements Command.Handler<GetHomePageQuery, HomePageDto> {

    private final Logger logger = LoggerFactory.getLogger(GetHomePageQueryHandler.class);
    private final LoggedUserAccessor loggedUserAccessor;
    private final ModelMapper modelMapper;
    private final GroupService groupService;
    private final MeetingRepository meetingRepository;
    private final ChannelRepository channelRepository;
    private final TaskRepository taskRepository;

    @Override
    public HomePageDto handle(GetHomePageQuery command) {
        var currentUserId = loggedUserAccessor.getCurrentUserId();

        var channelIds = channelRepository.findOwnChannelsByUserId(currentUserId).stream().map(Channel::getId).toList();
        var now = LocalDateTime.now(ZoneOffset.UTC);

        var meetings = meetingRepository.findAllAndHasUserAndStartBefore(channelIds, currentUserId, now).stream()
                .map(m -> {
                    var dto = modelMapper.map(m, NewMeetingDto.class);
                    dto.setTimeStart(m.getTimeStart());
                    dto.setTimeEnd(m.getTimeEnd());

                    return dto;
                })
                .toList();

        var tasks = taskRepository.findAllAndHasUserAndDeadlineAfter(channelIds, currentUserId, now).stream()
                .map(t -> {
                    var dto = modelMapper.map(t, NewTaskDto.class);
                    dto.setCreatedDate(t.getCreatedDate().toInstant().atZone(ZoneOffset.UTC).toLocalDateTime());
                    dto.setDeadline(t.getDeadline());
                    var status = t.getAssignees().stream()
                            .filter(a -> Objects.equals(a.getUser().getId(), currentUserId))
                            .findFirst()
                            .map(Assignee::getStatus)
                            .orElse(TaskStatus.TO_DO);

                    dto.setStatus(status);
                    return dto;
                })
                .toList();

        var events = Stream.concat(meetings.stream().map(m -> modelMapper.map(m, NewEventDto.class)), tasks.stream().map(t -> modelMapper.map(t, NewEventDto.class)))
                .filter(event -> event.getUpcomingTime() != null)
                .sorted(Comparator.comparing(NewEventDto::getUpcomingTime))
                .toList();

        List<GroupHomepageResponse> pinnedGroups = groupService.getUserPinnedGroups(currentUserId);

        Slice<GroupHomepageResponse> groups = groupService.getHomePageRecentGroupsOfUser(currentUserId, 0, 25);

        return HomePageDto.builder()
                .events(events)
                .pinnedGroups(pinnedGroups)
                .groups(groups)
                .build();
    }
}
