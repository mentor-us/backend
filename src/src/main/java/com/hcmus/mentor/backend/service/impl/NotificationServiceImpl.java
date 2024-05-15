package com.hcmus.mentor.backend.service.impl;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.payload.request.AddNotificationRequest;
import com.hcmus.mentor.backend.controller.payload.request.RescheduleMeetingRequest;
import com.hcmus.mentor.backend.controller.payload.request.SubscribeNotificationRequest;
import com.hcmus.mentor.backend.controller.payload.response.NotificationResponse;
import com.hcmus.mentor.backend.controller.payload.response.messages.MessageDetailResponse;
import com.hcmus.mentor.backend.controller.payload.response.messages.ReactMessageResponse;
import com.hcmus.mentor.backend.domain.*;
import com.hcmus.mentor.backend.domain.constant.NotificationType;
import com.hcmus.mentor.backend.repository.*;
import com.hcmus.mentor.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.modelmapper.ModelMapper;
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
    private final FirebaseMessagingServiceImpl firebaseMessagingManager;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final NotificationUserRepository notificationUserRepository;
    private final ModelMapper modelMapper;

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

//        var membersIds = channelTemp.getUsers().stream()
//                .filter(u -> !u.getId().equals(message.getSender().getId()))
//                .distinct()
//                .toList();
//        var channel = channelRepository.findById(message.getGroupId()).orElse(null);
//        if (channel == null) {
//            return;
//        }

//        var members = channel.getUsers().stream()
//                .filter(user -> !id.equals(message.getSender().getId()))
//                .distinct()
//                .toList();
        String title;
        List<String> members = new ArrayList<>();

        Optional<Group> groupWrapper = groupRepository.findById(message.getGroupId());
        if (groupWrapper.isEmpty()) {
            Channel channel = channelRepository.findById(message.getGroupId()).orElse(null);
            if (channel == null) {
                return;
            }

            title = String.format("%s%n%s", channel.getName(), message.getSender().getName());
        } else {
//            title = channel.getName();
            Group group = groupWrapper.get();
            title = group.getName();
            members = Stream.concat(group.getMentors().stream(), group.getMentees().stream())
                    .filter(id -> !id.equals(message.getSender().getId()))
                    .map(User::getId)
                    .distinct()
                    .toList();
        }

        if (members.isEmpty()) {
            return;
        }

        Map<String, String> data = attachDataNotification(message.getGroupId(), NEW_MESSAGE);
        String senderName = "";
        if (message.getSender() != null) {
            senderName = message.getSender().getName();
            data.put("sender", senderName);
            String imageUrl = message.getSender().getImageUrl() == null
                    || ("https://graph.microsoft.com/v1.0/me/photo/$value")
                    .equals(message.getSender().getImageUrl())
                    ? ""
                    : message.getSender().getImageUrl();
            data.put("imageUrl", imageUrl);
        }

        String body = senderName + ": " + Jsoup.parse(message.getContent()).text();
        try {
            firebaseMessagingManager.sendGroupNotification(members, title, body, data);
        } catch (FirebaseMessagingException e) {
            logger.error("[!]Error sending new message notification", e);
        }
    }

    private Map<String, String> attachDataNotification(String groupId, NotificationType type) {
        Map<String, String> data = new HashMap<>();
        data.put("type", type.name());
        data.put("screen", "chat");
        data.put("groupId", groupId);
        return data;
    }

    @Override
//    @Async
    public void sendNewTaskNotification(MessageDetailResponse message, Task task) {
        if (message.getTask() == null || task == null) {
            logger.warn("[!] Task message #{}, NULL cannot send notifications", message.getId());
            return;
        }

        Group group = task.getGroup().getGroup();
        if (group == null) {
            return;
        }

        String title = group.getName();
        String content = "Nhóm có công việc mới \"" + task.getTitle() + "\"";
        Notification notif = createNewTaskNotification(title, content, message.getSender().getId(), task);
        try {
            firebaseMessagingManager.sendGroupNotification(
                    notif.getReceivers().stream().map(n -> n.getUser().getId()).toList(),
                    title,
                    content,
                    attachDataNotification(message.getGroupId(), NEW_TASK));
        } catch (FirebaseMessagingException e) {
            logger.error("[!]Error sending new task notification", e);
        }
    }

    @Override
    public Notification createNewTaskNotification(String title, String content, String senderId, Task task) {
        var assignees = task.getAssignees();
        var assigner = task.getAssigner();

        Notification inAppNotification = Notification.builder()
                .title(title)
                .content(content)
                .type(NEW_TASK)
                .sender(assigner)
                .refId(task.getId())
                .build();

        List<NotificationUser> receivers = Stream.concat(assignees.stream().map(Assignee::getUser), Stream.of(assigner))
                .distinct()
                .map(u -> NotificationUser.builder().notification(inAppNotification).user(u).build())
                .toList();

        inAppNotification.setReceivers(receivers);
        notificationRepository.save(inAppNotification);

        return inAppNotification;
    }

    @Override
