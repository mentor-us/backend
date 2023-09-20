package com.hcmus.mentor.backend.service;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Notification;
import com.hcmus.mentor.backend.entity.*;
import com.hcmus.mentor.backend.manager.FirebaseMessagingManager;
import com.hcmus.mentor.backend.payload.request.AddNotificationRequest;
import com.hcmus.mentor.backend.payload.request.RescheduleMeetingRequest;
import com.hcmus.mentor.backend.payload.request.SubscribeNotificationRequest;
import com.hcmus.mentor.backend.payload.response.messages.MessageDetailResponse;
import com.hcmus.mentor.backend.payload.response.NotificationResponse;
import com.hcmus.mentor.backend.payload.response.messages.ReactMessageResponse;
import com.hcmus.mentor.backend.payload.response.tasks.TaskAssigneeResponse;
import com.hcmus.mentor.backend.payload.response.tasks.TaskMessageResponse;
import com.hcmus.mentor.backend.payload.response.users.ShortProfile;
import com.hcmus.mentor.backend.repository.GroupRepository;
import com.hcmus.mentor.backend.repository.NotificationRepository;
import com.hcmus.mentor.backend.repository.NotificationSubscriberRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.hcmus.mentor.backend.entity.Notif.Type.*;

@Service
public class NotificationService {

    private final static Logger LOGGER = LogManager.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    private final NotificationSubscriberRepository notificationSubscriberRepository;

    private final GroupRepository groupRepository;

    private final FirebaseMessagingManager firebaseMessagingManager;

    private final UserRepository userRepository;

    private final GroupService groupService;

    public NotificationService(NotificationRepository notificationRepository,
                               NotificationSubscriberRepository notificationSubscriberRepository,
                               GroupRepository groupRepository,
                               FirebaseMessagingManager firebaseMessagingManager,
                               UserRepository userRepository,
                               @Lazy GroupService groupService) {
        this.notificationRepository = notificationRepository;
        this.notificationSubscriberRepository = notificationSubscriberRepository;
        this.groupRepository = groupRepository;
        this.firebaseMessagingManager = firebaseMessagingManager;
        this.userRepository = userRepository;
        this.groupService = groupService;
    }

    public Map<String, Object> getOwnNotifications(String userId, int page, int size) {
        PageRequest paging = PageRequest.of(page, size,
                Sort.by("createdDate").descending());
        Slice<Notif> slice = notificationRepository
                .findByReceiverIdsIn(Collections.singletonList(userId), paging);
        List<String> senderIds = slice.get()
                .map(Notif::getSenderId)
                .collect(Collectors.toList());
        Map<String, ShortProfile> senders = userRepository
                .findByIds(senderIds).stream()
                .collect(Collectors
                        .toMap(ShortProfile::getId, profile -> profile, (p1, p2) -> p2));
        List<NotificationResponse> notifications = slice.getContent().stream()
                .map(notification -> NotificationResponse.from(notification,
                        senders.getOrDefault(notification.getSenderId(), null)))
                .collect(Collectors.toList());
        return pagingResponse(slice, notifications);
    }

    private Map<String, Object> pagingResponse(Slice<Notif> slice, List<NotificationResponse> notifications) {
        Map<String, Object> response = new HashMap<>();
        response.put("notifications", notifications);
        response.put("hasMore", slice.hasNext());
        return response;
    }

    public Notif createResponseNotification(String senderId, AddNotificationRequest request) {
        Notif notif = Notif.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .type(Notif.Type.NEED_RESPONSE)
                .senderId(senderId)
                .receiverIds(Collections.singletonList(request.getReceiverId()))
                .createdDate(request.getCreatedDate())
                .build();
        return notificationRepository.save(notif);
    }

    public Notif responseNotification(String userId, String notificationId, String action) {
        Optional<Notif> notificationWrapper = notificationRepository.findById(notificationId);
        if (!notificationWrapper.isPresent()) {
            return null;
        }

        Notif notif = notificationWrapper.get();
        if (!notif.getType().equals(Notif.Type.NEED_RESPONSE)) {
            return null;
        }

        switch (action) {
            case "seen":
                notif.seen(userId);
                break;
            case "accept":
                notif.accept(userId);
                break;
            case "refuse":
                notif.refuse(userId);
                break;
            default:
                break;
        }
        return notificationRepository.save(notif);
    }

