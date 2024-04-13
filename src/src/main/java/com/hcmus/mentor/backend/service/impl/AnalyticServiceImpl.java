package com.hcmus.mentor.backend.service.impl;

import com.hcmus.mentor.backend.controller.payload.ApiResponseDto;
import com.hcmus.mentor.backend.controller.payload.request.FindGroupGeneralAnalyticRequest;
import com.hcmus.mentor.backend.controller.payload.request.FindUserAnalyticRequest;
import com.hcmus.mentor.backend.controller.payload.request.UpdateStudentInformationRequest;
import com.hcmus.mentor.backend.controller.payload.response.analytic.GroupAnalyticResponse;
import com.hcmus.mentor.backend.controller.payload.response.analytic.ImportGeneralInformationResponse;
import com.hcmus.mentor.backend.controller.payload.response.analytic.SystemAnalyticChartResponse;
import com.hcmus.mentor.backend.controller.payload.response.analytic.SystemAnalyticResponse;
import com.hcmus.mentor.backend.controller.payload.response.groups.GroupGeneralResponse;
import com.hcmus.mentor.backend.controller.payload.response.meetings.MeetingResponse;
import com.hcmus.mentor.backend.controller.payload.response.messages.MessageResponse;
import com.hcmus.mentor.backend.domain.*;
import com.hcmus.mentor.backend.domain.constant.GroupStatus;
import com.hcmus.mentor.backend.domain.constant.TaskStatus;
import com.hcmus.mentor.backend.repository.*;
import com.hcmus.mentor.backend.service.AnalyticAttribute;
import com.hcmus.mentor.backend.service.AnalyticService;
import com.hcmus.mentor.backend.service.PermissionService;
import com.hcmus.mentor.backend.util.DateUtils;
import com.hcmus.mentor.backend.util.FileUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static com.hcmus.mentor.backend.controller.payload.returnCode.AnalyticReturnCode.*;
import static com.hcmus.mentor.backend.controller.payload.returnCode.InvalidPermissionCode.INVALID_PERMISSION;
import static com.hcmus.mentor.backend.controller.payload.returnCode.InvalidPermissionCode.INVALID_PERMISSION_STRING;
import static com.hcmus.mentor.backend.controller.payload.returnCode.SuccessCode.SUCCESS;

/**
 * Analytic service.
 */
@Service
@RequiredArgsConstructor
public class AnalyticServiceImpl implements AnalyticService {

    private final GroupRepository groupRepository;
    private final TaskRepository taskRepository;
    private final MeetingRepository meetingRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final PermissionService permissionService;
    private final GroupCategoryRepository groupCategoryRepository;
    private final GroupUserRepository groupUserRepository;
    @PersistenceContext
    private EntityManager entityManager;
    private final SpringTemplateEngine templateEngine;

    private SystemAnalyticResponse getGeneralInformationForSuperAdmin() {
        long totalGroups = groupRepository.count();
        long activeGroups = groupRepository.countByStatus(GroupStatus.ACTIVE);

        List<Task> tasks = taskRepository.findAll();
        long totalTasks = tasks.stream().map(task -> task.getAssignees().size()).reduce(0, Integer::sum);

        long totalMeetings = meetingRepository.count();
        long totalMessages = messageRepository.count();

        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByStatus(true);

        return SystemAnalyticResponse.builder()
                .totalGroups(totalGroups)
                .activeGroups(activeGroups)
                .totalTasks(totalTasks)
                .totalMeetings(totalMeetings)
                .totalMessages(totalMessages)
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .build();
    }

    private SystemAnalyticResponse getGeneralInformationForAdmin(String adminId) {
        List<Group> groups = groupRepository.findAllByCreatorId(adminId);
        var channelIds = groups.stream().flatMap(g -> g.getChannels().stream().map(Channel::getId)).toList();
        List<Task> tasks = taskRepository.findAllByGroupIdIn(channelIds);
        long totalTasks = tasks.stream().map(task -> task.getAssignees().size()).reduce(0, Integer::sum);

        long activeGroups = groupRepository.countByStatusAndCreatorId(GroupStatus.ACTIVE, adminId);
        long totalGroups = groups.size();
        long totalMeetings = meetingRepository.countByGroupIdIn(channelIds);
        long totalMessages = messageRepository.countByGroupIdIn(channelIds);
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByStatus(true);

        return SystemAnalyticResponse.builder()
                .totalGroups(totalGroups)
                .activeGroups(activeGroups)
                .totalTasks(totalTasks)
                .totalMeetings(totalMeetings)
                .totalMessages(totalMessages)
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .build();
    }

    @Override
    public ApiResponseDto<SystemAnalyticResponse> getGeneralInformation(String emailUser) {
        if (!permissionService.isAdmin(emailUser)) {
            return new ApiResponseDto<>(null, INVALID_PERMISSION, INVALID_PERMISSION_STRING);
        }
        SystemAnalyticResponse response;
        if (permissionService.isSuperAdmin(emailUser)) {
            response = getGeneralInformationForSuperAdmin();
        } else {
            var adminId = Objects.requireNonNull(userRepository.findByEmail(emailUser).orElse(null)).getId();
            response = getGeneralInformationForAdmin(adminId);
        }

        return new ApiResponseDto<>(response, SUCCESS, "");
    }

    @Override
    public ApiResponseDto<SystemAnalyticResponse> getGeneralInformationByGroupCategory(String emailUser, String groupCategoryId) {
        if (!permissionService.isAdmin(emailUser)) {
            return new ApiResponseDto<>(null, INVALID_PERMISSION, INVALID_PERMISSION_STRING);
        }
        List<Group> groups;
        long activeGroups;

        if (permissionService.isSuperAdmin(emailUser)) {
            groups = groupRepository.findAllByGroupCategory(groupCategoryId);
            activeGroups = groupRepository.countByGroupCategoryAndStatus(groupCategoryId, GroupStatus.ACTIVE);
        } else {
            var adminId = Objects.requireNonNull(userRepository.findByEmail(emailUser).orElse(null)).getId();
            groups = groupRepository.findAllByGroupCategoryAndCreatorId(groupCategoryId, adminId);
            activeGroups = groupRepository.countByGroupCategoryAndStatusAndCreatorId(groupCategoryId, GroupStatus.ACTIVE, adminId);
        }

        var channelIds = groups.stream().flatMap(g -> g.getChannels().stream().map(Channel::getId)).toList();
        List<Task> tasks = taskRepository.findAllByGroupIdIn(channelIds);

        long totalGroups = groups.size();
        long totalTasks = tasks.stream().map(task -> task.getAssignees().size()).reduce(0, Integer::sum);
        long totalMeetings = meetingRepository.countByGroupIdIn(channelIds);
        long totalMessages = messageRepository.countByGroupIdIn(channelIds);
        long totalUsers = groups.stream().map(g -> g.getGroupUsers().size()).reduce(0, Integer::sum);
        long activeUsers = groups.stream()
                .map(g -> g.getGroupUsers().stream().filter(gu -> gu.getUser().isStatus()).count())
                .reduce(0L, Long::sum);

        SystemAnalyticResponse response = SystemAnalyticResponse.builder()
                .totalGroups(totalGroups)
                .activeGroups(activeGroups)
                .totalTasks(totalTasks)
                .totalMeetings(totalMeetings)
                .totalMessages(totalMessages)
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .build();
        return new ApiResponseDto<>(response, SUCCESS, "");
    }

