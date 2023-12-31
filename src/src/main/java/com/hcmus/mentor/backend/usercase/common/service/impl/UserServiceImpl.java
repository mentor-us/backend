package com.hcmus.mentor.backend.usercase.common.service.impl;

import static com.hcmus.mentor.backend.entity.User.Role.*;
import static com.hcmus.mentor.backend.payload.returnCode.GroupReturnCode.INVALID_TEMPLATE;
import static com.hcmus.mentor.backend.payload.returnCode.InvalidPermissionCode.INVALID_PERMISSION;
import static com.hcmus.mentor.backend.payload.returnCode.SuccessCode.SUCCESS;
import static com.hcmus.mentor.backend.payload.returnCode.UserReturnCode.*;

import com.hcmus.mentor.backend.entity.*;
import com.hcmus.mentor.backend.infrastructure.fileupload.BlobStorage;
import com.hcmus.mentor.backend.payload.request.AddUserRequest;
import com.hcmus.mentor.backend.payload.request.FindUserRequest;
import com.hcmus.mentor.backend.payload.request.UpdateUserForAdminRequest;
import com.hcmus.mentor.backend.payload.request.UpdateUserRequest;
import com.hcmus.mentor.backend.payload.response.users.UserDataResponse;
import com.hcmus.mentor.backend.payload.response.users.UserDetailResponse;
import com.hcmus.mentor.backend.usercase.common.repository.GroupCategoryRepository;
import com.hcmus.mentor.backend.usercase.common.repository.GroupRepository;
import com.hcmus.mentor.backend.usercase.common.repository.UserRepository;
import com.hcmus.mentor.backend.usercase.common.service.MailService;
import com.hcmus.mentor.backend.usercase.common.service.PermissionService;
import com.hcmus.mentor.backend.usercase.common.service.UserService;
import com.hcmus.mentor.backend.usercase.common.util.FileUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
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
public class UserServiceImpl implements UserService {
  private final Integer ADMIN_ROLE = 1;
  private final UserRepository userRepository;
  private final MailService mailService;
  private final GroupRepository groupRepository;
  private final PermissionService permissionService;
  private final MongoTemplate mongoTemplate;
  private final GroupCategoryRepository groupCategoryRepository;
  private final BlobStorage blobStorage;

  @Override
  public String getOrCreateUserByEmail(String emailAddress, String groupName) {
    if (!userRepository.existsByEmail(emailAddress)) {
      addNewAccount(emailAddress);
    }
    Optional<User> menteeWrapper = userRepository.findByEmail(emailAddress);
    // mailService.sendInvitationMail(emailAddress, groupName);
    return menteeWrapper.map(User::getId).orElse(null);
  }

  private void sendEmail(String emailAddress) {
    Email email =
        Email.builder()
            .recipient(emailAddress)
            .msgBody("Welcome to MentorUS app!")
            .subject("Invite to MentorUS")
            .build();
    mailService.sendSimpleMail(email);
  }

  @Override
  public void addNewAccount(String emailAddress) {
    String initialName = "User " + randomString(6);
    User data =
        User.builder().name(initialName).initialName(initialName).email(emailAddress).build();
    userRepository.save(data);
  }

  private String randomString(int len) {
    String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    Random rnd = new Random();

    StringBuilder sb = new StringBuilder(len);
    for (int i = 0; i < len; i++) {
      sb.append(AB.charAt(rnd.nextInt(AB.length())));
    }
    return sb.toString();
  }

  @Override
  public String importUser(String emailAddress, String groupName) {
    if (!userRepository.existsByEmail(emailAddress)) {
      addNewAccount(emailAddress);
    }
    Optional<User> menteeWrapper = userRepository.findByEmail(emailAddress);
    // mailService.sendInvitationMail(emailAddress, group);
    return menteeWrapper.map(User::getId).orElse(null);
  }

