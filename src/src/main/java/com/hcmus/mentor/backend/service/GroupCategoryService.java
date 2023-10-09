package com.hcmus.mentor.backend.service;

import com.hcmus.mentor.backend.entity.GroupCategory;
import com.hcmus.mentor.backend.payload.request.CreateGroupCategoryRequest;
import com.hcmus.mentor.backend.payload.request.FindGroupCategoryRequest;
import com.hcmus.mentor.backend.payload.request.UpdateGroupCategoryRequest;
import java.io.IOException;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

public interface GroupCategoryService {
  List<GroupCategory> findAll();

  GroupCategoryReturn findById(String id);

  GroupCategoryReturn create(String emailUser, CreateGroupCategoryRequest request);

  GroupCategoryReturn update(String emailUser, String id, UpdateGroupCategoryRequest request);

  GroupCategoryReturn delete(String emailUser, String id, String newGroupCategoryId);

  GroupCategoryReturn findGroupCategories(
      String emailUser, FindGroupCategoryRequest request, int page, int pageSize);

  GroupCategoryReturn deleteMultiple(String emailUser, List<String> ids, String newGroupCategoryId);

  ResponseEntity<Resource> generateExportTable(String emailUser, List<String> remainColumns)
      throws IOException;

  ResponseEntity<Resource> generateExportTableBySearchConditions(
      String emailUser, FindGroupCategoryRequest request, List<String> remainColumns)
      throws IOException;
}
