package com.hcmus.mentor.backend.service;

import com.hcmus.mentor.backend.controller.payload.request.groups.AddMembersRequest;
import com.hcmus.mentor.backend.controller.payload.request.groups.UpdateGroupRequest;
import com.hcmus.mentor.backend.controller.payload.response.groups.GroupHomepageResponse;
import com.hcmus.mentor.backend.controller.payload.response.groups.UpdateGroupAvatarResponse;
import com.hcmus.mentor.backend.domain.Group;
import com.hcmus.mentor.backend.domain.constant.GroupStatus;
import com.hcmus.mentor.backend.service.dto.GroupServiceDto;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Date;
import java.util.List;

public interface GroupService {
//    Page<GroupHomepageResponse> findOwnGroups(String userId, int page, int pageSize);

    List<Group> getAllActiveOwnGroups(String userId);

//    Page<GroupHomepageResponse> findMentorGroups(String userId, int page, int pageSize);
//
//    Page<GroupHomepageResponse> findMenteeGroups(String userId, int page, int pageSize);

    Page<Group> findRecentGroupsOfUser(String userId, int page, int pageSize);

    Slice<Group> findMostRecentGroupsOfUser(String userId, int page, int pageSize);

//    GroupServiceDto createGroup(String creatorEmail, CreateGroupRequest request);

//    GroupServiceDto readGroups(Workbook workbook) throws ParseException;

//    GroupServiceDto importGroups(String emailUser, MultipartFile file) throws IOException;

    List<Group> validateTimeGroups(List<Group> groups);

    GroupServiceDto findGroups(
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

    GroupServiceDto addMembers(String emailUser, String groupId, AddMembersRequest request, final Boolean isMentor);

    GroupServiceDto deleteMentee(String emailUser, String groupId, String menteeId);

    GroupServiceDto deleteMentor(String emailUser, String groupId, String mentorId);

    GroupServiceDto promoteToMentor(String emailUser, String groupId, String menteeId);

    GroupServiceDto demoteToMentee(String emailUser, String groupId, String mentorId);

    InputStream loadTemplate(String pathToTemplate) throws Exception;

    GroupServiceDto updateGroup(String emailUser, String groupId, UpdateGroupRequest request);

    GroupServiceDto deleteGroup(String emailUser, String groupId);

    List<GroupHomepageResponse> getUserPinnedGroups(String userId);

    boolean isGroupMember(String groupId, String userId);

    Slice<GroupHomepageResponse> getHomePageRecentGroupsOfUser(String userId, int page, int pageSize);

    GroupServiceDto deleteMultiple(String emailUser, List<String> ids);

    GroupServiceDto disableMultiple(String emailUser, List<String> ids);

    GroupServiceDto enableMultiple(String emailUser, List<String> ids);

    GroupServiceDto getGroupMembers(String groupId, String userId);

    void pinGroup(String userId, String groupId);

    void unpinGroup(String userId, String groupId);

    GroupServiceDto getGroupDetail(String userId, String groupId);

    List<String> findAllMenteeIdsGroup(String groupId);

    void pingGroup(String groupId);

    GroupServiceDto getGroupMedia(String userId, String groupId);

    UpdateGroupAvatarResponse updateAvatar(String userId, String groupId, MultipartFile file);

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

    void pinChannelMessage(String userId, String channelId, String messageId);

    void unpinChannelMessage(String userId, String channelId, String messageId);

    void updateLastMessageId(String groupId, String messageId);

    //    GroupDetailResponse getGroupWorkspace(CustomerUserDetails user, String groupId);
    GroupServiceDto validateListMentorsMentees(List<String> mentors, List<String> mentees);

    GroupServiceDto validateTimeRange(Date timeStart, Date timeEnd);

    Date changeGroupTime(Date time, String type);

    Duration calculateDuration(Date from, Date to);

    GroupStatus getStatusFromTimeStartAndTimeEnd(Date timeStart, Date timeEnd);

//    void markMentee(CustomerUserDetails user, String groupId, String menteeId);
//
//    void unmarkMentee(CustomerUserDetails user, String groupId, String menteeId);
//
//    List<ChannelForwardResponse> getGroupForwards(CustomerUserDetails user, Optional<String> name);

}