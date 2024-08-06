package com.hcmus.mentor.backend.service.impl;

import com.google.common.base.Strings;
import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.controller.payload.ReturnCodeConstants;
import com.hcmus.mentor.backend.controller.payload.request.groupcategories.FindGroupCategoryRequest;
import com.hcmus.mentor.backend.controller.payload.request.groupcategories.UpdateGroupCategoryRequest;
import com.hcmus.mentor.backend.controller.payload.request.messages.CreateGroupCategoryRequest;
import com.hcmus.mentor.backend.domain.AuditRecord;
import com.hcmus.mentor.backend.domain.Group;
import com.hcmus.mentor.backend.domain.GroupCategory;
import com.hcmus.mentor.backend.domain.constant.*;
import com.hcmus.mentor.backend.repository.GroupCategoryRepository;
import com.hcmus.mentor.backend.repository.GroupRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.service.AuditRecordService;
import com.hcmus.mentor.backend.service.GroupCategoryService;
import com.hcmus.mentor.backend.service.PermissionService;
import com.hcmus.mentor.backend.service.dto.GroupCategoryServiceDto;
import com.hcmus.mentor.backend.service.fileupload.BlobStorage;
import com.hcmus.mentor.backend.util.FileUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.math3.util.Pair;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.hcmus.mentor.backend.controller.payload.ReturnCodeConstants.INVALID_PERMISSION;
import static com.hcmus.mentor.backend.controller.payload.ReturnCodeConstants.SUCCESS;

@Service
@Transactional
@RequiredArgsConstructor
public class GroupCategoryServiceImpl implements GroupCategoryService {

    private final GroupCategoryRepository groupCategoryRepository;
    private final GroupRepository groupRepository;
    private final PermissionService permissionService;
    private final BlobStorage blobStorage;
    private final AuditRecordService auditRecordService;
    private final UserRepository userRepository;

    @Override
    public List<GroupCategory> findAll() {
        return groupCategoryRepository.findAll(Sort.by(Sort.Direction.DESC, "createdDate"));
    }

    @Override
    public GroupCategoryServiceDto findById(String id) {
        var groupCategory = groupCategoryRepository.findById(id);
        if (groupCategory.isEmpty()) {
            return new GroupCategoryServiceDto(ReturnCodeConstants.GROUP_CATEGORY_NOT_FOUND, "Not found group category", null);
        }
        return new GroupCategoryServiceDto(SUCCESS, "", groupCategory);
    }

