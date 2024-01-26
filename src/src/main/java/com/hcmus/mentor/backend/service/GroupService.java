package com.hcmus.mentor.backend.service;

import com.hcmus.mentor.backend.controller.payload.response.channel.ChannelForwardResponse;
import com.hcmus.mentor.backend.domain.Group;
import com.hcmus.mentor.backend.controller.payload.request.groups.AddMenteesRequest;
import com.hcmus.mentor.backend.controller.payload.request.groups.AddMentorsRequest;
import com.hcmus.mentor.backend.controller.payload.request.groups.CreateGroupRequest;
import com.hcmus.mentor.backend.controller.payload.request.groups.UpdateGroupRequest;
import com.hcmus.mentor.backend.controller.payload.response.groups.GroupDetailResponse;
import com.hcmus.mentor.backend.controller.payload.response.groups.GroupHomepageResponse;
import com.hcmus.mentor.backend.security.UserPrincipal;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import java.util.Optional;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface GroupService {
    Page<GroupHomepageResponse> findOwnGroups(String userId, int page, int pageSize);

    List<Group> getAllActiveOwnGroups(String userId);

    Page<GroupHomepageResponse> findMentorGroups(String userId, int page, int pageSize);

    Page<GroupHomepageResponse> findMenteeGroups(String userId, int page, int pageSize);

    Page<Group> findRecentGroupsOfUser(String userId, int page, int pageSize);

    Slice<Group> findMostRecentGroupsOfUser(String userId, int page, int pageSize);

    GroupReturnService createNewGroup(String emailUser, CreateGroupRequest request);

    GroupReturnService readGroups(Workbook workbook) throws ParseException;

    GroupReturnService importGroups(String emailUser, MultipartFile file) throws IOException;

    List<Group> validateTimeGroups(List<Group> groups);

    GroupReturnService findGroups(
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
            int pageSize);

    GroupReturnService addMentees(String emailUser, String groupId, AddMenteesRequest request);

    GroupReturnService addMentors(String emailUser, String groupId, AddMentorsRequest request);

    GroupReturnService deleteMentee(String emailUser, String groupId, String menteeId);

    GroupReturnService deleteMentor(String emailUser, String groupId, String mentorId);

    GroupReturnService promoteToMentor(String emailUser, String groupId, String menteeId);

    GroupReturnService demoteToMentee(String emailUser, String groupId, String mentorId);

    void loadTemplate(File file) throws Exception;

    GroupReturnService updateGroup(String emailUser, String groupId, UpdateGroupRequest request);

    GroupReturnService deleteGroup(String emailUser, String groupId);

    List<GroupHomepageResponse> getUserPinnedGroups(String userId);

    boolean isGroupMember(String groupId, String userId);

    Slice<GroupHomepageResponse> getHomePageRecentGroupsOfUser(String userId, int page, int pageSize);

    GroupReturnService deleteMultiple(String emailUser, List<String> ids);

    GroupReturnService disableMultiple(String emailUser, List<String> ids);

    GroupReturnService enableMultiple(String emailUser, List<String> ids);

    GroupReturnService getGroupMembers(String groupId, String userId);

    void pinGroup(String userId, String groupId);

    void unpinGroup(String userId, String groupId);

    GroupReturnService getGroupDetail(String userId, String groupId);

    List<String> findAllMenteeIdsGroup(String groupId);

    void pingGroup(String groupId);

    GroupReturnService getGroupMedia(String userId, String groupId);

    GroupReturnService updateAvatar(String userId, String groupId, MultipartFile file)
            throws GeneralSecurityException, IOException, ServerException, InsufficientDataException, ErrorResponseException, InvalidResponseException, XmlParserException, InternalException;

    ResponseEntity<Resource> generateExportTable(String emailUser, List<String> remainColumns)
            throws IOException;

    ResponseEntity<Resource> generateExportTableBySearchConditions(
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
            throws IOException;

    ResponseEntity<Resource> generateExportTableMembers(
            String emailUser, List<String> remainColumns, String groupId, String type) throws IOException;

    boolean pinMessage(String userId, String groupId, String messageId);

    boolean pinChannelMessage(String userId, String groupId, String messageId);

    boolean unpinMessage(String userId, String groupId, String messageId);

    boolean unpinChannelMessage(String userId, String groupId, String messageId);

    void updateLastMessageId(String groupId, String messageId);

    void updateLastMessage(String groupId, String message);

    GroupDetailResponse getGroupWorkspace(UserPrincipal user, String groupId);

    boolean markMentee(UserPrincipal user, String groupId, String menteeId);

    boolean unmarkMentee(UserPrincipal user, String groupId, String menteeId);

    List<ChannelForwardResponse> getGroupForwards(UserPrincipal user, Optional<String> name) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException;

    @Getter
    @Setter
    @NoArgsConstructor
    class GroupReturnService {
        Integer returnCode;
        String message;
        Object data;

        public GroupReturnService(Integer returnCode, String message, Object data) {
            this.returnCode = returnCode;
            this.message = message;
            this.data = data;
        }
    }
}
