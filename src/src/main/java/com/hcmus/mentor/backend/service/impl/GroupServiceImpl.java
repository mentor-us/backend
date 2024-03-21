package com.hcmus.mentor.backend.service.impl;

import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.exception.ForbiddenException;
import com.hcmus.mentor.backend.controller.payload.request.groups.AddMenteesRequest;
import com.hcmus.mentor.backend.controller.payload.request.groups.AddMentorsRequest;
import com.hcmus.mentor.backend.controller.payload.request.groups.CreateGroupCommand;
import com.hcmus.mentor.backend.controller.payload.request.groups.UpdateGroupRequest;
import com.hcmus.mentor.backend.controller.payload.response.ShortMediaMessage;
import com.hcmus.mentor.backend.controller.payload.response.channel.ChannelForwardResponse;
import com.hcmus.mentor.backend.controller.payload.response.groups.GroupDetailResponse;
import com.hcmus.mentor.backend.controller.payload.response.groups.GroupHomepageResponse;
import com.hcmus.mentor.backend.controller.payload.response.groups.GroupMembersResponse;
import com.hcmus.mentor.backend.controller.payload.response.groups.UpdateGroupAvatarResponse;
import com.hcmus.mentor.backend.controller.payload.response.messages.MessageDetailResponse;
import com.hcmus.mentor.backend.controller.payload.response.messages.MessageResponse;
import com.hcmus.mentor.backend.controller.payload.response.users.ProfileResponse;
import com.hcmus.mentor.backend.controller.payload.response.users.ShortProfile;
import com.hcmus.mentor.backend.domain.*;
import com.hcmus.mentor.backend.domain.constant.ChannelStatus;
import com.hcmus.mentor.backend.domain.constant.ChannelType;
import com.hcmus.mentor.backend.domain.constant.GroupCategoryStatus;
import com.hcmus.mentor.backend.domain.constant.GroupStatus;
import com.hcmus.mentor.backend.repository.*;
import com.hcmus.mentor.backend.security.principal.userdetails.CustomerUserDetails;
import com.hcmus.mentor.backend.service.*;
import com.hcmus.mentor.backend.service.dto.GroupServiceDto;
import com.hcmus.mentor.backend.service.fileupload.BlobStorage;
import com.hcmus.mentor.backend.service.query.GroupSpecification;
import com.hcmus.mentor.backend.util.DateUtils;
import com.hcmus.mentor.backend.util.FileUtils;
import com.hcmus.mentor.backend.util.MailUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.math3.util.Pair;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tika.Tika;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.hcmus.mentor.backend.controller.payload.returnCode.GroupReturnCode.*;
import static com.hcmus.mentor.backend.controller.payload.returnCode.InvalidPermissionCode.INVALID_PERMISSION;
import static com.hcmus.mentor.backend.domain.Message.Status.DELETED;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {
    private static final Integer SUCCESS = 200;
    private static final Integer MAX_YEAR_FROM_TIME_START_AND_NOW = 4;
    //    private final Logger logger = LogManager.getLogger(this.getClass());
    private final GroupRepository groupRepository;
    private final GroupCategoryRepository groupCategoryRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final MailService mailService;
    private final PermissionService permissionService;
    private final MailUtils mailUtils;
    private final SystemConfigRepository systemConfigRepository;
    private final MessageRepository messageRepository;
    private final MessageService messageService;
    private final SocketIOService socketIOService;
    private final NotificationService notificationService;
    private final ChannelRepository channelRepository;
    private final BlobStorage blobStorage;
    private final ShareService shareService;

    private static Date changeGroupTime(Date time, String type) {
        LocalDateTime timeInstant =
                time.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        if (type.equals("START")) {
            timeInstant = timeInstant.withHour(0).withMinute(0);
        } else {
            timeInstant = timeInstant.withHour(23).withMinute(59);
        }

        ZonedDateTime zonedDateTime = timeInstant.atZone(ZoneId.of("UTC"));
        Instant instant = zonedDateTime.toInstant();

        return Date.from(instant);
    }

    @Override
    public Page<GroupHomepageResponse> findOwnGroups(String userId, int page, int pageSize) {
        Pageable pageRequest = PageRequest.of(page, pageSize);
        List<String> mentorIds = Collections.singletonList(userId);
        List<String> menteeIds = Collections.singletonList(userId);
        Slice<Group> wrapper = groupRepository.findByMentorsInAndStatusOrMenteesInAndStatus(
                mentorIds, GroupStatus.ACTIVE, menteeIds, GroupStatus.ACTIVE, pageRequest);
        List<GroupHomepageResponse> groups = mappingGroupHomepageResponse(wrapper.getContent(), userId);
        return new PageImpl<>(groups, pageRequest, wrapper.getNumberOfElements());
    }

    private List<GroupHomepageResponse> mappingGroupHomepageResponse(
            List<Group> groups,
            String userId) {
        Map<String, GroupCategory> categories = groupCategoryRepository.findAll().stream()
                .collect(Collectors.toMap(GroupCategory::getId, category -> category, (cat1, cat2) -> cat2));
        return groups.stream()
                .map(group -> {
                    String role = group.isMentee(userId) ? "MENTEE" : "MENTOR";
                    GroupCategory category = categories.get(group.getGroupCategory().getId());
                    String lastMessage = messageService.getLastGroupMessage(group.getId());
                    GroupHomepageResponse response = new GroupHomepageResponse(group, category.getName(), role);
                    response.setNewMessage(lastMessage);
                    return response;
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
        List<String> mentorIds = Collections.singletonList(userId);
        List<String> menteeIds = Collections.singletonList(userId);
        return groupRepository.findByMentorsInAndStatusOrMenteesInAndStatus(mentorIds, GroupStatus.ACTIVE, menteeIds, GroupStatus.ACTIVE);
    }

    /**
     * @param userId   user identity
     * @param page     page
     * @param pageSize items of page
     * @return list groupHomePageResponse user is mentor
     */
    @Override
    public Page<GroupHomepageResponse> findMentorGroups(String userId, int page, int pageSize) {
        Pageable pageRequest = PageRequest.of(page, pageSize);
        List<String> mentorIds = Collections.singletonList(userId);
        Page<Group> wrapper =
                groupRepository.findAllByMentorsInAndStatus(mentorIds, GroupStatus.ACTIVE, pageRequest);
        List<GroupHomepageResponse> groups = mappingGroupHomepageResponse(wrapper.getContent(), userId);
        return new PageImpl<>(groups, pageRequest, wrapper.getNumberOfElements());
    }

    /**
     * @param userId   user identity
     * @param page     page
     * @param pageSize items of page
     * @return list groupHomePageResponse user is mentee
     */
    @Override
    public Page<GroupHomepageResponse> findMenteeGroups(String userId, int page, int pageSize) {
        Pageable pageRequest = PageRequest.of(page, pageSize);
        List<String> menteeIds = Collections.singletonList(userId);
        Page<Group> wrapper =
                groupRepository.findAllByMenteesInAndStatus(menteeIds, GroupStatus.ACTIVE, pageRequest);
        List<GroupHomepageResponse> groups = mappingGroupHomepageResponse(wrapper.getContent(), userId);
        return new PageImpl<>(groups, pageRequest, wrapper.getNumberOfElements());
    }

    @Override
    public Page<Group> findRecentGroupsOfUser(String userId, int page, int pageSize) {
        List<String> mentorIds = Collections.singletonList(userId);
        List<String> menteeIds = Collections.singletonList(userId);
        Pageable pageRequest = PageRequest.of(page, pageSize, Sort.by("updatedDate").descending());
        return groupRepository.findAllByMentorsInOrMenteesIn(mentorIds, menteeIds, pageRequest);
    }

    @Override
    public Slice<Group> findMostRecentGroupsOfUser(String userId, int page, int pageSize) {
        List<String> mentorIds = Collections.singletonList(userId);
        List<String> menteeIds = Collections.singletonList(userId);
        Pageable pageRequest = PageRequest.of(page, pageSize, Sort.by("updatedDate").descending());
        return groupRepository.findByMentorsInAndStatusOrMenteesInAndStatus(
                mentorIds, GroupStatus.ACTIVE, menteeIds, GroupStatus.ACTIVE, pageRequest);
    }

    private boolean hasDuplicateElements(List<String> list1, List<String> list2) {
        Set<String> emails = new HashSet<>();
        for (String email : list1) {
            if (!emails.add(email)) {
                return true;
            }
        }
        for (String email : list2) {
            if (!emails.add(email)) {
                return true;
            }
        }
        return false;
    }

    private GroupServiceDto validateTimeRange(Date timeStart, Date timeEnd) {
        int maxYearsBetweenTimeStartAndTimeEnd = Integer.parseInt(systemConfigRepository
                .findByKey("valid_max_year")
                .getValue()
                .toString());
        LocalDate localTimeStart = timeStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate localTimeEnd = timeEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate localNow = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (timeEnd.before(timeStart) || timeEnd.equals(timeStart)) {
            return new GroupServiceDto(
                    TIME_END_BEFORE_TIME_START, "Time end can't be before time start", null);
        }
        if (timeEnd.before(new Date()) || timeEnd.equals(new Date())) {
            return new GroupServiceDto(TIME_END_BEFORE_NOW, "Time end can't be before now", null);
        }
        if (ChronoUnit.YEARS.between(localTimeStart, localTimeEnd)
                > maxYearsBetweenTimeStartAndTimeEnd) {
            return new GroupServiceDto(
                    TIME_END_TOO_FAR_FROM_TIME_START, "Time end is too far from time start", null);
        }
        if (Math.abs(ChronoUnit.YEARS.between(localTimeStart, localNow))
                > MAX_YEAR_FROM_TIME_START_AND_NOW) {
            return new GroupServiceDto(
                    TIME_START_TOO_FAR_FROM_NOW, "Time start is too far from now", null);
        }
        return new GroupServiceDto(SUCCESS, "", null);
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

    private GroupServiceDto validateListMentorsMentees(
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

    @Override
    public GroupServiceDto createGroup(String creatorEmail, CreateGroupCommand request) {
        if (!permissionService.isAdmin(creatorEmail)) {
            return new GroupServiceDto(INVALID_PERMISSION, "Invalid permission", null);
        }

        GroupServiceDto isValidTimeRange = validateTimeRange(request.getTimeStart(), request.getTimeEnd());
        if (!Objects.equals(isValidTimeRange.getReturnCode(), SUCCESS)) {
            return isValidTimeRange;
        }
        if (groupRepository.existsByName(request.getName())) {
            return new GroupServiceDto(DUPLICATE_GROUP, "Group name has been duplicated", null);
        }
        if (!groupCategoryRepository.existsById(request.getGroupCategory())) {
            return new GroupServiceDto(GROUP_CATEGORY_NOT_FOUND, "Group category not exists", null);
        }

        List<String> menteeEmails = request.getMenteeEmails();
        List<String> mentorEmails = request.getMentorEmails();
        GroupServiceDto isValidEmails = validateListMentorsMentees(mentorEmails, menteeEmails);
        if (!isValidEmails.getReturnCode().equals(SUCCESS)) {
            return isValidEmails;
        }

        List<String> menteeIds = menteeEmails.stream()
                .filter(email -> !email.isEmpty())
                .map(email -> userService.importUser(email, request.getName()))
                .filter(Objects::nonNull)
                .toList();

        List<String> mentorIds = mentorEmails.stream()
                .filter(email -> !email.isEmpty())
                .map(email -> userService.importUser(email, request.getName()))
                .filter(Objects::nonNull)
                .toList();
        Date timeStart = changeGroupTime(request.getTimeStart(), "START");
        Date timeEnd = changeGroupTime(request.getTimeEnd(), "END");
        Duration duration = calculateDuration(timeStart, timeEnd);
        GroupStatus status = getStatusFromTimeStartAndTimeEnd(timeStart, timeEnd);
        Optional<User> userOptional = userRepository.findByEmail(creatorEmail);
        String creatorId = null;
        if (userOptional.isPresent()) {
            creatorId = userOptional.get().getId();
        }

        Group group = Group.builder()
                .name(request.getName())
                .description(request.getDescription())
                .mentees((List<User>) userRepository.findAllById(menteeIds))
                .mentors((List<User>) userRepository.findAllById(mentorIds))
                .groupCategory(groupCategoryRepository.findById(request.getGroupCategory()).orElse(null))
                .status(status)
                .timeStart(timeStart)
                .timeEnd(timeEnd)
                .duration(duration)
                .creatorId(creatorId)
                .build();
        groupRepository.save(group);

        var channel = Channel.builder()
                .creatorId(creatorId)
                .status(ChannelStatus.ACTIVE)
                .description("Kênh chat chung")
                .name("Kênh chung")
                .type(ChannelType.PUBLIC)
                .parentId(group.getId())
                .build();
        channelRepository.save(channel);

        var userIds = new ArrayList<String>();
        userIds.addAll(menteeIds);
        userIds.addAll(mentorIds);

        addUsersToChannel(channel.getId(), userIds);


        group.setChannels(List.of(channel));
        group.setDefaultChannel(channel);
        groupRepository.save(group);

        menteeEmails.forEach(email -> mailService.sendInvitationToGroupMail(email, group));
        mentorEmails.forEach(email -> mailService.sendInvitationToGroupMail(email, group));

        return new GroupServiceDto(SUCCESS, null, group);
    }

    private Duration calculateDuration(Date from, Date to) {
        return Duration.between(from.toInstant(), to.toInstant());
    }

    private GroupStatus getStatusFromTimeStartAndTimeEnd(Date timeStart, Date timeEnd) {
        Date now = new Date();
        if (timeStart.before(now) && timeEnd.before(now)) {
            return GroupStatus.OUTDATED;
        }
        if (timeStart.before(now) && timeEnd.after(now)) {
            return GroupStatus.ACTIVE;
        }
        return GroupStatus.INACTIVE;
    }

    private void removeBlankRows(Sheet sheet) {
        int lastRowNum = sheet.getLastRowNum();
        for (int i = lastRowNum; i >= 0; i--) {
            Row row = sheet.getRow(i);
            if (row == null
                    || row.getCell(0) == null
                    || row.getCell(0).getCellType() == org.apache.poi.ss.usermodel.CellType.BLANK) {
                sheet.removeRow(row);
            }
        }
    }

    @Override
    public GroupServiceDto readGroups(Workbook workbook) throws ParseException {
        Map<String, Group> groups = new HashMap<>();
        Sheet sheet = workbook.getSheet("Data");
        removeBlankRows(sheet);
        String groupCategoryName;
        List<String> menteeEmails;
        List<String> mentorEmails;
        String groupName;
        String description = "";
        Date timeStart;
        Date timeEnd;

        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            var errorOnRow = String.format("tại dòng %d không có dữ liệu.", i);
            Row row = sheet.getRow(i);
            if (i == 0) {
                continue;
            }
            // Validate required fields
            // Group category
            if (row.getCell(1) == null || row.getCell(2).getStringCellValue().isEmpty()) {
                return new GroupServiceDto(NOT_ENOUGH_FIELDS, String.format("Loại nhóm %s", errorOnRow), null);
            }
            groupCategoryName = row.getCell(1).getStringCellValue();
            if (!groupCategoryRepository.existsByName(groupCategoryName)) {
                return new GroupServiceDto(GROUP_CATEGORY_NOT_FOUND, "Group category not exists", groupCategoryName);
            }
            String groupCategoryId = groupCategoryRepository.findByName(groupCategoryName).getId();

            // Mentee email
            if (row.getCell(2) == null || row.getCell(2).getStringCellValue().isEmpty()) {
                return new GroupServiceDto(NOT_ENOUGH_FIELDS, String.format("Email người được quản lý %s", errorOnRow), null);
            }
            menteeEmails = Arrays.stream(row.getCell(2).getStringCellValue().split("\n")).toList();

            // Group name
            if (row.getCell(3) == null || row.getCell(3).getStringCellValue().isEmpty()) {
                return new GroupServiceDto(NOT_ENOUGH_FIELDS, String.format("Tên nhóm  %s", errorOnRow), null);
            }
            groupName = row.getCell(3).getStringCellValue();
            if (groups.containsKey(groupName) || groupCategoryRepository.existsByName(groupName))
                return new GroupServiceDto(DUPLICATE_GROUP, "Group name has been duplicated", groupName);

            // Mentor email
            if (row.getCell(5) == null || row.getCell(5).getStringCellValue().isEmpty()) {
                return new GroupServiceDto(NOT_ENOUGH_FIELDS, String.format("Email người quản lý %s", errorOnRow), null);
            }
            mentorEmails = Arrays.stream(row.getCell(5).getStringCellValue().split("\n")).toList();

            // Start date
            if (row.getCell(6) == null || row.getCell(6).getStringCellValue().isEmpty()) {
                return new GroupServiceDto(NOT_ENOUGH_FIELDS, String.format("Ngày bắt đầu %s", errorOnRow), null);
            }
            timeStart = formatter.parse(row.getCell(6).getStringCellValue());

            // End date
            if (row.getCell(7).getStringCellValue().isEmpty()) {
                return new GroupServiceDto(NOT_ENOUGH_FIELDS, String.format("Ngày kết thúc %s", errorOnRow), null);
            }
            timeEnd = formatter.parse(row.getCell(7).getStringCellValue());

            GroupServiceDto isValidTimeRange = validateTimeRange(timeStart, timeEnd);
            if (!Objects.equals(isValidTimeRange.getReturnCode(), SUCCESS)) {
                return isValidTimeRange;
            }

            Group group = Group.builder()
                    .name(groupName)
                    .description(description)
                    .createdDate(new Date())
                    .mentees(userRepository.findByEmailIn(menteeEmails))
                    .mentors(userRepository.findByEmailIn(mentorEmails))
                    .groupCategory(groupCategoryRepository.findById(groupCategoryId).orElse(null))
                    .timeStart(timeStart)
                    .timeEnd(timeEnd)
                    .build();
            groups.put(groupName, group);
        }
        return new GroupServiceDto(SUCCESS, "", groups);
    }

    @Override
    public GroupServiceDto importGroups(String emailUser, MultipartFile file) throws IOException {
        if (!permissionService.isAdmin(emailUser)) {
            return new GroupServiceDto(INVALID_PERMISSION, "Invalid permission", null);
        }
        Map<String, Group> groups;
        try (InputStream data = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(data)) {
            List<String> nameHeaders = new ArrayList<>();
            nameHeaders.add("STT");
            nameHeaders.add("Loại nhóm *");
            nameHeaders.add("Emails người được quản lí *");
            nameHeaders.add("Tên nhóm *");
            nameHeaders.add("Mô tả");
            nameHeaders.add("Emails người quản lí *");
            nameHeaders.add("Ngày bắt đầu *\n" + "(dd/MM/YYYY)");
            nameHeaders.add("Ngày kết thúc *\n" + "(dd/MM/YYYY)");

            if (!shareService.isValidTemplate(workbook, 2, nameHeaders)) {
                return new GroupServiceDto(INVALID_TEMPLATE, "Invalid template", null);
            }

            GroupServiceDto validReadGroups = readGroups(workbook);
            if (!Objects.equals(validReadGroups.getReturnCode(), SUCCESS)) {
                return validReadGroups;
            }
            groups = (Map<String, Group>) validReadGroups.getData();
        } catch (ParseException e) {
            throw new DomainException(String.valueOf(e));
        }
        for (Group group : groups.values()) {
            GroupServiceDto isValidMails = validateListMentorsMentees(
                    group.getMentors().stream().map(User::getId).toList(),
                    group.getMentees().stream().map(User::getId).toList());
            if (!Objects.equals(isValidMails.getReturnCode(), SUCCESS)) {
                return isValidMails;
            }
        }

        List<CreateGroupCommand> createGroupRequests =
                groups.values().stream()
                        .map(group -> CreateGroupCommand.builder()
                                .name(group.getName())
                                .createdDate(new Date())
                                .menteeEmails(group.getMentees().stream().map(User::getEmail).toList())
                                .mentorEmails(group.getMentors().stream().map(User::getEmail).toList())
                                .groupCategory(group.getGroupCategory().getName())
                                .timeStart(group.getTimeStart())
                                .timeEnd(group.getTimeEnd())
                                .build())
                        .toList();
        for (CreateGroupCommand createGroupRequest : createGroupRequests) {
            GroupServiceDto returnData = createGroup(emailUser, createGroupRequest);
            if (!Objects.equals(returnData.getReturnCode(), SUCCESS)) {
                return returnData;
            }
        }
        for (Group group : groups.values()) {
            group.setId(groupRepository.findByName(group.getName()).getId());
            group.setDuration(groupRepository.findByName(group.getName()).getDuration());
            group.setMentors(groupRepository.findByName(group.getName()).getMentors());
            group.setMentees(groupRepository.findByName(group.getName()).getMentees());
        }

        return new GroupServiceDto(SUCCESS, null, groups.values());
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

    @Override
    public GroupServiceDto findGroups(
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
        if (!permissionService.isAdmin(emailUser)) {
            return new GroupServiceDto(INVALID_PERMISSION, "Invalid permission", null);
        }
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
                page,
                pageSize);
        return new GroupServiceDto(
                SUCCESS,
                "",
                new PageImpl<>(groups.getValue(), PageRequest.of(page, pageSize), groups.getKey()));
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
        Specification<Group> spec = GroupSpecification.withConditions(
                emailUser,
                name,
                mentorEmail,
                menteeEmail,
                groupCategory,
                status,
                timeStart1,
                timeEnd1,
                timeStart2,
                timeEnd2
        );

        Pageable pageable = PageRequest.of(page, pageSize);
        Page<Group> groupPage = groupRepository.findAll(spec, pageable);

        List<Group> data = groupPage.getContent();
        long count = groupPage.getTotalElements();
        data = validateTimeGroups(data);
        return new Pair<>(count, data);
    }


    @Override
    public GroupServiceDto addMentees(String emailUser, String groupId, AddMenteesRequest request) {
        var groupServiceDto = getGroupById(emailUser, groupId);
        if (!groupServiceDto.getReturnCode().equals(SUCCESS)) {
            return groupServiceDto;
        }
        Group group = (Group) groupServiceDto.getData();

        List<String> emails = request.getEmails();
        List<String> ids = emails.stream()
                .filter(email -> !email.isEmpty())
                .map(email -> userService.getOrCreateUserByEmail(email, group.getName()))
                .toList();

        if (hasDuplicateElements(ids, group.getMentees().stream().map(User::getId).toList())
                || hasDuplicateElements(ids, group.getMentors().stream().map(User::getId).toList())) {
            return new GroupServiceDto(DUPLICATE_EMAIL, "Duplicate emails", null);
        }
        group.getMentees().addAll((Collection<? extends User>) userRepository.findAllById(ids));
        var listMenteesAfterRemoveDuplicate = new ArrayList<>(new HashSet<>(group.getMentees()));
        group.setMentees(listMenteesAfterRemoveDuplicate);

        addUsersToChannel(group.getDefaultChannel().getId(), ids);

        groupRepository.save(group);
        for (String emailAddress : emails) {
            mailService.sendInvitationToGroupMail(emailAddress, group);
        }
        return new GroupServiceDto(SUCCESS, null, group);
    }

    @Override
    public GroupServiceDto addMentors(String emailUser, String groupId, AddMentorsRequest request) {
        var groupServiceDto = getGroupById(emailUser, groupId);
        if (!groupServiceDto.getReturnCode().equals(SUCCESS)) {
            return groupServiceDto;
        }
        Group group = (Group) groupServiceDto.getData();

        List<String> emails = request.getEmails();
        List<String> ids = emails.stream()
                .filter(email -> !email.isEmpty())
                .map(email -> userService.getOrCreateUserByEmail(email, group.getName()))
                .toList();

        if (hasDuplicateElements(ids, group.getMentees().stream().map(User::getId).toList())
                || hasDuplicateElements(ids, group.getMentors().stream().map(User::getId).toList())) {
            return new GroupServiceDto(DUPLICATE_EMAIL, "Duplicate emails", null);
        }

        group.getMentors().addAll((Collection<? extends User>) userRepository.findAllById(ids));
        var listMentorsAfterRemoveDuplicate = new ArrayList<>(new HashSet<>(group.getMentors()));
        group.setMentors(listMentorsAfterRemoveDuplicate);

        addUsersToChannel(group.getDefaultChannel().getId(), ids);

        groupRepository.save(group);
        for (String emailAddress : emails) {
            mailService.sendInvitationToGroupMail(emailAddress, group);
        }
        return new GroupServiceDto(SUCCESS, null, group);
    }


    private void addUsersToChannel(String channelId, List<String> userIds) {
        Channel channel = channelRepository.findById(channelId).orElse(null);
        if (channel == null) {
            return;
        }

        var userIdInChannel = channel.getUsers().stream().map(User::getId).toList();
        var userNotInChannel = userIds.stream().filter(id -> !userIdInChannel.contains(id)).toList();

        if (userNotInChannel.isEmpty()) {
            return;
        }

        var userInChannel = channel.getUsers();
        for (String userId : userNotInChannel) {
            var user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                continue;
            }

            userInChannel.add(user);
        }

        channelRepository.save(channel);
    }


    @Override
    public GroupServiceDto deleteMentee(String emailUser, String groupId, String menteeId) {
        var groupServiceDto = getGroupById(emailUser, groupId);
        if (!groupServiceDto.getReturnCode().equals(SUCCESS)) {
            return groupServiceDto;
        }
        Group group = (Group) groupServiceDto.getData();

        if (group.getMentees().remove(userRepository.findById(menteeId).orElse(null))) {
            group.setMentees(group.getMentees());
            groupRepository.save(group);
            return new GroupServiceDto(SUCCESS, null, group);
        }
        return new GroupServiceDto(MENTEE_NOT_FOUND, "Mentee not found", null);
    }

    @Override
    public GroupServiceDto deleteMentor(String emailUser, String groupId, String mentorId) {
        var groupServiceDto = getGroupById(emailUser, groupId);
        if (!groupServiceDto.getReturnCode().equals(SUCCESS)) {
            return groupServiceDto;
        }
        Group group = (Group) groupServiceDto.getData();

        if (group.getMentors().remove(userRepository.findById(mentorId).orElse(null))) {
            group.setMentors(group.getMentors());
            groupRepository.save(group);
            return new GroupServiceDto(SUCCESS, null, group);
        }

        return new GroupServiceDto(MENTOR_NOT_FOUND, "mentor not found", null);
    }

    @Override
    public GroupServiceDto promoteToMentor(String emailUser, String groupId, String menteeId) {
        var groupServiceDto = getGroupById(emailUser, groupId);
        if (!groupServiceDto.getReturnCode().equals(SUCCESS)) {
            return groupServiceDto;
        }
        Group group = (Group) groupServiceDto.getData();

        var userOpt = userRepository.findById(menteeId);
        if (userOpt.isPresent()) {
            var user = userOpt.get();
            group.getMentees().remove(user);
            group.getMentors().add(user);
            groupRepository.save(group);

            return new GroupServiceDto(SUCCESS, null, group);
        }

        return new GroupServiceDto(MENTEE_NOT_FOUND, "Mentee not found", null);
    }

    @Override
    public GroupServiceDto demoteToMentee(String emailUser, String groupId, String mentorId) {
        var groupServiceDto = getGroupById(emailUser, groupId);
        if (!groupServiceDto.getReturnCode().equals(SUCCESS)) {
            return groupServiceDto;
        }
        Group group = (Group) groupServiceDto.getData();

        var userOpt = userRepository.findById(mentorId);
        if (userOpt.isPresent()) {
            var user = userOpt.get();
            group.getMentors().remove(user);
            group.getMentees().add(user);
            groupRepository.save(group);
            return new GroupServiceDto(SUCCESS, null, group);
        }

        return new GroupServiceDto(MENTOR_NOT_FOUND, "Mentor not found", null);
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
    public GroupServiceDto updateGroup(String emailUser, String groupId, UpdateGroupRequest request) {
        var groupServiceDto = getGroupById(emailUser, groupId);
        if (!groupServiceDto.getReturnCode().equals(SUCCESS)) {
            return groupServiceDto;
        }
        Group group = (Group) groupServiceDto.getData();

        if (groupRepository.existsByName(request.getName())
                && !request.getName().equals(group.getName())) {
            return new GroupServiceDto(DUPLICATE_GROUP, "Group name has been duplicated", null);
        }
        Date timeStart = changeGroupTime(request.getTimeStart(), "START");
        Date timeEnd = changeGroupTime(request.getTimeEnd(), "END");
        group.update(
                request.getName(),
                request.getDescription(),
                request.getStatus(),
                timeStart,
                timeEnd,
                request.getGroupCategory());
        if (!groupCategoryRepository.existsById(group.getGroupCategory().getId())) {
            return new GroupServiceDto(GROUP_CATEGORY_NOT_FOUND, "Group category not exists", null);
        }

        GroupServiceDto isValidTimeRange = validateTimeRange(group.getTimeStart(), group.getTimeEnd());
        if (!Objects.equals(isValidTimeRange.getReturnCode(), SUCCESS)) {
            return isValidTimeRange;
        }
        Duration duration = calculateDuration(group.getTimeStart(), group.getTimeEnd());
        group.setDuration(duration);
        GroupStatus status =
                getStatusFromTimeStartAndTimeEnd(group.getTimeStart(), group.getTimeEnd());
        group.setStatus(status);
        if (request.getStatus().equals(GroupStatus.DISABLED)) {
            group.setStatus(GroupStatus.DISABLED);
        }
        group.setUpdatedDate(new Date());
        groupRepository.save(group);
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
        List<Group> groups = groupRepository.findByIdIn(user.getPinnedGroupsId());
        return mappingGroupHomepageResponse(groups, userId);
    }

    @Override
    public boolean isGroupMember(String groupId, String userId) {
        var group = groupRepository.findById(groupId).orElse(null);

        if (group != null) {
            var user = userRepository.findById(userId).orElse(null);
            return user != null && (group.getMentors().contains(user) || group.getMentees().contains(user));
        }

        var channelOpt = channelRepository.findById(groupId);
        if (channelOpt.isPresent()) {
            var channel = channelOpt.get();
            return channel.isMember(userId) || isGroupMember(channel.getParentId(), userId);
        }

        return false;
    }

    @Override
    public Slice<GroupHomepageResponse> getHomePageRecentGroupsOfUser(
            String userId, int page, int pageSize) {
        Slice<Group> groups = findMostRecentGroupsOfUser(userId, page, pageSize);
        List<GroupHomepageResponse> responses =
                mappingGroupHomepageResponse(groups.getContent(), userId);
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
    public GroupServiceDto disableMultiple(String emailUser, List<String> ids) {
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
        for (Group group : groups) {
            group.setStatus(GroupStatus.DISABLED);
            groupRepository.save(group);
        }

        return new GroupServiceDto(SUCCESS, null, groups);
    }

    @Override
    public GroupServiceDto enableMultiple(String emailUser, List<String> ids) {
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
        for (Group group : groups) {
            GroupStatus status =
                    getStatusFromTimeStartAndTimeEnd(group.getTimeStart(), group.getTimeEnd());
            group.setStatus(status);
            groupRepository.save(group);
        }

        return new GroupServiceDto(SUCCESS, null, groups);
    }

    @Override
    public GroupServiceDto getGroupMembers(String groupId, String userId) {
        Optional<Group> groupWrapper = groupRepository.findById(groupId);
        List<String> mentorIds = new ArrayList<>();
        List<String> menteeIds = new ArrayList<>();
        Group group = null;
        if (groupWrapper.isEmpty()) {
            Optional<Channel> channelWrapper = channelRepository.findById(groupId);
            if (channelWrapper.isEmpty()) {
                return new GroupServiceDto(NOT_FOUND, "Group not found", null);
            }

            Channel channel = channelWrapper.get();
            group = groupRepository.findById(channel.getParentId()).orElse(null);
            if (group == null) {
                return new GroupServiceDto(NOT_FOUND, "Group not found", null);
            }

            mentorIds = channel.getUsers().stream().filter(group::isMentor).map(User::getId).toList();
            menteeIds = channel.getUsers().stream().filter(group::isMentee).map(User::getId).toList();
        }

        if (groupWrapper.isPresent()) {
            if (!permissionService.isUserIdInGroup(userId, groupId)) {
                return new GroupServiceDto(INVALID_PERMISSION, "Invalid permission", null);
            }

            group = groupWrapper.get();
            mentorIds = group.getMentors().stream().map(User::getId).toList();
            menteeIds = group.getMentees().stream().map(User::getId).toList();
        }

        List<GroupMembersResponse.GroupMember> mentors = userRepository.findAllByIdIn(mentorIds).stream()
                .map(ProfileResponse::normalize)
                .map(profile -> GroupMembersResponse.GroupMember.from(profile, "MENTOR"))
                .toList();
        List<String> markedMentees = group.getMarkedMentees() != null ? group.getMarkedMentees().stream().map(User::getId).toList() : new ArrayList<>();
        List<GroupMembersResponse.GroupMember> mentees = userRepository.findAllByIdIn(menteeIds).stream()
                .map(ProfileResponse::normalize)
                .map(profile -> GroupMembersResponse.GroupMember.from(profile, "MENTEE", markedMentees.contains(profile.getId())))
                .toList();
        GroupMembersResponse response = GroupMembersResponse.builder().mentors(mentors).mentees(mentees).build();
        return new GroupServiceDto(SUCCESS, null, response);
    }

    @Override
    public void pinGroup(String userId, String groupId) {
        Optional<User> userWrapper = userRepository.findById(userId);
        if (userWrapper.isEmpty()) {
            return;
        }
        User user = userWrapper.get();
        user.pinGroup(groupId);
        userRepository.save(user);
    }

    @Override
    public void unpinGroup(String userId, String groupId) {
        Optional<User> userWrapper = userRepository.findById(userId);
        if (userWrapper.isEmpty()) {
            return;
        }
        User user = userWrapper.get();
        user.unpinGroup(groupId);
        userRepository.save(user);
    }

    @Override
    public GroupServiceDto getGroupDetail(String userId, String groupId) {
        List<GroupDetailResponse> groupWrapper = groupRepository.getGroupDetail(groupId);
        if (groupWrapper.isEmpty()) {
            Optional<Channel> channelWrapper = channelRepository.findById(groupId);
            if (channelWrapper.isEmpty()) {
                return new GroupServiceDto(NOT_FOUND, "Group not found", null);
            }
            Channel channel = channelWrapper.get();
            Optional<Group> parentGroupWrapper = groupRepository.findById(channel.getParentId());
            if (parentGroupWrapper.isEmpty()) {
                return new GroupServiceDto(NOT_FOUND, "Group not found", null);
            }

            Group parentGroup = parentGroupWrapper.get();
            if (!parentGroup.isMentor(userId) && !channel.isMember(userId)) {
                return new GroupServiceDto(INVALID_PERMISSION, "Invalid permission", null);
            }
            GroupDetailResponse channelDetail = fulfillChannelDetail(userId, channel, parentGroup);
            if (channelDetail == null) {
                return new GroupServiceDto(NOT_FOUND, "Group not found", null);
            }
            GroupDetailResponse response = fulfillGroupDetail(userId, channelDetail);
            return new GroupServiceDto(SUCCESS, null, response);
        }

        if (!permissionService.isUserIdInGroup(userId, groupId)) {
            return new GroupServiceDto(INVALID_PERMISSION, "Invalid permission", null);
        }

        GroupDetailResponse response = fulfillGroupDetail(userId, groupWrapper.get(0));
        return new GroupServiceDto(SUCCESS, null, response);
    }

    private GroupDetailResponse fulfillChannelDetail(
            String userId,
            Channel channel,
            Group parentGroup) {
        String channelName = channel.getName();
        String imageUrl = null;

        if (ChannelType.PRIVATE_MESSAGE.equals(channel.getType())) {
            String penpalId = channel.getUsers().stream()
                    .map(User::getId)
                    .filter(id -> !id.equals(userId))
                    .findFirst()
                    .orElse(null);
            if (userId == null) {
                return null;
            }
            ShortProfile penpal = userRepository.findShortProfile(penpalId);
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
                .pinnedMessageIds(channel.getPinnedMessageIds().stream().map(Message::getId).toList())
                .imageUrl(imageUrl)
                .role(parentGroup.isMentor(userId) ? "MENTOR" : "MENTEE")
                .parentId(parentGroup.getId())
                .totalMember(channel.getUsers().size())
                .type(channel.getType())
                .build();

        GroupCategory groupCategory = parentGroup.getGroupCategory();
        if (groupCategory != null) {
            response.setPermissions(groupCategory.getPermissions());
            response.setGroupCategory(groupCategory.getName());
        }

        List<MessageResponse> messages = new ArrayList<>();

        if (response.getPinnedMessageIds() != null && !response.getPinnedMessageIds().isEmpty()) {
            messages = response.getPinnedMessageIds().stream()
                    .map(messageRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .filter(message -> !message.isDeleted())
                    .map(message -> {
                        User user = userRepository.findById(message.getSenderId()).orElse(null);
                        return MessageResponse.from(message, ProfileResponse.from(user));
                    })
                    .toList();
        }
        response.setPinnedMessages(messageService.fulfillMessages(messages, userId));
        response.setTotalMember(channel.getUsers().size());
        return response;
    }

    private GroupDetailResponse fulfillGroupDetail(String userId, GroupDetailResponse response) {
        response.setRole(userId);

        Optional<User> userWrapper = userRepository.findById(userId);
        if (userWrapper.isPresent()) {
            User user = userWrapper.get();
            response.setPinned(user.isPinnedGroup(response.getId()));
        }
        GroupCategory groupCategory = groupCategoryRepository.findByName(response.getGroupCategory());
        if (groupCategory != null) {
            response.setPermissions(groupCategory.getPermissions());
        }

        response.setPinnedMessages(fullFillPinMessages(userId, response.getPinnedMessageIds()));
        return response;
    }


    private List<MessageDetailResponse> fullFillPinMessages(String userId, List<String> pinMessageIds) {
        List<MessageResponse> messageResponses = new ArrayList<>();
        if (pinMessageIds != null && !pinMessageIds.isEmpty()) {
            messageResponses = pinMessageIds.stream()
                    .map(messageRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .filter(message -> !message.isDeleted())
                    .map(message -> {
                        User user = userRepository.findById(message.getSenderId()).orElse(null);
                        return MessageResponse.from(message, ProfileResponse.from(user));
                    })
                    .toList();
        }
        return messageService.fulfillMessages(messageResponses, userId);
    }

    @Override
    public List<String> findAllMenteeIdsGroup(String groupId) {
        Optional<Group> wrapper = groupRepository.findById(groupId);
        if (wrapper.isEmpty()) {
            return Collections.emptyList();
        }
        Group group = wrapper.get();
        return group.getMentees().stream()
//                .filter(userRepository::existsById)
                .map(User::getId)
                .distinct()
                .toList();
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
    public GroupServiceDto getGroupMedia(String userId, String groupId) {
        Optional<Group> groupWrapper = groupRepository.findById(groupId);
        List<String> senderIds = new ArrayList<>();
        if (groupWrapper.isEmpty()) {
            Optional<Channel> channelWrapper = channelRepository.findById(groupId);
            if (channelWrapper.isEmpty()) {
                return new GroupServiceDto(NOT_FOUND, "Group not found", null);
            }

            Channel channel = channelWrapper.get();
            if (!channel.isMember(userId)
                    && !permissionService.isUserIdInGroup(userId, channel.getParentId())) {
                return new GroupServiceDto(INVALID_PERMISSION, "Invalid permission", null);
            }

            senderIds = channel.getUsers().stream().map(User::getId).toList();
        }

        if (groupWrapper.isPresent()) {
            Group group = groupWrapper.get();
            if (!group.isMember(userId)) {
                return new GroupServiceDto(INVALID_PERMISSION, "Invalid permission", null);
            }

            senderIds = Stream.concat(group.getMentees().stream().map(User::getId), group.getMentors().stream().map(User::getId))
                    .toList();
        }

        Map<String, ProfileResponse> senders = userRepository.findAllByIdIn(senderIds).stream()
                .collect(
                        Collectors.toMap(
                                ProfileResponse::getId, sender -> sender, (sender1, sender2) -> sender2));

        List<Message> mediaMessages = messageRepository.findByGroupIdAndTypeInAndStatusInOrderByCreatedDateDesc(
                groupId,
                Arrays.asList(Message.Type.IMAGE, Message.Type.FILE),
                Arrays.asList(Message.Status.SENT, Message.Status.EDITED));

        List<ShortMediaMessage> media = new ArrayList<>();
        mediaMessages.forEach(message -> {
            ProfileResponse sender = senders.getOrDefault(message.getSenderId(), null);

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
                                .file(message.getFile())
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
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            String dateStart = dateFormat.format(group.getTimeStart());
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
        List<String> headers =
                Arrays.asList(
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
        List<Integer> remainColumnIndexes = new ArrayList<>();
        remainColumnIndexes.add(0);
        remainColumns.forEach(
                remainColumn -> {
                    if (indexMap.containsKey(remainColumn)) {
                        remainColumnIndexes.add(indexMap.get(remainColumn));
                    }
                });

        File exportFile = FileUtils.generateExcel(headers, data, remainColumnIndexes, fileName);
        Resource resource = new FileSystemResource(exportFile.getAbsolutePath());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + resource.getFilename())
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .contentLength(resource.getFile().length())
                .body(resource);
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

        Pair<Long, List<Group>> groups =
                getGroupsByConditions(
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

    private List<List<String>> generateExportDataMembers(String groupId, String type) {
        Optional<Group> groupOptional = groupRepository.findById(groupId);
        List<String> memberIds = new ArrayList<>();
        if (groupOptional.isPresent()) {
            if (type.equals("MENTOR")) {
                memberIds = groupOptional.get().getMentors().stream().map(User::getId).toList();
            } else if (type.equals("MENTEE")) {
                memberIds = groupOptional.get().getMentees().stream().map(User::getId).toList();
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
            String emailUser, List<String> remainColumns, String groupId, String type)
            throws IOException {
        List<List<String>> data = generateExportDataMembers(groupId, type);
        List<String> headers = Arrays.asList("STT", "Email", "Họ tên");
        String fileName = "output.xlsx";
        Map<String, Integer> indexMap = new HashMap<>();
        indexMap.put("email", 1);
        indexMap.put("name", 2);
        List<Integer> remainColumnIndexes = new ArrayList<>();
        remainColumnIndexes.add(0);
        remainColumns.forEach(
                remainColumn -> {
                    if (indexMap.containsKey(remainColumn)) {
                        remainColumnIndexes.add(indexMap.get(remainColumn));
                    }
                });

        File exportFile = FileUtils.generateExcel(headers, data, remainColumnIndexes, fileName);
        Resource resource = new FileSystemResource(exportFile.getAbsolutePath());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + resource.getFilename())
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .contentLength(resource.getFile().length())
                .body(resource);
    }

    @Override
    public void pinChannelMessage(String userId, String channelId, String messageId) {
        Channel channel = channelRepository.findById(channelId).orElseThrow(() -> new DomainException("Channel not found"));
        if (channel.getPinnedMessageIds().size() >= 5) {
            throw new DomainException("Maximum pinned messages");
        }

        Message message = messageRepository.findByIdAndStatusNot(messageId, DELETED).orElseThrow(() -> new DomainException("Message not found"));
        User sender = userRepository.findById(message.getSenderId()).orElseThrow(() -> new DomainException("Sender not found"));

        if (!channel.getPinnedMessageIds().contains(message)) {
            channel.getPinnedMessageIds().add(message);

            channel.ping();
            channelRepository.save(channel);
        }

        MessageDetailResponse messageDetail = MessageDetailResponse.from(message, sender);
        socketIOService.sendNewPinMessage(messageDetail);

        var pinnerWrapper = userRepository.findById(userId).orElseThrow(() -> new DomainException("Pinner not found"));
        notificationService.sendNewPinNotification(messageDetail, pinnerWrapper);
    }

    @Override
    public void unpinChannelMessage(String userId, String channelId, String messageId) {
        Channel channel = channelRepository.findById(channelId).orElseThrow(() -> new DomainException("Channel not found"));
        Message message = messageRepository.findById(messageId).orElseThrow(() -> new DomainException("Message not found"));
        User sender = userRepository.findById(message.getSenderId()).orElseThrow(() -> new DomainException("Sender not found"));

        if (!channel.getPinnedMessageIds().contains(message)) {
            return;
        }

        channel.getPinnedMessageIds().remove(message);
        channel.ping();

        channelRepository.save(channel);
        socketIOService.sendNewUnpinMessage(channelId, messageId);

        User pinnerMessage = userRepository.findById(userId).orElseThrow(() -> new DomainException("Pinner not found"));
        notificationService.sendNewUnpinNotification(MessageDetailResponse.from(message, sender), pinnerMessage);
    }

    @Override
    public void updateLastMessageId(String groupId, String messageId) {
        Optional<Group> groupWrapper = groupRepository.findById(groupId);
        if (groupWrapper.isEmpty()) {
            return;
        }
        Group group = groupWrapper.get();
        group.setLastMessageId(messageId);
        groupRepository.save(group);
    }

    @Override
    public GroupDetailResponse getGroupWorkspace(CustomerUserDetails user, String groupId) {
        if (!permissionService.isUserIdInGroup(user.getId(), groupId)) {
            return null;
        }
        List<GroupDetailResponse> groupWrapper = groupRepository.getGroupDetail(groupId);
        if (groupWrapper.isEmpty()) {
            return null;
        }
        Group group = groupRepository.findById(groupId).orElse(null);
        if (group == null) {
            return null;
        }
        GroupDetailResponse detail = fulfillGroupDetail(user.getId(), groupWrapper.getFirst());

        List<String> channelIds = group.getChannels() != null
                ? group.getChannels().stream().map(Channel::getId).toList()
                : new ArrayList<>();
        List<GroupDetailResponse.GroupChannel> channels = channelRepository.findByIdIn(channelIds).stream()
                .map(GroupDetailResponse.GroupChannel::from)
                .sorted(Comparator.comparing(GroupDetailResponse.GroupChannel::getUpdatedDate).reversed())
                .toList();
        detail.setChannels(channels);

        List<GroupDetailResponse.GroupChannel> privates =
                channelRepository
                        .findByParentIdAndTypeAndUserIdsIn(groupId, ChannelType.PRIVATE_MESSAGE, Collections.singletonList(user.getId()))
                        .stream()
                        .map(channel -> {
                            String userId = Objects.requireNonNull(channel.getUsers().stream()
                                            .filter(id -> !id.equals(user.getId()))
                                            .findFirst()
                                            .orElse(null))
                                    .getId();
                            if (userId == null) {
                                return null;
                            }
                            ShortProfile penpal = userRepository.findShortProfile(userId);
                            if (penpal == null) {
                                return null;
                            }
                            channel.setName(penpal.getName());
                            channel.setImageUrl(penpal.getImageUrl());

                            List<String> markedMentees = group.getMarkedMentees() != null
                                    ? group.getMarkedMentees().stream().map(User::getId).toList()
                                    : new ArrayList<>();
                            return GroupDetailResponse.GroupChannel.from(channel, markedMentees.contains(penpal.getId()));
                        })
                        .filter(Objects::nonNull)
                        .sorted(Comparator.comparing(GroupDetailResponse.GroupChannel::getUpdatedDate).reversed())
                        .sorted(Comparator.comparing(GroupDetailResponse.GroupChannel::getMarked).reversed())
                        .toList();
        detail.setPrivates(privates);
        return detail;
    }

    @Override
    public void markMentee(CustomerUserDetails user, String groupId, String menteeId) {
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new DomainException("Không tìm thấy nhóm với id " + groupId));
        if (!group.isMentor(user.getId())) {
            throw new ForbiddenException("You are not mentor");
        }

        if (group.getMentees().stream().anyMatch(m -> m.getId().equals(menteeId))) {
            throw new DomainException("User not in group");
        }

        var markedMentees = group.getMarkedMentees();
        if (markedMentees.stream().anyMatch(m -> m.getId().equals(menteeId))) {
            throw new DomainException("Mentee already marked");
        }

        markedMentees.add(userRepository.findById(menteeId).orElseThrow(() -> new DomainException("Mentee not found")));
        groupRepository.save(group);
    }

    @Override
    public void unmarkMentee(CustomerUserDetails user, String groupId, String menteeId) {
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new DomainException("Không tìm thấy nhóm với id " + groupId));
        if (!group.isMentor(user.getId())) {
            throw new ForbiddenException("You are not mentor");
        }

        if (group.getMentees().stream().anyMatch(m -> m.getId().equals(menteeId))) {
            throw new DomainException("User not in group");
        }

        var markedMentees = group.getMarkedMentees();

        if (markedMentees == null || markedMentees.stream().noneMatch(m -> m.getId().equals(menteeId))) {
            throw new DomainException("Mentee not marked");
        }

        markedMentees.remove(userRepository.findById(menteeId).orElseThrow(() -> new DomainException("Mentee not found")));
        groupRepository.save(group);
    }

    /**
     * @param user UserPrincipal
     * @param name Optional name of groups.
     * @return List<GroupForwardResponse>
     */
    @Override
    public List<ChannelForwardResponse> getGroupForwards(CustomerUserDetails user, Optional<String> name) {
        List<Group> groups = groupRepository.findByMenteesContainsOrMentorsContainsAndStatusIs(user.getId(), user.getId(), GroupStatus.ACTIVE);

        var channels = groups.stream().map(Group::getChannels).toList();
        List<String> lstChannelIds = new ArrayList<>();
        for (List<Channel> channel : channels) {
            lstChannelIds.addAll(channel.stream().map(Channel::getId).toList());
        }

        return channelRepository.getListChannelForward(lstChannelIds, ChannelStatus.ACTIVE.name());
    }
}