  @Override
  public UserReturnService listByEmail(String emailUser, String email, Pageable pageable) {
    if (!permissionService.isAdmin(emailUser)) {
      return new UserReturnService(INVALID_PERMISSION, "Invalid permission", null);
    }
    if (email != null) {
      Page<User> users = userRepository.findByEmailLikeIgnoreCase(email, pageable);
      return new UserReturnService(SUCCESS, null, users);
    }
    Page<User> users = userRepository.findByEmailLikeIgnoreCase(email, pageable);
    return new UserReturnService(SUCCESS, null, users);
  }

  @Override
  public UserReturnService listAllPaging(String emailUser, Pageable pageable) {
    return findUsers(
        emailUser, new FindUserRequest(), pageable.getPageNumber(), pageable.getPageSize());
  }

  @Override
  public UserReturnService listAll() {
    List<User> users = userRepository.findAll();
    List<UserDataResponse> userDataResponses = getUsersData(users);
    return new UserReturnService(SUCCESS, null, userDataResponses);
  }

  @Override
  public UserReturnService listAllByEmail(String emailUser, String email) {
    if (!permissionService.isAdmin(emailUser)) {
      return new UserReturnService(INVALID_PERMISSION, "Invalid permission", null);
    }
    List<User> users = userRepository.findByEmailLikeIgnoreCase(email);
    return new UserReturnService(SUCCESS, null, users);
  }

  @Override
  public User findByEmail(String email) {
    Optional<User> user = userRepository.findByEmail(email);
    return user.orElse(null);
  }

  @Override
  public UserReturnService updateUser(String emailUser, String id, UpdateUserRequest request) {
    if (!permissionService.isAdmin(emailUser)) {
      return new UserReturnService(INVALID_PERMISSION, "Invalid permission", null);
    }
    return updateUser(id, request);
  }

  @Override
  public UserReturnService deleteUser(String emailUser, String id) {
    if (!permissionService.isAdmin(emailUser)) {
      return new UserReturnService(INVALID_PERMISSION, "Invalid permission", null);
    }

    Optional<User> userOptional = userRepository.findById(id);
    if (!userOptional.isPresent()) {
      return new UserReturnService(NOT_FOUND, "Not found user", null);
    }
    User user = userOptional.get();

    if (!permissionService.isSuperAdmin(emailUser)
        && permissionService.isSuperAdmin(user.getEmail())) {
      return new UserReturnService(INVALID_PERMISSION, "Invalid permission", null);
    }

    groupRepository.findAllByMenteesIn(id).forEach(group -> deleteMenteeInGroup(group, id));
    groupRepository.findAllByMentorsIn(id).forEach(group -> deleteMentorInGroup(group, id));
    UserDataResponse userDataResponse = getUserData(user);

    userRepository.delete(user);

    return new UserReturnService(SUCCESS, "", userDataResponse);
  }

  private void deleteMenteeInGroup(Group group, String menteeId) {
    if (group.getMentees().remove(menteeId)) {
      group.setMentees(group.getMentees());
      groupRepository.save(group);
    }
  }

  private void deleteMentorInGroup(Group group, String mentorId) {
    if (group.getMentors().remove(mentorId)) {
      group.setMentors(group.getMentors());
      groupRepository.save(group);
    }
  }

  @Override
  public UserReturnService addUser(String emailUser, AddUserRequest request) {
    if (!permissionService.isAdmin(emailUser)) {
      return new UserReturnService(INVALID_PERMISSION, "Invalid permission", null);
    }
    if (!permissionService.isSuperAdmin(emailUser) && request.getRole() == SUPER_ADMIN) {
      return new UserReturnService(INVALID_PERMISSION, "Invalid permission", null);
    }
    if (request.getName() == null
        || request.getName().isEmpty()
        || request.getEmailAddress() == null
        || request.getEmailAddress().isEmpty()
        || request.getRole() == null) {
      return new UserReturnService(NOT_ENOUGH_FIELDS, "Not enough required fields", null);
    }

    String emailAddress = request.getEmailAddress();
    Optional<User> userOptional = userRepository.findByEmail(emailAddress);
    if (userOptional.isPresent()) {
      return new UserReturnService(DUPLICATE_USER, "Duplicate user", null);
    }

    User.Role role = request.getRole();
    User user = User.builder().name(request.getName()).email(emailAddress).build();
    user.assignRole(role);
    userRepository.save(user);
    sendEmail(emailAddress);

    UserDataResponse userDataResponse =
        UserDataResponse.builder()
            .id(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .status(user.isStatus())
            .role(role)
            .build();
    return new UserReturnService(SUCCESS, "", userDataResponse);
  }

  private Boolean isValidTemplate(Workbook workbook) {
    int numberOfSheetInTemplate = 1;
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
        && row.getCell(1).getStringCellValue().equals("Họ tên *")
        && row.getCell(2).getStringCellValue().equals("Email *")
        && row.getCell(3).getStringCellValue().equals("Vai trò *"));
  }

