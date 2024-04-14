package com.hcmus.mentor.backend.service;

import com.hcmus.mentor.backend.controller.mapper.UserMapper;
import com.hcmus.mentor.backend.controller.payload.request.RescheduleMeetingRequest;
import com.hcmus.mentor.backend.controller.payload.request.meetings.CreateMeetingRequest;
import com.hcmus.mentor.backend.controller.payload.request.meetings.UpdateMeetingRequest;
import com.hcmus.mentor.backend.controller.payload.response.meetings.MeetingAttendeeResponse;
import com.hcmus.mentor.backend.controller.payload.response.meetings.MeetingDetailResponse;
import com.hcmus.mentor.backend.controller.payload.response.meetings.MeetingHistoryDetail;
import com.hcmus.mentor.backend.controller.payload.response.meetings.MeetingResponse;
import com.hcmus.mentor.backend.controller.payload.response.messages.MessageDetailResponse;
import com.hcmus.mentor.backend.controller.payload.response.messages.MessageResponse;
import com.hcmus.mentor.backend.controller.payload.response.users.ProfileResponse;
import com.hcmus.mentor.backend.controller.payload.response.users.ShortProfile;
import com.hcmus.mentor.backend.domain.*;
import com.hcmus.mentor.backend.domain.method.IRemindable;
import com.hcmus.mentor.backend.repository.*;
import com.hcmus.mentor.backend.util.DateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class MeetingService implements IRemindableService {

    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final GroupService groupService;
    private final MessageService messageService;
    private final SocketIOService socketIOService;
    private final ReminderRepository reminderRepository;
    private final NotificationService notificationService;
    private final ChannelRepository channelRepository;

    public List<MeetingResponse> getMostRecentMeetings(String userId) {
        List<String> groupIds = groupService.getAllActiveOwnGroups(userId).stream().map(Group::getId).toList();
        Date now = new Date();
        List<Meeting> meetings = meetingRepository
                .findAllByGroupIdInAndOrganizerIdAndTimeStartGreaterThanOrGroupIdInAndAttendeesInAndTimeStartGreaterThan(
                        groupIds,
                        userId,
                        now,
                        groupIds,
                        Arrays.asList("*", userId),
                        now,
                        PageRequest.of(0, 5, Sort.by("timeStart").descending()))
                .getContent();
        return meetings.stream()
                .map(meeting -> {
                    Group group = meeting.getGroup().getGroup();
                    User organizer = meeting.getOrganizer();
                    return MeetingResponse.from(meeting, organizer, group);
                })
                .toList();
    }

    public List<MeetingResponse> getMeetingGroup(String groupId) {
        return meetingRepository.findAllByGroupId(groupId);
    }

    public List<Meeting> getMeetingGroup(String groupId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Meeting> wrapper = meetingRepository.findByGroupId(groupId, pageRequest);
        return wrapper.getContent();
    }

    public Meeting createNewMeeting(CreateMeetingRequest request) {
        User organizer = userRepository.findById(request.getOrganizerId()).orElse(null);
        if (organizer == null) {
            return null;
        }

        var meeting = meetingRepository.save(Meeting.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .timeStart(request.getTimeStart())
                .timeEnd(request.getTimeEnd())
                .repeated(request.getRepeated())
                .place(request.getPlace())
                .organizer(organizer)
                .group(channelRepository.findById(request.getGroupId()).orElse(null))
                .attendees(userRepository.findByIdIn(request.getAttendees()))
                .build());
        Message newMessage = Message.builder()
                .sender(meeting.getOrganizer())
                .content("NEW MEETING")
                .createdDate(new Date())
                .type(Message.Type.MEETING)
                .channel(meeting.getGroup())
                .meeting(meeting)
                .build();
        messageService.saveMessage(newMessage);
        groupService.pingGroup(request.getGroupId());

        MessageDetailResponse response =
                messageService.fulfillMeetingMessage(
                        MessageResponse.from(newMessage, ProfileResponse.from(organizer)));
        socketIOService.sendBroadcastMessage(response, request.getGroupId());
        notificationService.sendNewMeetingNotification(response);
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
            meeting.reschedule(modifier, request);
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
        meetingRepository.deleteById(meetingId);
        reminderRepository.deleteByRemindableId(meetingId);
    }

    public MeetingDetailResponse getMeetingById(String userId, String meetingId) {
        var meeting = meetingRepository.findById(meetingId).orElse(null);
        if (meeting == null) {
            return null;
        }

        var organizer = meeting.getOrganizer();
        Group group;
        group = meeting.getGroup().getGroup();

        Map<String, ShortProfile> modifiers =
                meeting.getHistories().stream()
                        .map(MeetingHistory::getModifier)
                        .map(UserMapper.INSTANCE::userToShortProfile)
                        .collect(Collectors.toMap(ShortProfile::getId, profile -> profile, (p1, p2) -> p2));

        List<MeetingHistoryDetail> historyDetails =
                meeting.getHistories().stream()
                        .map(history -> {
                            ShortProfile user = modifiers.getOrDefault(history.getModifier().getId(), null);
                            return MeetingHistoryDetail.from(history, user);
                        })
                        .toList();

        boolean appliedAllGroup = meeting.getAttendees().contains("*");
        return MeetingDetailResponse.builder()
                .id(meetingId)
                .title(meeting.getTitle())
                .description(meeting.getDescription())
                .timeStart(meeting.getTimeStart())
                .timeEnd(meeting.getTimeEnd())
                .repeated(meeting.getRepeated())
                .place(meeting.getPlace())
                .organizer(organizer)
                .group(group)
                .isAll(appliedAllGroup)
                .canEdit(group.isMentor(userId) || organizer.getId().equals(userId))
                .totalAttendees(appliedAllGroup ? meeting.getGroup().getUsers().size() - 1 : meeting.getAttendees().size())
                .histories(historyDetails)
                .build();
    }

    public List<MeetingAttendeeResponse> getMeetingAttendees(String meetingId) {
       var meeting = meetingRepository.findById(meetingId).orElse(null);
        if (meeting == null) {
            return Collections.emptyList();
        }

        var channel = meeting.getGroup();
        if (channel == null) {
            return Collections.emptyList();
        }

        var group = channel.getGroup();

        return channel.getUsers().stream()
                .filter(user -> user.getId()!= meeting.getOrganizer().getId())
                .map(user -> MeetingAttendeeResponse.from(user, group.isMentor(user.getId())))
                .toList();
    }

    public List<Meeting> getAllOwnMeetings(String userId) {
        List<String> joinedGroupIds = groupService.getAllActiveOwnGroups(userId).stream()
                .map(Group::getId)
                .toList();
        return meetingRepository.findAllByGroupIdInAndOrganizerIdOrGroupIdInAndAttendeesIn(
                joinedGroupIds, userId, joinedGroupIds, Arrays.asList("*", userId));
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
        meeting.reschedule(modifier, request);

        groupService.pingGroup(meeting.getGroup().getGroup().getId());
        return meetingRepository.save(meeting);
    }

    @Override
    public void saveToReminder(IRemindable rewindable) {
        Reminder reminder = rewindable.toReminder();
        Meeting meeting = (Meeting) rewindable;

        reminder.setRecipients(meeting.getAttendees());
        reminder.setSubject("Bạn có 1 lịch hẹn sắp diễn ra");

        reminderRepository.save(reminder);
    }
}
