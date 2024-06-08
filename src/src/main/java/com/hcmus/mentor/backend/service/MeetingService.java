package com.hcmus.mentor.backend.service;

import an.awesome.pipelinr.Pipeline;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.payload.request.meetings.RescheduleMeetingRequest;
import com.hcmus.mentor.backend.controller.payload.request.meetings.CreateMeetingRequest;
import com.hcmus.mentor.backend.controller.payload.request.meetings.UpdateMeetingRequest;
import com.hcmus.mentor.backend.controller.payload.response.meetings.MeetingAttendeeResponse;
import com.hcmus.mentor.backend.controller.payload.response.meetings.MeetingDetailResponse;
import com.hcmus.mentor.backend.controller.payload.response.meetings.MeetingHistoryDetail;
import com.hcmus.mentor.backend.controller.payload.response.meetings.MeetingResponse;
import com.hcmus.mentor.backend.controller.payload.response.messages.MessageDetailResponse;
import com.hcmus.mentor.backend.controller.payload.response.users.ShortProfile;
import com.hcmus.mentor.backend.controller.usecase.channel.updatelastmessage.UpdateLastMessageCommand;
import com.hcmus.mentor.backend.domain.*;
import com.hcmus.mentor.backend.domain.method.IRemindable;
import com.hcmus.mentor.backend.repository.*;
import com.hcmus.mentor.backend.service.dto.GroupDto;
import com.hcmus.mentor.backend.service.dto.UserDto;
import com.hcmus.mentor.backend.util.DateUtils;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
@RequiredArgsConstructor
public class MeetingService implements IRemindableService {

    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;
    private final GroupService groupService;
    private final MessageService messageService;
    private final SocketIOService socketIOService;
    private final ReminderRepository reminderRepository;
    private final NotificationService notificationService;
    private final ChannelRepository channelRepository;
    private final Pipeline pipeline;
    private final ModelMapper modelMapper;
    private final MessageRepository messageRepository;
    private final MeetingHistoryRepository meetingHistoryRepository;

    @Transactional(readOnly = true)
    public List<MeetingResponse> getMeetingGroup(String groupId) {
        return meetingRepository.findAllByGroupId(groupId).stream().map(meeting -> {
            Group group = meeting.getGroup().getGroup();
            User organizer = meeting.getOrganizer();
            return MeetingResponse.from(meeting, organizer, group);
        }).toList();
    }

    public List<Meeting> getMeetingGroup(String groupId, int page, int size) {
        Pageable pageRequest = PageRequest.of(page, size);
        Page<Meeting> wrapper = meetingRepository.findByGroupId(groupId, pageRequest);
        return wrapper.getContent();
    }

    @jakarta.transaction.Transactional
    public Meeting createNewMeeting(CreateMeetingRequest request) {
        User organizer = userRepository.findById(request.getOrganizerId()).orElse(null);
        if (organizer == null) {
            return null;
        }

        var channel = channelRepository.findById(request.getGroupId()).orElse(null);
        if (channel == null) {
            return null;
        }

        var meetingHistory = MeetingHistory.builder()
                .timeStart(request.getTimeStart())
                .timeEnd(request.getTimeEnd())
                .place(request.getPlace())
                .modifier(organizer)
                .build();
        meetingHistory = meetingHistoryRepository.save(meetingHistory);

        var meeting = Meeting.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .timeStart(request.getTimeStart())
                .timeEnd(request.getTimeEnd())
                .repeated(request.getRepeated())
                .place(request.getPlace())
                .organizer(organizer)
                .group(channel)
                .histories(Collections.singletonList(meetingHistory))
                .attendees(request.getAttendees().contains("*") ? channel.getUsers() : userRepository.findByIdIn(request.getAttendees()))
                .build();
        meeting = meetingRepository.save(meeting);

        meetingHistory.setMeeting(meeting);
        meetingHistoryRepository.save(meetingHistory);

        Message newMessage = Message.builder()
                .sender(meeting.getOrganizer())
                .content("NEW MEETING")
                .createdDate(DateUtils.getDateNowAtUTC())
                .type(Message.Type.MEETING)
                .channel(meeting.getGroup())
                .meeting(meeting)
                .build();
        messageService.saveMessage(newMessage);

        groupService.pingGroup(request.getGroupId());

        pipeline.send(UpdateLastMessageCommand.builder().message(newMessage).channel(newMessage.getChannel()).build());

        MessageDetailResponse response = messageService.mappingToMessageDetailResponse(newMessage, request.getOrganizerId());
        socketIOService.sendBroadcastMessage(response, request.getGroupId());
        notificationService.sendForMeeting(meeting);
        saveToReminder(meeting);

        return meeting;
    }

