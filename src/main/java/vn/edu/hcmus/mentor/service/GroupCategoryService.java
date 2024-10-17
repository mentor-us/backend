package vn.edu.hcmus.mentor.service;

import vn.edu.hcmus.mentor.controller.payload.request.messages.CreateGroupCategoryRequest;
import vn.edu.hcmus.mentor.controller.payload.request.groupcategories.FindGroupCategoryRequest;
import vn.edu.hcmus.mentor.controller.payload.request.groupcategories.UpdateGroupCategoryRequest;
import vn.edu.hcmus.mentor.domain.GroupCategory;
import vn.edu.hcmus.mentor.service.dto.GroupCategoryServiceDto;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;

public interface GroupCategoryService {
    List<GroupCategory> findAll();

    GroupCategoryServiceDto findById(String id);

    GroupCategoryServiceDto create(String emailUser, CreateGroupCategoryRequest request);

    GroupCategoryServiceDto update(String emailUser, String id, UpdateGroupCategoryRequest request);

    GroupCategoryServiceDto delete(String emailUser, String id, String newGroupCategoryId);

    GroupCategoryServiceDto findGroupCategories(
            String emailUser, FindGroupCategoryRequest request, int page, int pageSize);

    GroupCategoryServiceDto deleteMultiple(String emailUser, List<String> ids, String newGroupCategoryId);

    ResponseEntity<Resource> generateExportTable(String emailUser, List<String> remainColumns)
            throws IOException;

    ResponseEntity<Resource> generateExportTableBySearchConditions(
            String emailUser, FindGroupCategoryRequest request, List<String> remainColumns)
            throws IOException;
}