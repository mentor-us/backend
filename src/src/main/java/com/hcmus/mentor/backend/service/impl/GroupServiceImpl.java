package com.hcmus.mentor.backend.service.impl;

import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.exception.ForbiddenException;
import com.hcmus.mentor.backend.controller.payload.FileModel;
import com.hcmus.mentor.backend.controller.payload.response.ShortMediaMessage;
import com.hcmus.mentor.backend.controller.payload.response.groups.GroupDetailResponse;
import com.hcmus.mentor.backend.controller.payload.response.groups.GroupHomepageResponse;
import com.hcmus.mentor.backend.controller.payload.response.groups.GroupMembersResponse;
import com.hcmus.mentor.backend.controller.payload.response.groups.UpdateGroupAvatarResponse;
import com.hcmus.mentor.backend.controller.payload.response.messages.MessageDetailResponse;
import com.hcmus.mentor.backend.controller.payload.response.users.ProfileResponse;
import com.hcmus.mentor.backend.controller.payload.response.users.ShortProfile;
import com.hcmus.mentor.backend.domain.*;
import com.hcmus.mentor.backend.domain.constant.ChannelType;
import com.hcmus.mentor.backend.domain.constant.GroupCategoryStatus;
import com.hcmus.mentor.backend.domain.constant.GroupStatus;
import com.hcmus.mentor.backend.domain.constant.GroupUserRole;
import com.hcmus.mentor.backend.repository.*;
import com.hcmus.mentor.backend.service.*;
import com.hcmus.mentor.backend.service.dto.GroupServiceDto;
import com.hcmus.mentor.backend.service.fileupload.BlobStorage;
import com.hcmus.mentor.backend.util.DateUtils;
import com.hcmus.mentor.backend.util.MailUtils;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.math3.util.Pair;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.tika.Tika;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.hcmus.mentor.backend.controller.payload.returnCode.GroupReturnCode.*;
import static com.hcmus.mentor.backend.controller.payload.returnCode.InvalidPermissionCode.INVALID_PERMISSION;
import static com.hcmus.mentor.backend.domain.Message.Status.DELETED;
import static com.hcmus.mentor.backend.service.impl.AnalyticServiceImpl.getResourceResponseEntity;

