package com.hcmus.mentor.backend.service.impl;

import static com.hcmus.mentor.backend.controller.payload.returnCode.GroupCategoryReturnCode.DUPLICATE_GROUP_CATEGORY;
import static com.hcmus.mentor.backend.controller.payload.returnCode.GroupCategoryReturnCode.NOT_ENOUGH_FIELDS;
import static com.hcmus.mentor.backend.controller.payload.returnCode.GroupCategoryReturnCode.NOT_FOUND;
import static com.hcmus.mentor.backend.controller.payload.returnCode.InvalidPermissionCode.INVALID_PERMISSION;
import static com.hcmus.mentor.backend.controller.payload.returnCode.SuccessCode.SUCCESS;

import com.hcmus.mentor.backend.domain.Group;
import com.hcmus.mentor.backend.domain.GroupCategory;
import com.hcmus.mentor.backend.controller.payload.request.CreateGroupCategoryRequest;
import com.hcmus.mentor.backend.controller.payload.request.FindGroupCategoryRequest;
import com.hcmus.mentor.backend.controller.payload.request.UpdateGroupCategoryRequest;
import com.hcmus.mentor.backend.domain.constant.GroupCategoryStatus;
import com.hcmus.mentor.backend.domain.constant.GroupStatus;
import com.hcmus.mentor.backend.repository.GroupCategoryRepository;
import com.hcmus.mentor.backend.repository.GroupRepository;
import com.hcmus.mentor.backend.service.dto.GroupCategoryServiceDto;
import com.hcmus.mentor.backend.service.GroupCategoryService;
import com.hcmus.mentor.backend.service.PermissionService;
import com.hcmus.mentor.backend.util.FileUtils;

import java.io.IOException;
import java.util.*;