  private List<AddUserRequest> getImportData(Workbook workbook) throws IOException {
    Sheet sheet = workbook.getSheet("Data");
    List<AddUserRequest> requests = new ArrayList<>();
    for (int i = 0; i <= sheet.getLastRowNum(); i++) {
      Row row = sheet.getRow(i);
      if (i == 0) {
        continue;
      }
      String name = row.getCell(1).getStringCellValue();
      String email = row.getCell(2).getStringCellValue();
      String roleString = row.getCell(3).getStringCellValue();
      User.Role role;
      if (roleString.equals("Quản trị viên cấp cao")) {
        role = SUPER_ADMIN;
      } else if (roleString.equals("Quản trị viên")) {
        role = ADMIN;
      } else {
        role = USER;
      }
      AddUserRequest request =
          AddUserRequest.builder().name(name).emailAddress(email).role(role).build();
      requests.add(request);
    }
    return requests;
  }

  @Override
  public UserReturnService importUsers(String emailUser, MultipartFile file) throws IOException {
    InputStream data = file.getInputStream();
    Workbook workbook = new XSSFWorkbook(data);
    if (!isValidTemplate(workbook)) {
      return new UserReturnService(INVALID_TEMPLATE, "Invalid template", null);
    }
    List<AddUserRequest> requests = getImportData(workbook);
    workbook.close();
    return addUsers(emailUser, requests);
  }

  @Override
  public UserReturnService addUsers(String emailUser, List<AddUserRequest> requests) {
    if (!permissionService.isAdmin(emailUser)) {
      return new UserReturnService(INVALID_PERMISSION, "Invalid permission", null);
    }
    List<User> users = new ArrayList<>();
    List<UserDataResponse> responses = new ArrayList<>();
    List<String> duplicateEmails = new ArrayList<>();
    for (AddUserRequest request : requests) {
      if (!permissionService.isSuperAdmin(emailUser) && request.getRole() == SUPER_ADMIN) {
        return new UserReturnService(INVALID_PERMISSION, "Invalid permission", null);
      }
      String name = request.getName();
      String emailAddress = request.getEmailAddress();
      User.Role role = request.getRole();
      Optional<User> userOptional = userRepository.findByEmail(emailAddress);
      if (userOptional.isPresent()) {
        duplicateEmails.add(emailAddress);
      }

      User user = User.builder().name(name).email(emailAddress).build();

      UserDataResponse userDataResponse =
          UserDataResponse.builder()
              .id(user.getId())
              .name(user.getName())
              .email(user.getEmail())
              .status(user.isStatus())
              .role(USER)
              .build();

      if (role != null) {
        List<User.Role> roles = user.getRoles();
        roles.add(role);
        user.setRoles(roles);
        userDataResponse.setRole(role);
      }
      users.add(user);
      responses.add(userDataResponse);
    }
    if (duplicateEmails.size() > 0) {
      return new UserReturnService(DUPLICATE_USER, "Duplicate user", duplicateEmails);
    }
    users.forEach(user -> sendEmail(user.getEmail()));
    userRepository.saveAll(users);

    return new UserReturnService(SUCCESS, "", responses);
  }

