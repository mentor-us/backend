package com.hcmus.mentor.backend.service.impl;

import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.payload.request.AddNotificationRequest;
import com.hcmus.mentor.backend.controller.payload.request.RescheduleMeetingRequest;
import com.hcmus.mentor.backend.controller.payload.request.SubscribeNotificationRequest;
import com.hcmus.mentor.backend.controller.payload.response.NotificationResponse;
import com.hcmus.mentor.backend.controller.payload.response.messages.MessageDetailResponse;
import com.hcmus.mentor.backend.controller.payload.response.messages.ReactMessageResponse;
import com.hcmus.mentor.backend.domain.*;
import com.hcmus.mentor.backend.domain.constant.NotificationType;
import com.hcmus.mentor.backend.event.SendFirebaseNotificationEvent;
import com.hcmus.mentor.backend.repository.*;
import com.hcmus.mentor.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Stream;

import static com.hcmus.mentor.backend.domain.constant.ChannelType.PRIVATE_MESSAGE;
import static com.hcmus.mentor.backend.domain.constant.NotificationType.*;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LogManager.getLogger(NotificationServiceImpl.class);
    private final NotificationRepository notificationRepository;
    private final NotificationSubscriberRepository notificationSubscriberRepository;
    private final GroupRepository groupRepository;
    private final FirebaseServiceImpl firebaseService;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final NotificationUserRepository notificationUserRepository;
    private final ModelMapper modelMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public Map<String, Object> getOwnNotifications(String userId, int page, int size) {
        PageRequest paging = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Notification> notifications = notificationRepository.findOwnNotifications(Collections.singletonList(userId), paging);
        List<NotificationResponse> notificationsResponse = notifications.stream()
                .map(notification -> modelMapper.map(notification, NotificationResponse.class)).toList();
        return pagingResponse(notifications, notificationsResponse);
    }

    /**
     * @param senderId
     * @param request
     * @return
     */
    @Override
    public Notification createResponseNotification(String senderId, AddNotificationRequest request) {
        Notification notif = notificationRepository.save(Notification.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .type(NotificationType.NEED_RESPONSE)
                .sender(userRepository.findById(senderId).orElse(null))
                .createdDate(request.getCreatedDate())
                .build());
        var receivers = NotificationUser.builder().notification(notif).user(userRepository.findById(request.getReceiverId()).orElse(null)).build();
        notif.setReceivers(Collections.singletonList(receivers));
        return notif;
    }


    /**
     * @param userId
     * @param notificationId
     * @param action
     * @return Notification
     */
    @Override
    public Notification responseNotification(String userId, String notificationId, String action) {
        Notification notif = notificationRepository.findById(notificationId).orElseThrow(() -> new NoSuchElementException("Notification not found"));

        if (!notif.getType().equals(NotificationType.NEED_RESPONSE)) {
            throw new DomainException("Notification is not a response type");
        }

        NotificationUser notificationUser = notif.getReceivers().stream()
                .filter(nu -> nu.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new DomainException("User is not a receiver of this notification"));

        switch (action) {
            case "seen":
                notificationUser.setIsReaded(true);
                break;
            case "accept":
                notificationUser.setIsReaded(true);
                notificationUser.setIsAgreed(true);
                break;
            case "refuse":
                notificationUser.setIsReaded(true);
                notificationUser.setIsAgreed(false);
                break;
            default:
                break;
        }
        notificationUserRepository.save(notificationUser);

        return notif;
    }


    /**
     * @param request
     */
    @Override
    public void subscribeNotification(SubscribeNotificationRequest request) {
        if (request == null) {
            return;
        }

        unsubscribeNotification(request.getUserId());

        if (request.getToken().isEmpty()) {
            logger.info("[*] Unsubscribe user notification: userID({})", request.getUserId());
            return;
        }
        logger.info("[*] Subscribe user notification: userID({}) | Token({})", request.getUserId(), request.getToken());

        List<NotificationSubscriber> subscribes =
                notificationSubscriberRepository.findByUserIdOrToken(
                        request.getUserId(), request.getToken());
        if (subscribes.isEmpty()) {
            NotificationSubscriber subscriber =
                    NotificationSubscriber.builder()
                            .user(userRepository.findById(request.getUserId()).orElse(null))
                            .token(request.getToken())
                            .build();
            notificationSubscriberRepository.save(subscriber);
            return;
        }

        subscribes.forEach(subscriber -> {
            subscriber.setToken(request.getToken());
            subscriber.setUser(userRepository.findById(request.getUserId()).orElse(null));
        });
        notificationSubscriberRepository.saveAll(subscribes);
    }

    private Map<String, Object> pagingResponse(Slice<Notification> slice, List<NotificationResponse> notifications) {
        Map<String, Object> response = new HashMap<>();
        response.put("notifications", notifications);
        response.put("hasMore", slice.hasNext());
        return response;
    }

    @Override
    public void unsubscribeNotification(String userId) {
        notificationSubscriberRepository.deleteByUserId(userId);
    }

    @Override
    @Async
    @Transactional(readOnly = true)
    public void sendNewMessageNotification(MessageDetailResponse message) {
        var channelTemp = channelRepository.findById(message.getGroupId()).orElse(null);
        if (channelTemp == null) {
            return;
        }

        var sender = message.getSender();
        if (sender == null) {
            return;
        }

        var senderId = sender.getId();
        var receiverIds = channelTemp.getUsers().stream()
                .map(User::getId)
                .filter(id -> !Objects.equals(id, senderId))
                .toList();
        if (receiverIds.isEmpty()) {
            return;
        }

        var title = String.format("%s%n%s", channelTemp.getName(), message.getSender().getName());
        var data = attachDataNotification(message.getGroupId(), NEW_MESSAGE);
        var senderName = sender.getName();
        data.put("sender", senderName);
        data.put("imageUrl", message.getSender().getImageUrl());
        var body = String.format("%s: %s", senderName, Jsoup.parse(message.getContent()).text());

        firebaseService.sendNotificationMulticast(receiverIds, title, body, data);
    }

    private Map<String, String> attachDataNotification(String groupId, NotificationType type) {
        Map<String, String> data = new HashMap<>();
        data.put("type", type.name());
        data.put("screen", "chat");
        data.put("groupId", groupId);
        return data;
    }

    @Override
    public void sendNewTaskNotification(MessageDetailResponse message, Task task) {
        if (message.getTask() == null || task == null) {
            logger.warn("[!] Task message #{}, NULL cannot send notifications", message.getId());
            return;
        }

        Group group = task.getGroup().getGroup();
        if (group == null) {
            return;
        }


        var title = group.getName();
        var body = "Nhóm có công việc mới \"" + task.getTitle() + "\"";
        var notification = createNewTaskNotification(title, body, message.getSender().getId(), task);
        var receivers = notification.getReceivers().stream().map(n -> n.getUser().getId()).toList();
        var data = attachDataNotification(message.getGroupId(), NEW_TASK);

        firebaseService.sendNotificationMulticast(receivers, title, body, data);
    }

    @Override
    public Notification createNewTaskNotification(String title, String content, String senderId, Task task) {
        var assignees = task.getAssignees();
        var assigner = task.getAssigner();

        Notification notification = Notification.builder()
                .title(title)
                .content(content)
                .type(NEW_TASK)
                .sender(assigner)
                .refId(task.getId())
                .build();

        var receivers = assignees.stream()
                .map(Assignee::getUser)
                .filter(user -> user != assigner)
                .map(u -> NotificationUser.builder()
                        .notification(notification)
                        .user(u).build())
                .toList();

        notification.setReceivers(receivers);
        notificationRepository.save(notification);

        return notification;
    }

    @Override
    public void sendNewMeetingNotification(Meeting meeting) {
        if (meeting == null) {
            logger.warn("[!] Meeting message #{}, NULL cannot send notifications", meeting.getId());
            return;
        }
        Group group = meeting.getGroup().getGroup();

        var title = group == null ? "" : group.getName();
        var body = String.format("Nhóm có lịch hẹn mới \"%s\"", meeting.getTitle());
        var notification = createNewMeetingNotification(title, body, meeting.getOrganizer().getId(), meeting);
        var receiverIds = notification.getReceivers().stream().map(nu -> nu.getUser().getId()).toList();
        var data = attachDataNotification(meeting.getGroup().getId(), NEW_MEETING);

        firebaseService.sendNotificationMulticast(receiverIds, title, body, data);
    }

    @Override
    public Notification createNewMeetingNotification(String title, String content, String senderId, Meeting meeting) {
        var notification = Notification.builder()
                .title(title)
                .content(content)
                .type(NEW_MEETING)
                .sender(userRepository.findById(senderId).orElse(null))
                .refId(meeting.getId())
                .build();

        var receiver = meeting.getAttendees().stream().map(user -> NotificationUser.builder().notification(notification).user(user).build()).toList();
        notification.setReceivers(receiver);

        return notificationRepository.save(notification);
    }

    @Override
    @Async
    public void sendNewMediaMessageNotification(MessageDetailResponse message) {
        boolean isImageMessage = Message.Type.IMAGE.equals(message.getType()) && message.getImages() != null && !message.getImages().isEmpty();
        boolean isFileMessage = Message.Type.FILE.equals(message.getType()) && message.getFile() != null;
        if (!isImageMessage && !isFileMessage) {
            logger.warn("[!] Media message #{}, empty cannot send notifications", message.getId());
            return;
        }

        String title;
        List<String> members;

        Optional<Group> groupWrapper = groupRepository.findById(message.getGroupId());
        if (groupWrapper.isEmpty()) {
            Optional<Channel> channelWrapper = channelRepository.findById(message.getGroupId());
            if (channelWrapper.isEmpty()) {
                return;
            }

            Channel channel = channelWrapper.get();
            Group parentGroup = channel.getGroup();
            title = PRIVATE_MESSAGE.equals(channel.getType())
                    ? parentGroup.getName() + "\n" + message.getSender().getName()
                    : channel.getName();
            members = channel.getUsers().stream().map(User::getId)
                    .filter(id -> !id.equals(message.getSender().getId()))
                    .distinct()
                    .toList();
        } else {
            Group group = groupWrapper.get();
            title = group.getName();
            members = Stream.concat(group.getMentors().stream().map(User::getId), group.getMentees().stream().map(User::getId))
                    .filter(id -> !id.equals(message.getSender().getId()))
                    .distinct()
                    .toList();
        }

        if (members.isEmpty()) {
            return;
        }

        NotificationType type;
        StringBuilder notificationBody = new StringBuilder(message.getSender().getName() + " đã gửi ");
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

        var body = notificationBody.toString();
        var data = attachDataNotification(message.getGroupId(), type);
        firebaseService.sendNotificationMulticast(members, title, body, data);
    }

    /**
     * @param title
     * @param content
     * @param senderId
     * @param group
     * @return
     */
    @Override
    public Notification createNewMediaNotification(final String title, final String content, final String senderId, final Group group) {
        return null;
    }

    @Override
    @Async
    public void sendNewReactNotification(Message message, ReactMessageResponse reaction, String senderId) {
        if (message == null || reaction == null || message.getSender() == null || senderId == null) {
            return;
        }
        var group = message.getChannel().getGroup();

        NotificationSubscriber subscriber = notificationSubscriberRepository.findByUserId(message.getSender().getId()).orElse(null);
        if (subscriber == null) {
            return;
        }
        var title = group.getName();
        var body = message.getSender().getName() + " đã thể hiện cảm xúc tin nhắn của bạn.";
        Map<String, String> data = attachDataNotification(group.getId(), NEW_REACTION);

        var event = new SendFirebaseNotificationEvent(this, Collections.singletonList(subscriber.getToken()), title, body, data);

        applicationEventPublisher.publishEvent(event);
    }

    @Override
    @Async
    public void sendRescheduleMeetingNotification(String modifierId, Meeting meeting, RescheduleMeetingRequest request) {
        if (meeting == null) {
            return;
        }

        Group group = meeting.getGroup().getGroup();
        if (group == null) {
            return;
        }

        var title = group.getName();
        var body = "Lịch hẹn: \"" + meeting.getTitle() + "\" đã được dời thời gian.";
        var notification = createRescheduleMeetingNotification(title, body, modifierId, group, meeting);
        var receiverIds = notification.getReceivers().stream().map(nu -> nu.getUser().getId()).toList();
        var data = attachDataNotification(meeting.getGroup().getGroup().getId(), RESCHEDULE_MEETING);

        firebaseService.sendNotificationMulticast(receiverIds, title, body, data);
    }

    @Override
    public Notification createRescheduleMeetingNotification(String title, String content, String senderId, Group group, Meeting meeting) {
        Notification notif = notificationRepository.save(Notification.builder()
                .title(title)
                .content(content)
                .type(RESCHEDULE_MEETING)
                .sender(userRepository.findById(senderId).orElse(null))
                .refId(meeting.getId())
                .build());
        var receiverIds = Stream.concat(group.getMentors().stream(), group.getMentees().stream())
                .filter(id -> !id.equals(senderId))
                .distinct()
                .map(user -> NotificationUser.builder().notification(notif).user(user).build())
                .toList();
        notif.setReceivers(receiverIds);
        notificationRepository.save(notif);
        return notif;
    }

    @Override
    public long getUnreadNumber(final String userId) {
        return 0;
    }

    @Override
    public void sendNewVoteNotification(String creatorId, Vote vote) {
        if (vote == null) {
            return;
        }

        var title = Optional.ofNullable(vote.getGroup()).map(Channel::getGroup).map(Group::getName).orElse("");
        var body = String.format("Nhóm có cuộc bình chọn mới \"%s\"", vote.getQuestion());
        var notification = createNewVoteNotification(title, body, vote);
        var receiverIds = notification.getReceivers().stream().map(nu -> nu.getUser().getId()).toList();
        var data = attachDataNotification(vote.getGroup().getId(), NEW_VOTE);

        firebaseService.sendNotificationMulticast(receiverIds, title, body, data);
    }

    @Override
    public Notification createNewVoteNotification(String title, String content, Vote vote) {
        var creator = vote.getCreator();
        Notification notification = Notification.builder()
                .title(title)
                .content(content)
                .type(NEW_VOTE)
                .sender(creator)
                .refId(vote.getId())
                .build();

        var receivers = Optional.ofNullable(vote.getGroup())
                .map(Channel::getUsers)
                .map(users -> users.stream()
                        .filter(user -> !user.getId().equals(creator.getId()))
                        .map(user -> NotificationUser.builder()
                                .notification(notification)
                                .user(user).build())
                        .toList())
                .orElse(null);

        notification.setReceivers(receivers);
        notificationRepository.save(notification);
        return notification;
    }

    @Override
    public void sendNewPinNotification(MessageDetailResponse message, User pinner) {
        if (message == null || pinner == null) {
            return;
        }
        String title;
        List<String> members;

        Optional<Group> groupWrapper = groupRepository.findById(message.getGroupId());
        if (groupWrapper.isEmpty()) {
            var channel = channelRepository.findById(message.getGroupId()).orElse(null);
            if (channel == null) {
                return;
            }

            Group parentGroup = channel.getGroup();
            title = PRIVATE_MESSAGE.equals(channel.getType())
                    ? parentGroup.getName() + "\n" + message.getSender().getName()
                    : channel.getName();
            members = channel.getUsers().stream()
                    .filter(id -> !id.equals(message.getSender().getId()))
                    .map(User::getId)
                    .distinct()
                    .toList();
        } else {
            Group group = groupWrapper.get();
            title = group.getName();
            members =
                    Stream.concat(group.getMentors().stream(), group.getMentees().stream())
                            .filter(id -> !id.equals(message.getSender().getId()))
                            .map(User::getId)
                            .distinct()
                            .toList();
        }

        if (members.isEmpty()) {
            return;
        }

        var data = attachDataNotification(message.getGroupId(), PIN_MESSAGE);
        var shortMessage = Jsoup.parse(message.getContent().substring(0, Math.min(message.getContent().length(), 25))).text();
        var content = pinner.getName() + " đã ghim tin nhắn \"" + shortMessage + "\"";
        firebaseService.sendNotificationMulticast(members, title, content, data);
    }

    @Override
    public void sendNewUnpinNotification(MessageDetailResponse message, User pinner) {
        if (message == null || pinner == null) {
            return;
        }

        String title;
        List<String> receiverIds;

        Optional<Group> groupWrapper = groupRepository.findById(message.getGroupId());
        if (groupWrapper.isEmpty()) {
            Channel channel = channelRepository.findById(message.getGroupId()).orElse(null);
            if (channel == null) {
                return;
            }

            Group parentGroup = channel.getGroup();
            title = PRIVATE_MESSAGE.equals(channel.getType())
                    ? parentGroup.getName() + "\n" + message.getSender().getName()
                    : channel.getName();
            receiverIds = channel.getUsers().stream()
                    .filter(id -> !id.equals(message.getSender().getId()))
                    .map(User::getId)
                    .distinct()
                    .toList();
        } else {
            Group group = groupWrapper.get();
            title = group.getName();
            receiverIds = Stream.concat(group.getMentors().stream(), group.getMentees().stream())
                    .map(User::getId)
                    .filter(id -> !id.equals(message.getSender().getId()))
                    .distinct()
                    .toList();
        }

        if (receiverIds.isEmpty()) {
            return;
        }

        Map<String, String> data = attachDataNotification(message.getGroupId(), UNPIN_MESSAGE);
        String shortMessage = "";
        if (message.getType() == Message.Type.TEXT) {
            shortMessage = Jsoup.parse(message.getContent().substring(0, Math.min(message.getContent().length(), 25)))
                    .text();
        } else {
            switch (message.getType()) {
                case IMAGE:
                    shortMessage = "ảnh.";
                    break;
                case FILE:
                    shortMessage = "tệp đính kèm.";
                    break;
                case VIDEO:
                    shortMessage = "đa phương tiện.";
                    break;
                case MEETING:
                    shortMessage = "lịch hẹn.";
                    break;
                case TASK:
                    shortMessage = "công việc.";
                    break;
                case VOTE:
                    shortMessage = "cuộc bình chọn.";
                    break;
                case SYSTEM:
                    shortMessage = "thông báo hệ thống.";
                    break;
                default:
                    break;
            }
        }
        String body = pinner.getName() + " đã bỏ ghim tin nhắn \"" + shortMessage + "\"";

        firebaseService.sendNotificationMulticast(receiverIds, title, body, data);
    }

    /**
     * @param title
     * @param content
     * @param senderId
     * @param group
     * @return
     */
    @Override
    public Notification createForwardNotification(final String title, final String content, final String senderId, final Group group) {
        return null;
    }

    /**
     * @param message Message to be sent
     * @param groupId Group identifier
     */
    @Override
    @SneakyThrows
    public void sendForwardNotification(MessageDetailResponse message, String groupId) {
        if (message == null || groupId == null) {
            return;
        }

        String title;
        List<String> members;

        Group group = groupRepository.findById(groupId).orElse(null);
        if (group == null) {
            Channel channel = channelRepository.findById(groupId).orElse(null);
            if (channel == null) {
                return;
            }

            Group parentGroup = channel.getGroup();
            title = PRIVATE_MESSAGE.equals(channel.getType()) ? parentGroup.getName() + "\n" + message.getSender().getName() : channel.getName();
            members = channel.getUsers().stream().map(User::getId).filter(id -> !id.equals(message.getSender().getId())).distinct().toList();
        } else {
            title = group.getName();
            members = group.getMembers().stream().map(User::getId).filter(id -> !id.equals(message.getSender().getId())).toList();
        }

        if (members.isEmpty()) {
            return;
        }

        if (message.getType() == Message.Type.FILE || message.getType() == Message.Type.IMAGE || message.getType() == Message.Type.VIDEO) {

            NotificationType type;
            StringBuilder notificationBody = new StringBuilder(message.getSender().getName() + " đã chuyển tiếp ");
            if (Message.Type.IMAGE.equals(message.getType())) {
                type = NEW_IMAGE_MESSAGE;
                notificationBody.append(message.getImages().size()).append(" ảnh.");
            } else if (Message.Type.FILE.equals(message.getType())) {
                type = NEW_FILE_MESSAGE;
                notificationBody.append(" một tệp đính kèm.");
            } else {
                type = SYSTEM;
                notificationBody.append(" một đa phương tiện.");
            }
            String body = notificationBody.toString();
            firebaseService.sendNotificationMulticast(members, title, body, attachDataNotification(message.getGroupId(), type));
            return;
        }

        Map<String, String> data = attachDataNotification(groupId, FORWARD);
        String shortMessage = Jsoup.parse(message.getContent().substring(0, Math.min(message.getContent().length(), 25))).text();
        String content = message.getSender().getName() + " đã chuyển tiếp tin nhắn \"" + shortMessage + "\"";
        firebaseService.sendNotificationMulticast(members, title, content, data);
    }
}