    @Override
    public ApiResponseDto<SystemAnalyticChartResponse> getDataForChart(String emailUser, int monthStart, int yearStart, int monthEnd, int yearEnd) throws ParseException {
        if (!permissionService.isAdmin(emailUser)) {
            return new ApiResponseDto<>(null, INVALID_PERMISSION, INVALID_PERMISSION_STRING);
        }
        LocalDate timeStart = LocalDate.of(yearStart, monthStart, 1);
        int lastDay = getLastDayOfMonth(yearEnd, monthEnd);
        LocalDate timeEnd = LocalDate.of(yearEnd, monthEnd, lastDay);
        if (timeEnd.isBefore(timeStart)) {
            return new ApiResponseDto<>(null, INVALID_TIME_RANGE, INVALID_TIME_RANGE_STRING);
        }
        List<SystemAnalyticChartResponse.MonthSystemAnalytic> data;
        if (permissionService.isSuperAdmin(emailUser)) {
            data = getChartByMonthForSuperAdmin(timeStart, timeEnd);
        } else {
            var adminId = Objects.requireNonNull(userRepository.findByEmail(emailUser).orElse(null)).getId();
            data = getChartByMonthForAdmin(timeStart, timeEnd, adminId);
        }
        SystemAnalyticChartResponse response = SystemAnalyticChartResponse.builder().data(data).build();
        return new ApiResponseDto<>(response, SUCCESS, "");
    }

