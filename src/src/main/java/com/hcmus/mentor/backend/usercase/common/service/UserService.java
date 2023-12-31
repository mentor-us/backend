package com.hcmus.mentor.backend.usercase.common.service;

import com.hcmus.mentor.backend.entity.User;
import com.hcmus.mentor.backend.payload.request.AddUserRequest;
import com.hcmus.mentor.backend.payload.request.FindUserRequest;
import com.hcmus.mentor.backend.payload.request.UpdateUserForAdminRequest;
import com.hcmus.mentor.backend.payload.request.UpdateUserRequest;
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
      throws GeneralSecurityException, IOException;

  UserReturnService updateWallpaper(String userId, MultipartFile file)
      throws GeneralSecurityException, IOException;

  ResponseEntity<Resource> generateExportTable(String emailUser, List<String> remainColumns)
      throws IOException;

  ResponseEntity<Resource> generateExportTableBySearchConditions(
      String emailUser, FindUserRequest request, List<String> remainColumns) throws IOException;

  ResponseEntity<Resource> generateExportTableMembers(
      String emailUser, List<String> remainColumns, String userId, String type) throws IOException;

  @Getter
  @Setter
  @NoArgsConstructor
  public static class UserReturnService {
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
