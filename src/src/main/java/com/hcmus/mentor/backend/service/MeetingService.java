package com.hcmus.mentor.backend.service;

import com.hcmus.mentor.backend.controller.exception.DomainException;
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
        List<String> groupIds = groupService.getAllActiveOwnGroups(userId).stream()
                .map(Group::getId)
                .toList();
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
                    Group group = groupRepository.findById(meeting.getGroupId()).orElse(null);
                    User organizer = userRepository.findById(meeting.getOrganizerId()).orElse(null);
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

        Meeting newMeeting = meetingRepository.save(Meeting.from(request));
        Message newMessage = Message.builder()
                .senderId(request.getOrganizerId())
                .content("NEW MEETING")
                .createdDate(new Date())
                .type(Message.Type.MEETING)
                .groupId(request.getGroupId())
                .meetingId(newMeeting.getId())
                .build();
        messageService.saveMessage(newMessage);
        groupService.pingGroup(request.getGroupId());

        MessageDetailResponse response = messageService.fulfillMeetingMessage(
                MessageResponse.from(newMessage, ProfileResponse.from(organizer)));
        socketIOService.sendBroadcastMessage(response, request.getGroupId());
        notificationService.sendNewMeetingNotification(response);
        saveToReminder(newMeeting);

        return newMeeting;
    }

    public Meeting updateMeeting(String modifierId, String meetingId, UpdateMeetingRequest request) {
        Optional<Meeting> wrapper = meetingRepository.findById(meetingId);
        if (wrapper.isEmpty()) {
            return null;
        }

        Meeting meeting = wrapper.get();
        if (!isEqualDate(meeting.getTimeStart(), request.getTimeStart())
                || !isEqualDate(meeting.getTimeEnd(), request.getTimeEnd())) {
            meeting.reschedule(modifierId, request);
        }
        meeting.update(request);

        groupService.pingGroup(meeting.getGroupId());

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
        Optional<Meeting> wrapper = meetingRepository.findById(meetingId);
        if (wrapper.isEmpty()) {
            return null;
        }
        Meeting meeting = wrapper.get();

        Optional<User> organizerWrapper = userRepository.findById(meeting.getOrganizerId());
        if (organizerWrapper.isEmpty()) {
            return null;
        }
        User organizer = organizerWrapper.get();

        var channel = channelRepository.findById(meeting.getGroupId()).orElseThrow(() -> new DomainException("Không tìm thấy kênh"));
        var group = groupRepository.findById(channel.getParentId()).orElseThrow(() -> new DomainException("Không tìm thấy nhóm"));

        Map<String, ShortProfile> modifiers = meeting.getHistories().stream()
                .map(Meeting.MeetingHistory::getModifierId)
                .map(userRepository::findShortProfile)
                .collect(Collectors.toMap(ShortProfile::getId, profile -> profile, (p1, p2) -> p2));

        List<MeetingHistoryDetail> historyDetails = meeting.getHistories().stream()
                .map(history -> {
                    ShortProfile user = modifiers.getOrDefault(history.getModifierId(), null);
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
                .totalAttendees(appliedAllGroup ? group.getTotalMember() - 1 : meeting.getAttendees().size())
                .histories(historyDetails)
                .build();
    }

    public List<MeetingAttendeeResponse> getMeetingAttendees(String meetingId) {
        Optional<Meeting> wrapper = meetingRepository.findById(meetingId);
        if (wrapper.isEmpty()) {
            return Collections.emptyList();
        }
        Meeting meeting = wrapper.get();

        Optional<Group> groupWrapper = groupRepository.findById(meeting.getGroupId());
        if (groupWrapper.isEmpty()) {
            return Collections.emptyList();
        }
        Group group = groupWrapper.get();

        List<String> attendeeIds;
        boolean appliedAllGroup = meeting.getAttendees().contains("*");
        if (appliedAllGroup) {
            attendeeIds = Stream.concat(group.getMentees().stream(), group.getMentors().stream())
                    .filter(id -> !id.equals(meeting.getOrganizerId()))
                    .toList();
        } else {
            attendeeIds = meeting.getAttendees();
        }

        return userRepository.findByIdIn(attendeeIds).stream()
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

    public Meeting rescheduleMeeting(
            String modifierId, String meetingId, RescheduleMeetingRequest request) {
        Optional<Meeting> meetingWrapper = meetingRepository.findById(meetingId);
        if (meetingWrapper.isEmpty()) {
            return null;
        }

        Meeting meeting = meetingWrapper.get();
        meeting.reschedule(modifierId, request);
        groupService.pingGroup(meeting.getGroupId());
        return meetingRepository.save(meeting);
    }

    @Override
    public void saveToReminder(IRemindable rewindable) {
        Reminder reminder = rewindable.toReminder();
        Meeting meeting = (Meeting) rewindable;
        List<String> emailUsers = new ArrayList<>();
        List<String> attendees = meeting.getAttendees();
        attendees.add(meeting.getOrganizerId());
        for (String userId : attendees) {
            Optional<User> userOptional = userRepository.findById(userId);
            userOptional.ifPresent(user -> emailUsers.add(user.getEmail()));
        }
        reminder.setRecipients(emailUsers);
        reminder.setSubject("Bạn có 1 lịch hẹn sắp diễn ra");

        reminderRepository.save(reminder);
    }
}