    public Meeting updateMeeting(String modifierId, String meetingId, UpdateMeetingRequest request) {
        var meeting = meetingRepository.findById(meetingId).orElse(null);
        if (meeting == null) {
            return null;
        }

        if (!isEqualDate(meeting.getTimeStart(), request.getTimeStart())
                || !isEqualDate(meeting.getTimeEnd(), request.getTimeEnd())) {
            var modifier = userRepository.findById(modifierId).orElse(null);

            var history = MeetingHistory.builder()
                    .timeStart(request.getTimeStart())
                    .timeEnd(request.getTimeEnd())
                    .place(request.getPlace())
                    .modifier(modifier)
                    .meeting(meeting)
                    .build();
            history = meetingHistoryRepository.save(history);

            var histories = meeting.getHistories();
            histories.add(history);
            meeting.setHistories(histories);
        }

        meeting.setTitle(request.getTitle());
        meeting.setDescription(request.getDescription());
        meeting.setTimeStart(request.getTimeStart());
        meeting.setTimeEnd(request.getTimeEnd());
        meeting.setRepeated(request.getRepeated());
        meeting.setPlace(request.getPlace());
        meeting.setAttendees(userRepository.findByIdIn(request.getAttendees()));

        groupService.pingGroup(meeting.getGroup().getGroup().getId());

        Reminder reminder = reminderRepository.findByRemindableId(meetingId);
        if (reminder != null) {
            reminder.setReminderDate(meeting.getReminderDate());
            reminderRepository.save(reminder);
        }

        messageRepository.findByMeetingId(meetingId).ifPresent(message -> {
            message.setCreatedDate(DateUtils.getDateNowAtUTC());
            messageRepository.save(message);
        });

        return meetingRepository.save(meeting);
    }

    private boolean isEqualDate(Date oldDate, Date newDate) {
        Calendar oldTime = Calendar.getInstance();
        oldTime.setTime(oldDate);

        Calendar newTime = Calendar.getInstance();
        newTime.setTime(newDate);

        return oldTime.equals(newTime);
    }

    public void deleteMeeting(String meetingId) {
        var meeting = meetingRepository.findById(meetingId).orElseThrow(() -> new DomainException("Không tìm thấy cuộc họp"));

        meeting.setIsDeleted(true);
        meetingRepository.save(meeting);

        var message = messageRepository.findByMeetingId(meetingId).orElse(null);
        if (message != null) {
            message.setStatus(Message.Status.DELETED);
            messageRepository.save(message);
        }

        reminderRepository.deleteByRemindableId(meetingId);
    }

    @Transactional(readOnly = true)
    public MeetingDetailResponse getMeetingById(String userId, String meetingId) {
        var meeting = meetingRepository.findById(meetingId).orElseThrow(() -> new DomainException("Không tìm thấy cuộc họp"));
        var organizer = meeting.getOrganizer();
        var channel = meeting.getGroup();
        var group = channel.getGroup();

        Map<String, ShortProfile> modifiers = meeting.getHistories().stream()
                .map(MeetingHistory::getModifier)
                .map(user -> modelMapper.map(user, ShortProfile.class))
                .collect(Collectors.toMap(ShortProfile::getId, profile -> profile, (p1, p2) -> p2));

        List<MeetingHistoryDetail> historyDetails = meeting.getHistories().stream()
                .map(history -> {
                    ShortProfile user = modifiers.getOrDefault(history.getModifier().getId(), null);
                    return MeetingHistoryDetail.from(history, user);
                })
                .toList();

        boolean appliedAllGroup = meeting.getAttendees().size() == channel.getUsers().size();

        return MeetingDetailResponse.builder()
                .id(meetingId)
                .title(meeting.getTitle())
                .description(meeting.getDescription())
                .timeStart(meeting.getTimeStart())
                .timeEnd(meeting.getTimeEnd())
                .repeated(meeting.getRepeated())
                .place(meeting.getPlace())
                .organizer(UserDto.from(organizer))
                .group(GroupDto.from(group))
                .isAll(appliedAllGroup)
                .canEdit(group.isMentor(userId) || organizer.getId().equals(userId))
                .totalAttendees(appliedAllGroup ? channel.getUsers().size() - 1 : meeting.getAttendees().size())
                .histories(historyDetails)
                .build();
    }