    @Override
    public GroupCategoryServiceDto create(String emailUser, CreateGroupCategoryRequest request) {
        if (!permissionService.isAdminByEmail(emailUser)) {
            return new GroupCategoryServiceDto(INVALID_PERMISSION, "Không có quyền thêm loại nhóm.", null);
        }
        if (Strings.isNullOrEmpty(request.getName())) {
            return new GroupCategoryServiceDto(ReturnCodeConstants.GROUP_CATEGORY_NOT_ENOUGH_FIELDS, "Tên không được để trống", null);
        }
        if (Strings.isNullOrEmpty(request.getIconUrl())) {
            return new GroupCategoryServiceDto(ReturnCodeConstants.GROUP_CATEGORY_NOT_ENOUGH_FIELDS, "Icon không được để trống", null);
        }
        if (request.getPermissions() == null || request.getPermissions().isEmpty()) {
            return new GroupCategoryServiceDto(ReturnCodeConstants.GROUP_CATEGORY_NOT_ENOUGH_FIELDS, "Quyền không được để trống", null);
        }
        if (request.getPermissions().stream().anyMatch(Objects::isNull)) {
            return new GroupCategoryServiceDto(ReturnCodeConstants.GROUP_CATEGORY_NOT_ENOUGH_FIELDS, "Quyền không đúng", null);
        }

        if (groupCategoryRepository.existsByName(request.getName())) {
            GroupCategory groupCategory = groupCategoryRepository.findByName(request.getName());

            if (groupCategory.getStatus().equals(GroupCategoryStatus.DELETED)) {
                if (request.getIconUrl().startsWith("data:")) {
                    try {
                        var rawBytes = Base64.getDecoder().decode(request.getIconUrl().substring(request.getIconUrl().indexOf(",") + 1).getBytes(StandardCharsets.UTF_8));
                        var mimeType = request.getIconUrl().substring(request.getIconUrl().indexOf(":") + 1, request.getIconUrl().indexOf(";"));
                        String key = blobStorage.generateBlobKey(mimeType);
                        blobStorage.post(rawBytes, key);
                        request.setIconUrl(key);
                    } catch (Exception e) {
                        throw new DomainException("Upload image failed");
                    }
                }

                groupCategory.setStatus(GroupCategoryStatus.ACTIVE);
                groupCategory.setDescription(request.getDescription());
                groupCategory.setIconUrl(request.getIconUrl());
                groupCategory.setPermissions(request.getPermissions());

                groupCategoryRepository.save(groupCategory);

                auditRecordService.save(AuditRecord.builder()
                        .action(ActionType.CREATED)
                        .domain(DomainType.GROUP_CATEGORY)
                        .entityId(groupCategory.getId())
                        .detail(String.format("Tạo mới loại nhóm %s", groupCategory.getName()))
                        .user(userRepository.findByEmail(emailUser).orElse(null))
                        .build());

                return new GroupCategoryServiceDto(SUCCESS, "", groupCategory);
            }
            return new GroupCategoryServiceDto(ReturnCodeConstants.GROUP_CATEGORY_DUPLICATE_GROUP_CATEGORY, "Duplicate group category", null);
        }

        if (request.getIconUrl().startsWith("data:")) {
            try {
                var rawBytes = Base64.getDecoder().decode(request.getIconUrl().substring(request.getIconUrl().indexOf(",") + 1).getBytes(StandardCharsets.UTF_8));
                var mimeType = request.getIconUrl().substring(request.getIconUrl().indexOf(":") + 1, request.getIconUrl().indexOf(";"));
                String key = blobStorage.generateBlobKey(mimeType);
                blobStorage.post(rawBytes, key);
                request.setIconUrl(key);
            } catch (Exception e) {
                throw new DomainException("Upload image failed");
            }
        }

        GroupCategory groupCategory = GroupCategory.builder()
                .name(request.getName())
                .description(request.getDescription())
                .iconUrl(request.getIconUrl())
                .permissions(request.getPermissions())
                .build();
        groupCategoryRepository.save(groupCategory);
        return new GroupCategoryServiceDto(SUCCESS, "", groupCategory);
    }

    @Override
    public GroupCategoryServiceDto update(String emailUser, String id, UpdateGroupCategoryRequest request) {
        if (!permissionService.isAdminByEmail(emailUser)) {
            return new GroupCategoryServiceDto(INVALID_PERMISSION, "Invalid permission", null);
        }
        var groupCategory = groupCategoryRepository.findById(id).orElse(null);
        if (groupCategory == null) {
            return new GroupCategoryServiceDto(ReturnCodeConstants.GROUP_CATEGORY_NOT_FOUND, "Not found group category", null);
        }

        if (groupCategoryRepository.existsByName(request.getName()) && !request.getName().equals(groupCategory.getName())) {
            return new GroupCategoryServiceDto(ReturnCodeConstants.GROUP_CATEGORY_DUPLICATE_GROUP_CATEGORY, "Duplicate group category", null);
        }

        if (!Objects.equals(request.getIconUrl(), groupCategory.getIconUrl())) {
            if (request.getIconUrl().startsWith("data:")) {
                try {
                    var rawBytes = Base64.getDecoder().decode(request.getIconUrl().substring(request.getIconUrl().indexOf(",") + 1).getBytes(StandardCharsets.UTF_8));
                    var mimeType = request.getIconUrl().substring(request.getIconUrl().indexOf(":") + 1, request.getIconUrl().indexOf(";"));
                    String key = blobStorage.generateBlobKey(mimeType);
                    blobStorage.post(rawBytes, key);
                    request.setIconUrl(key);
                } catch (Exception e) {
                    throw new DomainException("Upload image failed");
                }
            }
        }


        var detail = new StringBuilder();
        if (!groupCategory.getName().equals(request.getName())) {
            detail.append("\n").append("Tên: ").append(groupCategory.getName());
        }
        if (!groupCategory.getDescription().equals(request.getDescription())) {
            detail.append("\n").append("Mô tả: ").append(groupCategory.getDescription());
        }

        if (!groupCategory.getIconUrl().equals(request.getIconUrl())) {
            detail.append("\n").append("Icon: ").append(groupCategory.getIconUrl());
        }

        if (!groupCategory.getPermissions().equals(request.getPermissions())) {
            var permissions = new StringBuilder();
            for (GroupCategoryPermission permission : request.getPermissions()) {
                permissions.append(permission.getDescription()).append(", ");
            }
            detail.append("\n").append("Quyền: ").append(permissions);
        }

        groupCategoryRepository.save(groupCategory);
        auditRecordService.save(AuditRecord.builder()
                .action(ActionType.UPDATED)
                .domain(DomainType.GROUP_CATEGORY)
                .entityId(groupCategory.getId())
                .detail(String.format("Cập nhật loại nhóm %s %s", groupCategory.getName(), detail))
                .user(userRepository.findByEmail(emailUser).orElse(null))
                .build());


        return new GroupCategoryServiceDto(SUCCESS, "", groupCategory);
    }

