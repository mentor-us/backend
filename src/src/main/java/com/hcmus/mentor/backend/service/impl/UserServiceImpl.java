package com.hcmus.mentor.backend.service.impl;

import com.google.common.base.Strings;
import com.hcmus.mentor.backend.controller.payload.ReturnCodeConstants;
import com.hcmus.mentor.backend.controller.payload.request.users.AddUserRequest;
import com.hcmus.mentor.backend.controller.payload.request.users.FindUserRequest;
import com.hcmus.mentor.backend.controller.payload.request.users.UpdateUserForAdminRequest;
import com.hcmus.mentor.backend.controller.payload.request.users.UpdateUserRequest;
import com.hcmus.mentor.backend.controller.payload.response.users.UserDataResponse;
import com.hcmus.mentor.backend.controller.payload.response.users.UserDetailResponse;
import com.hcmus.mentor.backend.domain.AuditRecord;
import com.hcmus.mentor.backend.domain.Group;
import com.hcmus.mentor.backend.domain.GroupCategory;
import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.domain.constant.ActionType;
import com.hcmus.mentor.backend.domain.constant.DomainType;
import com.hcmus.mentor.backend.domain.constant.GroupUserRole;
import com.hcmus.mentor.backend.domain.constant.UserRole;
import com.hcmus.mentor.backend.repository.GroupCategoryRepository;
import com.hcmus.mentor.backend.repository.GroupRepository;
import com.hcmus.mentor.backend.repository.GroupUserRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.security.principal.LoggedUserAccessor;
import com.hcmus.mentor.backend.service.*;
import com.hcmus.mentor.backend.service.dto.UserServiceDto;
import com.hcmus.mentor.backend.service.fileupload.BlobStorage;
import com.hcmus.mentor.backend.util.FileUtils;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tika.Tika;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static com.hcmus.mentor.backend.controller.payload.ReturnCodeConstants.*;
import static com.hcmus.mentor.backend.domain.constant.UserRole.*;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger logger = LogManager.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final MailService mailService;
    private final GroupRepository groupRepository;
    private final PermissionService permissionService;
    private final GroupCategoryRepository groupCategoryRepository;
    private final BlobStorage blobStorage;
    private final ShareService shareService;
    private final GroupUserRepository groupUserRepository;
    private final AuditRecordService auditRecordService;
    private final LoggedUserAccessor loggedUserAccessor;

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public User getOrCreateUserByEmail(String emailAddress, String groupName) {
        if (!userRepository.existsByEmail(emailAddress)) {
            addNewAccount(emailAddress);
        }
        Optional<User> menteeWrapper = userRepository.findByEmail(emailAddress);
//        mailService.sendInvitationMail(emailAddress, groupName);
        return menteeWrapper.orElse(null);
    }

    @Override
    public void addNewAccount(String emailAddress) {
        String initialName = "User " + randomString(6);
        User data = User.builder().name(initialName).initialName(initialName).email(emailAddress).roles(Collections.singletonList(USER)).build();
        userRepository.save(data);
    }

    @Override
    public User importUser(String emailAddress, String groupName) {
        if (Boolean.FALSE.equals(userRepository.existsByEmail(emailAddress))) {
            addNewAccount(emailAddress);
        }
        return userRepository.findByEmail(emailAddress).orElse(null);
    }

    @Override
    public UserServiceDto listByEmail(String emailUser, String email, Pageable pageable) {
        if (!permissionService.isAdminByEmail(emailUser)) {
            return new UserServiceDto(INVALID_PERMISSION, "Invalid permission", null);
        }
        Page<User> users = userRepository.findByEmailLikeIgnoreCase(email, pageable);
        return new UserServiceDto(SUCCESS, null, users);
    }

    @Override
    public UserServiceDto listAll() {
        List<User> users = IteratorUtils.toList(userRepository.findAll().iterator());
        List<UserDataResponse> userDataResponses = getUsersData(users);
        return new UserServiceDto(SUCCESS, null, userDataResponses);
    }

    @Override
    public UserServiceDto listAllByEmail(String emailUser, String email) {
        if (!permissionService.isAdminByEmail(emailUser)) {
            return new UserServiceDto(INVALID_PERMISSION, "Invalid permission", null);
        }
        List<User> users;

        if (email.isEmpty()) {
            users = userRepository.findAll();
        } else {
            users = userRepository.findByEmailLikeIgnoreCase(email);
        }

        return new UserServiceDto(SUCCESS, null, users);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Override
    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    @Override
    public UserServiceDto deleteUser(String emailUser, String id) {
        if (!permissionService.isAdminByEmail(emailUser)) {
            return new UserServiceDto(INVALID_PERMISSION, "Invalid permission", null);
        }

        Optional<User> userOptional = userRepository.findById(id);
        if (!userOptional.isPresent()) {
            return new UserServiceDto(ReturnCodeConstants.USER_NOT_FOUND, "Not found user", null);
        }
        User user = userOptional.get();

        if (!permissionService.isSuperAdminByEmail(emailUser)
                && permissionService.isSuperAdminByEmail(user.getEmail())) {
            return new UserServiceDto(INVALID_PERMISSION, "Invalid permission", null);
        }

        groupUserRepository.deleteByUserId(id);

        UserDataResponse userDataResponse = getUserData(user);

        userRepository.delete(user);

        var auditRecord = AuditRecord.builder()
                .entityId(user.getId())
                .user(userRepository.findByEmail(emailUser).orElse(null))
                .action(ActionType.DELETED)
                .domain(DomainType.USER)
                .detail(String.format("Người dùng %s đã xoá", user.getEmail()))
                .build();

        auditRecordService.save(auditRecord);

        return new UserServiceDto(SUCCESS, "", userDataResponse);
    }


    @Override
    public UserServiceDto addUser(String emailUser, AddUserRequest request) {
        if (!permissionService.isAdminByEmail(emailUser)) {
            return new UserServiceDto(INVALID_PERMISSION, "Invalid permission", null);
        }
        if (!permissionService.isSuperAdminByEmail(emailUser) && request.getRole() == SUPER_ADMIN) {
            return new UserServiceDto(INVALID_PERMISSION, "Invalid permission", null);
        }

        var result = addUser(request);
        if (result.getReturnCode().equals(SUCCESS)) {
            var user = (UserDataResponse) result.getData();
            var auditRecord = AuditRecord.builder()
                    .entityId(user.getId())
                    .user(userRepository.findByEmail(emailUser).orElse(null))
                    .action(ActionType.CREATED)
                    .domain(DomainType.USER)
                    .detail(String.format("Người dùng %s đã được tạo", user.getEmail()))
                    .build();
            auditRecordService.save(auditRecord);
        }
        return result;
    }

    @Override
    public UserServiceDto addUser(AddUserRequest request) {
        if (request.getName() == null
                || request.getName().isEmpty()
                || request.getEmailAddress() == null
                || request.getEmailAddress().isEmpty()
                || request.getRole() == null) {
            return new UserServiceDto(ReturnCodeConstants.USER_NOT_ENOUGH_FIELDS, "Not enough required fields", null);
        }

        String email = request.getEmailAddress();
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            return new UserServiceDto(ReturnCodeConstants.USER_DUPLICATE_USER, "Duplicate user", null);
        }

        User user = User.builder().name(request.getName()).email(email).roles(Collections.singletonList(request.getRole())).build();
        userRepository.save(user);
        mailService.sendWelcomeMail(email);

        UserDataResponse userDataResponse = UserDataResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .status(user.isStatus())
                .role(user.getRoles().stream().findFirst().orElse(null))
                .build();
        return new UserServiceDto(SUCCESS, "", userDataResponse);
    }

    @Override
    public UserServiceDto importUsers(String emailUser, MultipartFile file) throws IOException {
        InputStream data = file.getInputStream();
        Workbook workbook = new XSSFWorkbook(data);
        List<String> nameHeader = new ArrayList<>();
        nameHeader.add("STT");
        nameHeader.add("Họ tên *");
        nameHeader.add("Email *");
        nameHeader.add("Vai trò *");
        if (!shareService.isValidTemplate(workbook, 1, nameHeader)) {
            return new UserServiceDto(GROUP_INVALID_TEMPLATE, "Invalid template", null);
        }
        Sheet sheet = workbook.getSheet("Data");
        List<AddUserRequest> requests = new ArrayList<>();
        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (i == 0) {
                continue;
            }
            AddUserRequest request = AddUserRequest.builder()
                    .name(row.getCell(1).getStringCellValue())
                    .emailAddress(row.getCell(2).getStringCellValue())
                    .role(switch (row.getCell(3).getStringCellValue()) {
                        case "Quản trị viên cấp cao" -> SUPER_ADMIN;
                        case "Quản trị viên" -> ADMIN;
                        default -> USER;
                    })
                    .build();
            requests.add(request);
        }
        workbook.close();
        return addUsers(emailUser, requests);
    }

    @Override
    public UserServiceDto addUsers(String emailUser, List<AddUserRequest> requests) {
        if (!permissionService.isAdminByEmail(emailUser)) {
            return new UserServiceDto(INVALID_PERMISSION, "Invalid permission", null);
        }
        List<User> users = new ArrayList<>();
        List<UserDataResponse> responses = new ArrayList<>();
        List<String> duplicateEmails = new ArrayList<>();
        for (AddUserRequest request : requests) {
            if (!permissionService.isSuperAdminByEmail(emailUser) && request.getRole() == SUPER_ADMIN) {
                return new UserServiceDto(INVALID_PERMISSION, "Invalid permission", null);
            }
            String name = request.getName();
            String emailAddress = request.getEmailAddress();
            UserRole role = request.getRole();
            Optional<User> userOptional = userRepository.findByEmail(emailAddress);
            if (userOptional.isPresent()) {
                duplicateEmails.add(emailAddress);
            }

            User user = User.builder().name(name).email(emailAddress).roles(Collections.singletonList(USER)).build();

            UserDataResponse userDataResponse = UserDataResponse.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .email(user.getEmail())
                    .status(user.isStatus())
                    .role(USER)
                    .build();

            if (role != null) {
                List<UserRole> roles = user.getRoles();
                roles.add(role);
                user.setRoles(roles);
                userDataResponse.setRole(role);
            }
            users.add(user);
            responses.add(userDataResponse);
        }
        if (!duplicateEmails.isEmpty()) {
            return new UserServiceDto(ReturnCodeConstants.USER_DUPLICATE_USER, "Duplicate user", duplicateEmails);
        }
        users.forEach(user -> mailService.sendWelcomeMail(user.getEmail()));

        userRepository.saveAll(users);

        List<AuditRecord> auditRecords = new ArrayList<>();
        users.forEach(user -> auditRecords.add(AuditRecord.builder()
                .entityId(user.getId())
                .user(userRepository.findByEmail(emailUser).orElse(null))
                .action(ActionType.CREATED)
                .domain(DomainType.USER)
                .detail(String.format("Người dùng %s đã được tạo", user.getEmail()))
                .build()));
        auditRecordService.saveAll(auditRecords);

        return new UserServiceDto(SUCCESS, "", responses);
    }

    @Override
    public UserServiceDto updateUser(String userId, UpdateUserRequest request) {
        var user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return new UserServiceDto(ReturnCodeConstants.USER_NOT_FOUND, "Not found user", null);
        }

        var recordString = "";

        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setBirthDate(request.getBirthDate());
        user.setGender(request.getGender());

        recordString += "\nTên: " + request.getName();
        recordString += "\nSố điện thoại: " + request.getPhone();
        recordString += "\nGiới tính: " + request.getGender();
        recordString += "\nNgày sinh: " + request.getBirthDate();

        userRepository.save(user);

        AuditRecord auditRecord = AuditRecord.builder()
                .entityId(user.getId())
                .user(user)
                .action(ActionType.UPDATED)
                .domain(DomainType.USER)
                .detail("Người dùng " + user.getEmail() + " đã được cập nhật với các thông tin mới: " + recordString)
                .build();
        auditRecordService.save(auditRecord);

        UserDataResponse userDataResponse = getUserData(user);

        return new UserServiceDto(SUCCESS, "", userDataResponse);
    }

    public List<User> getUsersByConditions(String emailUser, FindUserRequest request, int page, int pageSize) {
        Specification<User> spec = createSpecification(request);

        Pageable pageable = PageRequest.of(page, pageSize);
        return userRepository.findAll(spec, pageable).getContent();
    }


    @Override
    public UserServiceDto findUsers(String emailUser, FindUserRequest request, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<User> users = userRepository.findAll(createSpecification(request), pageable);
        List<UserDataResponse> findUserResponses = getUsersData(users.getContent());

        var data = new PageImpl<>(findUserResponses, pageable, users.getTotalElements());

        return new UserServiceDto(SUCCESS, "", data);
    }

    private UserDataResponse getUserData(User user) {
        UserRole role;
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
                .build();
    }

    private List<UserDataResponse> getUsersData(List<User> users) {
        List<UserDataResponse> userDataResponses = new ArrayList<>();
        users.forEach(user -> {
            UserDataResponse userDataResponse = getUserData(user);
            userDataResponses.add(userDataResponse);
        });
        return userDataResponses;
    }

    @Override
    public UserServiceDto deleteMultiple(String emailUser, List<String> userIds) {
        var currentUser = userRepository.findById(loggedUserAccessor.getCurrentUserId()).orElse(null);
        if (currentUser == null) {
            return new UserServiceDto(ReturnCodeConstants.USER_NOT_FOUND, "Not found user", null);
        }
        if (!permissionService.isAdminByEmail(currentUser.getEmail())) {
            return new UserServiceDto(INVALID_PERMISSION, "Invalid permission", null);
        }
        List<String> notFoundIds = new ArrayList<>();
        for (String id : userIds) {
            Optional<User> userOptional = userRepository.findById(id);
            if (userOptional.isEmpty()) {
                notFoundIds.add(id);
            }
        }
        if (!notFoundIds.isEmpty()) {
            return new UserServiceDto(ReturnCodeConstants.USER_NOT_FOUND, "Not found user", notFoundIds);
        }
        List<User> users = userRepository.findByIdIn(userIds);

        List<String> invalidIds = new ArrayList<>();
        for (User user : users) {
            if (!permissionService.isSuperAdminByEmail(currentUser.getEmail()) && permissionService.isSuperAdminByEmail(user.getEmail())) {
                invalidIds.add(user.getId());
            }
        }
        if (!invalidIds.isEmpty()) {
            return new UserServiceDto(INVALID_PERMISSION, "Invalid permission", invalidIds);
        }

        groupUserRepository.deleteByUserIdIn(userIds);
        userRepository.deleteAllById(userIds);

        List<AuditRecord> auditRecords = new ArrayList<>();
        users.forEach(user -> auditRecords.add(AuditRecord.builder()
                .entityId(user.getId())
                .user(userRepository.findByEmail(emailUser).orElse(null))
                .action(ActionType.CREATED)
                .domain(DomainType.USER)
                .detail(String.format("Người dùng %s đã được tạo", user.getEmail()))
                .build()));
        auditRecordService.saveAll(auditRecords);


        return new UserServiceDto(SUCCESS, "", users);
    }

    @Override
    public UserServiceDto disableMultiple(String emailUser, List<String> ids) {
        if (!permissionService.isAdminByEmail(emailUser)) {
            return new UserServiceDto(INVALID_PERMISSION, "Invalid permission", null);
        }
        List<String> notFoundIds = new ArrayList<>();
        for (String id : ids) {
            Optional<User> userOptional = userRepository.findById(id);
            if (userOptional.isEmpty()) {
                notFoundIds.add(id);
            }
        }
        if (!notFoundIds.isEmpty()) {
            return new UserServiceDto(ReturnCodeConstants.USER_NOT_FOUND, "Not found user", notFoundIds);
        }
        List<User> users = userRepository.findByIdIn(ids);

        List<String> invalidIds = new ArrayList<>();
        for (User user : users) {
            if (!permissionService.isSuperAdminByEmail(emailUser)
                    && permissionService.isSuperAdminByEmail(user.getEmail())) {
                invalidIds.add(user.getId());
            }
        }
        if (!invalidIds.isEmpty()) {
            return new UserServiceDto(INVALID_PERMISSION, "Invalid permission", invalidIds);
        }

        for (User user : users) {
            user.setStatus(false);
        }
        userRepository.saveAll(users);
        List<AuditRecord> auditRecords = new ArrayList<>();
        users.forEach(user -> auditRecords.add(AuditRecord.builder()
                .entityId(user.getId())
                .user(userRepository.findByEmail(emailUser).orElse(null))
                .action(ActionType.CREATED)
                .domain(DomainType.USER)
                .detail(String.format("Người dùng %s đã được tạo", user.getEmail()))
                .build()));
        auditRecordService.saveAll(auditRecords);

        List<UserDataResponse> userDataResponses = getUsersData(users);

        return new UserServiceDto(SUCCESS, "", userDataResponses);
    }

    @Override
    public UserServiceDto enableMultiple(String emailUser, List<String> ids) {
        if (!permissionService.isAdminByEmail(emailUser)) {
            return new UserServiceDto(INVALID_PERMISSION, "Invalid permission", null);
        }
        List<String> notFoundIds = new ArrayList<>();
        for (String id : ids) {
            Optional<User> userOptional = userRepository.findById(id);
            if (userOptional.isEmpty()) {
                notFoundIds.add(id);
            }
        }
        if (!notFoundIds.isEmpty()) {
            return new UserServiceDto(ReturnCodeConstants.USER_NOT_FOUND, "Not found user", notFoundIds);
        }
        List<User> users = userRepository.findByIdIn(ids);

        List<String> invalidIds = new ArrayList<>();
        for (User user : users) {
            if (!permissionService.isSuperAdminByEmail(emailUser)
                    && permissionService.isSuperAdminByEmail(user.getEmail())) {
                invalidIds.add(user.getId());
            }
        }
        if (!invalidIds.isEmpty()) {
            return new UserServiceDto(INVALID_PERMISSION, "Invalid permission", invalidIds);
        }

        List<AuditRecord> auditRecords = new ArrayList<>();
        for (User user : users) {
            user.setStatus(true);
            auditRecords.add(AuditRecord.builder()
                    .entityId(user.getId())
                    .user(userRepository.findByEmail(emailUser).orElse(null))
                    .action(ActionType.UPDATED)
                    .domain(DomainType.USER)
                    .detail("Người dùng " + user.getEmail() + " đã được kích hoạt bởi quản trị viên")
                    .build());
        }
        userRepository.saveAll(users);
        auditRecordService.saveAll(auditRecords);
        List<UserDataResponse> userDataResponses = getUsersData(users);

        return new UserServiceDto(SUCCESS, "", userDataResponses);
    }

    @Override
    public UserServiceDto getDetail(String emailUser, String id) {
        if (!permissionService.isAdminByEmail(emailUser)) {
            return new UserServiceDto(INVALID_PERMISSION, "Invalid permission", null);
        }
        Optional<User> userOptional = userRepository.findById(id);
        if (!userOptional.isPresent()) {
            return new UserServiceDto(ReturnCodeConstants.USER_NOT_FOUND, "Not found user", null);
        }
        User user = userOptional.get();
        UserDetailResponse userDetailResponse = UserDetailResponse.from(user);

        List<Group> groupMentees = groupRepository.findAllByMenteesIn(id);
        List<Group> groupMentors = groupRepository.findAllByMentorsIn(id);
        List<UserDetailResponse.GroupInfo> groupInfos = new ArrayList<>();
        for (Group group : groupMentees) {
            String groupCategoryName = groupCategoryRepository.findById(group.getGroupCategory().getId()).get().getName();
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
                    groupCategoryRepository.findById(group.getGroupCategory().getId()).get().getName();
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
        UserRole role;
        if (user.getRoles().contains(SUPER_ADMIN)) {
            role = SUPER_ADMIN;
        } else if (user.getRoles().contains(ADMIN)) {
            role = ADMIN;
        } else {
            role = USER;
        }
        userDetailResponse.setRole(role);
        return new UserServiceDto(SUCCESS, "", userDetailResponse);
    }

    @Override
    public UserServiceDto updateUserForAdmin(String emailUser, String userId, UpdateUserForAdminRequest request) {
        if (!permissionService.isAdminByEmail(emailUser)) {
            return new UserServiceDto(INVALID_PERMISSION, "Invalid permission", null);
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return new UserServiceDto(ReturnCodeConstants.USER_NOT_FOUND, "Not found user", null);
        }

        var detailUpdate = new StringBuilder();

        if (!Strings.isNullOrEmpty(request.getName()) && !request.getName().equals(user.getName())) {
            user.setName(request.getName());
            detailUpdate.append("\n").append("Tên: ").append(request.getName());
        }

        if (!Strings.isNullOrEmpty(request.getPhone()) && !request.getPhone().equals(user.getPhone())) {
            user.setPhone(request.getPhone());
            detailUpdate.append("\n").append("Số điện thoại: ").append(request.getPhone());
        }

        if (request.getGender() != null && !request.getGender().equals(user.getGender())) {
            user.setGender(request.getGender());
            detailUpdate.append("\n").append("Giới tính: ").append(request.getGender());
        }

        if (request.getBirthDate() != null && !request.getBirthDate().equals(user.getBirthDate())) {
            user.setBirthDate(request.getBirthDate());
            detailUpdate.append("\n").append("Ngày sinh: ").append(request.getBirthDate());
        }

        if (request.isStatus() != user.isStatus()) {
            user.setStatus(request.isStatus());
            detailUpdate.append("\n").append("Trạng thái: ").append(request.isStatus());
        }

        UserRole role = request.getRole();
        if (!permissionService.isSuperAdminByEmail(emailUser) && role == SUPER_ADMIN) {
            return new UserServiceDto(INVALID_PERMISSION, "Invalid permission", null);
        }

        if (!permissionService.isSuperAdminByEmail(emailUser) && permissionService.isSuperAdminByEmail(user.getEmail())) {
            return new UserServiceDto(INVALID_PERMISSION, "Invalid permission", null);
        }

        if (!role.equals(user.getRoles().stream().findFirst().orElse(null))) {
            user.setRoles(new ArrayList<>(List.of(role)));
            detailUpdate.append("\n").append("Vai trò: ").append(role);
        }

        if (!detailUpdate.isEmpty()) {
            userRepository.save(user);
            AuditRecord auditRecord = AuditRecord.builder()
                    .entityId(user.getId())
                    .user(user)
                    .action(ActionType.UPDATED)
                    .domain(DomainType.USER)
                    .detail(String.format("Người dùng %s đã được cập nhật bởi quản trị viên với các thông tin mới: %s", user.getEmail(), detailUpdate))
                    .build();
            auditRecordService.save(auditRecord);
        }

        UserDataResponse userDataResponse = getUserData(user);

        return new UserServiceDto(SUCCESS, "", userDataResponse);
    }

    @Override
    public UserServiceDto updateAvatar(String userId, MultipartFile file) throws IOException {
        Optional<User> userWrapper = userRepository.findById(userId);
        if (userWrapper.isEmpty()) {
            return new UserServiceDto(ReturnCodeConstants.USER_NOT_FOUND, "Not found user", null);
        }

        String key = blobStorage.generateBlobKey(new Tika().detect(file.getBytes()));
        blobStorage.post(file, key);

        User user = userWrapper.get();
        user.updateAvatar(key);
        userRepository.save(user);

        auditRecordService.save(AuditRecord.builder()
                .entityId(user.getId())
                .user(user)
                .action(ActionType.UPDATED)
                .domain(DomainType.USER)
                .detail("Người dùng " + user.getEmail() + " đã cập nhật ảnh đại diện")
                .build());

        return new UserServiceDto(SUCCESS, "", key);
    }

    @Override
    public UserServiceDto updateWallpaper(String userId, MultipartFile file)
            throws IOException {
        Optional<User> userWrapper = userRepository.findById(userId);
        if (userWrapper.isEmpty()) {
            return new UserServiceDto(ReturnCodeConstants.USER_NOT_FOUND, "Not found user", null);
        }

        String key = blobStorage.generateBlobKey(new Tika().detect(file.getBytes()));
        blobStorage.post(file, key);

        User user = userWrapper.get();
        user.updateWallpaper(key);
        userRepository.save(user);

        auditRecordService.save(AuditRecord.builder()
                .entityId(user.getId())
                .user(user)
                .action(ActionType.UPDATED)
                .domain(DomainType.USER)
                .detail("Người dùng " + user.getEmail() + " đã cập nhật ảnh bìa")
                .build());

        return new UserServiceDto(SUCCESS, "", key);
    }


    @Override
    public ResponseEntity<Resource> generateExportTable(String emailUser, List<String> remainColumns)
            throws IOException {
        List<User> users = getUsersByConditions(emailUser, new FindUserRequest(), 0, Integer.MAX_VALUE);
        return generateExportTable(users, remainColumns);
    }

    @Override
    public ResponseEntity<Resource> generateExportTableBySearchConditions(
            String emailUser, FindUserRequest request, List<String> remainColumns) throws IOException {
        List<User> users = getUsersByConditions(emailUser, request, 0, Integer.MAX_VALUE);
        ResponseEntity<Resource> response = generateExportTable(users, remainColumns);
        return response;
    }

    @Override
    public ResponseEntity<Resource> generateExportTableMembers(
            String emailUser, List<String> remainColumns, String userId, GroupUserRole groupUserRole) throws IOException {
        List<List<String>> data = generateExportDataMembers(userId, groupUserRole);
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
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + resource.getFilename())
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .contentLength(resource.getFile().length())
                .body(resource);
    }