  @Override
  public UserReturnService updateUser(String userId, UpdateUserRequest request) {
    Optional<User> userOptional = userRepository.findById(userId);
    if (!userOptional.isPresent()) {
      return new UserReturnService(NOT_FOUND, "Not found user", null);
    }

    User user = userOptional.get();
    user.update(request);
    userRepository.save(user);
    UserDataResponse userDataResponse = getUserData(user);

    return new UserReturnService(SUCCESS, "", userDataResponse);
  }

  List<User> getUsersByConditions(
      String emailUser, FindUserRequest request, int page, int pageSize) {
    Query query = new Query();

    if (request.getName() != null && !request.getName().isEmpty()) {
      query.addCriteria(Criteria.where("name").regex(request.getName(), "i"));
    }
    if (request.getEmail() != null && !request.getEmail().isEmpty()) {
      query.addCriteria(Criteria.where("email").regex(request.getEmail(), "i"));
    }
    if (request.getStatus() != null) {
      query.addCriteria(Criteria.where("status").is(request.getStatus()));
    }
    if (request.getRole() == null && !permissionService.isSuperAdmin(emailUser)) {
      query.addCriteria(Criteria.where("roles").nin(SUPER_ADMIN));
    }
    if (request.getRole() != null) {
      query.addCriteria(Criteria.where("roles").in(request.getRole()));
    }
    query.with(Sort.by(Sort.Direction.DESC, "createdDate"));

    List<User> users = mongoTemplate.find(query, User.class);
    return users;
  }

  @Override
  public UserReturnService findUsers(
      String emailUser, FindUserRequest request, int page, int pageSize) {
    if (!permissionService.isAdmin(emailUser)) {
      return new UserReturnService(INVALID_PERMISSION, "Invalid permission", null);
    }
    List<User> users = getUsersByConditions(emailUser, request, page, pageSize);
    List<UserDataResponse> findUserResponses = getUsersData(users);
    long count = users.size();

    List<UserDataResponse> pagedUserResponses =
        findUserResponses.stream()
            .skip((long) page * pageSize)
            .limit(pageSize)
            .collect(Collectors.toList());

    return new UserReturnService(
        SUCCESS, "", new PageImpl<>(pagedUserResponses, PageRequest.of(page, pageSize), count));
  }

  private UserDataResponse getUserData(User user) {
    User.Role role;
    if (user.getRoles().contains(SUPER_ADMIN)) {
      role = SUPER_ADMIN;
    } else if (user.getRoles().contains(ADMIN)) {
      role = ADMIN;
    } else {
      role = USER;
    }

    return UserDataResponse.builder()
        .id(user.getId())
        .name(user.getName())
        .email(user.getEmail())
        .status(user.isStatus())
        .role(role)
        .birthDate(user.getBirthDate())
        .emailVerified(user.getEmailVerified())
        .gender(user.getGender())
        .phone(user.getPhone())
        .personalEmail(user.getPersonalEmail())
        .build();
  }

  private List<UserDataResponse> getUsersData(List<User> users) {
    List<UserDataResponse> userDataResponses = new ArrayList<>();
    users.forEach(
        user -> {
          UserDataResponse userDataResponse = getUserData(user);
          userDataResponses.add(userDataResponse);
        });
    return userDataResponses;
  }

  @Override
  public UserReturnService deleteMultiple(String emailUser, List<String> userIds) {
    if (!permissionService.isAdmin(emailUser)) {
      return new UserReturnService(INVALID_PERMISSION, "Invalid permission", null);
    }
    List<String> notFoundIds = new ArrayList<>();
    for (String id : userIds) {
      Optional<User> userOptional = userRepository.findById(id);
      if (!userOptional.isPresent()) {
        notFoundIds.add(id);
      }
    }
    if (!notFoundIds.isEmpty()) {
      return new UserReturnService(NOT_FOUND, "Not found user", notFoundIds);
    }
    List<User> users = userRepository.findByIdIn(userIds);

    List<String> invalidIds = new ArrayList<>();
    for (User user : users) {
      if (!permissionService.isSuperAdmin(emailUser)
          && permissionService.isSuperAdmin(user.getEmail())) {
        invalidIds.add(user.getId());
      }
    }
    if (!invalidIds.isEmpty()) {
      return new UserReturnService(INVALID_PERMISSION, "Invalid permission", invalidIds);
    }

    userIds.forEach(
        userId -> {
          List<Group> groupMentees = groupRepository.findAllByMenteesIn(userId);
          List<Group> groupMentors = groupRepository.findAllByMentorsIn(userId);
          for (Group group : groupMentees) {
            deleteMenteeInGroup(group, userId);
          }
          for (Group group : groupMentors) {
            deleteMentorInGroup(group, userId);
          }
        });
    userRepository.deleteAllById(userIds);

    return new UserReturnService(SUCCESS, "", users);
  }

