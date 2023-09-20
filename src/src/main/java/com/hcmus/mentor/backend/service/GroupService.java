package com.hcmus.mentor.backend.service;

import com.hcmus.mentor.backend.entity.Group;
import com.hcmus.mentor.backend.entity.GroupCategory;
import com.hcmus.mentor.backend.entity.Message;
import com.hcmus.mentor.backend.entity.User;
import com.hcmus.mentor.backend.manager.GoogleDriveManager;
import com.hcmus.mentor.backend.payload.request.groups.*;
import com.hcmus.mentor.backend.payload.response.*;
import com.hcmus.mentor.backend.payload.response.groups.GroupDetailResponse;
import com.hcmus.mentor.backend.payload.response.groups.GroupHomepageResponse;
import com.hcmus.mentor.backend.payload.response.groups.GroupMembersResponse;
import com.hcmus.mentor.backend.payload.response.messages.MessageDetailResponse;
import com.hcmus.mentor.backend.payload.response.messages.MessageResponse;
import com.hcmus.mentor.backend.payload.response.users.ProfileResponse;
import com.hcmus.mentor.backend.repository.*;
import com.hcmus.mentor.backend.util.DateUtils;
import com.hcmus.mentor.backend.util.FileUtils;
import com.hcmus.mentor.backend.util.MailUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.math3.util.Pair;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.hcmus.mentor.backend.payload.returnCode.GroupReturnCode.*;
import static com.hcmus.mentor.backend.payload.returnCode.InvalidPermissionCode.INVALID_PERMISSION;


@Service
public class GroupService {
    @Getter
    @Setter
    @NoArgsConstructor
    public static class GroupReturnService {
        Integer returnCode;
        String message;
        Object data;

        public GroupReturnService(Integer returnCode, String message, Object data) {
            this.returnCode = returnCode;
            this.message = message;
            this.data = data;
        }
    }

    private final Integer SUCCESS = 200;

    private final GroupRepository groupRepository;

    private final GroupCategoryRepository groupCategoryRepository;

    private final UserRepository userRepository;

    private final UserService userService;

    private final MailService mailService;

    private final PermissionService permissionService;

    private final MongoTemplate mongoTemplate;

    private final MailUtils mailUtils;

    private final SystemConfigRepository systemConfigRepository;

    private final String TEMPLATE_PATH = "src/main/resources/templates/import-groups.xlsx";

    private final GoogleDriveManager googleDriveManager;

    private final MessageRepository messageRepository;

    private final MessageService messageService;

    private final SocketIOService socketIOService;

    private final NotificationService notificationService;

    public GroupService(GroupRepository groupRepository,
                        GroupCategoryRepository groupCategoryRepository,
                        UserRepository userRepository,
                        UserService userService,
                        MailService mailService,
                        PermissionService permissionService,
                        MongoTemplate mongoTemplate,
                        SystemConfigRepository systemConfigRepository,
                        GoogleDriveManager googleDriveManager,
                        MessageRepository messageRepository,
                        MessageService messageService,
                        SocketIOService socketIOService,
                        NotificationService notificationService) {
        this.groupRepository = groupRepository;
        this.groupCategoryRepository = groupCategoryRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.mailService = mailService;
        this.permissionService = permissionService;
        this.mongoTemplate = mongoTemplate;
        this.mailUtils = new MailUtils(systemConfigRepository);
        this.systemConfigRepository = systemConfigRepository;
        this.googleDriveManager = googleDriveManager;
        this.messageRepository = messageRepository;
        this.messageService = messageService;
        this.socketIOService = socketIOService;
        this.notificationService = notificationService;
    }

    public Page<GroupHomepageResponse> findOwnGroups(String userId, int page, int pageSize) {
        Pageable pageRequest = PageRequest.of(page, pageSize);
        List<String> mentorIds = Collections.singletonList(userId);
        List<String> menteeIds = Collections.singletonList(userId);
        Slice<Group> wrapper = groupRepository.findByMentorsInAndStatusOrMenteesInAndStatus(
                mentorIds,
                Group.Status.ACTIVE,
                menteeIds,
                Group.Status.ACTIVE,
                pageRequest
        );
        List<GroupHomepageResponse> groups = mappingGroupHomepageResponse(wrapper.getContent(), userId);
        return new PageImpl<>(groups, pageRequest, wrapper.getNumberOfElements());
    }

    private List<GroupHomepageResponse> mappingGroupHomepageResponse(List<Group> groups, String userId) {
        Map<String, GroupCategory> categories = groupCategoryRepository.findAll()
                .stream()
                .collect(Collectors.toMap(GroupCategory::getId, category -> category, (cat1, cat2) -> cat2));
        return groups.stream().map(group -> {
                    String role = group.isMentee(userId) ? "MENTEE" : "MENTOR";
                    GroupCategory category = categories.get(group.getGroupCategory());
                    String imageUrl = (group.getImageUrl() == null)
                            ? category.getIconUrl()
                            : group.getImageUrl();
                    GroupHomepageResponse response = new GroupHomepageResponse(group, category.getName(), role);
                    response.setImageUrl(imageUrl);
                    return response;
                })
                .sorted(Comparator.comparing(GroupHomepageResponse::getUpdatedDate).reversed())
                .collect(Collectors.toList());
    }

    public List<Group> getAllActiveOwnGroups(String userId) {
        List<String> mentorIds = Collections.singletonList(userId);
        List<String> menteeIds = Collections.singletonList(userId);
        return groupRepository.findByMentorsInAndStatusOrMenteesInAndStatus(
                mentorIds,
                Group.Status.ACTIVE,
                menteeIds,
                Group.Status.ACTIVE
        );
    }

    public Page<GroupHomepageResponse> findMentorGroups(String userId, int page, int pageSize) {
        Pageable pageRequest = PageRequest.of(page, pageSize);
        List<String> mentorIds = Collections.singletonList(userId);
        Page<Group> wrapper = groupRepository.findAllByMentorsInAndStatus(mentorIds, Group.Status.ACTIVE, pageRequest);
        List<GroupHomepageResponse> groups = mappingGroupHomepageResponse(wrapper.getContent(), userId);
        return new PageImpl<>(groups, pageRequest, wrapper.getNumberOfElements());
    }

    public Page<GroupHomepageResponse> findMenteeGroups(String userId, int page, int pageSize) {
        Pageable pageRequest = PageRequest.of(page, pageSize);
        List<String> menteeIds = Collections.singletonList(userId);
        Page<Group> wrapper = groupRepository.findAllByMenteesInAndStatus(menteeIds, Group.Status.ACTIVE, pageRequest);
        List<GroupHomepageResponse> groups = mappingGroupHomepageResponse(wrapper.getContent(), userId);
        return new PageImpl<>(groups, pageRequest, wrapper.getNumberOfElements());
    }

    public Page<Group> findRecentGroupsOfUser(String userId, int page, int pageSize) {
        List<String> mentorIds = Collections.singletonList(userId);
        List<String> menteeIds = Collections.singletonList(userId);
        Pageable pageRequest = PageRequest.of(page, pageSize,
                Sort.by("updatedDate").descending());
        return groupRepository.findAllByMentorsInOrMenteesIn(
                mentorIds, menteeIds, pageRequest);
    }

