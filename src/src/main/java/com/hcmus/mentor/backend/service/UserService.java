package com.hcmus.mentor.backend.service;

import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.controller.payload.request.AddUserRequest;
import com.hcmus.mentor.backend.controller.payload.request.FindUserRequest;
import com.hcmus.mentor.backend.controller.payload.request.UpdateUserForAdminRequest;
import com.hcmus.mentor.backend.controller.payload.request.UpdateUserRequest;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    String getOrCreateUserByEmail(String emailAddress, String groupName);

    void addNewAccount(String emailAddress);

    String importUser(String emailAddress, String groupName);

    UserReturnService listByEmail(String emailUser, String email, Pageable pageable);

    UserReturnService listAllPaging(String emailUser, Pageable pageable);

    UserReturnService listAll();

    UserReturnService listAllByEmail(String emailUser, String email);

    User findByEmail(String email);

    UserReturnService updateUser(String emailUser, String id, UpdateUserRequest request);

    UserReturnService deleteUser(String emailUser, String id);

    UserReturnService addUser(String emailUser, AddUserRequest request);

    UserReturnService importUsers(String emailUser, MultipartFile file) throws IOException;

    UserReturnService addUsers(String emailUser, List<AddUserRequest> requests);

    UserReturnService updateUser(String userId, UpdateUserRequest request);

    UserReturnService findUsers(String emailUser, FindUserRequest request, int page, int pageSize);

    UserReturnService deleteMultiple(String emailUser, List<String> userIds);

    UserReturnService disableMultiple(String emailUser, List<String> ids);

    UserReturnService enableMultiple(String emailUser, List<String> ids);

    UserReturnService getDetail(String emailUser, String id);

    UserReturnService updateUserForAdmin(
            String emailUser, String userId, UpdateUserForAdminRequest request);

    UserReturnService updateAvatar(String userId, MultipartFile file)
            throws GeneralSecurityException, IOException, ServerException, InsufficientDataException, ErrorResponseException, InvalidResponseException, XmlParserException, InternalException;

    UserReturnService updateWallpaper(String userId, MultipartFile file)
            throws GeneralSecurityException, IOException, ServerException, InsufficientDataException, ErrorResponseException, InvalidResponseException, XmlParserException, InternalException;

    ResponseEntity<Resource> generateExportTable(String emailUser, List<String> remainColumns)
            throws IOException;

    ResponseEntity<Resource> generateExportTableBySearchConditions(
            String emailUser, FindUserRequest request, List<String> remainColumns) throws IOException;

    ResponseEntity<Resource> generateExportTableMembers(
            String emailUser, List<String> remainColumns, String userId, String type) throws IOException;

    @Getter
    @Setter
    @NoArgsConstructor
    class UserReturnService {
        Integer returnCode;
        String message;
        Object data;

        public UserReturnService(Integer returnCode, String message, Object data) {
            this.returnCode = returnCode;
            this.message = message;
            this.data = data;
        }
    }
}