    @Override
    public GroupCategoryServiceDto delete(String emailUser, String id, String newGroupCategoryId) {
        if (!permissionService.isAdminByEmail(emailUser)) {
            return new GroupCategoryServiceDto(INVALID_PERMISSION, "Invalid permission", null);
        }
        var groupCategory = groupCategoryRepository.findById(id).orElse(null);
        if (groupCategory == null) {
            return new GroupCategoryServiceDto(ReturnCodeConstants.GROUP_CATEGORY_NOT_FOUND, "Not found group category", null);
        }

        List<Group> groups = groupRepository.findAllByGroupCategoryId(id);
        if (!newGroupCategoryId.isEmpty()) {
            groupCategory = groupCategoryRepository.findById(newGroupCategoryId).orElse(null);
            if (groupCategory == null) {
                return new GroupCategoryServiceDto(ReturnCodeConstants.GROUP_CATEGORY_NOT_FOUND, "Not found new group category", newGroupCategoryId);
            }
            for (Group group : groups) {
                group.setGroupCategory(groupCategory);
                groupRepository.save(group);
            }
        } else {
            for (Group group : groups) {
                group.setStatus(GroupStatus.DELETED);
            }
            groupRepository.saveAll(groups);
        }

        groupCategory.setStatus(GroupCategoryStatus.DELETED);
        groupCategoryRepository.save(groupCategory);
        auditRecordService.save(AuditRecord.builder()
                .action(ActionType.DELETED)
                .domain(DomainType.GROUP_CATEGORY)
                .entityId(groupCategory.getId())
                .detail(String.format("Xoá loại nhóm %s", groupCategory.getName()))
                .user(userRepository.findByEmail(emailUser).orElse(null))
                .build());
        return new GroupCategoryServiceDto(SUCCESS, "", groupCategory);
    }

    private Pair<Long, List<GroupCategory>> getGroupCategoriesBySearchConditions(
            FindGroupCategoryRequest request, int page, int pageSize) {

        int offset = page * pageSize;
        String name = request.getName();
        String description = request.getDescription();

        GroupCategoryStatus status = null;
        if (request.getStatus() != null) {
            status = GroupCategoryStatus.valueOf(request.getStatus().toUpperCase());
        }
        List<GroupCategory> groupCategories = groupCategoryRepository.findGroupCategoriesBySearchConditions(name, description, status, pageSize, offset);
        long count = groupCategoryRepository.countGroupCategoriesBySearchConditions(name, description, status);

        return new Pair<>(count, groupCategories);
    }

    @Override
    public GroupCategoryServiceDto findGroupCategories(
            String emailUser, FindGroupCategoryRequest request, int page, int pageSize) {
        if (!permissionService.isAdminByEmail(emailUser)) {
            return new GroupCategoryServiceDto(INVALID_PERMISSION, "Invalid permission", null);
        }
        Pair<Long, List<GroupCategory>> groupCategories = getGroupCategoriesBySearchConditions(request, page, pageSize);
        return new GroupCategoryServiceDto(SUCCESS, "", new PageImpl<>(groupCategories.getValue(), PageRequest.of(page, pageSize), groupCategories.getKey()));
    }

