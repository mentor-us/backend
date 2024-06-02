package com.hcmus.mentor.backend.service;

import com.hcmus.mentor.backend.controller.payload.request.AddUserRequest;
import com.hcmus.mentor.backend.controller.payload.request.FindUserRequest;
import com.hcmus.mentor.backend.controller.payload.request.UpdateUserForAdminRequest;
import com.hcmus.mentor.backend.controller.payload.request.UpdateUserRequest;
import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.domain.constant.GroupUserRole;
import com.hcmus.mentor.backend.service.dto.UserServiceDto;
import io.minio.errors.*;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public interface UserService {
    User getOrCreateUserByEmail(String emailAddress, String groupName);

    void addNewAccount(String emailAddress);

    User importUser(String emailAddress, String groupName);

    UserServiceDto listByEmail(String emailUser, String email, Pageable pageable);

    UserServiceDto listAll();

    UserServiceDto listAllByEmail(String emailUser, String email);

    User findByEmail(String email);

    User findById(String id);

    UserServiceDto updateUser(String emailUser, String id, UpdateUserRequest request);

    UserServiceDto deleteUser(String emailUser, String id);

    UserServiceDto addUser(String emailUser, AddUserRequest request);

    UserServiceDto addUser(AddUserRequest request);

    UserServiceDto importUsers(String emailUser, MultipartFile file) throws IOException;

    UserServiceDto addUsers(String emailUser, List<AddUserRequest> requests);

    UserServiceDto updateUser(String userId, UpdateUserRequest request);

    UserServiceDto findUsers(String emailUser, FindUserRequest request, int page, int pageSize);

    UserServiceDto deleteMultiple(String emailUser, List<String> userIds);

    UserServiceDto disableMultiple(String emailUser, List<String> ids);

    UserServiceDto enableMultiple(String emailUser, List<String> ids);

    UserServiceDto getDetail(String emailUser, String id);

    UserServiceDto updateUserForAdmin(
            String emailUser, String userId, UpdateUserForAdminRequest request);

    UserServiceDto updateAvatar(String userId, MultipartFile file)
            throws GeneralSecurityException, IOException, ServerException, InsufficientDataException, ErrorResponseException, InvalidResponseException, XmlParserException, InternalException;

    UserServiceDto updateWallpaper(String userId, MultipartFile file)
            throws GeneralSecurityException, IOException, ServerException, InsufficientDataException, ErrorResponseException, InvalidResponseException, XmlParserException, InternalException;

    ResponseEntity<Resource> generateExportTable(String emailUser, List<String> remainColumns)
            throws IOException;

    ResponseEntity<Resource> generateExportTableBySearchConditions(
            String emailUser, FindUserRequest request, List<String> remainColumns) throws IOException;

    ResponseEntity<Resource> generateExportTableMembers(
            String emailUser, List<String> remainColumns, String userId, GroupUserRole groupUserRole) throws IOException;

    UserServiceDto addAdditionalEmail(String userId, String email);

}