    private List<SystemAnalyticChartResponse.MonthSystemAnalytic> getChartByMonthForSuperAdmin(LocalDate timeStart, LocalDate timeEnd) {
        List<SystemAnalyticChartResponse.MonthSystemAnalytic> data = new ArrayList<>();
        for (LocalDate localDate = timeStart;
             localDate.isBefore(timeEnd);
             localDate = localDate.plusMonths(1)) {
            Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date dateAfterOneMonth = Date.from(localDate.plusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            long newGroups = groupRepository.countByCreatedDateBetween(date, dateAfterOneMonth);
            long newMessages = messageRepository.countByCreatedDateBetween(date, dateAfterOneMonth);

            List<Task> tasks = taskRepository.findByCreatedDateBetween(date, dateAfterOneMonth);

            long newTasks = tasks.stream().map(task -> task.getAssignees().size()).reduce(0, Integer::sum);
            long newMeetings = meetingRepository.countByCreatedDateBetween(date, dateAfterOneMonth);
            long newUsers = userRepository.countByCreatedDateBetween(date, dateAfterOneMonth);

            SystemAnalyticChartResponse.MonthSystemAnalytic monthSystemAnalytic = SystemAnalyticChartResponse.MonthSystemAnalytic.builder()
                    .month(date.getMonth() + 1)
                    .year(date.getYear() + 1900)
                    .newGroups(newGroups)
                    .newMessages(newMessages)
                    .newTasks(newTasks)
                    .newMeetings(newMeetings)
                    .newUsers(newUsers)
                    .build();
            data.add(monthSystemAnalytic);
        }
        return data;
    }

    private List<SystemAnalyticChartResponse.MonthSystemAnalytic> getChartByMonthForAdmin(LocalDate timeStart, LocalDate timeEnd, String adminId) {
        List<Group> groups = groupRepository.findAllByCreatorId(adminId);
        List<String> channelIds = groups.stream().flatMap(g -> g.getChannels().stream().map(Channel::getId)).toList();
        List<SystemAnalyticChartResponse.MonthSystemAnalytic> data = new ArrayList<>();
        for (LocalDate localDate = timeStart; localDate.isBefore(timeEnd); localDate = localDate.plusMonths(1)) {
            Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date dateAfterOneMonth = Date.from(localDate.plusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

            List<Task> tasks = taskRepository.findByGroupIdInAndCreatedDateBetween(channelIds, date, dateAfterOneMonth);

            long newGroups = groupRepository.countByCreatedDateBetweenAndCreatorId(date, dateAfterOneMonth, adminId);
            long newMessages = messageRepository.countByGroupIdInAndCreatedDateBetween(channelIds, date, dateAfterOneMonth);
            long newTasks = tasks.stream().map(task -> task.getAssignees().size()).reduce(0, Integer::sum);
            long newMeetings = meetingRepository.countByGroupIdInAndCreatedDateBetween(channelIds, date, dateAfterOneMonth);
            long newUsers = userRepository.countByCreatedDateBetween(date, dateAfterOneMonth);

            SystemAnalyticChartResponse.MonthSystemAnalytic monthSystemAnalytic = SystemAnalyticChartResponse.MonthSystemAnalytic.builder()
                    .month(date.getMonth() + 1)
                    .year(date.getYear() + 1900)
                    .newGroups(newGroups)
                    .newMessages(newMessages)
                    .newTasks(newTasks)
                    .newMeetings(newMeetings)
                    .newUsers(newUsers)
                    .build();
            data.add(monthSystemAnalytic);
        }
        return data;
    }

    @Override
    public ApiResponseDto<SystemAnalyticChartResponse> getDataForChartByGroupCategory(
            String emailUser,
            int monthStart,
            int yearStart,
            int monthEnd,
            int yearEnd,
            String groupCategoryId) {
        if (!permissionService.isAdmin(emailUser)) {
            return new ApiResponseDto<>(null, INVALID_PERMISSION, INVALID_PERMISSION_STRING);
        }
        LocalDate timeStart = LocalDate.of(yearStart, monthStart, 1);
        int lastDay = getLastDayOfMonth(yearEnd, monthEnd);
        LocalDate timeEnd = LocalDate.of(yearEnd, monthEnd, lastDay);
        if (timeEnd.isBefore(timeStart)) {
            return new ApiResponseDto<>(null, INVALID_TIME_RANGE, INVALID_TIME_RANGE_STRING);
        }
        ArrayList<SystemAnalyticChartResponse.MonthSystemAnalytic> data = new ArrayList<>();
        List<Group> groups;
        if (permissionService.isSuperAdmin(emailUser)) {
            groups = groupRepository.findAllByGroupCategory(groupCategoryId);
        } else {
            var adminId = Objects.requireNonNull(userRepository.findByEmail(emailUser).orElse(null)).getId();
            groups = groupRepository.findAllByCreatorId(adminId);
        }
        var channels = groups.stream().flatMap(g -> g.getChannels().stream()).toList();
        var channelIds = channels.stream().map(Channel::getId).collect(Collectors.toList());
        Set<User> users = groups.stream().flatMap(g -> g.getGroupUsers().stream().map(GroupUser::getUser)).collect(Collectors.toSet());

        for (LocalDate localDate = timeStart;
             localDate.isBefore(timeEnd);
             localDate = localDate.plusMonths(1)) {
            Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date dateAfterOneMonth = Date.from(localDate.plusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

            List<Task> tasks = taskRepository.findByGroupIdInAndCreatedDateBetween(channelIds, date, dateAfterOneMonth);

            long newGroups = groupRepository.countByGroupCategoryAndCreatedDateBetween(groupCategoryId, date, dateAfterOneMonth);
            long newMessages = messageRepository.countByGroupIdInAndCreatedDateBetween(channelIds, date, dateAfterOneMonth);
            long newTasks = tasks.stream().map(task -> task.getAssignees().size()).reduce(0, Integer::sum);
            long newMeetings = meetingRepository.countByGroupIdInAndCreatedDateBetween(channelIds, date, dateAfterOneMonth);
            long newUsers = users.stream().filter(user -> user.getCreatedDate().after(date) && user.getCreatedDate().before(dateAfterOneMonth)).count();

            SystemAnalyticChartResponse.MonthSystemAnalytic monthSystemAnalytic = SystemAnalyticChartResponse.MonthSystemAnalytic.builder()
                    .month(date.getMonth() + 1)
                    .year(date.getYear() + 1900)
                    .newGroups(newGroups)
                    .newMessages(newMessages)
                    .newTasks(newTasks)
                    .newMeetings(newMeetings)
                    .newUsers(newUsers)
                    .build();
            data.add(monthSystemAnalytic);
        }
        SystemAnalyticChartResponse response = SystemAnalyticChartResponse.builder().data(data).build();
        return new ApiResponseDto<>(response, SUCCESS, "");
    }

    private int getLastDayOfMonth(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        return cal.get(Calendar.DAY_OF_MONTH);
    }

    @Override
    public ApiResponseDto<GroupAnalyticResponse> getGroupAnalytic(String emailUser, String groupId) {
        if (!permissionService.isAdmin(emailUser)) {
            return new ApiResponseDto(null, INVALID_PERMISSION, INVALID_PERMISSION_STRING);
        }
        Optional<Group> groupOptional = groupRepository.findById(groupId);
        if (!groupOptional.isPresent()) {
            return new ApiResponseDto(null, NOT_FOUND_GROUP, "Not found group");
        }
        Group group = groupOptional.get();
        return new ApiResponseDto(getGeneralGroupAnalytic(group), SUCCESS, "");
    }

    private GroupAnalyticResponse getGeneralGroupAnalytic(Group group) {
        String groupId = group.getId();

        String categoryName = groupCategoryRepository
                .findById(group.getGroupCategory().getId())
                .map(GroupCategory::getName)
                .orElse(null);

        Date lastTimeActive = getLastTimeActive(groupId);

        long totalMessages = messageRepository.countByGroupId(groupId);

        List<Task> tasks = taskRepository.findAllByGroupIdIn(group.getChannels().stream().map(Channel::getId).toList());
        long totalTasks = getTotalTasks(tasks);

        long totalMeetings = meetingRepository.countByGroupId(groupId);

        List<GroupAnalyticResponse.Member> members = new ArrayList<>();
        addMembersToAnalytics(members, group.getMentors(), "MENTOR", group.getId());
        addMembersToAnalytics(members, group.getMentees(), "MENTEE", group.getId());

        return GroupAnalyticResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .category(categoryName)
                .lastTimeActive(lastTimeActive)
                .totalMentors(group.getMentors().size())
                .totalMentees(group.getMentees().size())
                .totalMessages(totalMessages)
                .totalMeetings(totalMeetings)
                .totalTasks(totalTasks)
                .status(group.getStatus())
                .members(members)
                .build();
    }

    private List<GroupAnalyticResponse.Member> getMembers(Group group) {
        List<GroupAnalyticResponse.Member> members = new ArrayList<>();
        if (group != null) {
            addMembersToAnalytics(members, group.getMentors(), "MENTOR", group.getId());
            addMembersToAnalytics(members, group.getMentees(), "MENTEE", group.getId());
        }

        return members;
    }

    private List<List<String>> generateExportGroupData(List<GroupAnalyticResponse.Member> members) {
        List<List<String>> data = new ArrayList<>();
        int index = 1;
        for (GroupAnalyticResponse.Member member : members) {
            List<String> row = new ArrayList<>();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            String lastTimeActive = "";
            if (member.getLastTimeActive() != null) {
                lastTimeActive = dateFormat.format(member.getLastTimeActive());
            }
            row.add(Integer.toString(index));
            row.add(member.getEmail());
            row.add(member.getName());
            row.add(member.getRole());
            row.add(Long.toString(member.getTotalMessages()));
            row.add(Long.toString(member.getTotalTasks()));
            row.add(Long.toString(member.getTotalMeetings()));
            row.add(lastTimeActive);

            data.add(row);
            index++;
        }

        return data;
    }

    @Override
    public ResponseEntity<Resource> generateExportGroupTable(List<GroupAnalyticResponse.Member> members, List<String> remainColumns) throws IOException {
        List<List<String>> data = generateExportGroupData(members);
        List<String> headers = Arrays.asList(
                "STT",
                "Email",
                "Họ tên",
                "Vai trò",
                "Số tin nhắn",
                "Số công việc",
                "Số cuộc hẹn tham gia",
                "Lần hoạt động gần nhất");
        String fileName = "output.xlsx";
        Map<String, Integer> indexMap = new HashMap<>();
        indexMap.put("email", 1);
        indexMap.put("name", 2);
        indexMap.put("role", 3);
        indexMap.put("totalMessages", 4);
        indexMap.put("totalTasks", 5);
        indexMap.put("totalMeetings", 6);
        indexMap.put("lastTimeActive", 7);
        List<Integer> remainColumnIndexes = new ArrayList<>();
        remainColumnIndexes.add(0);
        remainColumns.forEach(remainColumn -> {
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

    @Override
    public ResponseEntity<Resource> generateExportGroupTable(
            String emailUser, String groupId, List<String> remainColumns) throws IOException {
        Optional<Group> groupOptional = groupRepository.findById(groupId);
        Group group = groupOptional.orElse(null);
        List<GroupAnalyticResponse.Member> members = getMembers(group);
        return generateExportGroupTable(members, remainColumns);
    }

    @Override
    public ResponseEntity<Resource> generateExportGroupTableBySearchConditions(
            String emailUser, String groupId, FindUserAnalyticRequest request, List<String> remainColumns)
            throws IOException {
        List<GroupAnalyticResponse.Member> members =
                getUsersAnalyticBySearchConditions(emailUser, groupId, request);
        return generateExportGroupTable(members, remainColumns);
    }

    private void addMembersToAnalytics(
            List<GroupAnalyticResponse.Member> members,
            List<User> users,
            String role,
            String groupId) {
        for (User user : users) {
            long totalMessagesMember = messageRepository.countByChannelIdInAndSenderId(groupId, user.getId());
            long totalMeetingsMember = meetingRepository.countByGroupIdIdInAndIsMember(groupId, user.getId());

            long totalTasksMember = taskRepository.countAllOwnTaskOfGroup(groupId, user.getId());
            long totalDoneTasks = taskRepository.countAllOwnTaskOfGroupWithStatus(groupId, user.getId(), TaskStatus.DONE);

//            Date lastTimeTaskMember = Optional.ofNullable(taskRepository.findLatestOwnTaskByGroup(groupId, user.getId())).map(Task::getCreatedDate).orElse(null);

            Date lastTimeMessageMember = Optional.ofNullable(messageRepository.findLatestOwnMessageByChannel(groupId, user.getId())).map(Message::getCreatedDate).orElse(null);
//            Date lastTimeMeetingMember = Optional.ofNullable(meetingRepository.findFirstByGroupIdAndOrganizerIdOrderByCreatedDateDesc(groupId, user.getId()))
//                    .map(Meeting::getCreatedDate)
//                    .orElse(null);
            Date lastTimeActiveMember = getLatestDate(lastTimeMessageMember, lastTimeTaskMember, lastTimeMeetingMember);

            GroupAnalyticResponse.Member member =
                    GroupAnalyticResponse.Member.builder()
                            .id(user.getId())
                            .email(user.getEmail())
                            .name(user.getName())
                            .role(role)
                            .totalMeetings(totalMeetingsMember)
                            .totalTasks(totalTasksMember)
                            .totalMessages(totalMessagesMember)
                            .totalDoneTasks(totalDoneTasks)
                            .lastTimeActive(lastTimeActiveMember)
                            .trainingPoint(user.getTrainingPoint())
                            .hasEnglishCert(user.getHasEnglishCert())
                            .studyingPoint(user.getStudyingPoint())
                            .build();
            members.add(member);
        }
    }

    private Date getLatestDate(Date... dates) {
        return Arrays.stream(dates).filter(Objects::nonNull).max(Date::compareTo).orElse(null);
    }

    private long getTotalTasks(List<Task> tasks) {
        return tasks.stream().map(task -> task.getAssignees().size()).reduce(0, Integer::sum);
    }

    private Date getLastTimeActive(List<String> channelIds) {
        Date lastTimeMessage = Optional.ofNullable(messageRepository.findFirstByChannelIdInOrderByCreatedDateDesc(channelIds))
                        .map(Message::getCreatedDate)
                        .orElse(null);

        Date lastTimeTask = Optional.ofNullable(taskRepository.findFirstByGroupIdInOrderByCreatedDateDesc(channelIds))
                        .map(Task::getCreatedDate)
                        .orElse(null);

        Date lastTimeMeeting = Optional.ofNullable(meetingRepository.findFirstByGroupIdInOrderByCreatedDateDesc(channelIds))
                        .map(Meeting::getCreatedDate)
                        .orElse(null);

        return getLatestDate(lastTimeMessage, lastTimeTask, lastTimeMeeting);
    }

    private List<GroupGeneralResponse> getGroupGeneralAnalyticFromGroups(List<Group> groups) {
        return groups.stream().map(this::getGroupGeneralAnalytic).toList();
    }

    private GroupGeneralResponse getGroupGeneralAnalytic(Group group) {
        if (group.getStatus() != GroupStatus.DELETED && group.getStatus() != GroupStatus.DISABLED) {
            if (group.getTimeEnd().before(new Date())) {
                group.setStatus(GroupStatus.OUTDATED);
                groupRepository.save(group);
            }
            if (group.getTimeStart().after(new Date())) {
                group.setStatus(GroupStatus.INACTIVE);
                groupRepository.save(group);
            }
        }
        var channelIds = group.getChannels().stream().map(Channel::getId).toList();
        String categoryName = groupCategoryRepository
                .findById(group.getGroupCategory().getId())
                .map(GroupCategory::getName)
                .orElse(null);

        Date lastTimeActive = getLastTimeActive(group.getId());

        long totalMessages = messageRepository.countByGroupIdIn(channelIds);
        List<Task> tasks = taskRepository.findAllByGroupIdIn(channelIds);
        long totalTasks = getTotalTasks(tasks);
        long totalMeetings = meetingRepository.countByGroupIdIn(channelIds);

        long totalDoneTasks = tasks.stream()
                .map(t -> t.getAssignees().stream().filter(a -> a.getStatus().equals(TaskStatus.DONE)).count())
                .reduce(0L, Long::sum);

        return GroupGeneralResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .category(categoryName)
                .totalMentees(group.getGroupUsers().stream().filter(GroupUser::isMentor).count())
                .totalMentors(group.getGroupUsers().stream().filter(gu -> !gu.isMentor()).count())
                .lastTimeActive(lastTimeActive)
                .totalMeetings(totalMeetings)
                .totalTasks(totalTasks)
                .totalMessages(totalMessages)
                .totalDoneTasks(totalDoneTasks)
                .status(group.getStatus())
                .build();
    }

    @Override
    public ApiResponseDto getGroupGeneralAnalytic(
            String emailUser, Pageable pageRequest) {
        if (!permissionService.isAdmin(emailUser)) {
            return new ApiResponseDto(null, INVALID_PERMISSION, INVALID_PERMISSION_STRING);
        }
        Page<Group> groups;
        if (permissionService.isSuperAdmin(emailUser)) {
            groups = groupRepository.findAll(pageRequest);
        } else {
            var adminId = Objects.requireNonNull(userRepository.findByEmail(emailUser).orElse(null)).getId();
            groups = groupRepository.findAllByCreatorId(pageRequest, adminId);
        }
        List<GroupGeneralResponse> responses = getGroupGeneralAnalyticFromGroups(groups.toList());
        Page<GroupGeneralResponse> responsesPage =
                new PageImpl<>(responses, pageRequest, groups.getTotalElements());
        return new ApiResponseDto(pagingResponse(responsesPage), SUCCESS, "");
    }

    private List<GroupGeneralResponse> getAllGroupGeneralAnalytic(String emailUser) {
        List<Group> groups;
        boolean isSuperAdmin = permissionService.isSuperAdmin(emailUser);
        if (isSuperAdmin) {
            groups = groupRepository.findAllByOrderByCreatedDate();
        } else {
            String creatorId = userRepository.findByEmail(emailUser).get().getId();
            groups = groupRepository.findAllByCreatorIdOrderByCreatedDate(creatorId);
        }
        return getGroupGeneralAnalyticFromGroups(groups);
    }

    private List<List<String>> generateExportDataAllGroupGeneralAnalytic(List<GroupGeneralResponse> groups) {
        List<List<String>> data = new ArrayList<>();
        int index = 1;
        for (GroupGeneralResponse group : groups) {
            List<String> row = new ArrayList<>();

            Map<GroupStatus, String> statusMap = Group.getStatusMap();
            String status = statusMap.get(group.getStatus());
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            String lastTimeActive = "";
            if (group.getLastTimeActive() != null) {
                lastTimeActive = dateFormat.format(group.getLastTimeActive());
            }

            row.add(Integer.toString(index));
            row.add(group.getName());
            row.add(group.getCategory());
            row.add(status);
            row.add(Long.toString(group.getTotalMentors()));
            row.add(Long.toString(group.getTotalMentees()));
            row.add(Long.toString(group.getTotalMessages()));
            row.add(Long.toString(group.getTotalTasks()));
            row.add(Long.toString(group.getTotalMeetings()));
            row.add(lastTimeActive);

            data.add(row);
            index++;
        }
        return data;
    }

    private ResponseEntity<Resource> generateExportGroupsTable(
            List<GroupGeneralResponse> groups, List<String> remainColumns) throws IOException {
        List<List<String>> data = generateExportDataAllGroupGeneralAnalytic(groups);
        List<String> headers = Arrays.asList(
                "STT",
                "Tên nhóm",
                "Loại nhóm",
                "Trạng thái",
                "Tổng số mentor",
                "Tổng số mentee",
                "Tổng số tin nhắn",
                "Tổng số công việc",
                "Tổng số lịch hẹn",
                "Lần hoạt động gần nhất");
        String fileName = "output.xlsx";
        Map<String, Integer> indexMap = new HashMap<>();
        indexMap.put("name", 1);
        indexMap.put("category", 2);
        indexMap.put("status", 3);
        indexMap.put("totalMentors", 4);
        indexMap.put("totalMentees", 5);
        indexMap.put("totalMessages", 6);
        indexMap.put("totalTasks", 7);
        indexMap.put("totalMeetings", 8);
        indexMap.put("lastTimeActive", 9);
        return getResourceResponseEntity(remainColumns, data, headers, fileName, indexMap);
    }

    @NotNull
    static ResponseEntity<Resource> getResourceResponseEntity(List<String> remainColumns, List<List<String>> data, List<String> headers, String fileName, Map<String, Integer> indexMap) throws IOException {
        List<Integer> remainColumnIndexes = new ArrayList<>();
        remainColumnIndexes.add(0);
        remainColumns.forEach(remainColumn -> {
            if (indexMap.containsKey(remainColumn)) {
                remainColumnIndexes.add(indexMap.get(remainColumn));
            }
        });
        File exportFile = FileUtils.generateExcel(headers, data, remainColumnIndexes, fileName);
        Resource resource = new FileSystemResource(exportFile.getAbsolutePath());
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + resource.getFilename())
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .contentLength(resource.getFile().length())
                .body(resource);
    }

    @Override
    public ResponseEntity<Resource> generateExportGroupsTable(
            String emailUser, List<String> remainColumns) throws IOException {
        List<GroupGeneralResponse> groups = getAllGroupGeneralAnalytic(emailUser);
        return generateExportGroupsTable(groups, remainColumns);
    }

    @Override
    public ResponseEntity<Resource> generateExportGroupsTableBySearchConditions(
            String emailUser, FindGroupGeneralAnalyticRequest request, List<String> remainColumns)
            throws IOException {
        List<GroupGeneralResponse> groups = getGroupGeneralAnalyticBySearchConditions(emailUser, request);
        return generateExportGroupsTable(groups, remainColumns);
    }

//    private List<GroupGeneralResponse> getGroupGeneralAnalyticBySearchConditions(
//            String emailUser, FindGroupGeneralAnalyticRequest request) {
//        String groupName = request.getGroupName();
//        String groupCategory = request.getGroupCategory();
//        GroupStatus status = request.getStatus();
//        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
//        CriteriaQuery<Group> query = cb.createQuery(Group.class);
//        Root<Group> root = query.from(Group.class);
//
//
//        if (groupName != null && !groupName.isEmpty()) {
//            query.addCriteria(Criteria.where("name").regex(groupName, "i"));
//        }
//
//        if (groupCategory != null && !groupCategory.isEmpty()) {
//            query.addCriteria(Criteria.where("groupCategory").is(groupCategory));
//        }
//
//        if (status != null) {
//            query.addCriteria(Criteria.where("status").is(status));
//        }
//
//        GroupServiceImpl.validateSupperAdmin(emailUser, query, permissionService, userRepository);
//        List<Group> groups = mongoTemplate.find(query, Group.class);
//
//        List<GroupGeneralResponse> responses = getGroupGeneralAnalyticFromGroups(groups);
//        if (request.getTimeStart() != null && request.getTimeEnd() != null) {
//            responses =responses.stream().filter(response ->
//                            response.getLastTimeActive() != null
//                            && response.getLastTimeActive().after(request.getTimeStart())
//                            && response.getLastTimeActive().before(request.getTimeEnd()))
//                            .toList();
//        }
//        return responses;
//    }
    // TODO: Make sure this function works correctly

    private List<GroupGeneralResponse> getGroupGeneralAnalyticBySearchConditions(String emailUser, FindGroupGeneralAnalyticRequest request) {
        String groupName = request.getGroupName();
        String groupCategory = request.getGroupCategory();
        GroupStatus status = request.getStatus();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Group> query = cb.createQuery(Group.class);
        Root<Group> root = query.from(Group.class);

        List<Predicate> predicates = new ArrayList<>();

        if (groupName != null && !groupName.isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("name")), "%" + groupName.toLowerCase() + "%"));
        }

        if (groupCategory != null && !groupCategory.isEmpty()) {
            predicates.add(cb.equal(root.get("groupCategory"), groupCategory));
        }

        if (status != null) {
            predicates.add(cb.equal(root.get("status"), status));
        }

        query.where(predicates.toArray(new Predicate[0]));

        List<Group> groups = entityManager.createQuery(query).getResultList();

        List<GroupGeneralResponse> responses = getGroupGeneralAnalyticFromGroups(groups);

        if (request.getTimeStart() != null && request.getTimeEnd() != null) {
            responses = responses.stream()
                    .filter(response -> response.getLastTimeActive() != null &&
                            response.getLastTimeActive().after(request.getTimeStart()) &&
                            response.getLastTimeActive().before(request.getTimeEnd()))
                    .toList();
        }

        return responses;
    }

    @Override
    public ApiResponseDto findGroupGeneralAnalytic(
            String emailUser, Pageable pageRequest, FindGroupGeneralAnalyticRequest request) {
        if (!permissionService.isAdmin(emailUser)) {
            return new ApiResponseDto(null, INVALID_PERMISSION, INVALID_PERMISSION_STRING);
        }
        List<GroupGeneralResponse> responses =
                getGroupGeneralAnalyticBySearchConditions(emailUser, request);

        long offset = (long) pageRequest.getPageNumber() * pageRequest.getPageSize();
        List<GroupGeneralResponse> pagedResponses =
                responses.stream()
                        .skip(offset)
                        .limit(pageRequest.getPageSize())
                        .toList();

        Page<GroupGeneralResponse> responsesPage =
                new PageImpl<>(pagedResponses, pageRequest, responses.size());
        return new ApiResponseDto(pagingResponse(responsesPage), SUCCESS, "");
    }

    //    private List<GroupAnalyticResponse.Member> getUsersAnalyticBySearchConditions(
//            String emailUser, String groupId, FindUserAnalyticRequest request) {
//        Query query = new Query();
//        String name = request.getName();
//        String email = request.getEmail();
//        FindUserAnalyticRequest.Role role = request.getRole();
//
//        if (name != null && !name.isEmpty()) {
//            query.addCriteria(Criteria.where("name").regex(name, "i"));
//        }
//        if (email != null && !email.isEmpty()) {
//            query.addCriteria(Criteria.where("email").regex(email, "i"));
//        }
//
//        query.with(Sort.by(Sort.Direction.DESC, "createdDate"));
//
//        List<User> users = mongoTemplate.find(query, User.class);
//
//        List<String> mentorIds = new ArrayList<>();
//        List<String> menteeIds = new ArrayList<>();
//
//        for (User user : users) {
//            if (groupRepository.existsByIdAndMentorsIn(groupId, user.getId())) {
//                mentorIds.add(user.getId());
//            }
//            if (groupRepository.existsByIdAndMenteesIn(groupId, user.getId())) {
//                menteeIds.add(user.getId());
//            }
//        }
//        List<GroupAnalyticResponse.Member> members = new ArrayList<>();
//        if (role != null) {
//            if (role.equals(FindUserAnalyticRequest.Role.MENTOR)) {
//                addMembersToAnalytics(members, mentorIds, "MENTOR", groupId);
//            } else {
//                addMembersToAnalytics(members, menteeIds, "MENTEE", groupId);
//            }
//        } else {
//            addMembersToAnalytics(members, mentorIds, "MENTOR", groupId);
//            addMembersToAnalytics(members, menteeIds, "MENTEE", groupId);
//        }
//        if (request.getTimeStart() != null && request.getTimeEnd() != null) {
//            members =
//                    members.stream()
//                            .filter(
//                                    response ->
//                                            response.getLastTimeActive() != null
//                                                    && response.getLastTimeActive().after(request.getTimeStart())
//                                                    && response.getLastTimeActive().before(request.getTimeEnd()))
//                            .toList();
//        }
//        return members;
//    }
    // TODO: Make sure this function works correctly
    private List<GroupAnalyticResponse.Member> getUsersAnalyticBySearchConditions(String emailUser, String groupId, FindUserAnalyticRequest request) {
        String name = request.getName();
        String email = request.getEmail();
        FindUserAnalyticRequest.Role role = request.getRole();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> criteriaQuery = cb.createQuery(User.class);
        Root<User> root = criteriaQuery.from(User.class);

        List<Predicate> predicates = new ArrayList<>();

        if (name != null && !name.isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
        }
        if (email != null && !email.isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%"));
        }

        criteriaQuery.where(predicates.toArray(new Predicate[0]));
        criteriaQuery.orderBy(cb.desc(root.get("createdDate")));

        List<User> users = entityManager.createQuery(criteriaQuery).getResultList();

        List<User> mentors = new ArrayList<>();
        List<User> mentees = new ArrayList<>();

        for (User user : users) {
            var gu = groupUserRepository.findAllB
            if (groupRepository.existsByIdAndMentorsIn(groupId, user.getId())) {
                mentors.add(user);
            }
            if (groupRepository.existsByIdAndMenteesIn(groupId, user.getId())) {
                mentees.add(user);
            }
        }

        List<GroupAnalyticResponse.Member> members = new ArrayList<>();

        if (role != null) {
            if (role.equals(FindUserAnalyticRequest.Role.MENTOR)) {
                addMembersToAnalytics(members, mentors, "MENTOR", groupId);
            } else {
                addMembersToAnalytics(members, mentees, "MENTEE", groupId);
            }
        } else {
            addMembersToAnalytics(members, mentors, "MENTOR", groupId);
            addMembersToAnalytics(members, mentees, "MENTEE", groupId);
        }

        if (request.getTimeStart() != null && request.getTimeEnd() != null) {
            members = members.stream()
                    .filter(response ->
                            response.getLastTimeActive() != null &&
                                    response.getLastTimeActive().after(request.getTimeStart()) &&
                                    response.getLastTimeActive().before(request.getTimeEnd()))
                    .toList();
        }

        return members;
    }

    @Override
    public ApiResponseDto<List<GroupAnalyticResponse.Member>> findUserAnalytic(String emailUser, String groupId, FindUserAnalyticRequest request) {
        if (!permissionService.isAdmin(emailUser)) {
            return new ApiResponseDto<>(null, INVALID_PERMISSION, INVALID_PERMISSION_STRING);
        }

        List<GroupAnalyticResponse.Member> members = getUsersAnalyticBySearchConditions(emailUser, groupId, request);
        return new ApiResponseDto<>(members, SUCCESS, null);
    }

    private Map<String, Object> pagingResponse(Page<GroupGeneralResponse> groups) {
        Map<String, Object> response = new HashMap<>();
        response.put("groups", groups.getContent());
        response.put("currentPage", groups.getNumber());
        response.put("totalItems", groups.getTotalElements());
        response.put("totalPages", groups.getTotalPages());
        return response;
    }

    @Override
    public ApiResponseDto<Map<String, String>> importData(
            String emailUser, MultipartFile file, String type) throws IOException {
        if (!permissionService.isAdmin(emailUser)) {
            return new ApiResponseDto<>(null, INVALID_PERMISSION, INVALID_PERMISSION_STRING);
        }
        Map<String, String> data = parseExcelTwoColumns(file);
        ApiResponseDto<Map<String, String>> isValidData = validateData(data, type);
        if (!Objects.equals(isValidData.getReturnCode(), SUCCESS)) {
            return isValidData;
        }
        for (Map.Entry<String, String> entry : data.entrySet()) {
            String email = entry.getKey();
            String value = entry.getValue();
            User user = userRepository.findByEmail(email).get();
            switch (type) {
                case "TRAINING_POINT":
                    user.setTrainingPoint(Integer.parseInt(value));
                    break;
                case "HAS_ENGLISH_CERT":
                    user.setHasEnglishCert(value.equals("Có"));
                    break;
                case "STUDYING_POINT":
                    user.setStudyingPoint(Double.parseDouble(value));
                    break;
                default:
                    break;
            }
            userRepository.save(user);
        }

        return new ApiResponseDto<>(data, SUCCESS, "");
    }

    private ApiResponseDto<Map<String, String>> validateData(Map<String, String> data, String type) {
        Map<String, String> invalidValues = new HashMap<>();
        Map<String, String> notFoundsUser = new HashMap<>();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            String email = entry.getKey();
            String value = entry.getValue();
            Optional<User> user = userRepository.findByEmail(email);
            if (user.isEmpty()) {
                notFoundsUser.put(email, value);
            }

            switch (type) {
                case "TRAINING_POINT":
                    if (Boolean.FALSE.equals(isValidTrainingPoint(value))) {
                        invalidValues.put(email, value);
                    }
                    break;
                case "HAS_ENGLISH_CERT":
                    if (Boolean.FALSE.equals(isValidHasEnglishCert(value))) {
                        invalidValues.put(email, value);
                    }
                    break;
                case "STUDYING_POINT":
                    if (Boolean.FALSE.equals(isValidStudyingPoint(value))) {
                        invalidValues.put(email, value);
                    }
                    break;
                default:
                    break;
            }
        }
        if (!notFoundsUser.isEmpty()) {
            return new ApiResponseDto<>(notFoundsUser, NOT_FOUND_USER, "Not found users");
        }
        if (!invalidValues.isEmpty()) {
            return new ApiResponseDto<>(invalidValues, INVALID_VALUE, "Invalid values");
        }

        return new ApiResponseDto<>(null, SUCCESS, "");
    }

    private Boolean isValidTrainingPoint(String value) {
        int trainingPoint = Integer.parseInt(value);
        return trainingPoint <= 100 && trainingPoint >= 0;
    }

    private Boolean isValidHasEnglishCert(String value) {
        return value.equals("Có") || value.equals("Không");
    }

    private Boolean isValidStudyingPoint(String value) {
        double studyingPoint = Double.parseDouble(value);
        return studyingPoint <= 10 && studyingPoint >= 0;
    }

    private ApiResponseDto<List<ImportGeneralInformationResponse>> validateMultipleData(Map<String, List<String>> data) {
        List<ImportGeneralInformationResponse> invalidValues = new ArrayList<>();
        List<ImportGeneralInformationResponse> notFoundsUser = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : data.entrySet()) {
            String email = entry.getKey();
            List<String> value = entry.getValue();
            Optional<User> user = userRepository.findByEmail(email);
            ImportGeneralInformationResponse generalInformation = new ImportGeneralInformationResponse();

            boolean isNotFoundUser = false;
            generalInformation.setEmail(email);
            if (user.isEmpty()) {
                isNotFoundUser = true;
            }
            boolean isInvalid = false;
            if (Boolean.FALSE.equals(isValidTrainingPoint(value.get(0)))) {
                generalInformation.setTrainingPoint(value.getFirst());
                isInvalid = true;
            }
            if (Boolean.FALSE.equals(isValidHasEnglishCert(value.get(1)))) {
                generalInformation.setHasEnglishCert(value.get(1));
                isInvalid = true;
            }
            if (Boolean.FALSE.equals(isValidStudyingPoint(value.get(2)))) {
                generalInformation.setStudyingPoint(value.get(2));
                isInvalid = true;
            }
            if (isInvalid) {
                invalidValues.add(generalInformation);
            }
            if (isNotFoundUser) {
                notFoundsUser.add(generalInformation);
            }
        }

        if (!notFoundsUser.isEmpty()) {
            return new ApiResponseDto<>(notFoundsUser, NOT_FOUND_USER, NOT_FOUND_USER_STRING);
        }
        if (!invalidValues.isEmpty()) {
            return new ApiResponseDto<>(invalidValues, INVALID_VALUE, INVALID_VALUE_STRING);
        }
        return new ApiResponseDto<>(null, SUCCESS, "");
    }