    @Transactional(readOnly = true)
    public List<MeetingAttendeeResponse> getMeetingAttendees(String meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId).orElseThrow(() -> new DomainException("Không tìm thấy cuộc họp"));

        Channel channel = meeting.getGroup();
        Group group = channel.getGroup();

        List<String> attendeeIds = meeting.getAttendees().stream().map(User::getId).toList();

        return userRepository.findByIdIn(attendeeIds).stream()
                .map(user -> MeetingAttendeeResponse.from(user, group.isMentor(user.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Meeting> getAllOwnMeetings(String userId, List<String> channelIds) {
        return meetingRepository.findAllByOwn(channelIds, userId);
    }

    public List<Meeting> getAllOwnMeetingsByDate(String userId, Date date) {
        Date startTime = DateUtils.atStartOfDay(date);
        Date endTime = DateUtils.atEndOfDay(date);
        return getAllOwnMeetingsBetween(userId, startTime, endTime);
    }

    public List<Meeting> getAllOwnMeetingsBetween(String userId, Date startTime, Date endTime) {
        List<String> joinedGroupIds = groupService.getAllActiveOwnGroups(userId).stream()
                .map(Group::getId)
                .toList();
        List<Meeting> organizedMeetings = meetingRepository
                .findAllByGroupIdInAndOrganizerIdAndTimeStartGreaterThanEqualAndTimeEndLessThanEqual(
                        joinedGroupIds, userId, startTime, endTime);
        List<Meeting> attendeeMeetings = meetingRepository
                .findAllByGroupIdInAndAttendeesInAndTimeStartGreaterThanEqualAndTimeEndLessThanEqual(
                        joinedGroupIds, Arrays.asList("*", userId), startTime, endTime);
        return Stream.concat(organizedMeetings.stream(), attendeeMeetings.stream())
                .toList();
    }

    public List<Meeting> getAllOwnMeetingsByMonth(String userId, Date date) {
        Date startTime = DateUtils.atStartOfMonth(date);
        Date endTime = DateUtils.atEndOfMonth(date);
        return getAllOwnMeetingsBetween(userId, startTime, endTime);
    }

    public Meeting rescheduleMeeting(String modifierId, String meetingId, RescheduleMeetingRequest request) {
        var meeting = meetingRepository.findById(meetingId).orElse(null);
        if (meeting == null) {
            return null;
        }
        var modifier = userRepository.findById(modifierId).orElse(null);

        var history = MeetingHistory.builder()
                .timeStart(request.getTimeStart())
                .timeEnd(request.getTimeEnd())
                .place(request.getPlace())
                .modifier(modifier)
                .meeting(meeting)
                .build();
        history = meetingHistoryRepository.save(history);

        var histories = meeting.getHistories();
        histories.add(history);
        meeting.setHistories(histories);
        meeting.setTimeStart(request.getTimeStart());
        meeting.setTimeEnd(request.getTimeEnd());
        meeting.setPlace(request.getPlace());
        meetingRepository.save(meeting);

        messageRepository.findByMeetingId(meetingId).ifPresent(message -> {
            message.setCreatedDate(DateUtils.getDateNowAtUTC());
            messageRepository.save(message);
        });

        groupService.pingGroup(meeting.getGroup().getGroup().getId());

        notificationService.sendForRescheduleMeeting(modifier, meeting, request);

        return meeting;
    }

    @Override
    public void saveToReminder(IRemindable rewindable) {
        Reminder reminder = rewindable.toReminder();
        Meeting meeting = (Meeting) rewindable;

        reminder.setRecipients(new ArrayList<>(meeting.getAttendees()));
        reminder.setSubject("Bạn có 1 lịch hẹn sắp diễn ra");

        reminderRepository.save(reminder);
    }
}