import lombok.RequiredArgsConstructor;
import org.apache.commons.math3.util.Pair;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GroupCategoryServiceImpl implements GroupCategoryService {

    private final GroupCategoryRepository groupCategoryRepository;
    private final GroupRepository groupRepository;
    private final PermissionService permissionService;
    private final MongoTemplate mongoTemplate;

    @Override
    public List<GroupCategory> findAll() {
        return groupCategoryRepository.findAll(Sort.by(Sort.Direction.DESC, "createdDate"));
    }

    @Override
    public GroupCategoryServiceDto findById(String id) {
        Optional<GroupCategory> groupCategoryOptional = groupCategoryRepository.findById(id);
        if (!groupCategoryOptional.isPresent()) {
            return new GroupCategoryServiceDto(NOT_FOUND, "Not found group category", null);
        }
        return new GroupCategoryServiceDto(SUCCESS, "", groupCategoryOptional.get());
    }

    @Override
    public GroupCategoryServiceDto create(String emailUser, CreateGroupCategoryRequest request) {
        if (!permissionService.isAdmin(emailUser)) {
            return new GroupCategoryServiceDto(INVALID_PERMISSION, "Invalid permission", null);
        }
        if (request.getName() == null
                || request.getName().isEmpty()
                || request.getIconUrl() == null
                || request.getIconUrl().isEmpty()) {
            return new GroupCategoryServiceDto(NOT_ENOUGH_FIELDS, "Not enough required fields", null);
        }
        if (groupCategoryRepository.existsByName(request.getName())) {
            GroupCategory groupCategory = groupCategoryRepository.findByName(request.getName());
            if (groupCategory.getStatus().equals(GroupCategoryStatus.DELETED)) {
                groupCategory.setStatus(GroupCategoryStatus.ACTIVE);
                groupCategory.setDescription(request.getDescription());
                groupCategory.setIconUrl(request.getIconUrl());
                groupCategory.setPermissions(request.getPermissions());
                groupCategoryRepository.save(groupCategory);
                return new GroupCategoryServiceDto(SUCCESS, "", groupCategory);
            }
            return new GroupCategoryServiceDto(DUPLICATE_GROUP_CATEGORY, "Duplicate group category", null);
        }

        GroupCategory groupCategory =
                GroupCategory.builder()
                        .name(request.getName())
                        .description(request.getDescription())
                        .iconUrl(request.getIconUrl())
                        .permissions(request.getPermissions())
                        .build();
        groupCategoryRepository.save(groupCategory);
        return new GroupCategoryServiceDto(SUCCESS, "", groupCategory);
    }

    @Override
    public GroupCategoryServiceDto update(
            String emailUser, String id, UpdateGroupCategoryRequest request) {
        if (!permissionService.isAdmin(emailUser)) {
            return new GroupCategoryServiceDto(INVALID_PERMISSION, "Invalid permission", null);
        }
        Optional<GroupCategory> groupCategoryOptional = groupCategoryRepository.findById(id);
        if (!groupCategoryOptional.isPresent()) {
            return new GroupCategoryServiceDto(NOT_FOUND, "Not found group category", null);
        }

        GroupCategory groupCategory = groupCategoryOptional.get();

        if (groupCategoryRepository.existsByName(request.getName())
                && !request.getName().equals(groupCategory.getName())) {
            return new GroupCategoryServiceDto(DUPLICATE_GROUP_CATEGORY, "Duplicate group category", null);
        }
        groupCategory.update(
                request.getName(),
                request.getDescription(),
                request.getIconUrl(),
                request.getPermissions());
        groupCategoryRepository.save(groupCategory);

        return new GroupCategoryServiceDto(SUCCESS, "", groupCategory);
    }

    @Override
    public GroupCategoryServiceDto delete(String emailUser, String id, String newGroupCategoryId) {
        if (!permissionService.isAdmin(emailUser)) {
            return new GroupCategoryServiceDto(INVALID_PERMISSION, "Invalid permission", null);
        }
        Optional<GroupCategory> groupCategoryOptional = groupCategoryRepository.findById(id);
        if (!groupCategoryOptional.isPresent()) {
            return new GroupCategoryServiceDto(NOT_FOUND, "Not found group category", null);
        }

        GroupCategory groupCategory = groupCategoryOptional.get();
        List<Group> groups = groupRepository.findAllByGroupCategory(id);
        if (!newGroupCategoryId.isEmpty()) {
            groupCategoryOptional = groupCategoryRepository.findById(newGroupCategoryId);
            if (!groupCategoryOptional.isPresent()) {
                return new GroupCategoryServiceDto(
                        NOT_FOUND, "Not found new group category", newGroupCategoryId);
            }
            for (Group group : groups) {
                group.setGroupCategory(newGroupCategoryId);
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
        return new GroupCategoryServiceDto(SUCCESS, "", groupCategory);
    }

    private Pair<Long, List<GroupCategory>> getGroupCategoriesBySearchConditions(
            FindGroupCategoryRequest request, int page, int pageSize) {
        Query query = new Query();

        if (request.getName() != null && !request.getName().isEmpty()) {
            query.addCriteria(Criteria.where("name").regex(request.getName(), "i"));
        }
        if (request.getDescription() != null && !request.getDescription().isEmpty()) {
            query.addCriteria(Criteria.where("description").regex(request.getDescription(), "i"));
        }
        if (request.getStatus() != null) {
            query.addCriteria(Criteria.where("status").is(request.getStatus()));
        }
        query.with(Sort.by(Sort.Direction.DESC, "createdDate"));

        long count = mongoTemplate.count(query, GroupCategory.class);
        query.with(PageRequest.of(page, pageSize));

        List<GroupCategory> groupCategories = mongoTemplate.find(query, GroupCategory.class);
        return new Pair<>(count, groupCategories);
    }

    @Override
    public GroupCategoryServiceDto findGroupCategories(
            String emailUser, FindGroupCategoryRequest request, int page, int pageSize) {
        if (!permissionService.isAdmin(emailUser)) {
            return new GroupCategoryServiceDto(INVALID_PERMISSION, "Invalid permission", null);
        }
        Pair<Long, List<GroupCategory>> groupCategories =
                getGroupCategoriesBySearchConditions(request, page, pageSize);
        return new GroupCategoryServiceDto(
                SUCCESS,
                "",
                new PageImpl<>(
                        groupCategories.getValue(), PageRequest.of(page, pageSize), groupCategories.getKey()));
    }

    @Override
    public GroupCategoryServiceDto deleteMultiple(
            String emailUser, List<String> ids, String newGroupCategoryId) {
        if (!permissionService.isAdmin(emailUser)) {
            return new GroupCategoryServiceDto(INVALID_PERMISSION, "Invalid permission", null);
        }
        List<String> notFoundIds = new ArrayList<>();
        for (String id : ids) {
            Optional<GroupCategory> groupCategoryOptional = groupCategoryRepository.findById(id);
            if (!groupCategoryOptional.isPresent()) {
                notFoundIds.add(id);
            }
        }
        if (!notFoundIds.isEmpty()) {
            return new GroupCategoryServiceDto(NOT_FOUND, "Not found group category", notFoundIds);
        }
        List<Group> groups = groupRepository.findAllByGroupCategoryIn(ids);
        List<GroupCategory> groupCategories = groupCategoryRepository.findByIdIn(ids);
        if (!newGroupCategoryId.isEmpty()) {
            Optional<GroupCategory> groupCategoryOptional =
                    groupCategoryRepository.findById(newGroupCategoryId);
            if (!groupCategoryOptional.isPresent()) {
                return new GroupCategoryServiceDto(
                        NOT_FOUND, "Not found new group category", newGroupCategoryId);
            }
            for (Group group : groups) {
                group.setGroupCategory(newGroupCategoryId);
                groupRepository.save(group);
            }
        } else {
            groups.forEach(
                    group -> {
                        group.setStatus(GroupStatus.DELETED);
                    });
            groupRepository.saveAll(groups);
        }
        groupCategories.forEach(
                groupCategory -> {
                    groupCategory.setStatus(GroupCategoryStatus.DELETED);
                });

        groupCategoryRepository.saveAll(groupCategories);
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