    public Slice<Group> findMostRecentGroupsOfUser(String userId, int page, int pageSize) {
        List<String> mentorIds = Collections.singletonList(userId);
        List<String> menteeIds = Collections.singletonList(userId);
        Pageable pageRequest = PageRequest.of(page, pageSize,
                Sort.by("updatedDate").descending());
        return groupRepository.findByMentorsInAndStatusOrMenteesInAndStatus(
                mentorIds, Group.Status.ACTIVE, menteeIds, Group.Status.ACTIVE, pageRequest);
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

    private GroupReturnService validateTimeRange(Date timeStart, Date timeEnd) {
        int maxYearsBetweenTimeStartAndTimeEnd = Integer.parseInt(systemConfigRepository.findByKey("valid_max_year").getValue().toString());
        int maxYearsBetweenTimeStartAndNow = 4;
        LocalDate localTimeStart = timeStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate localTimeEnd = timeEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate localNow = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (timeEnd.before(timeStart) || timeEnd.equals(timeStart)) {
            return new GroupReturnService(TIME_END_BEFORE_TIME_START, "Time end can't be before time start", null);
        }
        if (timeEnd.before(new Date()) || timeEnd.equals(new Date())) {
            return new GroupReturnService(TIME_END_BEFORE_NOW, "Time end can't be before now", null);
        }
        if (ChronoUnit.YEARS.between(localTimeStart, localTimeEnd) > maxYearsBetweenTimeStartAndTimeEnd) {
            return new GroupReturnService(TIME_END_TOO_FAR_FROM_TIME_START, "Time end is too far from time start", null);
        }
        if (Math.abs(ChronoUnit.YEARS.between(localTimeStart, localNow)) > maxYearsBetweenTimeStartAndNow) {
            return new GroupReturnService(TIME_START_TOO_FAR_FROM_NOW, "Time start is too far from now", null);
        }
        return new GroupReturnService(SUCCESS, "", null);
    }

    private GroupReturnService validateTimeRangeForUpdate(Date timeStart, Date timeEnd) {
        int maxYearsBetweenTimeStartAndTimeEnd = 7;
        int maxYearsBetweenTimeStartAndNow = 4;
        LocalDate localTimeStart = timeStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate localTimeEnd = timeEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate localNow = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (timeEnd.before(timeStart) || timeEnd.equals(timeStart)) {
            return new GroupReturnService(TIME_END_BEFORE_TIME_START, "Time end can't be before time start", null);
        }
        if (ChronoUnit.YEARS.between(localTimeStart, localTimeEnd) > maxYearsBetweenTimeStartAndTimeEnd) {
            return new GroupReturnService(TIME_END_TOO_FAR_FROM_TIME_START, "Time end is too far from time start", null);
        }
        if (Math.abs(ChronoUnit.YEARS.between(localTimeStart, localNow)) > maxYearsBetweenTimeStartAndNow) {
            return new GroupReturnService(TIME_START_TOO_FAR_FROM_NOW, "Time start is too far from now", null);
        }
        return new GroupReturnService(SUCCESS, "", null);
    }

    private List<String> validateInvalidMails(List<String> mentors, List<String> mentees) {
        return Stream.concat(mentors.stream(), mentees.stream())
                .filter(email -> !MailUtils.isValidEmail(email))
                .collect(Collectors.toList());
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
                .collect(Collectors.toList());
    }

    private GroupReturnService validateListMentorsMentees(List<String> mentors, List<String> mentees) {
        List<String> invalidEmails = validateInvalidMails(mentors, mentees);
        if (!invalidEmails.isEmpty()) {
            return new GroupReturnService(INVALID_EMAILS, "Invalid emails", invalidEmails);
        }
        invalidEmails = validateDomainMails(mentors, mentees);
        if (!invalidEmails.isEmpty()) {
            return new GroupReturnService(INVALID_DOMAINS, "Invalid domains", invalidEmails);
        }
        invalidEmails = validateDuplicatedMails(mentors, mentees);
        if (!invalidEmails.isEmpty()) {
            return new GroupReturnService(DUPLICATE_EMAIL, "Duplicate emails", invalidEmails);
        }
        return new GroupReturnService(SUCCESS, "", null);
    }

    public GroupReturnService createNewGroup(String emailUser, CreateGroupRequest request) {
        if (!permissionService.isAdmin(emailUser)) {
            return new GroupReturnService(INVALID_PERMISSION, "Invalid permission", null);
        }
        if (request.getName() == null
                || request.getName().isEmpty()
                || request.getTimeStart() == null
                || request.getTimeEnd() == null) {
            return new GroupReturnService(NOT_ENOUGH_FIELDS, "Not enough required fields", null);
        }
        GroupReturnService isValidTimeRange = validateTimeRange(request.getTimeStart(), request.getTimeEnd());
        if (isValidTimeRange.getReturnCode() != SUCCESS) {
            return isValidTimeRange;
        }
        if (groupRepository.existsByName(request.getName())) {
            return new GroupReturnService(DUPLICATE_GROUP, "Group name has been duplicated", null);
        }
        if (!groupCategoryRepository.existsById(request.getGroupCategory())) {
            return new GroupReturnService(GROUP_CATEGORY_NOT_FOUND, "Group category not exists", null);
        }

        List<String> menteeEmails = request.getMenteeEmails();
        List<String> mentorEmails = request.getMentorEmails();
        GroupReturnService isValidEmails = validateListMentorsMentees(mentorEmails, menteeEmails);
        if (!isValidEmails.getReturnCode().equals(SUCCESS)) {
            return isValidEmails;
        }

        List<String> menteeIds = menteeEmails.stream()
                .filter(email -> !email.isEmpty())
                .map(email -> userService.importUser(email, request.getName()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());


        List<String> mentorIds = mentorEmails.stream()
                .filter(email -> !email.isEmpty())
                .map(email -> userService.importUser(email, request.getName()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        Date timeStart = changeGroupTime(request.getTimeStart(), "START");
        Date timeEnd = changeGroupTime(request.getTimeEnd(), "END");
        Duration duration = calculateDuration(timeStart, timeEnd);
        Group.Status status = getStatusFromTimeStartAndTimeEnd(timeStart, timeEnd);
        Optional<User> userOptional = userRepository.findByEmail(emailUser);
        String creatorId = null;
        if(userOptional.isPresent()){
            creatorId = userOptional.get().getId();
        }
        Group group = Group.builder()
                .name(request.getName())
                .description(request.getDescription())
                .mentees(menteeIds)
                .mentors(mentorIds)
                .groupCategory(request.getGroupCategory())
                .status(status)
                .timeStart(timeStart)
                .timeEnd(timeEnd)
                .duration(duration)
                .creatorId(creatorId)
                .build();
        groupRepository.save(group);
        menteeEmails.stream().forEach(email -> mailService.sendInvitationMail(email, group));
        mentorEmails.stream().forEach(email -> mailService.sendInvitationMail(email, group));

        return new GroupReturnService(SUCCESS, null, group);
    }

    //    public Group addMentee(String groupId, AddMenteeRequest request) {
//        Optional<Group> groupWrapper = groupRepository.findById(groupId);
//        if (!groupWrapper.isPresent()) {
//            return null;
//        }
//        Group group = groupWrapper.get();
//        String email = request.getEmail();
//        String menteeId = userService.getOrCreateUserByEmail(email);
//        group.addMentee(menteeId);
//        return groupRepository.save(group);
//    }
    private Duration calculateDuration(Date from, Date to) {
        return Duration.between(from.toInstant(), to.toInstant());
    }

    private static Date changeGroupTime(Date time, String type) {
        LocalDateTime timeInstant = time.toInstant().atZone(ZoneOffset.systemDefault()).toLocalDateTime();

        if (type.equals("START")) {
            timeInstant = timeInstant.withHour(0).withMinute(0);
        } else {
            timeInstant = timeInstant.withHour(23).withMinute(59);
        }

        ZonedDateTime zonedDateTime = timeInstant.atZone(ZoneId.of("UTC"));
        Instant instant = zonedDateTime.toInstant();

        return Date.from(instant);
    }
    private Group.Status getStatusFromTimeStartAndTimeEnd(Date timeStart, Date timeEnd) {
        Date now = new Date();
        if (timeStart.before(now) && timeEnd.before(now)) {
            return Group.Status.OUTDATED;
        }
        if (timeStart.before(now) && timeEnd.after(now)) {
            return Group.Status.ACTIVE;
        }
        return Group.Status.INACTIVE;
    }

    private void removeBlankRows(Sheet sheet) {
        int lastRowNum = sheet.getLastRowNum();
        for (int i = lastRowNum; i >= 0; i--) {
            Row row = sheet.getRow(i);
            if (row == null || row.getCell(0) == null || row.getCell(0).getCellType() == org.apache.poi.ss.usermodel.CellType.BLANK) {
                sheet.removeRow(row);
            }
        }
    }

    private Boolean isValidTemplate(Workbook workbook) {
        int numberOfSheetInTemplate = 2;
        if (workbook.getNumberOfSheets() != numberOfSheetInTemplate) {
            return false;
        }
        Sheet sheet = workbook.getSheet("Data");
        if(sheet == null) {
            return false;
        }

        Row row = sheet.getRow(0);
        if (!isValidHeader(row)) {
            return false;
        }
        return true;
    }

    private Boolean isValidHeader(Row row){
        return (row.getCell(0).getStringCellValue().equals("STT")
                && row.getCell(1).getStringCellValue().equals("Loại nhóm *")
                && row.getCell(2).getStringCellValue().equals("Emails người được quản lí *")
                && row.getCell(3).getStringCellValue().equals("Tên nhóm *")
                && row.getCell(4).getStringCellValue().equals("Mô tả")
                && row.getCell(5).getStringCellValue().equals("Emails người quản lí *")
                && row.getCell(6).getStringCellValue().equals("Ngày bắt đầu *\n" + "(dd/MM/YYYY)")
                && row.getCell(7).getStringCellValue().equals("Ngày kết thúc *\n" + "(dd/MM/YYYY)"));
    }

    public GroupReturnService readGroups(Workbook workbook) throws ParseException {
        Map<String, Group> groups = new HashMap<>();
        Sheet sheet = workbook.getSheet("Data");
        removeBlankRows(sheet);
        String groupCategoryName = "";
        List<String> menteeEmails = new ArrayList<>();
        List<String> mentorEmails = new ArrayList<>();
        String menteeEmail;
        String groupName = "";
        String description = "";
        Date timeStart = new Date();
        Date timeEnd = new Date();
        for(int i = 0; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (i == 0) {
                continue;
            }
            if(!row.getCell(3).getStringCellValue().isEmpty()){
                groupCategoryName = row.getCell(1).getStringCellValue();
                menteeEmails = new ArrayList<>();
                menteeEmail = row.getCell(2).getStringCellValue();
                menteeEmails.add(menteeEmail);
                groupName = row.getCell(3).getStringCellValue();
                description = row.getCell(4).getStringCellValue();
                mentorEmails = Arrays.stream(row.getCell(5).getStringCellValue()
                                .split("\n"))
                                .collect(Collectors.toList());
                DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
                timeStart = formatter.parse(row.getCell(6).getStringCellValue());
                timeEnd = formatter.parse(row.getCell(7).getStringCellValue());
            }

            Boolean isAddMentee = false;
            while(i <= sheet.getLastRowNum() && row.getCell(3).getStringCellValue().isEmpty()){
                isAddMentee  = true;
                menteeEmail = row.getCell(2).getStringCellValue();
                menteeEmails.add(menteeEmail);
                row = sheet.getRow(++i);
            }
            if(isAddMentee){
                GroupReturnService isValidTimeRange = validateTimeRange(timeStart, timeEnd);
                if (isValidTimeRange.getReturnCode() != SUCCESS) {
                    return isValidTimeRange;
                }
                if (!groupCategoryRepository.existsByName(groupCategoryName)) {
                    return new GroupReturnService(GROUP_CATEGORY_NOT_FOUND, "Group category not exists", groupCategoryName);
                }
                String groupCategoryId = groupCategoryRepository.findByName(groupCategoryName).getId();
                if (!groups.containsKey(groupName) && !groupRepository.existsByName(groupName)) {
                    Group group = Group.builder()
                            .name(groupName)
                            .description(description)
                            .createdDate(new Date())
                            .mentees(menteeEmails)
                            .mentors(mentorEmails)
                            .groupCategory(groupCategoryId)
                            .timeStart(timeStart)
                            .timeEnd(timeEnd)
                            .build();
                    groups.put(groupName, group);
                } else {
                    return new GroupReturnService(DUPLICATE_GROUP, "Group name has been duplicated", groupName);
                }
                i--;
            }
        }
        return new GroupReturnService(SUCCESS, "", groups);
    }
    public GroupReturnService importGroups(String emailUser, MultipartFile file) throws IOException {
        if (!permissionService.isAdmin(emailUser)) {
            return new GroupReturnService(INVALID_PERMISSION, "Invalid permission", null);
        }
        Map<String, Group> groups;
        try (InputStream data = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(data)
        ) {
            if (!isValidTemplate(workbook)) {
                return new GroupReturnService(INVALID_TEMPLATE, "Invalid template", null);
            }
            GroupReturnService validReadGroups = readGroups(workbook);
            if (validReadGroups.getReturnCode() != SUCCESS) {
                return validReadGroups;
            }
            groups = (Map<String, Group>) validReadGroups.getData();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        for (Group group : groups.values()) {
            GroupReturnService isValidMails = validateListMentorsMentees(group.getMentors(), group.getMentees());
            if (isValidMails.getReturnCode() != SUCCESS) {
                return isValidMails;
            }
        }

        List<CreateGroupRequest> createGroupRequests = groups.values().stream()
                .map(group -> CreateGroupRequest.builder()
                        .name(group.getName())
                        .createdDate(new Date())
                        .menteeEmails(group.getMentees())
                        .mentorEmails(group.getMentors())
                        .groupCategory(group.getGroupCategory())
                        .timeStart(group.getTimeStart())
                        .timeEnd(group.getTimeEnd())
                        .build())
                .collect(Collectors.toList());
        for (CreateGroupRequest createGroupRequest : createGroupRequests) {
            GroupReturnService returnData = createNewGroup(emailUser, createGroupRequest);
            if (returnData.getReturnCode() != SUCCESS) {
                return returnData;
            }
        }
        for (Group group : groups.values()) {
            group.setId(groupRepository.findByName(group.getName()).getId());
            group.setDuration(groupRepository.findByName(group.getName()).getDuration());
            group.setMentors(groupRepository.findByName(group.getName()).getMentors());
            group.setMentees(groupRepository.findByName(group.getName()).getMentees());
        }

        return new GroupReturnService(SUCCESS, null, groups.values());
    }

    public List<Group> validateTimeGroups(List<Group> groups) {
        for (Group group : groups) {
            if(group.getStatus() != Group.Status.DELETED && group.getStatus() != Group.Status.DISABLED) {
                if (group.getTimeEnd().before(new Date())) {
                    group.setStatus(Group.Status.OUTDATED);
                }
                if (group.getTimeStart().after(new Date())) {
                    group.setStatus(Group.Status.INACTIVE);
                }
            }
            groupRepository.save(group);
        }
        return groups;
    }

    public GroupReturnService findGroups(
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
            int page, int pageSize) {
        if (!permissionService.isAdmin(emailUser)) {
            return new GroupReturnService(INVALID_PERMISSION, "Invalid permission", null);
        }
        Pair<Long, List<Group>> groups = getGroupsByConditions(emailUser, name, mentorEmail, menteeEmail, groupCategory, timeStart1, timeEnd1, timeStart2, timeEnd2, status, page, pageSize);
        return new GroupReturnService(SUCCESS, "", new PageImpl<>(groups.getValue(), PageRequest.of(page, pageSize), groups.getKey()));
    }

    private Pair<Long, List<Group>> getGroupsByConditions(String emailUser,
                                                             String name,
                                                             String mentorEmail,
                                                             String menteeEmail,
                                                             String groupCategory,
                                                             Date timeStart1,
                                                             Date timeEnd1,
                                                             Date timeStart2,
                                                             Date timeEnd2,
                                                             String status,
                                                             int page, int pageSize){
        Query query = new Query();
        if (name != null && !name.isEmpty()) {
            query.addCriteria(Criteria.where("name").regex(name, "i"));
        }

        if (menteeEmail != null && !menteeEmail.isEmpty()) {
            User mentee = userService.findByEmail(menteeEmail);
            String menteeId = "";
            if (mentee != null) {
                menteeId = mentee.getId();
            }
            query.addCriteria(Criteria.where("mentees").in(menteeId));
        }

        if (mentorEmail != null && !mentorEmail.isEmpty()) {
            User mentor = userService.findByEmail(mentorEmail);
            String mentorId = "";
            if (mentor != null) {
                mentorId = mentor.getId();
            }
            query.addCriteria(Criteria.where("mentors").in(mentorId));
        }

        if (groupCategory != null && !groupCategory.isEmpty()) {
            query.addCriteria(Criteria.where("groupCategory").is(groupCategory));
        }

        if (status != null) {
            query.addCriteria(Criteria.where("status").is(status));
        }

        if (timeStart1 != null && timeEnd1 != null) {
            query.addCriteria(Criteria.where("timeStart").gte(timeStart1).lte(timeEnd1));
        }

        if (timeStart2 != null && timeEnd2 != null) {
            query.addCriteria(Criteria.where("timeEnd").gte(timeStart2).lte(timeEnd2));
        }
        if (!permissionService.isSuperAdmin(emailUser)) {
            Optional<User> userOptional = userRepository.findByEmail(emailUser);
            String userId = null;
            if(userOptional.isPresent()){
                userId = userOptional.get().getId();
            }
            query.addCriteria(Criteria.where("creatorId").is(userId));
        }
        query.with(Sort.by(Sort.Direction.DESC, "createdDate"));

        long count = mongoTemplate.count(query, Group.class);
        query.with(PageRequest.of(page, pageSize));

        List<Group> data = mongoTemplate.find(query, Group.class);
        data = validateTimeGroups(data);
        return new Pair<>(count, data);
    }

    public GroupReturnService addMentees(String emailUser, String groupId, AddMenteesRequest request) {
        if (!permissionService.isAdmin(emailUser)) {
            return new GroupReturnService(INVALID_PERMISSION, "Invalid permission", null);
        }
        Optional<Group> groupWrapper = groupRepository.findById(groupId);
        if (!groupWrapper.isPresent()) {
            return new GroupReturnService(NOT_FOUND, "Group not found", null);
        }
        Group group = groupWrapper.get();
        List<String> emails = request.getEmails();
        List<String> ids = emails.stream().
                filter(email -> !email.isEmpty()).
                map(email -> userService.getOrCreateUserByEmail(email, group.getName())).
                collect(Collectors.toList());

        if (hasDuplicateElements(ids, group.getMentees()) || hasDuplicateElements(ids, group.getMentors())) {
            return new GroupReturnService(DUPLICATE_EMAIL, "Duplicate emails", null);
        }
        group.getMentees().addAll(ids);
        List<String> listMenteesAfterRemoveDuplicate = new ArrayList<>(new HashSet<>(group.getMentees()));
        group.setMentees(listMenteesAfterRemoveDuplicate);
        groupRepository.save(group);
        for (String emailAddress : emails) {
            mailService.sendInvitationMail(emailAddress, group);
        }

        return new GroupReturnService(SUCCESS, null, group);
    }

    public GroupReturnService addMentors(String emailUser, String groupId, AddMentorsRequest request) {
        if (!permissionService.hasPermissionOnGroup(emailUser, groupId)) {
            return new GroupReturnService(INVALID_PERMISSION, "Invalid permission", null);
        }
        Optional<Group> groupWrapper = groupRepository.findById(groupId);
        if (!groupWrapper.isPresent()) {
            return new GroupReturnService(NOT_FOUND, "Group not found", null);
        }
        Group group = groupWrapper.get();

        List<String> emails = request.getEmails();
        List<String> ids = emails.stream().
                filter(email -> !email.isEmpty()).
                map(email -> userService.getOrCreateUserByEmail(email, group.getName())).
                collect(Collectors.toList());

        if (hasDuplicateElements(ids, group.getMentees()) || hasDuplicateElements(ids, group.getMentors())) {
            return new GroupReturnService(DUPLICATE_EMAIL, "Duplicate emails", null);
        }

        group.getMentors().addAll(ids);
        List<String> listMentorsAfterRemoveDuplicate = new ArrayList<>(new HashSet<>(group.getMentors()));
        group.setMentors(listMentorsAfterRemoveDuplicate);

        groupRepository.save(group);
        for (String emailAddress : emails) {
            mailService.sendInvitationMail(emailAddress, group);
        }

        return new GroupReturnService(SUCCESS, null, group);
    }

    public GroupReturnService deleteMentee(String emailUser, String groupId, String menteeId) {
        if (!permissionService.hasPermissionOnGroup(emailUser, groupId)) {
            return new GroupReturnService(INVALID_PERMISSION, "Invalid permission", null);
        }
        Optional<Group> groupWrapper = groupRepository.findById(groupId);
        if (!groupWrapper.isPresent()) {
            return new GroupReturnService(NOT_FOUND, "Group not found", null);
        }
        Group group = groupWrapper.get();

        if (group.getMentees().remove(menteeId)) {
            group.setMentees(group.getMentees());
            groupRepository.save(group);
            return new GroupReturnService(SUCCESS, null, group);
        }
        return new GroupReturnService(MENTEE_NOT_FOUND, "Mentee not found", null);
    }

    public GroupReturnService deleteMentor(String emailUser, String groupId, String mentorId) {
        if (!permissionService.hasPermissionOnGroup(emailUser, groupId)) {
            return new GroupReturnService(INVALID_PERMISSION, "Invalid permission", null);
        }
        Optional<Group> groupWrapper = groupRepository.findById(groupId);
        if (!groupWrapper.isPresent()) {
            return new GroupReturnService(NOT_FOUND, "Group not found", null);
        }
        Group group = groupWrapper.get();

        if (group.getMentors().remove(mentorId)) {
            group.setMentors(group.getMentors());
            groupRepository.save(group);
            return new GroupReturnService(SUCCESS, null, group);
        }

        return new GroupReturnService(MENTOR_NOT_FOUND, "mentor not found", null);

    }

    public GroupReturnService promoteToMentor(String emailUser, String groupId, String menteeId) {
        if (!permissionService.hasPermissionOnGroup(emailUser, groupId)) {
            return new GroupReturnService(INVALID_PERMISSION, "Invalid permission", null);
        }
        Optional<Group> groupWrapper = groupRepository.findById(groupId);
        if (!groupWrapper.isPresent()) {
            return new GroupReturnService(NOT_FOUND, "Group not found", null);
        }
        Group group = groupWrapper.get();

        if (group.getMentees().remove(menteeId)) {
            group.getMentors().add(menteeId);
            groupRepository.save(group);
            return new GroupReturnService(SUCCESS, null, group);
        }
        return new GroupReturnService(MENTEE_NOT_FOUND, "Mentee not found", null);
    }

    public GroupReturnService demoteToMentee(String emailUser, String groupId, String mentorId) {
        if (!permissionService.hasPermissionOnGroup(emailUser, groupId)) {
            return new GroupReturnService(INVALID_PERMISSION, "Invalid permission", null);
        }
        Optional<Group> groupWrapper = groupRepository.findById(groupId);
        if (!groupWrapper.isPresent()) {
            return new GroupReturnService(NOT_FOUND, "Group not found", null);
        }
        Group group = groupWrapper.get();

        if (group.getMentors().remove(mentorId)) {
            group.getMentees().add(mentorId);
            groupRepository.save(group);
            return new GroupReturnService(SUCCESS, null, group);
        }
        return new GroupReturnService(MENTOR_NOT_FOUND, "Mentor not found", null);
    }

    private void clearValidation(Sheet sheet) {
        for (Row row : sheet) {
            for (Cell cell : row) {
                cell.removeCellComment();
                cell.removeHyperlink();
                cell.removeFormula();
                cell.setCellType(CellType.BLANK);
            }
        }
    }

    public void loadTemplate(File file) throws Exception {
        int lastRow = 10000;
        List<GroupCategory> groupCategories = groupCategoryRepository.findAllByStatus(GroupCategory.Status.ACTIVE);
        String[] groupCategoryNames = groupCategories.stream().
                map(groupCategory -> groupCategory.getName()).
                collect(Collectors.toList()).
                toArray(new String[0]);
        FileInputStream inputStream = new FileInputStream(file);
        Workbook workbook = WorkbookFactory.create(inputStream);

        Sheet dataSheet = workbook.getSheet("Data");
        clearSheet(dataSheet, 1, dataSheet.getLastRowNum());

        DataValidationHelper validationHelper = new XSSFDataValidationHelper((XSSFSheet) dataSheet);
        CellRangeAddressList addressList = new CellRangeAddressList(1, lastRow,1,1);
        DataValidationConstraint constraintCategory =validationHelper.createExplicitListConstraint(groupCategoryNames);

        DataValidation dataValidationCategory = validationHelper.createValidation(constraintCategory, addressList);
        dataValidationCategory.setSuppressDropDownArrow(true);
        dataValidationCategory.setEmptyCellAllowed(false);
        dataSheet.addValidationData(dataValidationCategory);

//        XSSFDataValidationConstraint validationConstraintDate = (XSSFDataValidationConstraint)dvHelper.createDateConstraint(
//                DataValidationConstraint.OperatorType.BETWEEN,
//                "01/01/1970",
//                "31/12/2200",
//                "dd/mm/yyyy"
//        );
//        CellRangeAddressList addressListDate = new CellRangeAddressList(1, lastRow, 4, 5);
//        DataValidation dataValidationDate = dvHelper.createValidation(
//                validationConstraintDate, addressListDate);
//
//        dataValidationDate.setShowErrorBox(true);
//        dataValidationDate.setSuppressDropDownArrow(true);
//        dataValidationDate.setEmptyCellAllowed(false);

//        groupsSheet.addValidationData(dataValidationDate);
        FileOutputStream outputStream = new FileOutputStream(file);
        workbook.write(outputStream);
        workbook.close();
        inputStream.close();
        outputStream.close();
    }

    private static void clearSheet(Sheet sheet, int firstRow, int lastRow) {
        for (int i = firstRow; i <= lastRow; i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                sheet.removeRow(row);
            }
        }
    }

    public GroupReturnService updateGroup(String emailUser, String groupId, UpdateGroupRequest request) {
        if (!permissionService.hasPermissionOnGroup(emailUser, groupId)) {
            return new GroupReturnService(INVALID_PERMISSION, "Invalid permission", null);
        }
        Optional<Group> groupWrapper = groupRepository.findById(groupId);
        if (!groupWrapper.isPresent()) {
            return new GroupReturnService(NOT_FOUND, "Group not found", null);
        }
        Group group = groupWrapper.get();

        if (groupRepository.existsByName(request.getName()) && !request.getName().equals(group.getName())) {
            return new GroupReturnService(DUPLICATE_GROUP, "Group name has been duplicated", null);
        }
        Date timeStart = changeGroupTime(request.getTimeStart(), "START");
        Date timeEnd = changeGroupTime(request.getTimeEnd(), "END");
        group.update(request.getName(), request.getDescription(), request.getStatus(), timeStart, timeEnd, request.getGroupCategory());
        if (!groupCategoryRepository.existsById(group.getGroupCategory())) {
            return new GroupReturnService(GROUP_CATEGORY_NOT_FOUND, "Group category not exists", null);
        }

        GroupReturnService isValidTimeRange = validateTimeRangeForUpdate(group.getTimeStart(), group.getTimeEnd());
        if (isValidTimeRange.getReturnCode() != SUCCESS) {
            return isValidTimeRange;
        }
        Duration duration = calculateDuration(group.getTimeStart(), group.getTimeEnd());
        group.setDuration(duration);
        Group.Status status = getStatusFromTimeStartAndTimeEnd(group.getTimeStart(), group.getTimeEnd());
        group.setStatus(status);
        if (request.getStatus().equals(Group.Status.DISABLED)) {
            group.setStatus(Group.Status.DISABLED);
        }
        group.setUpdatedDate(new Date());
        groupRepository.save(group);
        return new GroupReturnService(SUCCESS, null, group);
    }

    public GroupReturnService deleteGroup(String emailUser, String groupId) {
        if (!permissionService.hasPermissionOnGroup(emailUser, groupId)) {
            return new GroupReturnService(INVALID_PERMISSION, "Invalid permission", null);
        }
        Optional<Group> groupWrapper = groupRepository.findById(groupId);
        if (!groupWrapper.isPresent()) {
            return new GroupReturnService(NOT_FOUND, "Group not found", null);
        }
        Group group = groupWrapper.get();
        group.setStatus(Group.Status.DELETED);
        groupRepository.save(group);
        return new GroupReturnService(SUCCESS, null, group);
    }

    public List<GroupHomepageResponse> getUserPinnedGroups(String userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return new ArrayList<>();
        }
        List<Group> groups = groupRepository.findByIdIn(user.getPinnedGroupsId());
        return mappingGroupHomepageResponse(groups, userId);
    }

    public boolean isGroupMember(String groupId, String userId) {
        Optional<Group> wrapper = groupRepository.findById(groupId);
        if (!wrapper.isPresent()) {
            return false;
        }
        Group group = wrapper.get();
        return group.getMentors().contains(userId) || group.getMentees().contains(userId);
    }

    public Slice<GroupHomepageResponse> getHomePageRecentGroupsOfUser(String userId, int page, int pageSize) {
        Slice<Group> groups = findMostRecentGroupsOfUser(userId, page, pageSize);
        List<GroupHomepageResponse> responses = mappingGroupHomepageResponse(groups.getContent(), userId);
        return new SliceImpl<>(responses, PageRequest.of(page, pageSize), groups.hasNext());
    }

    public GroupReturnService deleteMultiple(String emailUser, List<String> ids) {
        if(!permissionService.isAdmin(emailUser)){
            return new GroupReturnService(INVALID_PERMISSION, "Invalid permission", null);
        }
        List<String> notFoundIds = new ArrayList<>();
        for(String id: ids){
            Optional<Group> groupOptional = groupRepository.findById(id);
            if (!groupOptional.isPresent()) {
                notFoundIds.add(id);
            }
        }

        if (!notFoundIds.isEmpty()) {
            return new GroupReturnService(NOT_FOUND, "Group not found", notFoundIds);
        }
        List<Group> groups = groupRepository.findByIdIn(ids);
        groups.forEach(group -> group.setStatus(Group.Status.DELETED));
        groupRepository.saveAll(groups);
        return new GroupReturnService(SUCCESS, null, groups);
    }

    public GroupReturnService disableMultiple(String emailUser, List<String> ids) {
        if(!permissionService.isAdmin(emailUser)){
            return new GroupReturnService(INVALID_PERMISSION, "Invalid permission", null);
        }
        List<String> notFoundIds = new ArrayList<>();
        for(String id: ids){
            Optional<Group> groupOptional = groupRepository.findById(id);
            if (!groupOptional.isPresent()) {
                notFoundIds.add(id);
            }
        }

        if (!notFoundIds.isEmpty()) {
            return new GroupReturnService(NOT_FOUND, "Group not found", notFoundIds);
        }
        List<Group> groups = groupRepository.findByIdIn(ids);
        for(Group group: groups){
            group.setStatus(Group.Status.DISABLED);
            groupRepository.save(group);
        }

        return new GroupReturnService(SUCCESS, null, groups);
    }

    public GroupReturnService enableMultiple(String emailUser, List<String> ids) {
        if(!permissionService.isAdmin(emailUser)){
            return new GroupReturnService(INVALID_PERMISSION, "Invalid permission", null);
        }
        List<String> notFoundIds = new ArrayList<>();
        for(String id: ids){
            Optional<Group> groupOptional = groupRepository.findById(id);
            if (!groupOptional.isPresent()) {
                notFoundIds.add(id);
            }
        }

        if (!notFoundIds.isEmpty()) {
            return new GroupReturnService(NOT_FOUND, "Group not found", notFoundIds);
        }
        List<Group> groups = groupRepository.findByIdIn(ids);
        for(Group group: groups){
            Group.Status status = getStatusFromTimeStartAndTimeEnd(group.getTimeStart(), group.getTimeEnd());
            group.setStatus(status);
            groupRepository.save(group);
        }

        return new GroupReturnService(SUCCESS, null, groups);
    }

    public GroupReturnService getGroupMembers(String groupId, String userId) {
        if (!permissionService.isUserIdInGroup(userId, groupId)) {
            return new GroupReturnService(INVALID_PERMISSION, "Invalid permission", null);
        }
        Optional<Group> groupWrapper = groupRepository.findById(groupId);
        if (!groupWrapper.isPresent()) {
            return new GroupReturnService(NOT_FOUND, "Group not found", null);
        }
        Group group = groupWrapper.get();

        List<ProfileResponse> mentors = userRepository.findAllByIdIn(group.getMentors())
                .stream().map(ProfileResponse::normalize).collect(Collectors.toList());
        List<ProfileResponse> mentees = userRepository.findAllByIdIn(group.getMentees())
                .stream().map(ProfileResponse::normalize).collect(Collectors.toList());
        GroupMembersResponse response = GroupMembersResponse.builder()
               .mentors(mentors).mentees(mentees).build();
        return new GroupReturnService(SUCCESS, null, response);
    }

    public void pinGroup(String userId, String groupId) {
        Optional<User> userWrapper = userRepository.findById(userId);
        if (!userWrapper.isPresent()) {
            return;
        }
        User user = userWrapper.get();
        user.pinGroup(groupId);
        userRepository.save(user);
    }

    public void unpinGroup(String userId, String groupId) {
        Optional<User> userWrapper = userRepository.findById(userId);
        if (!userWrapper.isPresent()) {
            return;
        }
        User user = userWrapper.get();
        user.unpinGroup(groupId);
        userRepository.save(user);
    }

    public GroupReturnService getGroupDetail(String userId, String groupId) {
        if (!permissionService.isUserIdInGroup(userId, groupId)) {
            return new GroupReturnService(INVALID_PERMISSION, "Invalid permission", null);
        }
        List<GroupDetailResponse> groupWrapper = groupRepository.getGroupDetail(groupId);
        if (groupWrapper.size() == 0) {
            return new GroupReturnService(NOT_FOUND, "Group not found", null);
        }
        GroupDetailResponse response = groupWrapper.get(0);
        response.setRole(userId);

        Optional<User> userWrapper = userRepository.findById(userId);
        if (userWrapper.isPresent()){
            User user = userWrapper.get();
            response.setPinned(user.isPinnedGroup(groupId));
        }
        GroupCategory groupCategory = groupCategoryRepository.findByName(response.getGroupCategory());
        response.setPermissions(groupCategory.getPermissions());

        List<MessageResponse> messages = new ArrayList<>();
        if (response.getPinnedMessageIds() != null && response.getPinnedMessageIds().size() != 0) {
            messages = response.getPinnedMessageIds().stream()
                    .map(messageRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(message -> {
                        User user = userRepository.findById(message.getSenderId())
                                .orElse(null);
                        return MessageResponse.from(message, ProfileResponse.from(user));
                    })
                    .collect(Collectors.toList());
        }
        response.setPinnedMessages(messageService.fulfillMessages(messages, userId));
        response.setImageUrl((response.getImageUrl() == null) ? groupCategory.getIconUrl() : response.getImageUrl());
        return new GroupReturnService(SUCCESS, null, response);
    }

    public List<String> findAllMenteeIdsGroup(String groupId) {
        Optional<Group> wrapper = groupRepository.findById(groupId);
        if (!wrapper.isPresent()) {
            return Collections.emptyList();
        }
        Group group = wrapper.get();
        return group.getMentees().stream()
                .filter(userRepository::existsById)
                .distinct().collect(Collectors.toList());
    }

    public void pingGroup(String groupId) {
        Optional<Group> groupWrapper = groupRepository.findById(groupId);
        if (!groupWrapper.isPresent()) {
            return;
        }
        Group group = groupWrapper.get();
        group.ping();
        groupRepository.save(group);
    }

    public GroupReturnService getGroupMedia(String userId, String groupId) {
        Optional<Group> groupWrapper = groupRepository.findById(groupId);
        if (!groupWrapper.isPresent()) {
            return new GroupReturnService(NOT_FOUND, "Group not found", null);
        }
        Group group = groupWrapper.get();
        if (!group.isMember(userId)) {
            return new GroupReturnService(INVALID_PERMISSION, "Invalid permission", null);
        }

        List<String> senderIds = Stream.concat(group.getMentees().stream(),
                group.getMentors().stream()).collect(Collectors.toList());
        Map<String, ProfileResponse> senders = userRepository.findAllByIdIn(senderIds).stream()
                .collect(Collectors
                        .toMap(ProfileResponse::getId, sender -> sender, (sender1, sender2) -> sender2));

        List<Message> mediaMessages = messageRepository
                .findByGroupIdAndTypeInAndStatusInOrderByCreatedDateDesc(groupId,
                        Arrays.asList(Message.Type.IMAGE, Message.Type.FILE),
                        Arrays.asList(Message.Status.SENT, Message.Status.EDITED));

        List<ShortMediaMessage> media = new ArrayList<>();
        mediaMessages.forEach(message -> {
            ProfileResponse sender = senders.getOrDefault(message.getSenderId(), null);

            if (Message.Type.IMAGE.equals(message.getType())) {
                List<ShortMediaMessage> images = message.getImages()
                                .stream()
                        .map(url -> ShortMediaMessage.builder()
                                .id(message.getId())
                                .sender(sender)
                                .imageUrl(url)
                                .type(message.getType())
                                .createdDate(message.getCreatedDate())
                                .build())
                        .collect(Collectors.toList());
                media.addAll(images);
            }

            if (Message.Type.FILE.equals(message.getType())) {
                ShortMediaMessage file = ShortMediaMessage.builder()
                        .id(message.getId())
                        .sender(sender)
                        .file(message.getFile())
                        .type(message.getType())
                        .createdDate(message.getCreatedDate())
                        .build();
                media.add(file);
            }
        });
        return new GroupReturnService(SUCCESS, null, media);
    }

    public GroupReturnService updateAvatar(String userId, String groupId, MultipartFile file) throws GeneralSecurityException, IOException {
        Optional<Group> groupWrapper = groupRepository.findById(groupId);
        if (!groupWrapper.isPresent()) {
            return new GroupReturnService(NOT_FOUND, "Group not found", null);
        }
        Group group = groupWrapper.get();
        if (!group.isMember(userId)) {
            return new GroupReturnService(INVALID_PERMISSION, "Invalid permission", null);
        }

        com.google.api.services.drive.model.File uploadedFile = googleDriveManager.uploadToFolder("avatars", file);
        String imageUrl = "https://drive.google.com/uc?export=view&id=" + uploadedFile.getId();
        group.setImageUrl(imageUrl);
        groupRepository.save(group);
        return new GroupReturnService(SUCCESS, "", imageUrl);
    }

    private List<Group> getGroupsForAdmin(String emailUser){
        List<Group> groups;
        boolean isSuperAdmin = permissionService.isSuperAdmin(emailUser);
        if (isSuperAdmin) {
            groups = groupRepository.findAllByOrderByCreatedDate();
        }
        else{
            String creatorId = userRepository.findByEmail(emailUser).get().getId();
            groups = groupRepository.findAllByCreatorIdOrderByCreatedDate(creatorId);
        }
        for (Group group : groups) {
            if(group.getStatus() != Group.Status.DELETED && group.getStatus() != Group.Status.DISABLED) {
                if (group.getTimeEnd().before(new Date())) {
                    group.setStatus(Group.Status.OUTDATED);
                    groupRepository.save(group);
                }
                if (group.getTimeStart().after(new Date())) {
                    group.setStatus(Group.Status.INACTIVE);
                    groupRepository.save(group);
                }
            }
        }
        return groups;
    }

    private List<List<String>> generateExportData(List<Group> groups){
        List<List<String>> data = new ArrayList<>();
        int index = 1;
        for(Group group: groups){
            List<String> row = new ArrayList<>();

            Optional<GroupCategory> groupCategoryOptional = groupCategoryRepository.findById(group.getGroupCategory());
            String groupCategoryName = groupCategoryOptional.isPresent() ? groupCategoryOptional.get().getName() : "";

            Map statusMap = Group.getStatusMap();
            String status = (String) statusMap.get(group.getStatus());
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            String dateStart = dateFormat.format(group.getTimeStart());
            String dateEnd = dateFormat.format(group.getTimeEnd());
            String duration = DateUtils.parseDuration(group.getDuration());

            row.add(Integer.toString(index));
            row.add(group.getName());
            row.add(groupCategoryName);
            row.add(status);
            row.add(dateStart);
            row.add(dateEnd);
            row.add(duration);

            data.add(row);
            index++;
        }

        return data;
    }
    private ResponseEntity<Resource> generateExportTable(List<Group> groups, List<String> remainColumns) throws IOException {
        List<List<String>> data = generateExportData(groups);
        List<String> headers = Arrays.asList("STT", "Tên nhóm", "Loại nhóm", "Trạng thái", "Thời gian bắt đầu", "Thời gian kết thúc", "Thời hạn");
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
        remainColumns.forEach(remainColumn->{
            if(indexMap.containsKey(remainColumn)){
                remainColumnIndexes.add(indexMap.get(remainColumn));
            }
        });

        File exportFile = FileUtils.generateExcel(headers, data, remainColumnIndexes, fileName);
        Resource resource = new FileSystemResource(exportFile.getAbsolutePath());
        ResponseEntity<Resource> response = ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + resource.getFilename())
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .contentLength(resource.getFile().length())
                .body(resource);
        return response;
    }

    public ResponseEntity<Resource> generateExportTable(String emailUser,
                                                        List<String> remainColumns) throws IOException {
        List<Group> groups = getGroupsForAdmin(emailUser);
        ResponseEntity<Resource> response = generateExportTable(groups, remainColumns);
        return response;
    }
    public ResponseEntity<Resource> generateExportTableBySearchConditions(String emailUser,
                                                                          String name,
                                                                          String mentorEmail,
                                                                          String menteeEmail,
                                                                          String groupCategory,
                                                                          Date timeStart1,
                                                                          Date timeEnd1,
                                                                          Date timeStart2,
                                                                          Date timeEnd2,
                                                                          String status,
                                                                          List<String> remainColumns) throws IOException {

        Pair<Long, List<Group>> groups = getGroupsByConditions(emailUser, name, mentorEmail, menteeEmail, groupCategory, timeStart1, timeEnd1, timeStart2, timeEnd2, status, 0, Integer.MAX_VALUE);
        ResponseEntity<Resource> response = generateExportTable(groups.getValue(), remainColumns);
        return response;
    }

    private List<List<String>> generateExportDataMembers(String groupId, String type){
        Optional<Group> groupOptional = groupRepository.findById(groupId);
        List<String> memberIds = new ArrayList<>();
        if(groupOptional.isPresent()){
            if(type.equals("MENTOR")){
                memberIds = groupOptional.get().getMentors();
            } else if(type.equals("MENTEE")){
                memberIds = groupOptional.get().getMentees();
            }
        }
        List<List<String>> data = new ArrayList<>();
        int index = 1;
        for(String memberId: memberIds){
            List<String> row = new ArrayList<>();
            User user = userRepository.findById(memberId).orElse(null);
            if(user != null){
                row.add(Integer.toString(index));
                row.add(user.getEmail());
                row.add(user.getName());
            }
            data.add(row);
            index++;
        }

        return data;
    }

    public ResponseEntity<Resource> generateExportTableMembers(String emailUser, List<String> remainColumns, String groupId, String type) throws IOException {
        List<List<String>> data = generateExportDataMembers(groupId, type);
        List<String> headers = Arrays.asList("STT", "Email", "Họ tên");
        String fileName = "output.xlsx";
        Map<String, Integer> indexMap = new HashMap<>();
        indexMap.put("email", 1);
        indexMap.put("name", 2);
        List<Integer> remainColumnIndexes = new ArrayList<>();
        remainColumnIndexes.add(0);
        remainColumns.forEach(remainColumn->{
            if(indexMap.containsKey(remainColumn)){
                remainColumnIndexes.add(indexMap.get(remainColumn));
            }
        });

        File exportFile = FileUtils.generateExcel(headers, data, remainColumnIndexes, fileName);
        Resource resource = new FileSystemResource(exportFile.getAbsolutePath());
        ResponseEntity<Resource> response = ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + resource.getFilename())
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .contentLength(resource.getFile().length())
                .body(resource);
        return response;
    }

    public boolean pinMessage(String userId, String groupId, String messageId) {
        Optional<Group> groupWrapper = groupRepository.findById(groupId);
        if (!groupWrapper.isPresent()) {
            return false;
        }
        Group group = groupWrapper.get();
        if (!group.isMember(userId)) {
            return false;
        }

        if (group.isMaximumPinnedMessages()) {
            return false;
        }

        Optional<Message> messageWrapper = messageRepository.findById(messageId);
        if (!messageWrapper.isPresent()) {
            return false;
        }

        Message message = messageWrapper.get();
        if (Message.Status.DELETED.equals(message.getStatus())) {
            return false;
        }

        User sender = userRepository.findById(message.getSenderId())
                .orElse(null);
        if (sender == null) {
            return false;
        }
        group.pinMessage(messageId);
        group.ping();
        groupRepository.save(group);

        MessageDetailResponse messageDetail = MessageDetailResponse.from(message, sender);
        socketIOService.sendNewPinMessage(messageDetail);

        Optional<User> pinnerWrapper = userRepository.findById(userId);
        if (!pinnerWrapper.isPresent()) {
            notificationService.sendNewPinNotification(messageDetail, pinnerWrapper.get());
        }
        return true;
    }

    public boolean unpinMessage(String userId, String groupId, String messageId) {
        Optional<Group> groupWrapper = groupRepository.findById(groupId);
        if (!groupWrapper.isPresent()) {
            return false;
        }
        Group group = groupWrapper.get();
        if (!group.isMember(userId)) {
            return false;
        }

        Optional<Message> messageWrapper = messageRepository.findById(messageId);
        if (!messageWrapper.isPresent()) {
            return false;
        }

        Message message = messageWrapper.get();
        User sender = userRepository.findById(message.getSenderId())
                .orElse(null);
        if (sender == null) {
            return false;
        }

        group.unpinMessage(messageId);
        group.ping();
        groupRepository.save(group);
        socketIOService.sendNewUnpinMessage(groupId, messageId);

        Optional<User> pinnerWrapper = userRepository.findById(userId);
        if (!pinnerWrapper.isPresent()) {
            notificationService.sendNewUnpinNotification(MessageDetailResponse.from(message, sender), pinnerWrapper.get());
        }

        return true;
    }

    public Group addChannel(String adderId, AddChannelRequest request) {
        Optional<Group> groupWrapper = groupRepository.findById(request.getGroupId());
        if (!groupWrapper.isPresent()) {
            return null;
        }
        Group group = groupWrapper.get();
        if (!group.isMentor(adderId)) {
            return null;
        }

        List<Group> oldChannels = groupRepository.findByIdIn(group.getChannels());
        boolean isExistedChannel = oldChannels.stream()
                .anyMatch(channel -> channel.getName().equals(request.getChannelName()));
        if (isExistedChannel) {
            return null;
        }

        Group newChannel = Group.builder()
                .id(request.getChannelId())
                .name(request.getChannelName())
                .type(Group.Type.CHANNEL)
                .parentId(request.getGroupId())
                .mentors(Collections.singletonList(adderId))
                .createdDate(new Date())
                .build();
        group.addChannel(request.getChannelId());
        groupRepository.save(group);
        return groupRepository.save(newChannel);
    }

    public void updateLastMessage(String groupId, String message) {
        Optional<Group> groupWrapper = groupRepository.findById(groupId);
        if (!groupWrapper.isPresent()) {
            return;
        }
        Group group = groupWrapper.get();
        group.setLastMessage(message);
        groupRepository.save(group);
    }
}
