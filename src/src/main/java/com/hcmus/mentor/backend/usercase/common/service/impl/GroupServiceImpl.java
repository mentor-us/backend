package com.hcmus.mentor.backend.usercase.common.service.impl;

import static com.hcmus.mentor.backend.payload.returnCode.GroupReturnCode.*;
import static com.hcmus.mentor.backend.payload.returnCode.InvalidPermissionCode.INVALID_PERMISSION;

import com.hcmus.mentor.backend.entity.*;
import com.hcmus.mentor.backend.infrastructure.fileupload.BlobStorage;
import com.hcmus.mentor.backend.payload.request.groups.*;
import com.hcmus.mentor.backend.payload.response.ShortMediaMessage;
import com.hcmus.mentor.backend.payload.response.groups.GroupDetailResponse;
import com.hcmus.mentor.backend.payload.response.groups.GroupHomepageResponse;
import com.hcmus.mentor.backend.payload.response.groups.GroupMembersResponse;
import com.hcmus.mentor.backend.payload.response.messages.MessageDetailResponse;
import com.hcmus.mentor.backend.payload.response.messages.MessageResponse;
import com.hcmus.mentor.backend.payload.response.users.ProfileResponse;
import com.hcmus.mentor.backend.payload.response.users.ShortProfile;
import com.hcmus.mentor.backend.usercase.common.repository.ChannelRepository;
import com.hcmus.mentor.backend.usercase.common.repository.GroupCategoryRepository;
import com.hcmus.mentor.backend.usercase.common.repository.GroupRepository;
import com.hcmus.mentor.backend.usercase.common.repository.MessageRepository;
import com.hcmus.mentor.backend.usercase.common.repository.SystemConfigRepository;
import com.hcmus.mentor.backend.usercase.common.repository.UserRepository;
import com.hcmus.mentor.backend.usercase.common.service.GroupService;
import com.hcmus.mentor.backend.usercase.common.service.MailService;
import com.hcmus.mentor.backend.usercase.common.service.MessageService2;
import com.hcmus.mentor.backend.usercase.common.service.NotificationService;
import com.hcmus.mentor.backend.usercase.common.service.PermissionService;
import com.hcmus.mentor.backend.usercase.common.service.SocketIOService;
import com.hcmus.mentor.backend.usercase.common.service.UserService;
import com.hcmus.mentor.backend.web.infrastructure.security.UserPrincipal;
import com.hcmus.mentor.backend.usercase.common.util.DateUtils;
import com.hcmus.mentor.backend.usercase.common.util.FileUtils;
import com.hcmus.mentor.backend.usercase.common.util.MailUtils;
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
import lombok.RequiredArgsConstructor;
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
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {
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
  private final MessageRepository messageRepository;
  private final MessageService2 messageService2;
  private final SocketIOService socketIOService;
  private final NotificationService notificationService;
  private final ChannelRepository channelRepository;
  private final BlobStorage blobStorage;

  private static Date changeGroupTime(Date time, String type) {
    LocalDateTime timeInstant =
        time.toInstant().atZone(ZoneOffset.systemDefault()).toLocalDateTime();

    if (type.equals("START")) {
      timeInstant = timeInstant.withHour(0).withMinute(0);
    } else {
      timeInstant = timeInstant.withHour(23).withMinute(59);
    }

    ZonedDateTime zonedDateTime = timeInstant.atZone(ZoneId.of("UTC"));
    Instant instant = zonedDateTime.toInstant();

    return Date.from(instant);
  }

  private static void clearSheet(Sheet sheet, int firstRow, int lastRow) {
    for (int i = firstRow; i <= lastRow; i++) {
      Row row = sheet.getRow(i);
      if (row != null) {
        sheet.removeRow(row);
      }
    }
  }

  @Override
  public Page<GroupHomepageResponse> findOwnGroups(String userId, int page, int pageSize) {
    Pageable pageRequest = PageRequest.of(page, pageSize);
    List<String> mentorIds = Collections.singletonList(userId);
    List<String> menteeIds = Collections.singletonList(userId);
    Slice<Group> wrapper =
        groupRepository.findByMentorsInAndStatusOrMenteesInAndStatus(
            mentorIds, Group.Status.ACTIVE, menteeIds, Group.Status.ACTIVE, pageRequest);
    List<GroupHomepageResponse> groups = mappingGroupHomepageResponse(wrapper.getContent(), userId);
    return new PageImpl<>(groups, pageRequest, wrapper.getNumberOfElements());
  }

  private List<GroupHomepageResponse> mappingGroupHomepageResponse(
      List<Group> groups, String userId) {
    Map<String, GroupCategory> categories =
        groupCategoryRepository.findAll().stream()
            .collect(
                Collectors.toMap(GroupCategory::getId, category -> category, (cat1, cat2) -> cat2));
    return groups.stream()
        .map(
            group -> {
              String role = group.isMentee(userId) ? "MENTEE" : "MENTOR";
              GroupCategory category = categories.get(group.getGroupCategory());
              String imageUrl =
                  (group.getImageUrl() == null) ? category.getIconUrl() : group.getImageUrl();
              String lastMessage = messageService2.getLastGroupMessage(group.getId());
              GroupHomepageResponse response =
                  new GroupHomepageResponse(group, category.getName(), role);
              response.setImageUrl(imageUrl);
              response.setNewMessage(lastMessage);
              return response;
            })
        .sorted(Comparator.comparing(GroupHomepageResponse::getUpdatedDate).reversed())
        .collect(Collectors.toList());
  }

  @Override
  public List<Group> getAllActiveOwnGroups(String userId) {
    List<String> mentorIds = Collections.singletonList(userId);
    List<String> menteeIds = Collections.singletonList(userId);
    return groupRepository.findByMentorsInAndStatusOrMenteesInAndStatus(
        mentorIds, Group.Status.ACTIVE, menteeIds, Group.Status.ACTIVE);
  }

  @Override
  public Page<GroupHomepageResponse> findMentorGroups(String userId, int page, int pageSize) {
    Pageable pageRequest = PageRequest.of(page, pageSize);
    List<String> mentorIds = Collections.singletonList(userId);
    Page<Group> wrapper =
        groupRepository.findAllByMentorsInAndStatus(mentorIds, Group.Status.ACTIVE, pageRequest);
    List<GroupHomepageResponse> groups = mappingGroupHomepageResponse(wrapper.getContent(), userId);
    return new PageImpl<>(groups, pageRequest, wrapper.getNumberOfElements());
  }

  @Override
  public Page<GroupHomepageResponse> findMenteeGroups(String userId, int page, int pageSize) {
    Pageable pageRequest = PageRequest.of(page, pageSize);
    List<String> menteeIds = Collections.singletonList(userId);
    Page<Group> wrapper =
        groupRepository.findAllByMenteesInAndStatus(menteeIds, Group.Status.ACTIVE, pageRequest);
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
    int maxYearsBetweenTimeStartAndTimeEnd =
        Integer.parseInt(systemConfigRepository.findByKey("valid_max_year").getValue().toString());
    int maxYearsBetweenTimeStartAndNow = 4;
    LocalDate localTimeStart = timeStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    LocalDate localTimeEnd = timeEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    LocalDate localNow = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    if (timeEnd.before(timeStart) || timeEnd.equals(timeStart)) {
      return new GroupReturnService(
          TIME_END_BEFORE_TIME_START, "Time end can't be before time start", null);
    }
    if (timeEnd.before(new Date()) || timeEnd.equals(new Date())) {
      return new GroupReturnService(TIME_END_BEFORE_NOW, "Time end can't be before now", null);
    }
    if (ChronoUnit.YEARS.between(localTimeStart, localTimeEnd)
        > maxYearsBetweenTimeStartAndTimeEnd) {
      return new GroupReturnService(
          TIME_END_TOO_FAR_FROM_TIME_START, "Time end is too far from time start", null);
    }
    if (Math.abs(ChronoUnit.YEARS.between(localTimeStart, localNow))
        > maxYearsBetweenTimeStartAndNow) {
      return new GroupReturnService(
          TIME_START_TOO_FAR_FROM_NOW, "Time start is too far from now", null);
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
      return new GroupReturnService(
          TIME_END_BEFORE_TIME_START, "Time end can't be before time start", null);
    }
    if (ChronoUnit.YEARS.between(localTimeStart, localTimeEnd)
        > maxYearsBetweenTimeStartAndTimeEnd) {
      return new GroupReturnService(
          TIME_END_TOO_FAR_FROM_TIME_START, "Time end is too far from time start", null);
    }
    if (Math.abs(ChronoUnit.YEARS.between(localTimeStart, localNow))
        > maxYearsBetweenTimeStartAndNow) {
      return new GroupReturnService(
          TIME_START_TOO_FAR_FROM_NOW, "Time start is too far from now", null);
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

  private GroupReturnService validateListMentorsMentees(
      List<String> mentors, List<String> mentees) {
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

  @Override
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
    GroupReturnService isValidTimeRange =
        validateTimeRange(request.getTimeStart(), request.getTimeEnd());
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

    List<String> menteeIds =
        menteeEmails.stream()
            .filter(email -> !email.isEmpty())
            .map(email -> userService.importUser(email, request.getName()))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

    List<String> mentorIds =
        mentorEmails.stream()
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
    if (userOptional.isPresent()) {
      creatorId = userOptional.get().getId();
    }
    Group group =
        Group.builder()
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
      if (row == null
          || row.getCell(0) == null
          || row.getCell(0).getCellType() == org.apache.poi.ss.usermodel.CellType.BLANK) {
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
    if (sheet == null) {
      return false;
    }

    Row row = sheet.getRow(0);
    if (!isValidHeader(row)) {
      return false;
    }
    return true;
  }

  private Boolean isValidHeader(Row row) {
    return (row.getCell(0).getStringCellValue().equals("STT")
        && row.getCell(1).getStringCellValue().equals("Loại nhóm *")
        && row.getCell(2).getStringCellValue().equals("Emails người được quản lí *")
        && row.getCell(3).getStringCellValue().equals("Tên nhóm *")
        && row.getCell(4).getStringCellValue().equals("Mô tả")
        && row.getCell(5).getStringCellValue().equals("Emails người quản lí *")
        && row.getCell(6).getStringCellValue().equals("Ngày bắt đầu *\n" + "(dd/MM/YYYY)")
        && row.getCell(7).getStringCellValue().equals("Ngày kết thúc *\n" + "(dd/MM/YYYY)"));
  }

  @Override
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
    for (int i = 0; i <= sheet.getLastRowNum(); i++) {
      Row row = sheet.getRow(i);
      if (i == 0) {
        continue;
      }
      if (!row.getCell(3).getStringCellValue().isEmpty()) {
        groupCategoryName = row.getCell(1).getStringCellValue();
        menteeEmails = new ArrayList<>();
        menteeEmail = row.getCell(2).getStringCellValue();
        menteeEmails.add(menteeEmail);
        groupName = row.getCell(3).getStringCellValue();
        description = row.getCell(4).getStringCellValue();
        mentorEmails =
            Arrays.stream(row.getCell(5).getStringCellValue().split("\n"))
                .collect(Collectors.toList());
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        timeStart = formatter.parse(row.getCell(6).getStringCellValue());
        timeEnd = formatter.parse(row.getCell(7).getStringCellValue());
      }

      Boolean isAddMentee = false;
      while (i <= sheet.getLastRowNum() && row.getCell(3).getStringCellValue().isEmpty()) {
        isAddMentee = true;
        menteeEmail = row.getCell(2).getStringCellValue();
        menteeEmails.add(menteeEmail);
        row = sheet.getRow(++i);
      }
      if (isAddMentee) {
        GroupReturnService isValidTimeRange = validateTimeRange(timeStart, timeEnd);
        if (isValidTimeRange.getReturnCode() != SUCCESS) {
          return isValidTimeRange;
        }
        if (!groupCategoryRepository.existsByName(groupCategoryName)) {
          return new GroupReturnService(
              GROUP_CATEGORY_NOT_FOUND, "Group category not exists", groupCategoryName);
        }
        String groupCategoryId = groupCategoryRepository.findByName(groupCategoryName).getId();
        if (!groups.containsKey(groupName) && !groupRepository.existsByName(groupName)) {
          Group group =
              Group.builder()
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
          return new GroupReturnService(
              DUPLICATE_GROUP, "Group name has been duplicated", groupName);
        }
        i--;
      }
    }
    return new GroupReturnService(SUCCESS, "", groups);
  }

  @Override
  public GroupReturnService importGroups(String emailUser, MultipartFile file) throws IOException {
    if (!permissionService.isAdmin(emailUser)) {
      return new GroupReturnService(INVALID_PERMISSION, "Invalid permission", null);
    }
    Map<String, Group> groups;
    try (InputStream data = file.getInputStream();
        Workbook workbook = new XSSFWorkbook(data)) {
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
      GroupReturnService isValidMails =
          validateListMentorsMentees(group.getMentors(), group.getMentees());
      if (isValidMails.getReturnCode() != SUCCESS) {
        return isValidMails;
      }
    }

    List<CreateGroupRequest> createGroupRequests =
        groups.values().stream()
            .map(
                group ->
                    CreateGroupRequest.builder()
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

  @Override
  public List<Group> validateTimeGroups(List<Group> groups) {
    for (Group group : groups) {
      if (group.getStatus() != Group.Status.DELETED && group.getStatus() != Group.Status.DISABLED) {
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

  @Override
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
      int page,
      int pageSize) {
    if (!permissionService.isAdmin(emailUser)) {
      return new GroupReturnService(INVALID_PERMISSION, "Invalid permission", null);
    }
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
            page,
            pageSize);
    return new GroupReturnService(
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
      if (userOptional.isPresent()) {
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

  @Override
  public GroupReturnService addMentees(
      String emailUser, String groupId, AddMenteesRequest request) {
    if (!permissionService.isAdmin(emailUser)) {
      return new GroupReturnService(INVALID_PERMISSION, "Invalid permission", null);
    }
    Optional<Group> groupWrapper = groupRepository.findById(groupId);
    if (!groupWrapper.isPresent()) {
      return new GroupReturnService(NOT_FOUND, "Group not found", null);
    }
    Group group = groupWrapper.get();
    List<String> emails = request.getEmails();
    List<String> ids =
        emails.stream()
            .filter(email -> !email.isEmpty())
            .map(email -> userService.getOrCreateUserByEmail(email, group.getName()))
            .collect(Collectors.toList());

    if (hasDuplicateElements(ids, group.getMentees())
        || hasDuplicateElements(ids, group.getMentors())) {
      return new GroupReturnService(DUPLICATE_EMAIL, "Duplicate emails", null);
    }
    group.getMentees().addAll(ids);
    List<String> listMenteesAfterRemoveDuplicate =
        new ArrayList<>(new HashSet<>(group.getMentees()));
    group.setMentees(listMenteesAfterRemoveDuplicate);
    groupRepository.save(group);
    for (String emailAddress : emails) {
      mailService.sendInvitationMail(emailAddress, group);
    }

    return new GroupReturnService(SUCCESS, null, group);
  }

  @Override
  public GroupReturnService addMentors(
      String emailUser, String groupId, AddMentorsRequest request) {
    if (!permissionService.hasPermissionOnGroup(emailUser, groupId)) {
      return new GroupReturnService(INVALID_PERMISSION, "Invalid permission", null);
    }
    Optional<Group> groupWrapper = groupRepository.findById(groupId);
    if (!groupWrapper.isPresent()) {
      return new GroupReturnService(NOT_FOUND, "Group not found", null);
    }
    Group group = groupWrapper.get();

    List<String> emails = request.getEmails();
    List<String> ids =
        emails.stream()
            .filter(email -> !email.isEmpty())
            .map(email -> userService.getOrCreateUserByEmail(email, group.getName()))
            .collect(Collectors.toList());

    if (hasDuplicateElements(ids, group.getMentees())
        || hasDuplicateElements(ids, group.getMentors())) {
      return new GroupReturnService(DUPLICATE_EMAIL, "Duplicate emails", null);
    }

    group.getMentors().addAll(ids);
    List<String> listMentorsAfterRemoveDuplicate =
        new ArrayList<>(new HashSet<>(group.getMentors()));
    group.setMentors(listMentorsAfterRemoveDuplicate);

    groupRepository.save(group);
    for (String emailAddress : emails) {
      mailService.sendInvitationMail(emailAddress, group);
    }

    return new GroupReturnService(SUCCESS, null, group);
  }

  @Override
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

  @Override
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

  @Override
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

  @Override
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

  @Override
  public void loadTemplate(File file) throws Exception {
    int lastRow = 10000;
    List<GroupCategory> groupCategories =
        groupCategoryRepository.findAllByStatus(GroupCategory.Status.ACTIVE);
    String[] groupCategoryNames =
        groupCategories.stream()
            .map(groupCategory -> groupCategory.getName())
            .collect(Collectors.toList())
            .toArray(new String[0]);
    FileInputStream inputStream = new FileInputStream(file);
    Workbook workbook = WorkbookFactory.create(inputStream);

    Sheet dataSheet = workbook.getSheet("Data");
    clearSheet(dataSheet, 1, dataSheet.getLastRowNum());

    DataValidationHelper validationHelper = new XSSFDataValidationHelper((XSSFSheet) dataSheet);
    CellRangeAddressList addressList = new CellRangeAddressList(1, lastRow, 1, 1);
    DataValidationConstraint constraintCategory =
        validationHelper.createExplicitListConstraint(groupCategoryNames);

    DataValidation dataValidationCategory =
        validationHelper.createValidation(constraintCategory, addressList);
    dataValidationCategory.setSuppressDropDownArrow(true);
    dataValidationCategory.setEmptyCellAllowed(false);
    dataSheet.addValidationData(dataValidationCategory);

    //        XSSFDataValidationConstraint validationConstraintDate =
    // (XSSFDataValidationConstraint)dvHelper.createDateConstraint(
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

  @Override
  public GroupReturnService updateGroup(
      String emailUser, String groupId, UpdateGroupRequest request) {
    if (!permissionService.hasPermissionOnGroup(emailUser, groupId)) {
      return new GroupReturnService(INVALID_PERMISSION, "Invalid permission", null);
    }
    Optional<Group> groupWrapper = groupRepository.findById(groupId);
    if (!groupWrapper.isPresent()) {
      return new GroupReturnService(NOT_FOUND, "Group not found", null);
    }
    Group group = groupWrapper.get();

    if (groupRepository.existsByName(request.getName())
        && !request.getName().equals(group.getName())) {
      return new GroupReturnService(DUPLICATE_GROUP, "Group name has been duplicated", null);
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
    if (!groupCategoryRepository.existsById(group.getGroupCategory())) {
      return new GroupReturnService(GROUP_CATEGORY_NOT_FOUND, "Group category not exists", null);
    }

    GroupReturnService isValidTimeRange =
        validateTimeRangeForUpdate(group.getTimeStart(), group.getTimeEnd());
    if (isValidTimeRange.getReturnCode() != SUCCESS) {
      return isValidTimeRange;
    }
    Duration duration = calculateDuration(group.getTimeStart(), group.getTimeEnd());
    group.setDuration(duration);
    Group.Status status =
        getStatusFromTimeStartAndTimeEnd(group.getTimeStart(), group.getTimeEnd());
    group.setStatus(status);
    if (request.getStatus().equals(Group.Status.DISABLED)) {
      group.setStatus(Group.Status.DISABLED);
    }
    group.setUpdatedDate(new Date());
    groupRepository.save(group);
    return new GroupReturnService(SUCCESS, null, group);
  }

  @Override
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
    Optional<Group> wrapper = groupRepository.findById(groupId);
    if (!wrapper.isPresent()) {
      Optional<Channel> channelWrapper = channelRepository.findById(groupId);
      if (!channelWrapper.isPresent()) {
        return false;
      }
      Channel channel = channelWrapper.get();
      return channel.isMember(userId) || isGroupMember(channel.getParentId(), userId);
    }
    Group group = wrapper.get();
    return group.getMentors().contains(userId) || group.getMentees().contains(userId);
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
  public GroupReturnService deleteMultiple(String emailUser, List<String> ids) {
    if (!permissionService.isAdmin(emailUser)) {
      return new GroupReturnService(INVALID_PERMISSION, "Invalid permission", null);
    }
    List<String> notFoundIds = new ArrayList<>();
    for (String id : ids) {
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

  @Override
  public GroupReturnService disableMultiple(String emailUser, List<String> ids) {
    if (!permissionService.isAdmin(emailUser)) {
      return new GroupReturnService(INVALID_PERMISSION, "Invalid permission", null);
    }
    List<String> notFoundIds = new ArrayList<>();
    for (String id : ids) {
      Optional<Group> groupOptional = groupRepository.findById(id);
      if (!groupOptional.isPresent()) {
        notFoundIds.add(id);
      }
    }

    if (!notFoundIds.isEmpty()) {
      return new GroupReturnService(NOT_FOUND, "Group not found", notFoundIds);
    }
    List<Group> groups = groupRepository.findByIdIn(ids);
    for (Group group : groups) {
      group.setStatus(Group.Status.DISABLED);
      groupRepository.save(group);
    }

    return new GroupReturnService(SUCCESS, null, groups);
  }

  @Override
  public GroupReturnService enableMultiple(String emailUser, List<String> ids) {
    if (!permissionService.isAdmin(emailUser)) {
      return new GroupReturnService(INVALID_PERMISSION, "Invalid permission", null);
    }
    List<String> notFoundIds = new ArrayList<>();
    for (String id : ids) {
      Optional<Group> groupOptional = groupRepository.findById(id);
      if (!groupOptional.isPresent()) {
        notFoundIds.add(id);
      }
    }

    if (!notFoundIds.isEmpty()) {
      return new GroupReturnService(NOT_FOUND, "Group not found", notFoundIds);
    }
    List<Group> groups = groupRepository.findByIdIn(ids);
    for (Group group : groups) {
      Group.Status status =
          getStatusFromTimeStartAndTimeEnd(group.getTimeStart(), group.getTimeEnd());
      group.setStatus(status);
      groupRepository.save(group);
    }

    return new GroupReturnService(SUCCESS, null, groups);
  }

  @Override
  public GroupReturnService getGroupMembers(String groupId, String userId) {
    Optional<Group> groupWrapper = groupRepository.findById(groupId);
    List<String> mentorIds = new ArrayList<>();
    List<String> menteeIds = new ArrayList<>();
    Group group = null;
    if (!groupWrapper.isPresent()) {
      Optional<Channel> channelWrapper = channelRepository.findById(groupId);
      if (!channelWrapper.isPresent()) {
        return new GroupReturnService(NOT_FOUND, "Group not found", null);
      }

      Channel channel = channelWrapper.get();
      group = groupRepository.findById(channel.getParentId()).orElse(null);
      if (group == null) {
        return new GroupReturnService(NOT_FOUND, "Group not found", null);
      }

      mentorIds =
          channel.getUserIds().stream().filter(group::isMentor).collect(Collectors.toList());
      menteeIds =
          channel.getUserIds().stream().filter(group::isMentee).collect(Collectors.toList());
    }

    if (groupWrapper.isPresent()) {
      if (!permissionService.isUserIdInGroup(userId, groupId)) {
        return new GroupReturnService(INVALID_PERMISSION, "Invalid permission", null);
      }

      group = groupWrapper.get();
      mentorIds = group.getMentors();
      menteeIds = group.getMentees();
    }

    List<GroupMembersResponse.GroupMember> mentors =
        userRepository.findAllByIdIn(mentorIds).stream()
            .map(ProfileResponse::normalize)
            .map(profile -> GroupMembersResponse.GroupMember.from(profile, "MENTOR"))
            .collect(Collectors.toList());
    List<String> markedMentees =
        group.getMarkedMenteeIds() != null ? group.getMarkedMenteeIds() : new ArrayList<>();
    List<GroupMembersResponse.GroupMember> mentees =
        userRepository.findAllByIdIn(menteeIds).stream()
            .map(ProfileResponse::normalize)
            .map(
                profile ->
                    GroupMembersResponse.GroupMember.from(
                        profile, "MENTEE", markedMentees.contains(profile.getId())))
            .collect(Collectors.toList());
    GroupMembersResponse response =
        GroupMembersResponse.builder().mentors(mentors).mentees(mentees).build();
    return new GroupReturnService(SUCCESS, null, response);
  }

  @Override
  public void pinGroup(String userId, String groupId) {
    Optional<User> userWrapper = userRepository.findById(userId);
    if (!userWrapper.isPresent()) {
      return;
    }
    User user = userWrapper.get();
    user.pinGroup(groupId);
    userRepository.save(user);
  }

  @Override
  public void unpinGroup(String userId, String groupId) {
    Optional<User> userWrapper = userRepository.findById(userId);
    if (!userWrapper.isPresent()) {
      return;
    }
    User user = userWrapper.get();
    user.unpinGroup(groupId);
    userRepository.save(user);
  }

  @Override
  public GroupReturnService getGroupDetail(String userId, String groupId) {
    List<GroupDetailResponse> groupWrapper = groupRepository.getGroupDetail(groupId);
    if (groupWrapper.size() == 0) {
      Optional<Channel> channelWrapper = channelRepository.findById(groupId);
      if (!channelWrapper.isPresent()) {
        return new GroupReturnService(NOT_FOUND, "Group not found", null);
      }
      Channel channel = channelWrapper.get();
      Optional<Group> parentGroupWrapper = groupRepository.findById(channel.getParentId());
      if (!parentGroupWrapper.isPresent()) {
        return new GroupReturnService(NOT_FOUND, "Group not found", null);
      }

      Group parentGroup = parentGroupWrapper.get();
      if (!parentGroup.isMentor(userId) && !channel.isMember(userId)) {
        return new GroupReturnService(INVALID_PERMISSION, "Invalid permission", null);
      }
      GroupDetailResponse channelDetail = fulfillChannelDetail(userId, channel, parentGroup);
      if (channelDetail == null) {
        return new GroupReturnService(NOT_FOUND, "Group not found", null);
      }
      GroupDetailResponse response = fulfillGroupDetail(userId, channelDetail);
      response.setPermissions(
          response.getPermissions().contains(GroupCategory.Permission.SEND_FILES)
              ? Collections.singletonList(GroupCategory.Permission.SEND_FILES)
              : Collections.emptyList());
      return new GroupReturnService(SUCCESS, null, response);
    }

    if (!permissionService.isUserIdInGroup(userId, groupId)) {
      return new GroupReturnService(INVALID_PERMISSION, "Invalid permission", null);
    }

    GroupDetailResponse response = fulfillGroupDetail(userId, groupWrapper.get(0));
    return new GroupReturnService(SUCCESS, null, response);
  }

  private GroupDetailResponse fulfillChannelDetail(
      String userId, Channel channel, Group parentGroup) {
    String channelName = channel.getName();
    String imageUrl = null;
    if (Channel.Type.PRIVATE_MESSAGE.equals(channel.getType())) {
      String penpalId =
          channel.getUserIds().stream().filter(id -> !id.equals(userId)).findFirst().orElse(null);
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
    GroupDetailResponse response =
        GroupDetailResponse.builder()
            .id(channel.getId())
            .name(channelName)
            .description(channel.getDescription())
            .pinnedMessageIds(channel.getPinnedMessageIds())
            .imageUrl(imageUrl)
            .role(parentGroup.isMentor(userId) ? "MENTOR" : "MENTEE")
            .parentId(parentGroup.getId())
            .totalMember(channel.getUserIds().size())
            .type(channel.getType())
            .build();
    GroupCategory groupCategory =
        groupCategoryRepository.findById(parentGroup.getGroupCategory()).orElse(null);
    if (groupCategory != null) {
      response.setPermissions(groupCategory.getPermissions());
      response.setGroupCategory(groupCategory.getName());
    }
    List<MessageResponse> messages = new ArrayList<>();
    if (response.getPinnedMessageIds() != null && response.getPinnedMessageIds().size() != 0) {
      messages =
          response.getPinnedMessageIds().stream()
              .map(messageRepository::findById)
              .filter(Optional::isPresent)
              .map(Optional::get)
              .filter(message -> !message.isDeleted())
              .map(
                  message -> {
                    User user = userRepository.findById(message.getSenderId()).orElse(null);
                    return MessageResponse.from(message, ProfileResponse.from(user));
                  })
              .collect(Collectors.toList());
    }
    response.setPinnedMessages(messageService2.fulfillMessages(messages, userId));
    response.setTotalMember(channel.getUserIds().size());
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

    List<MessageResponse> messages = new ArrayList<>();
    if (response.getPinnedMessageIds() != null && response.getPinnedMessageIds().size() != 0) {
      messages =
          response.getPinnedMessageIds().stream()
              .map(messageRepository::findById)
              .filter(Optional::isPresent)
              .map(Optional::get)
              .filter(message -> !message.isDeleted())
              .map(
                  message -> {
                    User user = userRepository.findById(message.getSenderId()).orElse(null);
                    return MessageResponse.from(message, ProfileResponse.from(user));
                  })
              .collect(Collectors.toList());
    }
    response.setPinnedMessages(messageService2.fulfillMessages(messages, userId));
    response.setImageUrl(
        (response.getImageUrl() == null)
            ? groupCategory != null ? groupCategory.getIconUrl() : null
            : response.getImageUrl());
    return response;
  }

  @Override
  public List<String> findAllMenteeIdsGroup(String groupId) {
    Optional<Group> wrapper = groupRepository.findById(groupId);
    if (!wrapper.isPresent()) {
      return Collections.emptyList();
    }
    Group group = wrapper.get();
    return group.getMentees().stream()
        .filter(userRepository::existsById)
        .distinct()
        .collect(Collectors.toList());
  }

  @Override
  public void pingGroup(String groupId) {
    Optional<Group> groupWrapper = groupRepository.findById(groupId);
    if (!groupWrapper.isPresent()) {
      Optional<Channel> channelWrapper = channelRepository.findById(groupId);
      if (!channelWrapper.isPresent()) {
        return;
      }

      Channel channel = channelWrapper.get();
      channel.ping();
      channelRepository.save(channel);
      return;
    }
    Group group = groupWrapper.get();
    group.ping();
    groupRepository.save(group);
  }

  @Override
  public GroupReturnService getGroupMedia(String userId, String groupId) {
    Optional<Group> groupWrapper = groupRepository.findById(groupId);
    List<String> senderIds = new ArrayList<>();
    if (!groupWrapper.isPresent()) {
      Optional<Channel> channelWrapper = channelRepository.findById(groupId);
      if (!channelWrapper.isPresent()) {
        return new GroupReturnService(NOT_FOUND, "Group not found", null);
      }

      Channel channel = channelWrapper.get();
      if (!channel.isMember(userId)
          && !permissionService.isUserIdInGroup(userId, channel.getParentId())) {
        return new GroupReturnService(INVALID_PERMISSION, "Invalid permission", null);
      }

      senderIds = channel.getUserIds();
    }

    if (groupWrapper.isPresent()) {
      Group group = groupWrapper.get();
      if (!group.isMember(userId)) {
        return new GroupReturnService(INVALID_PERMISSION, "Invalid permission", null);
      }

      senderIds =
          Stream.concat(group.getMentees().stream(), group.getMentors().stream())
              .collect(Collectors.toList());
    }

    Map<String, ProfileResponse> senders =
        userRepository.findAllByIdIn(senderIds).stream()
            .collect(
                Collectors.toMap(
                    ProfileResponse::getId, sender -> sender, (sender1, sender2) -> sender2));

    List<Message> mediaMessages =
        messageRepository.findByGroupIdAndTypeInAndStatusInOrderByCreatedDateDesc(
            groupId,
            Arrays.asList(Message.Type.IMAGE, Message.Type.FILE),
            Arrays.asList(Message.Status.SENT, Message.Status.EDITED));

    List<ShortMediaMessage> media = new ArrayList<>();
    mediaMessages.forEach(
        message -> {
          ProfileResponse sender = senders.getOrDefault(message.getSenderId(), null);

          if (Message.Type.IMAGE.equals(message.getType())) {
            List<ShortMediaMessage> images =
                message.getImages().stream()
                    .map(
                        url ->
                            ShortMediaMessage.builder()
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
    return new GroupReturnService(SUCCESS, null, media);
  }

  @Override
  public GroupReturnService updateAvatar(String userId, String groupId, MultipartFile multipartFile)
      throws GeneralSecurityException, IOException {
    Optional<Group> groupWrapper = groupRepository.findById(groupId);
    if (!groupWrapper.isPresent()) {
      return new GroupReturnService(NOT_FOUND, "Group not found", null);
    }
    Group group = groupWrapper.get();
    if (!group.isMember(userId)) {
      return new GroupReturnService(INVALID_PERMISSION, "Invalid permission", null);
    }

    File file = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));
    try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
      fileOutputStream.write(multipartFile.getBytes());
    }
    String mimeType = new Tika().detect(file);

    String key = blobStorage.generateBlobKey(mimeType);

    blobStorage.post(key, file);

    // #TODO: Current save the link of google drive
    group.setImageUrl(key);
    groupRepository.save(group);
    return new GroupReturnService(SUCCESS, "", key);
  }

  private List<Group> getGroupsForAdmin(String emailUser) {
    List<Group> groups;
    boolean isSuperAdmin = permissionService.isSuperAdmin(emailUser);
    if (isSuperAdmin) {
      groups = groupRepository.findAllByOrderByCreatedDate();
    } else {
      String creatorId = userRepository.findByEmail(emailUser).get().getId();
      groups = groupRepository.findAllByCreatorIdOrderByCreatedDate(creatorId);
    }
    for (Group group : groups) {
      if (group.getStatus() != Group.Status.DELETED && group.getStatus() != Group.Status.DISABLED) {
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

  private List<List<String>> generateExportData(List<Group> groups) {
    List<List<String>> data = new ArrayList<>();
    int index = 1;
    for (Group group : groups) {
      List<String> row = new ArrayList<>();

      Optional<GroupCategory> groupCategoryOptional =
          groupCategoryRepository.findById(group.getGroupCategory());
      String groupCategoryName =
          groupCategoryOptional.isPresent() ? groupCategoryOptional.get().getName() : "";

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
    ResponseEntity<Resource> response =
        ResponseEntity.ok()
            .header(
                HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + resource.getFilename())
            .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
            .contentLength(resource.getFile().length())
            .body(resource);
    return response;
  }

  @Override
  public ResponseEntity<Resource> generateExportTable(String emailUser, List<String> remainColumns)
      throws IOException {
    List<Group> groups = getGroupsForAdmin(emailUser);
    ResponseEntity<Resource> response = generateExportTable(groups, remainColumns);
    return response;
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
    ResponseEntity<Resource> response = generateExportTable(groups.getValue(), remainColumns);
    return response;
  }

  private List<List<String>> generateExportDataMembers(String groupId, String type) {
    Optional<Group> groupOptional = groupRepository.findById(groupId);
    List<String> memberIds = new ArrayList<>();
    if (groupOptional.isPresent()) {
      if (type.equals("MENTOR")) {
        memberIds = groupOptional.get().getMentors();
      } else if (type.equals("MENTEE")) {
        memberIds = groupOptional.get().getMentees();
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
    ResponseEntity<Resource> response =
        ResponseEntity.ok()
            .header(
                HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + resource.getFilename())
            .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
            .contentLength(resource.getFile().length())
            .body(resource);
    return response;
  }

  @Override
  public boolean pinMessage(String userId, String groupId, String messageId) {
    Optional<Group> groupWrapper = groupRepository.findById(groupId);
    if (!groupWrapper.isPresent()) {
      return pinChannelMessage(userId, groupId, messageId);
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

    User sender = userRepository.findById(message.getSenderId()).orElse(null);
    if (sender == null) {
      return false;
    }
    group.pinMessage(messageId);
    group.ping();
    groupRepository.save(group);

    MessageDetailResponse messageDetail = MessageDetailResponse.from(message, sender);
    socketIOService.sendNewPinMessage(messageDetail);

    Optional<User> pinnerWrapper = userRepository.findById(userId);
    pinnerWrapper.ifPresent(
        user -> notificationService.sendNewPinNotification(messageDetail, user));
    return true;
  }

  @Override
  public boolean pinChannelMessage(String userId, String groupId, String messageId) {
    Optional<Channel> channelWrapper = channelRepository.findById(groupId);
    if (!channelWrapper.isPresent()) {
      return false;
    }

    Channel channel = channelWrapper.get();
    if (channel.isMaximumPinnedMessages()) {
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

    User sender = userRepository.findById(message.getSenderId()).orElse(null);
    if (sender == null) {
      return false;
    }
    channel.pinMessage(messageId);
    channel.ping();
    channelRepository.save(channel);

    MessageDetailResponse messageDetail = MessageDetailResponse.from(message, sender);
    socketIOService.sendNewPinMessage(messageDetail);

    Optional<User> pinnerWrapper = userRepository.findById(userId);
    pinnerWrapper.ifPresent(
        user -> notificationService.sendNewPinNotification(messageDetail, user));

    return true;
  }

  @Override
  public boolean unpinMessage(String userId, String groupId, String messageId) {
    Optional<Group> groupWrapper = groupRepository.findById(groupId);
    if (!groupWrapper.isPresent()) {
      return false;
    }
    Group group = groupWrapper.get();
    if (!group.isMember(userId)) {
      return unpinChannelMessage(userId, groupId, messageId);
    }

    Optional<Message> messageWrapper = messageRepository.findById(messageId);
    if (!messageWrapper.isPresent()) {
      return false;
    }

    Message message = messageWrapper.get();
    User sender = userRepository.findById(message.getSenderId()).orElse(null);
    if (sender == null) {
      return false;
    }

    group.unpinMessage(messageId);
    group.ping();
    groupRepository.save(group);
    socketIOService.sendNewUnpinMessage(groupId, messageId);

    Optional<User> pinnerWrapper = userRepository.findById(userId);
    if (!pinnerWrapper.isPresent()) {
      notificationService.sendNewUnpinNotification(
          MessageDetailResponse.from(message, sender), pinnerWrapper.get());
    }

    return true;
  }

  @Override
  public boolean unpinChannelMessage(String userId, String groupId, String messageId) {
    Optional<Channel> channelWrapper = channelRepository.findById(groupId);
    if (!channelWrapper.isPresent()) {
      return false;
    }

    Channel channel = channelWrapper.get();

    Optional<Message> messageWrapper = messageRepository.findById(messageId);
    if (!messageWrapper.isPresent()) {
      return false;
    }

    Message message = messageWrapper.get();
    User sender = userRepository.findById(message.getSenderId()).orElse(null);
    if (sender == null) {
      return false;
    }

    channel.unpinMessage(messageId);
    channel.ping();
    channelRepository.save(channel);
    socketIOService.sendNewUnpinMessage(groupId, messageId);

    Optional<User> pinnerWrapper = userRepository.findById(userId);
    if (!pinnerWrapper.isPresent()) {
      notificationService.sendNewUnpinNotification(
          MessageDetailResponse.from(message, sender), pinnerWrapper.get());
    }
    return true;
  }

  @Override
  public void updateLastMessageId(String groupId, String messageId) {
    Optional<Group> groupWrapper = groupRepository.findById(groupId);
    if (!groupWrapper.isPresent()) {
      return;
    }
    Group group = groupWrapper.get();
    group.setLastMessageId(messageId);
    groupRepository.save(group);
  }

  @Override
  public void updateLastMessage(String groupId, String message) {
    Optional<Group> groupWrapper = groupRepository.findById(groupId);
    if (!groupWrapper.isPresent()) {
      return;
    }
    Group group = groupWrapper.get();
    group.setLastMessage(message);
    groupRepository.save(group);
  }

  @Override
  public GroupDetailResponse getGroupWorkspace(UserPrincipal user, String groupId) {
    if (!permissionService.isUserIdInGroup(user.getId(), groupId)) {
      return null;
    }
    List<GroupDetailResponse> groupWrapper = groupRepository.getGroupDetail(groupId);
    if (groupWrapper.size() == 0) {
      return null;
    }
    Group group = groupRepository.findById(groupId).orElse(null);
    if (group == null) {
      return null;
    }
    GroupDetailResponse detail = fulfillGroupDetail(user.getId(), groupWrapper.get(0));
    detail.setImageUrl(group.getImageUrl() == null ? detail.getImageUrl() : group.getImageUrl());

    List<String> channelIds =
        group.getChannelIds() != null ? group.getChannelIds() : new ArrayList<>();
    List<String> privateIds =
        group.getPrivateIds() != null ? group.getPrivateIds() : new ArrayList<>();
    List<GroupDetailResponse.GroupChannel> channels =
        channelRepository.findByIdIn(channelIds).stream()
            .map(GroupDetailResponse.GroupChannel::from)
            .sorted(
                Comparator.comparing(GroupDetailResponse.GroupChannel::getUpdatedDate).reversed())
            .collect(Collectors.toList());
    detail.setChannels(channels);

    List<GroupDetailResponse.GroupChannel> privates =
        channelRepository
            .findByParentIdAndTypeAndUserIdsIn(
                groupId, Channel.Type.PRIVATE_MESSAGE, Collections.singletonList(user.getId()))
            .stream()
            .map(
                channel -> {
                  String userId =
                      channel.getUserIds().stream()
                          .filter(id -> !id.equals(user.getId()))
                          .findFirst()
                          .orElse(null);
                  if (userId == null) {
                    return null;
                  }
                  ShortProfile penpal = userRepository.findShortProfile(userId);
                  if (penpal == null) {
                    return null;
                  }
                  channel.setName(penpal.getName());
                  channel.setImageUrl(penpal.getImageUrl());

                  List<String> markedMentees =
                      group.getMarkedMenteeIds() != null
                          ? group.getMarkedMenteeIds()
                          : new ArrayList<>();
                  return GroupDetailResponse.GroupChannel.from(
                      channel, markedMentees.contains(penpal.getId()));
                })
            .filter(Objects::nonNull)
            .sorted(
                Comparator.comparing(GroupDetailResponse.GroupChannel::getUpdatedDate).reversed())
            .sorted(Comparator.comparing(GroupDetailResponse.GroupChannel::getMarked).reversed())
            .collect(Collectors.toList());
    detail.setPrivates(privates);
    return detail;
  }

  @Override
  public boolean markMentee(UserPrincipal user, String groupId, String menteeId) {
    Optional<Group> groupWrapper = groupRepository.findById(groupId);
    Group group = null;
    if (!groupWrapper.isPresent()) {
      Optional<Channel> channelWrapper = channelRepository.findById(groupId);
      if (!channelWrapper.isPresent()) {
        return false;
      }
      Channel channel = channelWrapper.get();
      Optional<Group> parentGroup = groupRepository.findById(channel.getParentId());
      if (!parentGroup.isPresent()) {
        return false;
      }
      group = parentGroup.get();
    } else {
      group = groupWrapper.get();
    }

    if (!group.isMentor(user.getId())) {
      return false;
    }

    group.markMentee(menteeId);
    groupRepository.save(group);
    return true;
  }

  @Override
  public boolean unmarkMentee(UserPrincipal user, String groupId, String menteeId) {
    Optional<Group> groupWrapper = groupRepository.findById(groupId);
    Group group = null;
    if (!groupWrapper.isPresent()) {
      Optional<Channel> channelWrapper = channelRepository.findById(groupId);
      if (!channelWrapper.isPresent()) {
        return false;
      }
      Channel channel = channelWrapper.get();
      Optional<Group> parentGroup = groupRepository.findById(channel.getParentId());
      if (!parentGroup.isPresent()) {
        return false;
      }
      group = parentGroup.get();
    } else {
      group = groupWrapper.get();
    }

    if (!group.isMentor(user.getId())) {
      return false;
    }

    group.unmarkMentee(menteeId);
    groupRepository.save(group);
    return true;
  }
}