    @Override
    public ApiResponseDto<List<ImportGeneralInformationResponse>> importMultipleData(String emailUser, MultipartFile file) throws IOException {
        if (!permissionService.isAdmin(emailUser)) {
            return new ApiResponseDto<>(null, INVALID_PERMISSION, INVALID_PERMISSION_STRING);
        }
        Map<String, List<String>> data = parseExcelFourColumns(file);
        ApiResponseDto<List<ImportGeneralInformationResponse>> isValidMultipleData = validateMultipleData(data);
        if (!Objects.equals(isValidMultipleData.getReturnCode(), SUCCESS)) {
            return isValidMultipleData;
        }
        List<ImportGeneralInformationResponse> dataResponse = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : data.entrySet()) {
            ImportGeneralInformationResponse generalInformation = new ImportGeneralInformationResponse();
            String email = entry.getKey();
            List<String> value = entry.getValue();
            int trainingPoint = Integer.parseInt(value.get(0));
            Boolean hasEnglishCert = value.get(1).equals("Có");
            double studyingPoint = Double.parseDouble(value.get(2));

            generalInformation.setEmail(email);
            generalInformation.setTrainingPoint(value.get(0));
            generalInformation.setHasEnglishCert(value.get(1));
            generalInformation.setStudyingPoint(value.get(2));
            dataResponse.add(generalInformation);

            Optional<User> userWrapper = userRepository.findByEmail(email);
            if (userWrapper.isPresent()) {
                User user = userWrapper.get();
                user.setTrainingPoint(trainingPoint);
                user.setHasEnglishCert(hasEnglishCert);
                user.setStudyingPoint(studyingPoint);
                userRepository.save(user);
            }
        }