  @Override
  public UserReturnService disableMultiple(String emailUser, List<String> ids) {
    if (!permissionService.isAdmin(emailUser)) {
      return new UserReturnService(INVALID_PERMISSION, "Invalid permission", null);
    }
    List<String> notFoundIds = new ArrayList<>();
    for (String id : ids) {
      Optional<User> userOptional = userRepository.findById(id);
      if (!userOptional.isPresent()) {
        notFoundIds.add(id);
      }
    }
    if (!notFoundIds.isEmpty()) {
      return new UserReturnService(NOT_FOUND, "Not found user", notFoundIds);
    }
    List<User> users = userRepository.findByIdIn(ids);

    List<String> invalidIds = new ArrayList<>();
    for (User user : users) {
      if (!permissionService.isSuperAdmin(emailUser)
          && permissionService.isSuperAdmin(user.getEmail())) {
        invalidIds.add(user.getId());
      }
    }
    if (!invalidIds.isEmpty()) {
      return new UserReturnService(INVALID_PERMISSION, "Invalid permission", invalidIds);
    }

    for (User user : users) {
      user.setStatus(false);
      userRepository.save(user);
    }
    List<UserDataResponse> userDataResponses = getUsersData(users);

    return new UserReturnService(SUCCESS, "", userDataResponses);
  }

  @Override
  public UserReturnService enableMultiple(String emailUser, List<String> ids) {
    if (!permissionService.isAdmin(emailUser)) {
      return new UserReturnService(INVALID_PERMISSION, "Invalid permission", null);
    }
    List<String> notFoundIds = new ArrayList<>();
    for (String id : ids) {
      Optional<User> userOptional = userRepository.findById(id);
      if (!userOptional.isPresent()) {
        notFoundIds.add(id);
      }
    }
    if (!notFoundIds.isEmpty()) {
      return new UserReturnService(NOT_FOUND, "Not found user", notFoundIds);
    }
    List<User> users = userRepository.findByIdIn(ids);

    List<String> invalidIds = new ArrayList<>();
    for (User user : users) {
      if (!permissionService.isSuperAdmin(emailUser)
          && permissionService.isSuperAdmin(user.getEmail())) {
        invalidIds.add(user.getId());
      }
    }
    if (!invalidIds.isEmpty()) {
      return new UserReturnService(INVALID_PERMISSION, "Invalid permission", invalidIds);
    }

    for (User user : users) {
      user.setStatus(true);
      userRepository.save(user);
    }
    List<UserDataResponse> userDataResponses = getUsersData(users);

    return new UserReturnService(SUCCESS, "", userDataResponses);
  }

