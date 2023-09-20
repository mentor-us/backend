package com.hcmus.mentor.backend.service;

import com.hcmus.mentor.backend.entity.Group;
import com.hcmus.mentor.backend.entity.GroupCategory;
import com.hcmus.mentor.backend.entity.User;
import com.hcmus.mentor.backend.payload.request.CreateGroupCategoryRequest;
import com.hcmus.mentor.backend.payload.request.FindGroupCategoryRequest;
import com.hcmus.mentor.backend.payload.request.UpdateGroupCategoryRequest;
import com.hcmus.mentor.backend.repository.GroupCategoryRepository;
import com.hcmus.mentor.backend.repository.GroupRepository;
import com.hcmus.mentor.backend.util.FileUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

import static com.hcmus.mentor.backend.payload.returnCode.GroupCategoryReturnCode.DUPLICATE_GROUP_CATEGORY;
import static com.hcmus.mentor.backend.payload.returnCode.GroupCategoryReturnCode.NOT_FOUND;
import static com.hcmus.mentor.backend.payload.returnCode.GroupCategoryReturnCode.NOT_ENOUGH_FIELDS;
import static com.hcmus.mentor.backend.payload.returnCode.InvalidPermissionCode.INVALID_PERMISSION;
import static com.hcmus.mentor.backend.payload.returnCode.SuccessCode.SUCCESS;

@Service
public class GroupCategoryService {
    @Getter
    @Setter
    @NoArgsConstructor
    public static class GroupCategoryReturn {
        Integer returnCode;
        String message;
        Object data;

        public GroupCategoryReturn(Integer returnCode, String message, Object data) {
            this.returnCode = returnCode;
            this.message = message;
            this.data = data;
        }
    }

    private final GroupCategoryRepository groupCategoryRepository;
    private final GroupRepository groupRepository;
    private final PermissionService permissionService;
    private final MongoTemplate mongoTemplate;

    public GroupCategoryService(GroupCategoryRepository groupCategoryRepository, GroupRepository groupRepository, PermissionService permissionService, MongoTemplate mongoTemplate) {
        this.groupCategoryRepository = groupCategoryRepository;
        this.groupRepository = groupRepository;
        this.permissionService = permissionService;
        this.mongoTemplate = mongoTemplate;
    }

    public List<GroupCategory> findAll() {
        return groupCategoryRepository.findAll(Sort.by(Sort.Direction.DESC, "createdDate"));
    }

    public GroupCategoryReturn findById(String id) {
        Optional<GroupCategory> groupCategoryOptional = groupCategoryRepository.findById(id);
        if (!groupCategoryOptional.isPresent()) {
            return new GroupCategoryReturn(NOT_FOUND, "Not found group category", null);
        }
        return new GroupCategoryReturn(SUCCESS, "", groupCategoryOptional.get());
    }

    public GroupCategoryReturn create(String emailUser, CreateGroupCategoryRequest request) {
        if (!permissionService.isAdmin(emailUser)) {
            return new GroupCategoryReturn(INVALID_PERMISSION, "Invalid permission", null);
        }
        if (request.getName() == null || request.getName().isEmpty() ||
                request.getIconUrl() == null || request.getIconUrl().isEmpty()) {
            return new GroupCategoryReturn(NOT_ENOUGH_FIELDS, "Not enough required fields", null);
        }
        if (groupCategoryRepository.existsByName(request.getName())) {
            return new GroupCategoryReturn(DUPLICATE_GROUP_CATEGORY, "Duplicate group category", null);
        }

        GroupCategory groupCategory = GroupCategory.builder()
                .name(request.getName())
                .description(request.getDescription())
                .iconUrl(request.getIconUrl())
                .permissions(request.getPermissions())
                .build();
        groupCategoryRepository.save(groupCategory);
        return new GroupCategoryReturn(SUCCESS, "", groupCategory);
    }

    public GroupCategoryReturn update(String emailUser, String id, UpdateGroupCategoryRequest request) {
        if (!permissionService.isAdmin(emailUser)) {
            return new GroupCategoryReturn(INVALID_PERMISSION, "Invalid permission", null);
        }
        Optional<GroupCategory> groupCategoryOptional = groupCategoryRepository.findById(id);
        if (!groupCategoryOptional.isPresent()) {
            return new GroupCategoryReturn(NOT_FOUND, "Not found group category", null);
        }

        GroupCategory groupCategory = groupCategoryOptional.get();

        if (groupCategoryRepository.existsByName(request.getName()) && !request.getName().equals(groupCategory.getName())) {
            return new GroupCategoryReturn(DUPLICATE_GROUP_CATEGORY, "Duplicate group category", null);
        }
        groupCategory.update(request.getName(), request.getDescription(), request.getIconUrl(), request.getPermissions());
        groupCategoryRepository.save(groupCategory);

        return new GroupCategoryReturn(SUCCESS, "", groupCategory);
    }

    public GroupCategoryReturn delete(String emailUser, String id, String newGroupCategoryId) {
        if (!permissionService.isAdmin(emailUser)) {
            return new GroupCategoryReturn(INVALID_PERMISSION, "Invalid permission", null);
        }
        Optional<GroupCategory> groupCategoryOptional = groupCategoryRepository.findById(id);
        if (!groupCategoryOptional.isPresent()) {
            return new GroupCategoryReturn(NOT_FOUND, "Not found group category", null);
        }

        GroupCategory groupCategory = groupCategoryOptional.get();
        List<Group> groups = groupRepository.findAllByGroupCategory(id);
        if (!newGroupCategoryId.isEmpty()) {
            groupCategoryOptional = groupCategoryRepository.findById(newGroupCategoryId);
            if (!groupCategoryOptional.isPresent()) {
                return new GroupCategoryReturn(NOT_FOUND, "Not found new group category", newGroupCategoryId);
            }
            for (Group group : groups) {
                group.setGroupCategory(newGroupCategoryId);
                groupRepository.save(group);
            }
        } else {
            for (Group group : groups) {
                group.setStatus(Group.Status.DELETED);
            }
            groupRepository.saveAll(groups);
        }

        groupCategory.setStatus(GroupCategory.Status.DELETED);
        groupCategoryRepository.save(groupCategory);
        return new GroupCategoryReturn(SUCCESS, "", groupCategory);
    }