    public void subscribe(SubscribeNotificationRequest request) {
        if (request == null) {
            return;
        }

        if (request.getToken().isEmpty()) {
            LOGGER.info("[*] Unsubscribe user notification: userID({})",
                    request.getUserId());
            unsubscribe(request.getUserId());
            return;
        }
        LOGGER.info("[*] Subscribe user notification: userID({}) | Token({})",
                request.getUserId(), request.getToken());

        List<NotificationSubscriber> subscribes = notificationSubscriberRepository
                .findByUserIdOrToken(request.getUserId(), request.getToken());
        if (subscribes.size() == 0) {
            NotificationSubscriber subscriber = NotificationSubscriber.builder()
                    .userId(request.getUserId())
                    .token(request.getToken())
                    .build();
            notificationSubscriberRepository.save(subscriber);
            return;
        }

        subscribes.forEach(subscriber -> subscriber.update(request));
        notificationSubscriberRepository.saveAll(subscribes);
    }

    public void unsubscribe(String userId) {
        notificationSubscriberRepository.deleteByUserId(userId);
    }

    @Async
    public void sendNewMessageNotification(MessageDetailResponse message) {
        Optional<Group> groupWrapper = groupRepository.findById(message.getGroupId());
        if (!groupWrapper.isPresent()) {
            return;
        }
        Group group = groupWrapper.get();

        Map<String, String> data = attachDataNotification(message.getGroupId(), NEW_MESSAGE);
        String senderName = "";
        if (message.getSender() != null) {
            senderName = message.getSender().getName();
            data.put("sender", senderName);
            String imageUrl = message.getSender().getImageUrl() == null
                    || ("https://graph.microsoft.com/v1.0/me/photo/$value").equals(message.getSender().getImageUrl())
                    ? "" : message.getSender().getImageUrl();
            data.put("imageUrl", imageUrl);
        }

        String title = group.getName();
        String body = senderName + ": " + Jsoup.parse(message.getContent()).text();
        groupService.updateLastMessage(group.getId(), body);
        try {
            List<String> members = Stream.concat(group.getMentors().stream(),
                            group.getMentees().stream())
                    .filter(id -> !id.equals(message.getSender().getId()))
                    .distinct().collect(Collectors.toList());
            firebaseMessagingManager.sendGroupNotification(members, title, body, data);
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
    }

    private Map<String, String> attachDataNotification(String groupId, Notif.Type type) {
        Map<String, String> data = new HashMap<>();
        data.put("type", type.name());
        data.put("screen", "chat");
        data.put("groupId", groupId);
        return data;
    }

    @Async
    public void sendNewTaskNotification(MessageDetailResponse message) {
        if (message.getTask() == null) {
            LOGGER.warn("[!] Task message #{}, NULL cannot send notifications",
                    message.getId());
            return;
        }
        TaskMessageResponse task = message.getTask();

        Optional<Group> groupWrapper = groupRepository.findById(message.getGroupId());
        if (!groupWrapper.isPresent()) {
            return;
        }
        Group group = groupWrapper.get();

        String title = group.getName();
        String content = "Nhóm có công việc mới \"" + task.getTitle() + "\"";
        Notif notif = createNewTaskNotification(title, content,
                message.getSender().getId(), task);
        groupService.updateLastMessage(group.getId(), content);
        try {
            firebaseMessagingManager.sendGroupNotification(notif.getReceiverIds(), title,
                    content, attachDataNotification(message.getGroupId(), NEW_TASK));
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
    }

    public Notif createNewTaskNotification(String title, String content, String senderId,
                                           TaskMessageResponse task) {
        List<String> assigneeIds = task.getAssignees().stream()
                .map(TaskAssigneeResponse::getId)
                .collect(Collectors.toList());
        List<String> receiverIds = Stream.concat(assigneeIds.stream(),
                        Stream.of(task.getAssignerId()))
                .distinct().collect(Collectors.toList());
        Notif inAppNotification = Notif.builder()
                .title(title)
                .content(content)
                .type(NEW_TASK)
                .senderId(senderId)
                .refId(task.getId())
                .receiverIds(receiverIds)
                .build();
        return notificationRepository.save(inAppNotification);
    }

    @Async
    public void sendNewMeetingNotification(MessageDetailResponse message) {
        if (message.getMeeting() == null) {
            LOGGER.warn("[!] Meeting message #{}, NULL cannot send notifications",
                    message.getId());
            return;
        }
        Meeting meeting = message.getMeeting();

        Optional<Group> groupWrapper = groupRepository.findById(message.getGroupId());
        if (!groupWrapper.isPresent()) {
            return;
        }
        Group group = groupWrapper.get();

        String title = group.getName();
        String content = "Nhóm có lịch hẹn mới \"" + meeting.getTitle() + "\"";
        Notif notif = createNewMeetingNotification(title, content,
                message.getSender().getId(), meeting);
        groupService.updateLastMessage(group.getId(), content);
        try {
            firebaseMessagingManager.sendGroupNotification(notif.getReceiverIds(), title,
                    content, attachDataNotification(message.getGroupId(), NEW_MEETING));
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
    }

    public Notif createNewMeetingNotification(String title, String content, String senderId,
                                              Meeting meeting) {
        List<String> receiverIds = Stream.concat(meeting.getAttendees().stream(),
                        Stream.of(meeting.getOrganizerId()))
                .distinct().collect(Collectors.toList());
        Notif notif = Notif.builder()
                .title(title)
                .content(content)
                .type(NEW_MEETING)
                .senderId(senderId)
                .refId(meeting.getId())
                .receiverIds(receiverIds)
                .build();
        return notificationRepository.save(notif);
    }

    @Async
    public void sendNewMediaMessageNotification(MessageDetailResponse message) {
        boolean isImageMessage = Message.Type.IMAGE.equals(message.getType())
                && message.getImages() != null
                && message.getImages().size() != 0;
        boolean isFileMessage = Message.Type.FILE.equals(message.getType())
                && message.getFile() != null;
        if (!isImageMessage && !isFileMessage) {
            LOGGER.warn("[!] Media message #{}, empty cannot send notifications",
                    message.getId());
            return;
        }

        Optional<Group> groupWrapper = groupRepository.findById(message.getGroupId());
        if (!groupWrapper.isPresent()) {
            return;
        }
        Group group = groupWrapper.get();

        Notif.Type type;
        StringBuilder notificationBody = new StringBuilder(
                (message.getSender() != null)
                        ? (message.getSender().getName() + " đã gửi ")
                        : "");
        if (Message.Type.IMAGE.equals(message.getType())) {
            type = NEW_IMAGE_MESSAGE;
            notificationBody.append(message.getImages().size()).append(" ảnh mới.");
        } else if (Message.Type.FILE.equals(message.getType())) {
            type = NEW_FILE_MESSAGE;
            notificationBody.append(" một tệp đính kèm mới.");
        } else {
            type = SYSTEM;
            notificationBody.append(" một đa phương tiện mới.");
        }

        String title = group.getName();
        String body = notificationBody.toString();
        groupService.updateLastMessage(group.getId(), body);
        try {
            List<String> receiverIds = Stream.concat(group.getMentors().stream(),
                            group.getMentees().stream())
                    .filter(id -> !id.equals(message.getSender().getId()))
                    .distinct().collect(Collectors.toList());
            firebaseMessagingManager.sendGroupNotification(receiverIds, title,
                    body, attachDataNotification(message.getGroupId(), type));
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
    }

    public Notif createNewMediaNotification(String title, String content, String senderId,
                                            Group group) {
        List<String> receiverIds = Stream.concat(group.getMentors().stream(),
                        group.getMentees().stream())
                .filter(id -> !id.equals(senderId))
                .distinct().collect(Collectors.toList());
        Notif notif = Notif.builder()
                .title(title)
                .content(content)
                .type(NEW_FILE_MESSAGE)
                .senderId(senderId)
                .refId(group.getId())
                .receiverIds(receiverIds)
                .build();
        return notificationRepository.save(notif);
    }

    @Async
    public void sendNewReactNotification(Message message, ReactMessageResponse reaction, String senderId) {
        if (message == null || reaction == null) {
            return;
        }

        if (message.getSenderId() == null || senderId == null) {
            return;
        }

        if (senderId.equals(message.getSenderId())) {
            return;
        }

        Optional<Group> groupWrapper = groupRepository.findById(message.getGroupId());
        if (!groupWrapper.isPresent()) {
            return;
        }
        Group group = groupWrapper.get();

        Map<String, String> data = attachDataNotification(message.getGroupId(), NEW_REACTION);
        Notification notification = Notification.builder()
                .setTitle(group.getName())
                .setBody(reaction.getName() + " đã thể hiện cảm xúc tin nhắn của bạn.")
                .build();
        try {
            Optional<NotificationSubscriber> wrapper = notificationSubscriberRepository
                    .findByUserId(message.getSenderId());
            if (!wrapper.isPresent()) {
                return;
            }
            NotificationSubscriber subscriber = wrapper.get();
            firebaseMessagingManager.sendNotification(subscriber.getToken(), notification, data);
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
    }

    @Async
    public void sendRescheduleMeetingNotification(String modifierId, Meeting meeting, RescheduleMeetingRequest request) {
        if (meeting == null) {
            return;
        }

        Optional<Group> groupWrapper = groupRepository.findById(meeting.getGroupId());
        if (!groupWrapper.isPresent()) {
            return;
        }
        Group group = groupWrapper.get();

        String title = group.getName();
        String content = "Lịch hẹn: \"" + meeting.getTitle() + "\" đã được dời thời gian.";
        Notif notif = createRescheduleMeetingNotification(title, content,
                modifierId, group, meeting);
        groupService.updateLastMessage(group.getId(), content);
        try {
            firebaseMessagingManager.sendGroupNotification(notif.getReceiverIds(), title,
                    content, attachDataNotification(meeting.getGroupId(), RESCHEDULE_MEETING));
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
    }

    public Notif createRescheduleMeetingNotification(String title, String content,
                                                     String senderId, Group group,
                                                     Meeting meeting) {
        List<String> receiverIds = Stream.concat(group.getMentors().stream(),
                        group.getMentees().stream())
                .filter(id -> !id.equals(senderId))
                .distinct().collect(Collectors.toList());
        Notif notif = Notif.builder()
                .title(title)
                .content(content)
                .type(RESCHEDULE_MEETING)
                .senderId(senderId)
                .refId(meeting.getId())
                .receiverIds(receiverIds)
                .build();
        return notificationRepository.save(notif);
    }

    public long getUnreadNumber(String userId) {
        return notificationRepository.countDistinctByReceiverIdsIn(Arrays.asList(userId));
    }

    @Async
    public void sendNewVoteNotification(String creatorId, Vote vote) {
        if (vote == null) {
            return;
        }

        Optional<Group> groupWrapper = groupRepository.findById(vote.getGroupId());
        if (!groupWrapper.isPresent()) {
            return;
        }
        Group group = groupWrapper.get();

        String title = group.getName();
        String content = "Nhóm có cuộc bình chọn mới \"" + vote.getQuestion() + "\"";
        Notif notif = createNewVoteNotification(title, content, creatorId, group, vote);
        groupService.updateLastMessage(group.getId(), content);
        try {
            firebaseMessagingManager.sendGroupNotification(notif.getReceiverIds(), title,
                    content, attachDataNotification(vote.getGroupId(), NEW_VOTE));
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
    }

    public Notif createNewVoteNotification(String title, String content,
                                           String senderId, Group group,
                                           Vote vote) {
        List<String> receiverIds = Stream.concat(group.getMentors().stream(),
                        group.getMentees().stream())
                .filter(id -> !id.equals(senderId))
                .distinct().collect(Collectors.toList());
        Notif notif = Notif.builder()
                .title(title)
                .content(content)
                .type(NEW_VOTE)
                .senderId(senderId)
                .refId(vote.getId())
                .receiverIds(receiverIds)
                .build();
        return notificationRepository.save(notif);
    }

    public void sendNewPinNotification(MessageDetailResponse message, User pinner) {
        if (message == null) {
            return;
        }

        if (pinner == null) {
            return;
        }

        Optional<Group> groupWrapper = groupRepository.findById(message.getGroupId());
        if (!groupWrapper.isPresent()) {
            return;
        }
        Group group = groupWrapper.get();

        Map<String, String> data = attachDataNotification(message.getGroupId(), PIN_MESSAGE);
        String title = group.getName();
        String shortMessage = message.getContent().substring(0, Math.min(message.getContent().length(), 25));
        String content = pinner.getName() + " đã ghim tin nhắn \"" + shortMessage + "\"";
        groupService.updateLastMessage(group.getId(), content);
        try {
            List<String> members = Stream.concat(group.getMentors().stream(),
                            group.getMentees().stream())
                    .filter(id -> !id.equals(pinner.getId()))
                    .distinct().collect(Collectors.toList());
            firebaseMessagingManager.sendGroupNotification(members, title, content, data);
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
    }

    public void sendNewUnpinNotification(MessageDetailResponse message, User pinner) {
        if (message == null) {
            return;
        }

        if (pinner == null) {
            return;
        }

        Optional<Group> groupWrapper = groupRepository.findById(message.getGroupId());
        if (!groupWrapper.isPresent()) {
            return;
        }
        Group group = groupWrapper.get();

        Map<String, String> data = attachDataNotification(message.getGroupId(), UNPIN_MESSAGE);
        String title = group.getName();
        String shortMessage = message.getContent().substring(0, Math.min(message.getContent().length(), 25));
        String content = pinner.getName() + " đã bỏ ghim tin nhắn \"" + shortMessage + "\"";
        groupService.updateLastMessage(group.getId(), content);
        try {
            List<String> members = Stream.concat(group.getMentors().stream(),
                            group.getMentees().stream())
                    .filter(id -> !id.equals(pinner.getId()))
                    .distinct().collect(Collectors.toList());
            firebaseMessagingManager.sendGroupNotification(members, title, content, data);
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
    }
}