//    /**
//     * @param userId id of user
//     * @param email to add
//     * @return UserReturnService
//     */
//    @Override
//    public UserServiceDto addAdditionalEmail(String userId, String email) {
//        if (userRepository.findByAdditionalEmailsContains(email).isPresent() || userRepository.findByEmail(email).isPresent()) {
//            return new UserServiceDto(ReturnCodeConstants.USER_DUPLICATE_EMAIL, "Duplicate email", null);
//        }
//
//        Optional<User> userOptional = userRepository.findById(userId);
//        if (userOptional.isEmpty()) {
//            return new UserServiceDto(ReturnCodeConstants.USER_NOT_FOUND, "Not found user", null);
//        }
//
//        var user = userOptional.get();
//        var additionEmails = user.getAdditionalEmails();
//        additionEmails.add(email);
//        user.setAdditionalEmails(additionEmails);
//        userRepository.save(user);
//
//        return new UserServiceDto(SUCCESS, "Add addition email success", user);
//    }

    private Specification<User> createSpecification(FindUserRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by name
            if (request.getName() != null && !request.getName().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + request.getName().toLowerCase() + "%"));
            }

            // Filter by email
            if (request.getEmail() != null && !request.getEmail().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("email")), "%" + request.getEmail().toLowerCase() + "%"));
            }

            // Filter by status
            if (request.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }

            // Filter by role
            if (request.getRole() != null) {
                Join<User, UserRole> rolesJoin = root.join("roles");
                predicates.add(cb.equal(rolesJoin, request.getRole()));
            }

            // Apply sorting by createdDate (assuming it's a field in the User entity)
            query.orderBy(cb.desc(root.get("createdDate"))); // Sort by createdDate in descending order

            return cb.and(predicates.toArray(new Predicate[0]));
        };
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

    private List<List<String>> generateExportDataMembers(String userId, GroupUserRole groupUserRole) {
        List<Group> groups = new ArrayList<>();
        if (groupUserRole.equals(GroupUserRole.MENTOR)) {
            groups = groupRepository.findAllByMentorsIn(userId);
        } else if (groupUserRole.equals(GroupUserRole.MENTEE)) {
            groups = groupRepository.findAllByMenteesIn(userId);
        }

        List<List<String>> data = new ArrayList<>();
        int index = 1;
        for (Group group : groups) {
            Optional<GroupCategory> groupCategoryOptional =
                    groupCategoryRepository.findById(group.getGroupCategory().getId());
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

    private ResponseEntity<Resource> generateExportTable(List<User> users, List<String> remainColumns) throws IOException {
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
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + resource.getFilename())
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .contentLength(resource.getFile().length())
                .body(resource);
    }

    private List<List<String>> generateExportData(List<User> users) {
        List<List<String>> data = new ArrayList<>();
        int index = 1;
        for (User user : users) {
            List<String> row = new ArrayList<>();
            String status = user.isStatus() ? "Đang hoạt động" : "Bị khoá";
            List<UserRole> roles = user.getRoles();
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
}