        return new ApiResponseDto<>(dataResponse, SUCCESS, "");
    }

    private Map<String, String> parseExcelTwoColumns(MultipartFile file) throws IOException {
        Workbook workbook = WorkbookFactory.create(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        Map<String, String> data = new HashMap<>();
        int i = 0;
        for (Row row : sheet) {
            if (i == 0) {
                i++;
                continue;
            }
            Cell keyCell = row.getCell(0);
            Cell valueCell = row.getCell(1);
            if (keyCell != null && valueCell != null) {
                String key = keyCell.getStringCellValue();
                DataFormatter formatter =
                        new DataFormatter(); // creating formatter using the default locale
                String value = formatter.formatCellValue(valueCell);
                // String value = valueCell.getStringCellValue();
                data.put(key, value);
            }
        }
        workbook.close();
        return data;
    }

    private Map<String, List<String>> parseExcelFourColumns(MultipartFile file) throws IOException {
        Workbook workbook = WorkbookFactory.create(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        Map<String, List<String>> data = new HashMap<>();
        int i = 0;
        for (Row row : sheet) {
            if (i == 0) {
                i++;
                continue;
            }
            List<String> rowData = new ArrayList<>();
            Cell keyCell = row.getCell(0);
            Cell trainingPointCell = row.getCell(1);
            Cell hasEnglishCertCell = row.getCell(2);
            Cell studyingPointCell = row.getCell(3);

            if (keyCell != null
                    && trainingPointCell != null
                    && hasEnglishCertCell != null
                    && studyingPointCell != null) {
                String key = keyCell.getStringCellValue();
                DataFormatter formatter =
                        new DataFormatter(); // creating formatter using the default locale
                String trainingPoint = formatter.formatCellValue(trainingPointCell);
                String hasEnglishCert = formatter.formatCellValue(hasEnglishCertCell);
                String studyingPoint = formatter.formatCellValue(studyingPointCell);
                rowData.add(trainingPoint);
                rowData.add(hasEnglishCert);
                rowData.add(studyingPoint);
                data.put(key, rowData);
            }
        }
        workbook.close();
        return data;
    }

    @Override
    public ApiResponseDto<User> updateStudentInformation(String emailUser, String userId, UpdateStudentInformationRequest request) {
        if (!permissionService.isAdmin(emailUser)) {
            return new ApiResponseDto<>(null, INVALID_PERMISSION, INVALID_PERMISSION_STRING);
        }
        var user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return new ApiResponseDto<>(null, NOT_FOUND_USER, "Not found user");
        }
        if (!isValidStudyingPoint(request.getStudyingPoint().toString()) || !isValidTrainingPoint(request.getTrainingPoint().toString())) {
            return new ApiResponseDto<>(null, INVALID_VALUE, "Invalid value");
        }
        user.update(request);
        userRepository.save(user);
        return new ApiResponseDto<>(user, SUCCESS, "");
    }

    @Override
    public String exportGroupReport(String exporterEmail, String groupId, WebContext context) {
        var group = groupRepository.findById(groupId).orElse(null);
        if (group == null) {
            return null;
        }
        if (!permissionService.isAdmin(exporterEmail)) {
            return null;
        }
        GroupAnalyticResponse data = getGeneralGroupAnalytic(group);
        Map<GroupStatus, String> statusMap = Group.getStatusMap();
        String status = statusMap.get(group.getStatus());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String groupTime = dateFormat.format(group.getTimeStart()) + " - " + dateFormat.format((group.getTimeEnd()));
        context.setVariable("GROUP_NAME", data.getName());
        context.setVariable("GROUP_CATEGORY", data.getCategory());
        context.setVariable("GROUP_STATUS", status);
        context.setVariable("GROUP_TIME", groupTime);
        if (data.getLastTimeActive() != null) {
            context.setVariable("LAST_TIME", dateFormat.format(data.getLastTimeActive()));
        }

        context.setVariable("TOTAL_MEMBERS", data.getMembers().size());
        context.setVariable("TOTAL_MENTORS", data.getTotalMentors());
        context.setVariable("TOTAL_MENTEES", data.getTotalMentees());

        context.setVariable("TOTAL_MESSAGES", data.getTotalMessages());
        context.setVariable("TOTAL_MEETINGS", data.getTotalMeetings());
        context.setVariable("TOTAL_TASK", data.getTotalTasks());

        context.setVariable("MEMBERS", data.getMembers());

        return templateEngine.process("reports/group-report", context);
    }

    @Override
    public byte[] getGroupLog(String exporterEmail, String groupId, List<AnalyticAttribute> attributes) throws IOException {
        if (attributes.isEmpty()) {
            return null;
        }

        if (!groupRepository.existsById(groupId)) {
            return null;
        }
        if (!permissionService.isAdmin(exporterEmail)) {
            return null;
        }

        Workbook workbook = new XSSFWorkbook();

        if (attributes.contains(AnalyticAttribute.MEETINGS)) {
            Sheet meetingsSheet = workbook.createSheet("Lịch hẹn");
            List<MeetingResponse> meetings = meetingRepository.findAllByGroupId(groupId);
            addMeetingsData(meetingsSheet, meetings);
        }
        if (attributes.contains(AnalyticAttribute.TASKS)) {
            Sheet tasksSheet = workbook.createSheet("Công việc");
            List<Task> tasks = taskRepository.findAllByGroupId(groupId);
            addTasksData(tasksSheet, tasks);
        }
        if (attributes.contains(AnalyticAttribute.MESSAGES)) {
            Sheet messagesSheet = workbook.createSheet("Tin nhắn");
            List<MessageResponse> messages = messageRepository.getAllGroupMessagesByGroupId(groupId);
            addMessagesData(messagesSheet, messages);
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        FileUtils.formatWorkbook(workbook);
        FileUtils.autoSizeColumns(workbook);
        workbook.write(outputStream);
        byte[] workbookBytes = outputStream.toByteArray();
        workbook.close();
        return workbookBytes;
    }

    private void setHeaders(Row headerRow, List<String> headers) {
        for (int i = 0; i < headers.size(); i++) {
            headerRow.createCell(i).setCellValue(headers.get(i));
        }
    }

    private void addMeetingsData(Sheet sheet, List<MeetingResponse> meetings) {
        Row headerRow = sheet.createRow(0);
        List<String> headers = Arrays.asList(
                "ID",
                "Tiêu đề",
                "Mô tả",
                "Thời gian bắt đầu",
                "Thời gian kết thúc",
                "Địa điểm",
                "Người tạo",
                "Lịch sử cuộc hẹn");
        setHeaders(headerRow, headers);

        int rowNum = 1;
        for (MeetingResponse meeting : meetings) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(meeting.getId());
            row.createCell(1).setCellValue(meeting.getTitle());
            row.createCell(2).setCellValue(meeting.getDescription());
            row.createCell(3).setCellValue(DateUtils.formatDate(meeting.getTimeStart()));
            row.createCell(4).setCellValue(DateUtils.formatDate(meeting.getTimeEnd()));
            row.createCell(5).setCellValue(meeting.getPlace());
            row.createCell(6).setCellValue(meeting.getOrganizer().toString());
            if (meeting.getHistories() != null) {
                row.createCell(7).setCellValue(meeting.getHistories().toString());
            }
        }
    }

    private void addTasksData(Sheet sheet, List<Task> tasks) {
        Row headerRow = sheet.createRow(0);
        List<String> headers = Arrays.asList("ID", "Tiêu đề", "Mô tả", "Ngày tới hạn", "Ngày tạo");
        setHeaders(headerRow, headers);

        int rowNum = 1;
        for (Task task : tasks) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(task.getId());
            row.createCell(1).setCellValue(task.getTitle());
            row.createCell(2).setCellValue(task.getDescription());
            row.createCell(3).setCellValue(DateUtils.formatDate(task.getDeadline()));
            row.createCell(4).setCellValue(DateUtils.formatDate(task.getCreatedDate()));
        }
    }

    private void addMessagesData(Sheet sheet, List<MessageResponse> messages) {
        Row headerRow = sheet.createRow(0);
        List<String> headers =
                Arrays.asList("ID", "Người gửi", "Nội dung", "Loại", "Ngày gửi", "Trạng thái");
        setHeaders(headerRow, headers);

        int rowNum = 1;
        for (MessageResponse message : messages) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(message.getId());
            row.createCell(1).setCellValue(message.getSender().toString());
            row.createCell(2).setCellValue(message.getContent());
            row.createCell(3).setCellValue(message.getType().toString());
            row.createCell(4).setCellValue(DateUtils.formatDate(message.getCreatedDate()));
            if (message.getStatus() != null) {
                row.createCell(5).setCellValue(message.getStatus().toString());
            }
        }
    }
}