  @Override
  public UserReturnService getDetail(String emailUser, String id) {
    if (!permissionService.isAdmin(emailUser)) {
      return new UserReturnService(INVALID_PERMISSION, "Invalid permission", null);
    }
    Optional<User> userOptional = userRepository.findById(id);
    if (!userOptional.isPresent()) {
      return new UserReturnService(NOT_FOUND, "Not found user", null);
    }
    User user = userOptional.get();
    UserDetailResponse userDetailResponse = UserDetailResponse.from(user);

    List<Group> groupMentees = groupRepository.findAllByMenteesIn(id);
    List<Group> groupMentors = groupRepository.findAllByMentorsIn(id);
    List<UserDetailResponse.GroupInfo> groupInfos = new ArrayList<>();
    for (Group group : groupMentees) {
      String groupCategoryName =
          groupCategoryRepository.findById(group.getGroupCategory()).get().getName();
      UserDetailResponse.GroupInfo groupInfo =
          UserDetailResponse.GroupInfo.builder()
              .id(group.getId())
              .name(group.getName())
              .role(UserDetailResponse.Role.MENTEE)
              .groupCategory(groupCategoryName)
              .build();
      groupInfos.add(groupInfo);
    }
    for (Group group : groupMentors) {
      String groupCategoryName =
          groupCategoryRepository.findById(group.getGroupCategory()).get().getName();
      UserDetailResponse.GroupInfo groupInfo =
          UserDetailResponse.GroupInfo.builder()
              .id(group.getId())
              .name(group.getName())
              .role(UserDetailResponse.Role.MENTOR)
              .groupCategory(groupCategoryName)
              .build();
      groupInfos.add(groupInfo);
    }
    userDetailResponse.setGroups(groupInfos);
    User.Role role;
    if (user.getRoles().contains(SUPER_ADMIN)) {
      role = SUPER_ADMIN;
    } else if (user.getRoles().contains(ADMIN)) {
      role = ADMIN;
    } else {
      role = USER;
    }
    userDetailResponse.setRole(role);
    return new UserReturnService(SUCCESS, "", userDetailResponse);
  }

  @Override
  public UserReturnService updateUserForAdmin(
      String emailUser, String userId, UpdateUserForAdminRequest request) {
    if (!permissionService.isAdmin(emailUser)) {
      return new UserReturnService(INVALID_PERMISSION, "Invalid permission", null);
    }
    Optional<User> userOptional = userRepository.findById(userId);
    if (!userOptional.isPresent()) {
      return new UserReturnService(NOT_FOUND, "Not found user", null);
    }

    User user = userOptional.get();
    user.update(request);
    User.Role role = request.getRole() == USER ? ROLE_USER : request.getRole();
    if (!permissionService.isSuperAdmin(emailUser) && role == SUPER_ADMIN) {
      return new UserReturnService(INVALID_PERMISSION, "Invalid permission", null);
    }
    if (!permissionService.isSuperAdmin(emailUser)
        && permissionService.isSuperAdmin(user.getEmail())) {
      return new UserReturnService(INVALID_PERMISSION, "Invalid permission", null);
    }
    List<User.Role> roles = new ArrayList<>();
    roles.add(role);
    if (role != ROLE_USER) {
      roles.add(ROLE_USER);
    }
    user.setRoles(roles);

    userRepository.save(user);
    UserDataResponse userDataResponse = getUserData(user);

    return new UserReturnService(SUCCESS, "", userDataResponse);
  }

