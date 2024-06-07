package com.hcmus.mentor.backend.service;

import com.hcmus.mentor.backend.controller.payload.request.messages.CreateGroupCategoryRequest;
import com.hcmus.mentor.backend.controller.payload.request.groupcategories.FindGroupCategoryRequest;
import com.hcmus.mentor.backend.controller.payload.request.groupcategories.UpdateGroupCategoryRequest;
import com.hcmus.mentor.backend.domain.GroupCategory;
import com.hcmus.mentor.backend.service.dto.GroupCategoryServiceDto;
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