//    @Async
    public void sendNewMeetingNotification(Meeting meeting) {
        if (meeting == null) {
            logger.warn("[!] Meeting message #{}, NULL cannot send notifications", meeting.getId());
            return;
        }
        Group group = meeting.getGroup().getGroup();
        String title = group == null ? "" : group.getName();
        String content = "Nhóm có lịch hẹn mới \"" + meeting.getTitle() + "\"";
        Notification notif = createNewMeetingNotification(title, content, meeting.getOrganizer().getId(), meeting);
        try {
            firebaseMessagingManager.sendGroupNotification(
                    notif.getReceivers().stream().map(n -> n.getUser().getId()).toList(),
                    title,
                    content,
                    attachDataNotification(meeting.getGroup().getId(), NEW_MEETING));
        } catch (FirebaseMessagingException e) {
            logger.error("[!]Error sending new task notification", e);
        }
    }

    @Override
    public Notification createNewMeetingNotification(String title, String content, String senderId, Meeting meeting) {
        Notification notification = notificationRepository.save(Notification.builder()
                .title(title)
                .content(content)
                .type(NEW_MEETING)
                .sender(userRepository.findById(senderId).orElse(null))
                .refId(meeting.getId())
                .build());

        return notification;
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

        String body = notificationBody.toString();
        try {
            firebaseMessagingManager.sendGroupNotification(members, title, body, attachDataNotification(message.getGroupId(), type));
        } catch (FirebaseMessagingException e) {
            logger.error("[!]Error sending media message notification", e);
        }
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

        Map<String, String> data = attachDataNotification(group.getId(), NEW_REACTION);
        com.google.firebase.messaging.Notification notification = com.google.firebase.messaging.Notification.builder()
                .setTitle(group.getName())
                .setBody(reaction.getName() + " đã thể hiện cảm xúc tin nhắn của bạn.")
                .build();
        try {
            NotificationSubscriber subscriber = notificationSubscriberRepository.findByUserId(message.getSender().getId()).orElse(null);
            if (subscriber == null) {
                return;
            }
            firebaseMessagingManager.sendNotification(subscriber.getToken(), notification, data);
        } catch (FirebaseMessagingException e) {
            logger.error("[!] Error sending new react notification", e);
        }
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

        String title = group.getName();
        String content = "Lịch hẹn: \"" + meeting.getTitle() + "\" đã được dời thời gian.";
        Notification notif = createRescheduleMeetingNotification(title, content, modifierId, group, meeting);
        try {
            firebaseMessagingManager.sendGroupNotification(
                    notif.getReceivers().stream().map(nu -> nu.getUser().getId()).toList(),
                    title,
                    content,
                    attachDataNotification(meeting.getGroup().getGroup().getId(), RESCHEDULE_MEETING));
        } catch (FirebaseMessagingException e) {
            logger.error("[!] Error sending reschedule meeting notification", e);
        }
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

    /**
     * @param userId
     * @return
     */
    @Override
    public long getUnreadNumber(final String userId) {
        return 0;
    }

    @Override
    public void sendNewVoteNotification(String creatorId, Vote vote) {
        if (vote == null) {
            return;
        }

        Group group = vote.getGroup().getGroup();
        String title = group.getName();
        String content = "Nhóm có cuộc bình chọn mới \"" + vote.getQuestion() + "\"";
        Notification notif = createNewVoteNotification(title, content, vote.getCreator(), group, vote);
        try {
            firebaseMessagingManager.sendGroupNotification(
                    notif.getReceivers().stream().map(nu -> nu.getUser().getId()).toList(),
                    title,
                    content,
                    attachDataNotification(vote.getGroup().getId(), NEW_VOTE));
        } catch (FirebaseMessagingException e) {
            logger.error("[!]Error sending new vote notification", e);
        }
    }

    @Override
    public Notification createNewVoteNotification(String title, String content, User sender, Group group, Vote vote) {
        Notification notification = Notification.builder()
                .title(title)
                .content(content)
                .type(NEW_VOTE)
                .sender(sender)
                .refId(vote.getId())
                .build();

        var receiverIds = group.getGroupUsers().stream()
                .map(gu -> NotificationUser.builder().notification(notification).user(gu.getUser()).build())
                .toList();
        
        notification.setReceivers(receiverIds);
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

        Map<String, String> data = attachDataNotification(message.getGroupId(), PIN_MESSAGE);
        String shortMessage =
                Jsoup.parse(message.getContent().substring(0, Math.min(message.getContent().length(), 25)))
                        .text();
        String content = pinner.getName() + " đã ghim tin nhắn \"" + shortMessage + "\"";
        try {
            firebaseMessagingManager.sendGroupNotification(members, title, content, data);
        } catch (FirebaseMessagingException e) {
            logger.error("[!]Error sending pin message notification", e);
        }
    }

    @Override
    public void sendNewUnpinNotification(MessageDetailResponse message, User pinner) {
        if (message == null || pinner == null) {
            return;
        }

        String title;
        List<String> members;

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
            members = channel.getUsers().stream()
                    .filter(id -> !id.equals(message.getSender().getId()))
                    .map(User::getId)
                    .distinct()
                    .toList();
        } else {
            Group group = groupWrapper.get();
            title = group.getName();
            members = Stream.concat(group.getMentors().stream(), group.getMentees().stream())
                    .map(User::getId)
                    .filter(id -> !id.equals(message.getSender().getId()))
                    .distinct()
                    .toList();
        }

        if (members.isEmpty()) {
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
        String content = pinner.getName() + " đã bỏ ghim tin nhắn \"" + shortMessage + "\"";
        try {
            firebaseMessagingManager.sendGroupNotification(members, title, content, data);
        } catch (FirebaseMessagingException e) {
            logger.error("[!] Error sending unpin message notification", e);
        }
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
            firebaseMessagingManager.sendGroupNotification(members, title, body, attachDataNotification(message.getGroupId(), type));
            return;
        }

        Map<String, String> data = attachDataNotification(groupId, FORWARD);
        String shortMessage = Jsoup.parse(message.getContent().substring(0, Math.min(message.getContent().length(), 25))).text();
        String content = message.getSender().getName() + " đã chuyển tiếp tin nhắn \"" + shortMessage + "\"";
        firebaseMessagingManager.sendGroupNotification(members, title, content, data);
    }
}