    private Pair<Long, List<GroupCategory>> getGroupCategoriesBySearchConditions(FindGroupCategoryRequest request,
                                                                                 int page, int pageSize) {
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

    public GroupCategoryReturn findGroupCategories(
            String emailUser,
            FindGroupCategoryRequest request,
            int page, int pageSize) {
        if (!permissionService.isAdmin(emailUser)) {
            return new GroupCategoryReturn(INVALID_PERMISSION, "Invalid permission", null);
        }
        Pair<Long, List<GroupCategory>> groupCategories = getGroupCategoriesBySearchConditions(request, page, pageSize);
        return new GroupCategoryReturn(SUCCESS, "", new PageImpl<>(groupCategories.getValue(), PageRequest.of(page, pageSize), groupCategories.getKey()));
    }

    public GroupCategoryReturn deleteMultiple(String emailUser, List<String> ids, String newGroupCategoryId) {
        if (!permissionService.isAdmin(emailUser)) {
            return new GroupCategoryReturn(INVALID_PERMISSION, "Invalid permission", null);
        }
        List<String> notFoundIds = new ArrayList<>();
        for (String id : ids) {
            Optional<GroupCategory> groupCategoryOptional = groupCategoryRepository.findById(id);
            if (!groupCategoryOptional.isPresent()) {
                notFoundIds.add(id);
            }
        }
        if (!notFoundIds.isEmpty()) {
            return new GroupCategoryReturn(NOT_FOUND, "Not found group category", notFoundIds);
        }
        List<Group> groups = groupRepository.findAllByGroupCategoryIn(ids);
        List<GroupCategory> groupCategories = groupCategoryRepository.findByIdIn(ids);
        if (!newGroupCategoryId.isEmpty()) {
            Optional<GroupCategory> groupCategoryOptional = groupCategoryRepository.findById(newGroupCategoryId);
            if (!groupCategoryOptional.isPresent()) {
                return new GroupCategoryReturn(NOT_FOUND, "Not found new group category", newGroupCategoryId);
            }
            for (Group group : groups) {
                group.setGroupCategory(newGroupCategoryId);
                groupRepository.save(group);
            }
        } else {
            groups.forEach(group -> {
                group.setStatus(Group.Status.DELETED);
            });
            groupRepository.saveAll(groups);
        }
        groupCategories.forEach(groupCategory -> {
            groupCategory.setStatus(GroupCategory.Status.DELETED);
        });

        groupCategoryRepository.saveAll(groupCategories);
        return new GroupCategoryReturn(SUCCESS, "", groupCategories);
    }

    private List<List<String>> generateExportData(List<GroupCategory> groupCategories) {
        List<List<String>> data = new ArrayList<>();
        int index = 1;
        for (GroupCategory groupCategory : groupCategories) {
            List<String> row = new ArrayList<>();

            row.add(Integer.toString(index));
            row.add(groupCategory.getName());
            row.add(groupCategory.getDescription());
            if(groupCategory.getStatus().equals(GroupCategory.Status.ACTIVE)){
                row.add("Đang hoạt động");
            }
            else{
                row.add("Đã xoá");
            }

            data.add(row);
            index++;
        }

        return data;
    }

    private ResponseEntity<Resource> generateExportTable(List<GroupCategory> groupCategories, List<String> remainColumns) throws IOException {
        List<List<String>> data = generateExportData(groupCategories);
        List<String> headers = Arrays.asList("STT", "Tên", "Mô tả", "Trạng thái");
        String fileName = "output.xlsx";
        Map<String, Integer> indexMap = new HashMap<>();
        indexMap.put("name", 1);
        indexMap.put("description", 2);
        indexMap.put("status", 3);
        List<Integer> remainColumnIndexes = new ArrayList<>();
        remainColumnIndexes.add(0);
        remainColumns.forEach(remainColumn -> {
            if (indexMap.containsKey(remainColumn)) {
                remainColumnIndexes.add(indexMap.get(remainColumn));
            }
        });
        java.io.File exportFile = FileUtils.generateExcel(headers, data, remainColumnIndexes, fileName);
        Resource resource = new FileSystemResource(exportFile.getAbsolutePath());
        ResponseEntity<Resource> response = ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + resource.getFilename())
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .contentLength(resource.getFile().length())
                .body(resource);
        return response;
    }

    public ResponseEntity<Resource> generateExportTable(String emailUser, List<String> remainColumns) throws IOException {
        List<GroupCategory> groupCategories = groupCategoryRepository.findAll();
        ResponseEntity<Resource> response = generateExportTable(groupCategories, remainColumns);
        return response;
    }

    public ResponseEntity<Resource> generateExportTableBySearchConditions(String emailUser, FindGroupCategoryRequest request, List<String> remainColumns) throws IOException {
        List<GroupCategory> groupCategories = getGroupCategoriesBySearchConditions(request, 0, Integer.MAX_VALUE).getValue();
        ResponseEntity<Resource> response = generateExportTable(groupCategories, remainColumns);
        return response;
    }
}