    @Override
    public GroupCategoryServiceDto deleteMultiple(String emailUser, List<String> ids, String newGroupCategoryId) {
        if (!permissionService.isAdminByEmail(emailUser)) {
            return new GroupCategoryServiceDto(INVALID_PERMISSION, "Invalid permission", null);
        }

        List<String> notFoundIds = new ArrayList<>();
        for (String id : ids) {
            groupCategoryRepository.findById(id).ifPresentOrElse(
                    groupCategory -> {
                        if (groupCategory.getStatus().equals(GroupCategoryStatus.DELETED)) {
                            notFoundIds.add(id);
                        }
                    },
                    () -> notFoundIds.add(id));
        }
        if (!notFoundIds.isEmpty()) {
            return new GroupCategoryServiceDto(ReturnCodeConstants.GROUP_CATEGORY_NOT_FOUND, "Not found group category", notFoundIds);
        }

        List<Group> groups = groupRepository.findAllByGroupCategoryIdIn(ids);
        List<GroupCategory> groupCategories = groupCategoryRepository.findByIdIn(ids);

        if (!newGroupCategoryId.isEmpty()) {
            var groupCategory = groupCategoryRepository.findById(newGroupCategoryId).orElse(null);
            if (groupCategory == null) {
                return new GroupCategoryServiceDto(ReturnCodeConstants.GROUP_CATEGORY_NOT_FOUND, "Not found new group category", newGroupCategoryId);
            }
            for (Group group : groups) {
                group.setGroupCategory(groupCategory);
                groupRepository.save(group);
            }
        } else {
            groups.forEach(group -> group.setStatus(GroupStatus.DELETED));
            groupRepository.saveAll(groups);
        }
        var auditRecords = new ArrayList<AuditRecord>();
        groupCategories.forEach(groupCategory ->
        {
            groupCategory.setStatus(GroupCategoryStatus.DELETED);
            auditRecords.add(AuditRecord.builder()
                    .action(ActionType.DELETED)
                    .domain(DomainType.GROUP_CATEGORY)
                    .entityId(groupCategory.getId())
                    .detail(String.format("Xoá loại nhóm %s", groupCategory.getName()))
                    .user(userRepository.findByEmail(emailUser).orElse(null))
                    .build());
        });

        groupCategoryRepository.saveAll(groupCategories);
        auditRecordService.saveAll(auditRecords);
        return new GroupCategoryServiceDto(SUCCESS, "", groupCategories);
    }

    private List<List<String>> generateExportData(List<GroupCategory> groupCategories) {
        List<List<String>> data = new ArrayList<>();
        int index = 1;
        for (GroupCategory groupCategory : groupCategories) {
            List<String> row = new ArrayList<>();

            row.add(Integer.toString(index));
            row.add(groupCategory.getName());
            row.add(groupCategory.getDescription());
            if (groupCategory.getStatus().equals(GroupCategoryStatus.ACTIVE)) {
                row.add("Đang hoạt động");
            } else {
                row.add("Đã xoá");
            }

            data.add(row);
            index++;
        }

        return data;
    }

    private ResponseEntity<Resource> generateExportTable(
            List<GroupCategory> groupCategories, List<String> remainColumns) throws IOException {
        List<List<String>> data = generateExportData(groupCategories);
        List<String> headers = Arrays.asList("STT", "Tên", "Mô tả", "Trạng thái");
        String fileName = "output.xlsx";
        Map<String, Integer> indexMap = new HashMap<>();
        indexMap.put("name", 1);
        indexMap.put("description", 2);
        indexMap.put("status", 3);
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

    @Override
    public ResponseEntity<Resource> generateExportTable(String emailUser, List<String> remainColumns)
            throws IOException {
        List<GroupCategory> groupCategories = groupCategoryRepository.findAll();
        ResponseEntity<Resource> response = generateExportTable(groupCategories, remainColumns);
        return response;
    }

    @Override
    public ResponseEntity<Resource> generateExportTableBySearchConditions(
            String emailUser, FindGroupCategoryRequest request, List<String> remainColumns)
            throws IOException {
        List<GroupCategory> groupCategories =
                getGroupCategoriesBySearchConditions(request, 0, Integer.MAX_VALUE).getValue();
        ResponseEntity<Resource> response = generateExportTable(groupCategories, remainColumns);
        return response;
    }

}