@Service
@Transactional
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {
    private static final Integer SUCCESS = 200;
    private final GroupRepository groupRepository;
    private final GroupCategoryRepository groupCategoryRepository;
    private final UserRepository userRepository;
    private final PermissionService permissionService;
    private final MailUtils mailUtils;
    private final MessageRepository messageRepository;
    private final MessageService messageService;
    private final SocketIOService socketIOService;
    private final NotificationService notificationService;
    private final ChannelRepository channelRepository;
    private final BlobStorage blobStorage;
    private final GroupUserRepository groupUserRepository;
    private final ModelMapper modelMapper;


    /**
     * @param groups List of groups
     * @param userId current user id
     * @return List of GroupHomepageResponse to show in home page.
     */
    private List<GroupHomepageResponse> mappingGroupHomepageResponse(List<Group> groups, String userId) {
        return groups.stream()
                .map(group -> {
                    Optional<GroupUser> groupUser = group.getGroupUsers().stream()
                            .filter(gu -> gu.getUser().getId().equals(userId))
                            .findFirst();
                    String role = groupUser.filter(GroupUser::isMentor).map(gu -> GroupUserRole.MENTOR.toString()).orElse(GroupUserRole.MENTEE.toString());
                    boolean isPinned = groupUser.map(GroupUser::isPinned).orElse(false);
                    return new GroupHomepageResponse(group, role, isPinned);
                })
                .sorted(Comparator.comparing(GroupHomepageResponse::getUpdatedDate).reversed())
                .toList();
    }

    /**
     * @param userId User id
     * @return all group user is participant
     */
    @Override
    public List<Group> getAllActiveOwnGroups(String userId) {
        return groupRepository.findByIsMemberAndStatus(userId, GroupStatus.ACTIVE);
    }


    @Override
    @Transactional(readOnly = true)
    public Page<Group> findRecentGroupsOfUser(String userId, int page, int pageSize) {
        Pageable pageRequest = PageRequest.of(page, pageSize, Sort.by("updatedDate").descending());
        return groupRepository.findAllByIsMember(userId, pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Slice<Group> findMostRecentGroupsOfUser(String userId, int page, int pageSize) {
        Pageable pageRequest = PageRequest.of(page, pageSize, Sort.by("updatedDate").descending());
        return groupRepository.findByIsMemberAndStatus(userId, GroupStatus.ACTIVE, pageRequest);
    }

    private List<String> validateInvalidMails(List<String> mentors, List<String> mentees) {
        return Stream.concat(mentors.stream(), mentees.stream())
                .filter(email -> !MailUtils.isValidEmail(email))
                .toList();
    }

    private List<String> validateDuplicatedMails(List<String> mentors, List<String> mentees) {
        List<String> invalidEmails = new ArrayList<>();
        Set<String> emailSet = new HashSet<>();
        for (String mentor : mentors) {
            if (!emailSet.add(mentor)) {
                invalidEmails.add(mentor);
            }
        }
        for (String mentee : mentees) {
            if (!emailSet.add(mentee)) {
                invalidEmails.add(mentee);
            }
        }
        return invalidEmails;
    }

    private List<String> validateDomainMails(List<String> mentors, List<String> mentees) {
        return Stream.concat(mentors.stream(), mentees.stream())
                .filter(email -> !mailUtils.isValidDomain(email))
                .toList();
    }

    public GroupServiceDto validateListMentorsMentees(
            List<String> mentors, List<String> mentees) {
        List<String> invalidEmails = validateInvalidMails(mentors, mentees);
        if (!invalidEmails.isEmpty()) {
            return new GroupServiceDto(INVALID_EMAILS, "Invalid emails", invalidEmails);
        }
        invalidEmails = validateDomainMails(mentors, mentees);
        if (!invalidEmails.isEmpty()) {
            return new GroupServiceDto(INVALID_DOMAINS, "Invalid domains", invalidEmails);
        }
        invalidEmails = validateDuplicatedMails(mentors, mentees);
        if (!invalidEmails.isEmpty()) {
            return new GroupServiceDto(DUPLICATE_EMAIL, "Duplicate emails", invalidEmails);
        }
        return new GroupServiceDto(SUCCESS, "", null);
    }

    public GroupStatus getStatusFromTimeStartAndTimeEnd(LocalDateTime timeStart, LocalDateTime timeEnd) {
        var now = LocalDateTime.now(ZoneOffset.UTC);
        if (timeStart.isBefore(now) && timeEnd.isBefore(now)) {
            return GroupStatus.OUTDATED;
        }
        if (timeStart.isAfter(now) && timeEnd.isAfter(now)) {
            return GroupStatus.ACTIVE;
        }
        return GroupStatus.INACTIVE;
    }

    @Override
    public List<Group> validateTimeGroups(List<Group> groups) {
        for (Group group : groups) {
            if (group.getStatus() == GroupStatus.DISABLED || group.getStatus() == GroupStatus.DELETED) {
                continue;
            }

            group.setStatus(getStatusFromTimeStartAndTimeEnd(group.getTimeStart(), group.getTimeEnd()));
            groupRepository.save(group);
        }

        return groups;
    }

    private Pair<Long, List<Group>> getGroupsByConditions(
            String emailUser,
            String name,
            String mentorEmail,
            String menteeEmail,
            String groupCategory,
            Date timeStart1,
            Date timeEnd1,
            Date timeStart2,
            Date timeEnd2,
            String status,
            int page,
            int pageSize) {
        Specification<Group> spec = (Root<Group> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (name != null && !name.isEmpty()) {
                predicates.add(builder.like(builder.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }

            if (mentorEmail != null && !mentorEmail.isEmpty()) {
                predicates.add(builder.equal(root.join("mentors").get("email"), mentorEmail));
            }

            if (menteeEmail != null && !menteeEmail.isEmpty()) {
                predicates.add(builder.equal(root.join("mentees").get("email"), menteeEmail));
            }

            if (groupCategory != null && !groupCategory.isEmpty()) {
                predicates.add(builder.equal(root.get("groupCategory"), groupCategory));
            }

            if (status != null) {
                predicates.add(builder.equal(root.get("status"), status));
            }

            if (timeStart1 != null && timeEnd1 != null) {
                predicates.add(builder.between(root.get("timeStart"), timeStart1, timeEnd1));
            }

            if (timeStart2 != null && timeEnd2 != null) {
                predicates.add(builder.between(root.get("timeEnd"), timeStart2, timeEnd2));
            }

            if (emailUser != null && !emailUser.isEmpty() && !permissionService.isSuperAdmin(emailUser)) {
                String userId = getUserIdByEmail(emailUser);
                if (!StringUtils.isEmpty(userId)) {
                    predicates.add(builder.equal(root.get("creatorId"), userId));
                }
            }


            return builder.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(page, pageSize);
        Page<Group> groupPage = groupRepository.findAll(spec, pageable);

        List<Group> data = groupPage.getContent();
        long count = groupPage.getTotalElements();
        data = validateTimeGroups(data);
        return new Pair<>(count, data);
    }

    private String getUserIdByEmail(String emailUser) {
        Optional<User> userOptional = userRepository.findByEmail(emailUser);
        return userOptional.map(User::getId).orElse(null);
    }

    @Override
    public InputStream loadTemplate(String pathToTemplate) throws Exception {
        String[] groupCategoryNames = groupCategoryRepository
                .findAllByStatus(GroupCategoryStatus.ACTIVE)
                .stream()
                .map(GroupCategory::getName)
                .toList()
                .toArray(new String[0]);

        InputStream tempTemplateStream = getClass().getResourceAsStream(pathToTemplate);

        if (tempTemplateStream == null) {
            throw new DomainException("Template not found");
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            try (Workbook workbook = WorkbookFactory.create(tempTemplateStream)) {
                Sheet dataSheet = workbook.getSheet("Data");
                for (int i = 1; i <= dataSheet.getLastRowNum(); i++) {
                    Row row = dataSheet.getRow(i);
                    if (row != null) {
                        dataSheet.removeRow(row);
                    }
                }

                DataValidationHelper validationHelper = new XSSFDataValidationHelper((XSSFSheet) dataSheet);

                CellRangeAddressList addressList = new CellRangeAddressList(1, 10000, 1, 1);
                DataValidationConstraint constraintCategory = validationHelper.createExplicitListConstraint(groupCategoryNames);
                DataValidation dataValidationCategory = validationHelper.createValidation(constraintCategory, addressList);

                dataValidationCategory.setSuppressDropDownArrow(true);
                dataValidationCategory.setEmptyCellAllowed(false);
                dataValidationCategory.setErrorStyle(DataValidation.ErrorStyle.STOP);

                dataSheet.addValidationData(dataValidationCategory);

                workbook.write(outputStream);
            }

            return new ByteArrayInputStream(outputStream.toByteArray());
        }
    }

    private GroupServiceDto getGroupById(String emailUser, String groupId) {
        if (!permissionService.hasPermissionOnGroup(emailUser, groupId)) {
            return new GroupServiceDto(INVALID_PERMISSION, "Invalid permission", null);
        }
        Optional<Group> groupWrapper = groupRepository.findById(groupId);
        if (groupWrapper.isEmpty()) {
            return new GroupServiceDto(NOT_FOUND, "Group not found", null);
        }
        Group group = groupWrapper.get();
        if (group.getStatus() == GroupStatus.DELETED) {
            return new GroupServiceDto(NOT_FOUND, "Group has been deleted", null);
        }
        return new GroupServiceDto(SUCCESS, null, group);
    }

    @Override
    public GroupServiceDto deleteGroup(String emailUser, String groupId) {
        var groupServiceDto = getGroupById(emailUser, groupId);
        if (!groupServiceDto.getReturnCode().equals(SUCCESS)) {
            return groupServiceDto;
        }
        Group group = (Group) groupServiceDto.getData();

        if (Objects.equals(group.getDefaultChannel().getId(), groupId)) {
            return new GroupServiceDto(INVALID_PERMISSION, "Invalid permission", null);
        }

        group.setStatus(GroupStatus.DELETED);
        groupRepository.save(group);
        return new GroupServiceDto(SUCCESS, null, group);
    }

    @Override
    public List<GroupHomepageResponse> getUserPinnedGroups(String userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return new ArrayList<>();
        }
        List<Group> groups = groupRepository.findGroupsByMembersContaining(user.getId());
        return mappingGroupHomepageResponse(groups, userId);
    }

    @Override
    public boolean isGroupMember(String groupId, String userId) {
        var group = groupRepository.findById(groupId).orElse(null);

        if (group != null) {
            return group.isMember(userId);
        }

        var channelOpt = channelRepository.findById(groupId);
        if (channelOpt.isPresent()) {
            var channel = channelOpt.get();
            return channel.isMember(userId) || isGroupMember(channel.getGroup().getId(), userId);
        }

        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public Slice<GroupHomepageResponse> getHomePageRecentGroupsOfUser(String userId, int page, int pageSize) {
        Slice<Group> groups = findMostRecentGroupsOfUser(userId, page, pageSize);
        List<GroupHomepageResponse> responses = mappingGroupHomepageResponse(groups.getContent(), userId);
        return new SliceImpl<>(responses, PageRequest.of(page, pageSize), groups.hasNext());
    }

    @Override
    public GroupServiceDto deleteMultiple(String emailUser, List<String> ids) {
        if (!permissionService.isAdmin(emailUser)) {
            return new GroupServiceDto(INVALID_PERMISSION, "Invalid permission", null);
        }
        List<String> notFoundIds = new ArrayList<>();
        for (String id : ids) {
            Optional<Group> groupOptional = groupRepository.findById(id);
            if (groupOptional.isEmpty()) {
                notFoundIds.add(id);
            }
        }

        if (!notFoundIds.isEmpty()) {
            return new GroupServiceDto(NOT_FOUND, "Group not found", notFoundIds);
        }
        List<Group> groups = groupRepository.findByIdIn(ids);
        groups.forEach(group -> group.setStatus(GroupStatus.DELETED));
        groupRepository.saveAll(groups);
        return new GroupServiceDto(SUCCESS, null, groups);
    }

    @Override
    @Transactional(readOnly = true)
    public GroupServiceDto getGroupMembers(String groupId, String userId) {
        Optional<Channel> channelOptional = channelRepository.findById(groupId);

        Function<Channel, GroupServiceDto> getMembersFromChannel = channel -> {
            if (channel.getUsers().stream().noneMatch(user -> user.getId().equals(userId))) {
                return new GroupServiceDto(INVALID_PERMISSION, "Invalid permission", null);
            }

            // Get user in channel
            List<User> channelUsers = channel.getUsers();

            // Get mentors in channel
            Stream<GroupUser> mentorsStream = channel.getGroup().getGroupUsers().stream().filter(GroupUser::isMentor);
            List<GroupMembersResponse.GroupMember> mentors = mentorsStream.map(GroupUser::getUser).filter(channelUsers::contains)
                    .map(mentor -> GroupMembersResponse.GroupMember.from(ProfileResponse.from(mentor), GroupUserRole.MENTOR.name())).toList();

            // Get mentees in channel
            Stream<GroupUser> menteesStream = channel.getGroup().getGroupUsers().stream().filter(gu -> !gu.isMentor());
            List<GroupMembersResponse.GroupMember> mentees = menteesStream.filter(mentee -> channelUsers.contains(mentee.getUser()))
                    .map(mentee -> {
                        var profile = ProfileResponse.from(mentee.getUser());
                        return GroupMembersResponse.GroupMember.from(profile, GroupUserRole.MENTEE.name(), mentee.isMarked());
                    }).toList();

            GroupMembersResponse response = GroupMembersResponse.builder().mentors(mentors).mentees(mentees).build();
            return new GroupServiceDto(SUCCESS, null, response);
        };

        Supplier<GroupServiceDto> getMembersFromGroup = () -> {
            Optional<Group> groupOptional = groupRepository.findById(groupId);

            return groupOptional.map(group -> {
                if (group.getGroupUsers().stream().noneMatch(gu -> gu.getUser().getId().equals(userId))) {
                    return new GroupServiceDto(INVALID_PERMISSION, "Invalid permission", null);
                }

                List<GroupMembersResponse.GroupMember> mentors = group.getGroupUsers().stream().filter(GroupUser::isMentor)
                        .map(GroupUser::getUser)
                        .map(ProfileResponse::from)
                        .map(profile -> GroupMembersResponse.GroupMember.from(profile, GroupUserRole.MENTOR.name()))
                        .toList();
                List<GroupMembersResponse.GroupMember> mentees = new ArrayList<>();
                group.getGroupUsers().stream().filter(gu -> !gu.isMentor()).forEach(gu -> {
                    var user = gu.getUser();
                    var profile = ProfileResponse.from(user);
                    mentees.add(GroupMembersResponse.GroupMember.from(profile, GroupUserRole.MENTEE.name(), gu.isMarked()));
                });

                GroupMembersResponse response = GroupMembersResponse.builder().mentors(mentors).mentees(mentees).build();
                return new GroupServiceDto(SUCCESS, null, response);
            }).orElse(new GroupServiceDto(NOT_FOUND, "Group not found", null));
        };

        return channelOptional.map(getMembersFromChannel).orElseGet(getMembersFromGroup);
    }

    @Override
    public void pinGroup(String userId, String groupId) {
        var group = groupRepository.findById(groupId).orElseThrow(() -> new DomainException("Group not found"));
        var groupUser = group.getGroupUsers().stream().filter(gu -> gu.getUser().getId().equals(userId)).findFirst().orElseThrow(() -> new DomainException("User not found"));
        groupUser.setPinned(true);
        groupUserRepository.save(groupUser);
    }

    @Override
    public void unpinGroup(String userId, String groupId) {
        var group = groupRepository.findById(groupId).orElseThrow(() -> new DomainException("Group not found"));
        var groupUser = group.getGroupUsers().stream().filter(gu -> gu.getUser().getId().equals(userId)).findFirst().orElseThrow(() -> new DomainException("User not found"));
        groupUser.setPinned(false);
        groupUserRepository.save(groupUser);
    }

    @Override
    public GroupServiceDto getGroupDetail(String userId, String groupId) {
        var channel = channelRepository.findById(groupId).orElse(null);
        if (channel == null) {
            return new GroupServiceDto(NOT_FOUND, "Group not found", null);
        }

        var group = channel.getGroup();

        GroupDetailResponse channelDetail = fulfillChannelDetail(userId, channel, group);
        if (channelDetail == null) {
            return new GroupServiceDto(NOT_FOUND, "Group not found", null);
        }

        channelDetail.setPinnedMessages(messageService.mappingToMessageDetailResponse(messageRepository.findByIdIn(channelDetail.getPinnedMessageIds()), userId));

        return new GroupServiceDto(SUCCESS, null, channelDetail);
    }

    private GroupDetailResponse fulfillChannelDetail(String userId, Channel channel, Group parentGroup) {

        String channelName = channel.getName();
        String imageUrl = null;

        if (ChannelType.PRIVATE_MESSAGE.equals(channel.getType())) {
            ShortProfile penpal = channel.getUsers().stream()
                    .filter(u -> Objects.equals(u.getId(), userId))
                    .map(u -> modelMapper.map(u, ShortProfile.class))
                    .findFirst()
                    .orElse(null);
            if (penpal == null) {
                return null;
            }
            channelName = penpal.getName();
            imageUrl = penpal.getImageUrl();
        }

        GroupDetailResponse response = GroupDetailResponse.builder()
                .id(channel.getId())
                .name(channelName)
                .description(channel.getDescription())
                .pinnedMessageIds(channel.getMessagesPinned().stream().map(Message::getId).toList())
                .imageUrl(imageUrl)
                .role(parentGroup.isMentor(userId) ? GroupUserRole.MENTOR : GroupUserRole.MENTEE)
                .parentId(parentGroup.getId())
                .totalMember(channel.getUsers().size())
                .type(channel.getType())
                .mentors(parentGroup.getMentors().stream().map(User::getId).toList())
                .mentors(parentGroup.getMentees().stream().map(User::getId).toList())
                .build();

        GroupCategory groupCategory = parentGroup.getGroupCategory();
        if (groupCategory != null) {
            response.setPermissions(groupCategory.getPermissions());
            response.setGroupCategory(groupCategory.getName());
        }

//        List<MessageResponse> messages = new ArrayList<>();
//
//        if (response.getPinnedMessageIds() != null && !response.getPinnedMessageIds().isEmpty()) {
//            messages = response.getPinnedMessageIds().stream()
//                    .map(messageRepository::findById)
//                    .filter(Optional::isPresent)
//                    .map(Optional::get)
//                    .filter(message -> !message.isDeleted())
//                    .map(message -> {
//                        User user = message.getSender();
//                        return MessageResponse.from(message, ProfileResponse.from(user));
//                    })
//                    .toList();
//        }
        response.setPinnedMessages(messageService.mappingToMessageDetailResponse(
                messageRepository.findByIdIn(response.getPinnedMessageIds()), userId));
        response.setTotalMember(channel.getUsers().size());
        return response;
    }

    @Override
    public void pingGroup(String groupId) {
        var groupOpt = groupRepository.findById(groupId);
        if (groupOpt.isPresent()) {
            var group = groupOpt.get();
            group.ping();
            groupRepository.save(group);
        }

        var channelOpt = channelRepository.findById(groupId);
        if (channelOpt.isPresent()) {
            var channel = channelOpt.get();
            channel.ping();
            channelRepository.save(channel);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public GroupServiceDto getGroupMedia(String userId, String groupId) {
        Optional<Group> groupWrapper = groupRepository.findById(groupId);
        List<String> senderIds = new ArrayList<>();
        if (groupWrapper.isEmpty()) {
            Optional<Channel> channelWrapper = channelRepository.findById(groupId);
            if (channelWrapper.isEmpty()) {
                return new GroupServiceDto(NOT_FOUND, "Group not found", null);
            }

            Channel channel = channelWrapper.get();
            if (!channel.isMember(userId) && !permissionService.isMemberInGroup(userId, channel.getGroup().getId())) {
                return new GroupServiceDto(INVALID_PERMISSION, "Invalid permission", null);
            }

            senderIds = channel.getUsers().stream().map(User::getId).toList();
        }

        if (groupWrapper.isPresent()) {
            Group group = groupWrapper.get();
            if (!group.isMember(userId)) {
                return new GroupServiceDto(INVALID_PERMISSION, "Invalid permission", null);
            }

            senderIds = group.getGroupUsers().stream().map(gu -> gu.getUser().getId()).toList();
        }

        Map<String, ProfileResponse> senders = userRepository.findAllByIdIn(senderIds).stream()
                .collect(Collectors.toMap(User::getId, ProfileResponse::from));

        List<Message> mediaMessages = messageRepository.findByChannelIdAndTypeInAndStatusInOrderByCreatedDateDesc(
                groupId,
                Arrays.asList(Message.Type.IMAGE, Message.Type.FILE),
                Arrays.asList(Message.Status.SENT, Message.Status.EDITED));

        List<ShortMediaMessage> media = new ArrayList<>();
        mediaMessages.forEach(message -> {
            ProfileResponse sender = senders.getOrDefault(message.getSender(), null);

            if (Message.Type.IMAGE.equals(message.getType())) {
                List<ShortMediaMessage> images = message.getImages().stream()
                        .map(url -> ShortMediaMessage.builder()
                                .id(message.getId())
                                .sender(sender)
                                .imageUrl(url)
                                .type(message.getType())
                                .createdDate(message.getCreatedDate())
                                .build())
                        .toList();
                media.addAll(images);
            }

            if (Message.Type.FILE.equals(message.getType())) {
                ShortMediaMessage file =
                        ShortMediaMessage.builder()
                                .id(message.getId())
                                .sender(sender)
                                .file(new FileModel(message.getFile()))
                                .type(message.getType())
                                .createdDate(message.getCreatedDate())
                                .build();
                media.add(file);
            }
        });
        return new GroupServiceDto(SUCCESS, null, media);
    }

    @Override
    @SneakyThrows
    public UpdateGroupAvatarResponse updateAvatar(String userId, String groupId, MultipartFile file) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new DomainException("Group not found"));

        if (!group.isMember(userId)) {
            throw new ForbiddenException("Invalid permission");
        }

        String key = blobStorage.generateBlobKey(new Tika().detect(file.getBytes()));
        try {
            blobStorage.post(file, key);
        } catch (Exception e) {
            throw new DomainException("Upload image failed");
        }

        group.setImageUrl(key);
        groupRepository.save(group);
        return new UpdateGroupAvatarResponse(key);
    }

    private List<Group> getGroupsForAdmin(String emailUser) {
        List<Group> groups;
        boolean isSuperAdmin = permissionService.isSuperAdmin(emailUser);
        if (isSuperAdmin) {
            groups = groupRepository.findAllByOrderByCreatedDate();
        } else {
            var userOpt = userRepository.findByEmail(emailUser).orElseThrow(() -> new DomainException("User not found"));
            String creatorId = userOpt.getId();
            groups = groupRepository.findAllByCreatorIdOrderByCreatedDate(creatorId);
        }

        return validateTimeGroups(groups);
    }

    private List<List<String>> generateExportData(List<Group> groups) {
        List<List<String>> data = new ArrayList<>();
        int index = 1;
        for (Group group : groups) {
            List<String> row = new ArrayList<>();

            Map<GroupStatus, String> statusMap = Group.getStatusMap();
            String status = statusMap.get(group.getStatus());
            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String dateStart = group.getTimeStart().format(dateFormat);
            String dateEnd = dateFormat.format(group.getTimeEnd());
            String duration = DateUtils.parseDuration(group.getDuration());

            row.add(Integer.toString(index));
            row.add(group.getName());
            row.add(group.getGroupCategory().getName());
            row.add(status);
            row.add(dateStart);
            row.add(dateEnd);
            row.add(duration);

            data.add(row);
            index++;
        }

        return data;
    }

    private ResponseEntity<Resource> generateExportTable(
            List<Group> groups, List<String> remainColumns) throws IOException {
        List<List<String>> data = generateExportData(groups);
        List<String> headers = Arrays.asList(
                "STT",
                "Tên nhóm",
                "Loại nhóm",
                "Trạng thái",
                "Thời gian bắt đầu",
                "Thời gian kết thúc",
                "Thời hạn");
        String fileName = "output.xlsx";
        Map<String, Integer> indexMap = new HashMap<>();
        indexMap.put("name", 1);
        indexMap.put("groupCategory", 2);
        indexMap.put("status", 3);
        indexMap.put("timeStart", 4);
        indexMap.put("timeEnd", 5);
        indexMap.put("duration", 6);
        return getResourceResponseEntity(remainColumns, data, headers, fileName, indexMap);
    }

    @Override
    public ResponseEntity<Resource> generateExportTable(String emailUser, List<String> remainColumns)
            throws IOException {
        List<Group> groups = getGroupsForAdmin(emailUser);
        return generateExportTable(groups, remainColumns);
    }

    @Override
    public ResponseEntity<Resource> generateExportTableBySearchConditions(
            String emailUser,
            String name,
            String mentorEmail,
            String menteeEmail,
            String groupCategory,
            Date timeStart1,
            Date timeEnd1,
            Date timeStart2,
            Date timeEnd2,
            String status,
            List<String> remainColumns)
            throws IOException {

        Pair<Long, List<Group>> groups = getGroupsByConditions(
                emailUser,
                name,
                mentorEmail,
                menteeEmail,
                groupCategory,
                timeStart1,
                timeEnd1,
                timeStart2,
                timeEnd2,
                status,
                0,
                Integer.MAX_VALUE);
        return generateExportTable(groups.getValue(), remainColumns);
    }

    private List<List<String>> generateExportDataMembers(String groupId, GroupUserRole groupUserRole) {
        Optional<Group> groupOptional = groupRepository.findById(groupId);
        List<String> memberIds = new ArrayList<>();
        if (groupOptional.isPresent()) {
            if (groupUserRole.equals(GroupUserRole.MENTOR)) {
                memberIds = groupOptional.get().getGroupUsers().stream().filter(GroupUser::isMentor).map(gu -> gu.getUser().getId()).toList();
            } else if (groupUserRole.equals(GroupUserRole.MENTEE)) {
                memberIds = groupOptional.get().getGroupUsers().stream().filter(gu -> !gu.isMentor()).map(gu -> gu.getUser().getId()).toList();
            }
        }
        List<List<String>> data = new ArrayList<>();
        int index = 1;
        for (String memberId : memberIds) {
            List<String> row = new ArrayList<>();
            User user = userRepository.findById(memberId).orElse(null);
            if (user != null) {
                row.add(Integer.toString(index));
                row.add(user.getEmail());
                row.add(user.getName());
            }
            data.add(row);
            index++;
        }

        return data;
    }

    @Override
    public ResponseEntity<Resource> generateExportTableMembers(
            String emailUser, List<String> remainColumns, String groupId, GroupUserRole groupUserRole)
            throws IOException {
        List<List<String>> data = generateExportDataMembers(groupId, groupUserRole);
        List<String> headers = Arrays.asList("STT", "Email", "Họ tên");
        String fileName = "output.xlsx";
        Map<String, Integer> indexMap = new HashMap<>();
        indexMap.put("email", 1);
        indexMap.put("name", 2);
        return getResourceResponseEntity(remainColumns, data, headers, fileName, indexMap);
    }

    @Override
    public void pinChannelMessage(String userId, String channelId, String messageId) {
        Channel channel = channelRepository.findById(channelId).orElseThrow(() -> new DomainException("Channel not found"));
        if (channel.getMessagesPinned().size() >= 5) {
            throw new DomainException("Maximum pinned messages");
        }

        Message message = messageRepository.findByIdAndStatusNot(messageId, DELETED).orElseThrow(() -> new DomainException("Message not found"));
        if (channel.getMessagesPinned().stream().anyMatch(m -> Objects.equals(m.getId(), messageId))) {
            return;
        }
        channel.getMessagesPinned().add(message);

        channel.ping();
        channelRepository.save(channel);

        MessageDetailResponse messageDetail = messageService.mappingToMessageDetailResponse(message, null);
        socketIOService.sendNewPinMessage(messageDetail);
        notificationService.sendForTogglePin(message, userRepository.findById(userId).orElseThrow(() -> new DomainException("User not found")), true);
    }

    @Override
    public void unpinChannelMessage(String userId, String channelId, String messageId) {
        Channel channel = channelRepository.findById(channelId).orElseThrow(() -> new DomainException("Channel not found"));
        Message message = messageRepository.findById(messageId).orElseThrow(() -> new DomainException("Message not found"));
        if (channel.getMessagesPinned().stream().noneMatch(m -> Objects.equals(m.getId(), messageId))) {
            return;
        }

        channel.getMessagesPinned().remove(message);
        channel.ping();

        channelRepository.save(channel);
        socketIOService.sendNewUnpinMessage(channelId, messageId);

        User pinnerMessage = userRepository.findById(userId).orElseThrow(() -> new DomainException("Pinner not found"));
        notificationService.sendForTogglePin(message, pinnerMessage, false);
    }

//    @Override
//    public void updateLastMessageId(String groupId, String messageId) {
//        Optional<Group> groupWrapper = groupRepository.findById(groupId);
//        if (groupWrapper.isEmpty()) {
//            return;
//        }
//
//        Group group = groupWrapper.get();
//        group.setLastMessage(messageRepository.findById(messageId).orElse(null));
//        groupRepository.save(group);
//    }

//    @Override
//    public GroupDetailResponse getGroupWorkspace(CustomerUserDetails user, String groupId) {
//        if (!permissionService.isUserIdInGroup(user.getId(), groupId)) {
//            return null;
//        }
//
//        List<GroupDetailResponse> groupDetailResponses = groupRepository.getGroupDetail(groupId);
//        if (groupDetailResponses.isEmpty()) {
//            return null;
//        }
//
//        Group group = groupRepository.findById(groupId).orElse(null);
//        if (group == null) {
//            return null;
//        }
//
//        GroupDetailResponse detail = fulfillGroupDetail(user.getId(), groupDetailResponses.getFirst());
//
//        List<Channel> channelIds = group.getChannels();
//        List<GroupDetailResponse.GroupChannel> channels = channelIds.stream()
//                .filter(channel -> channel.isMember(user.getId()) && channel.getType() ==ChannelType.PUBLIC)
//                .map(GroupDetailResponse.GroupChannel::from)
//                .sorted(Comparator.comparing(GroupDetailResponse.GroupChannel::getUpdatedDate).reversed())
//                .toList();
//        channels.forEach(ch -> {
//            if (ch.getNewMessageId() != null) {
//                ch.setNewMessage(messageService.getMessageContentById(ch.getNewMessageId()));
//            } else {
//                ch.setNewMessage(null);
//            }
//        });
//        detail.setChannels(channels);
//
//        List<GroupDetailResponse.GroupChannel> privates = channelIds.stream()
//                .filter(channel -> channel.isMember(user.getId()) && channel.getType() ==ChannelType.PUBLIC)
//                .map(channel -> {
//                    var userTemp = channel.getUsers().stream()
//                            .filter(u -> !u.getId().equals(user.getId()))
//                            .findFirst()
//                            .orElse(null);
//                    if (userTemp == null) {
//                        return null;
//                    }
//                    ShortProfile penpal = new ShortProfile(userTemp);
//                    channel.setName(penpal.getName());
//                    channel.setImageUrl(penpal.getImageUrl());
//
//                    List<String> markedMentees = group.getGroupUsers().stream()
//                            .filter(GroupUser::isMarked)
//                            .map(gu -> gu.getUser().getId())
//                            .toList();
//                    return GroupDetailResponse.GroupChannel.from(channel, markedMentees.contains(penpal.getId()));
//                })
//                .filter(Objects::nonNull)
//                .sorted(Comparator.comparing(GroupDetailResponse.GroupChannel::getUpdatedDate).reversed())
//                .sorted(Comparator.comparing(GroupDetailResponse.GroupChannel::getMarked).reversed())
//                .toList();
//        privates.forEach(ch -> {
//            if (ch.getNewMessageId() != null) {
//                ch.setNewMessage(messageService.getMessageContentById(ch.getNewMessageId()));
//            } else {
//                ch.setNewMessage(null);
//            }
//        });
//
//        detail.setPrivates(privates);
//
//        return detail;
//    }

//    @Override
//    public void markMentee(CustomerUserDetails user, String groupId, String menteeId) {
//        Group group = groupRepository.findById(groupId).orElseThrow(() -> new DomainException("Group not found"));
//        if (!group.isMentor(user.getId())) {
//            throw new ForbiddenException("You are not mentor");
//        }
//
//        var groupUser = group.getGroupUsers().stream().filter(gu -> gu.getUser().getId().equals(menteeId)).findFirst().orElse(null);
//        if (groupUser == null) {
//            throw new DomainException("Mentee not found");
//        }
//        groupUser.setMarked(true);
//        groupUserRepository.save(groupUser);
//    }
//
//    @Override
//    public void unmarkMentee(CustomerUserDetails user, String groupId, String menteeId) {
//        Group group = groupRepository.findById(groupId).orElseThrow(() -> new DomainException("Group not found"));
//        if (!group.isMentor(user.getId())) {
//            throw new ForbiddenException("You are not mentor");
//        }
//
//        group.unmarkMentee(menteeId);
//        groupRepository.save(group);
//    }

//    /**
//     * @param user UserPrincipal
//     * @param name Optional name of groups.
//     * @return List<GroupForwardResponse>
//     */
//    @Override
//    public List<ChannelForwardResponse> getGroupForwards(CustomerUserDetails user, Optional<String> name) {
//        List<Group> groups = groupRepository.findByMenteesContainsOrMentorsContainsAndStatusIs(user.getId(), user.getId(), GroupStatus.ACTIVE);
//
//        var listChannelIds = groups.stream().map(g -> g.getChannels().stream().map(Channel::getId).toList()).toList();
//        List<String> lstChannelIds = Stream.concat(listChannelIds.stream().flatMap(Collection::stream), groups.stream().map(g -> g.getDefaultChannel().getId())).toList();
//
//
//        return channelRepository.getListChannelForward(lstChannelIds, user.getId(), ChannelStatus.ACTIVE);
//    }
}