  @Override
  public UserReturnService updateAvatar(String userId, MultipartFile multipartFile)
      throws GeneralSecurityException, IOException {
    Optional<User> userWrapper = userRepository.findById(userId);
    if (!userWrapper.isPresent()) {
      return new UserReturnService(NOT_FOUND, "Not found user", null);
    }

    File file = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));
    try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
      fileOutputStream.write(multipartFile.getBytes());
    }
    String mimeType = new Tika().detect(file);

    String key = blobStorage.generateBlobKey(mimeType);

    blobStorage.post(key, file);

    User user = userWrapper.get();
    user.updateAvatar(key);
    userRepository.save(user);

    return new UserReturnService(SUCCESS, "", key);
  }

  @Override
  public UserReturnService updateWallpaper(String userId, MultipartFile multipartFile)
      throws IOException {
    Optional<User> userWrapper = userRepository.findById(userId);
    if (!userWrapper.isPresent()) {
      return new UserReturnService(NOT_FOUND, "Not found user", null);
    }

    File file = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));
    try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
      fileOutputStream.write(multipartFile.getBytes());
    }
    String mimeType = new Tika().detect(file);

    String key = blobStorage.generateBlobKey(mimeType);

    blobStorage.post(key, file);

    User user = userWrapper.get();
    user.updateWallpaper(key);
    userRepository.save(user);

    return new UserReturnService(SUCCESS, "", key);
  }

  private List<List<String>> generateExportData(List<User> users) {
    List<List<String>> data = new ArrayList<>();
    int index = 1;
    for (User user : users) {
      List<String> row = new ArrayList<>();
      String status = user.isStatus() ? "Đang hoạt động" : "Bị khoá";
      List<User.Role> roles = user.getRoles();
      String role = "Người dùng";

      if (roles.contains(SUPER_ADMIN)) {
        role = "Quản trị viên cấp cao";
      } else if (roles.contains(ADMIN)) {
        role = "Quản trị viên";
      }

      row.add(Integer.toString(index));
      row.add(user.getEmail());
      row.add(user.getName());
      row.add(role);
      row.add(status);

      data.add(row);
      index++;
    }

    return data;
  }

  ResponseEntity<Resource> generateExportTable(List<User> users, List<String> remainColumns)
      throws IOException {
    List<List<String>> data = generateExportData(users);
    List<String> headers = Arrays.asList("STT", "Email", "Tên người dùng", "Vai trò", "Trạng thái");
    String fileName = "output.xlsx";
    Map<String, Integer> indexMap = new HashMap<>();
    indexMap.put("email", 1);
    indexMap.put("name", 2);
    indexMap.put("role", 3);
    indexMap.put("status", 4);
    List<Integer> remainColumnIndexes = new ArrayList<>();
    remainColumnIndexes.add(0);
    remainColumns.forEach(
        remainColumn -> {
          if (indexMap.containsKey(remainColumn)) {
            remainColumnIndexes.add(indexMap.get(remainColumn));
          }
        });
    java.io.File exportFile = FileUtils.generateExcel(headers, data, remainColumnIndexes, fileName);
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
    List<User> users = getUsersByConditions(emailUser, new FindUserRequest(), 0, Integer.MAX_VALUE);
    ResponseEntity<Resource> response = generateExportTable(users, remainColumns);
    return response;
  }

  @Override
  public ResponseEntity<Resource> generateExportTableBySearchConditions(
      String emailUser, FindUserRequest request, List<String> remainColumns) throws IOException {
    List<User> users = getUsersByConditions(emailUser, request, 0, Integer.MAX_VALUE);
    ResponseEntity<Resource> response = generateExportTable(users, remainColumns);
    return response;
  }

  private List<List<String>> generateExportDataMembers(String userId, String type) {
    List<Group> groups = new ArrayList<>();
    if (type.equals("MENTOR")) {
      groups = groupRepository.findAllByMentorsIn(userId);
    } else if (type.equals("MENTEE")) {
      groups = groupRepository.findAllByMenteesIn(userId);
    }

    List<List<String>> data = new ArrayList<>();
    int index = 1;
    for (Group group : groups) {
      Optional<GroupCategory> groupCategoryOptional =
          groupCategoryRepository.findById(group.getGroupCategory());
      String groupCategoryName =
          groupCategoryOptional.isPresent() ? groupCategoryOptional.get().getName() : "";
      List<String> row = new ArrayList<>();

      row.add(Integer.toString(index));
      row.add(group.getName());
      row.add(groupCategoryName);

      data.add(row);
      index++;
    }

    return data;
  }

  @Override
  public ResponseEntity<Resource> generateExportTableMembers(
      String emailUser, List<String> remainColumns, String userId, String type) throws IOException {
    List<List<String>> data = generateExportDataMembers(userId, type);
    List<String> headers = Arrays.asList("STT", "Tên nhóm", "Loại nhóm");
    String fileName = "output.xlsx";
    Map<String, Integer> indexMap = new HashMap<>();
    indexMap.put("name", 1);
    indexMap.put("groupCategory", 2);
    List<Integer> remainColumnIndexes = new ArrayList<>();
    remainColumnIndexes.add(0);
    remainColumns.forEach(
        remainColumn -> {
          if (indexMap.containsKey(remainColumn)) {
            remainColumnIndexes.add(indexMap.get(remainColumn));
          }
        });

    java.io.File exportFile = FileUtils.generateExcel(headers, data, remainColumnIndexes, fileName);
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
}
