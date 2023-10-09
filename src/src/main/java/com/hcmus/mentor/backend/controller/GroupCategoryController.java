package com.hcmus.mentor.backend.controller;

import com.hcmus.mentor.backend.entity.Group;
import com.hcmus.mentor.backend.entity.GroupCategory;
import com.hcmus.mentor.backend.payload.APIResponse;
import com.hcmus.mentor.backend.payload.request.*;
import com.hcmus.mentor.backend.payload.returnCode.GroupCategoryReturnCode;
import com.hcmus.mentor.backend.security.CurrentUser;
import com.hcmus.mentor.backend.security.UserPrincipal;
import com.hcmus.mentor.backend.service.GroupCategoryService;
import com.hcmus.mentor.backend.service.GroupCategoryService.GroupCategoryReturn;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Group Category APIs", description = "REST APIs for Group Category collections")
@RestController
@RequestMapping("/api/group-categories")
@SecurityRequirement(name = "bearer")
public class GroupCategoryController {

  private final GroupCategoryService groupCategoryService;

  public GroupCategoryController(GroupCategoryService groupCategoryService) {
    this.groupCategoryService = groupCategoryService;
  }

  private Map<String, Object> pagingResponse(Page<GroupCategory> groupCategories) {
    Map<String, Object> response = new HashMap<>();
    response.put("content", groupCategories.getContent());
    response.put("currentPage", groupCategories.getNumber());
    response.put("totalItems", groupCategories.getTotalElements());
    response.put("totalPages", groupCategories.getTotalPages());
    return response;
  }

  @Operation(summary = "Get all group categories", description = "", tags = "Group Category APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Retrieve successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class))))
  })
  @GetMapping(value = {""})
  public APIResponse<List<GroupCategory>> findAll() {
    return APIResponse.success(groupCategoryService.findAll());
  }

  @Operation(summary = "Get group category by ID", description = "", tags = "Group Category APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Retrieve successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class))))
  })
  @GetMapping("/{id}")
  public APIResponse<GroupCategory> findById(@PathVariable String id) {
    GroupCategoryReturn groupCategoryReturn = groupCategoryService.findById(id);
    return new APIResponse(
        groupCategoryReturn.getData(),
        groupCategoryReturn.getReturnCode(),
        groupCategoryReturn.getMessage());
  }

  @Operation(summary = "Create new group category", description = "", tags = "Group Category APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Retrieve successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))),
    @ApiResponse(responseCode = "401", description = "Need authentication"),
    @ApiResponse(
        responseCode = GroupCategoryReturnCode.NOT_ENOUGH_FIELDS_STRING,
        description = "Not enough required fields"),
    @ApiResponse(
        responseCode = GroupCategoryReturnCode.DUPLICATE_GROUP_CATEGORY_STRING,
        description = "Duplicate group category"),
  })
  @PostMapping(value = {""})
  public APIResponse<GroupCategory> create(
      @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
      @RequestBody CreateGroupCategoryRequest request) {
    String email = userPrincipal.getEmail();
    GroupCategoryReturn groupCategoryReturn = groupCategoryService.create(email, request);
    return new APIResponse(
        groupCategoryReturn.getData(),
        groupCategoryReturn.getReturnCode(),
        groupCategoryReturn.getMessage());
  }

  @Operation(
      summary = "Update an existing group category",
      description = "",
      tags = "Group Category APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Retrieve successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))),
    @ApiResponse(responseCode = "401", description = "Need authentication"),
    @ApiResponse(
        responseCode = GroupCategoryReturnCode.NOT_FOUND_STRING,
        description = "Not found group category"),
    @ApiResponse(
        responseCode = GroupCategoryReturnCode.DUPLICATE_GROUP_CATEGORY_STRING,
        description = "Duplicate group category"),
  })
  @PatchMapping("/{id}")
  public APIResponse<GroupCategory> update(
      @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
      @PathVariable String id,
      @RequestBody UpdateGroupCategoryRequest request) {
    String email = userPrincipal.getEmail();
    GroupCategoryReturn groupCategoryReturn = groupCategoryService.update(email, id, request);
    return new APIResponse(
        groupCategoryReturn.getData(),
        groupCategoryReturn.getReturnCode(),
        groupCategoryReturn.getMessage());
  }

  @Operation(
      summary = "Delete an existing group category",
      description = "",
      tags = "Group Category APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Retrieve successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))),
    @ApiResponse(responseCode = "401", description = "Need authentication"),
    @ApiResponse(
        responseCode = GroupCategoryReturnCode.NOT_FOUND_STRING,
        description = "Not found group category"),
  })
  @DeleteMapping("/{id}")
  public APIResponse delete(
      @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
      @RequestBody DeleteGroupCategoryRequest request,
      @PathVariable String id) {
    String email = userPrincipal.getEmail();
    String newGroupCategoryId = request.getNewGroupCategoryId();
    GroupCategoryReturn groupCategoryReturn =
        groupCategoryService.delete(email, id, newGroupCategoryId);
    return new APIResponse(
        groupCategoryReturn.getData(),
        groupCategoryReturn.getReturnCode(),
        groupCategoryReturn.getMessage());
  }

  @Operation(
      summary = "Find group categories",
      description = "Find group categories with multiple filters",
      tags = "Group Category APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Retrieve successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class))))
  })
  @GetMapping("/find")
  public APIResponse<Page<Group>> get(
      @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
      @RequestParam(defaultValue = "") String name,
      @RequestParam(defaultValue = "") String description,
      @RequestParam(required = false) String status,
      @RequestParam(defaultValue = "0") Integer page,
      @RequestParam(defaultValue = "25") Integer size)
      throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
    String email = userPrincipal.getEmail();
    FindGroupCategoryRequest request = new FindGroupCategoryRequest(name, description, status);
    GroupCategoryReturn groupCategoryReturn =
        groupCategoryService.findGroupCategories(email, request, page, size);
    return new APIResponse(
        pagingResponse((Page<GroupCategory>) groupCategoryReturn.getData()),
        groupCategoryReturn.getReturnCode(),
        groupCategoryReturn.getMessage());
  }

  @Operation(
      summary = "Delete multiple existing group categories",
      description = "",
      tags = "Group Category APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Retrieve successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))),
    @ApiResponse(responseCode = "401", description = "Need authentication"),
    @ApiResponse(
        responseCode = GroupCategoryReturnCode.NOT_FOUND_STRING,
        description = "Not found group category"),
  })
  @DeleteMapping(value = {""})
  public APIResponse deleteMultiple(
      @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
      @RequestBody DeleteMultipleGroupCategoryRequest request) {
    String email = userPrincipal.getEmail();
    List<String> ids = request.getIds();
    String newGroupCategoryId = request.getNewGroupCategoryId();
    GroupCategoryReturn groupCategoryReturn =
        groupCategoryService.deleteMultiple(email, ids, newGroupCategoryId);
    return new APIResponse(
        groupCategoryReturn.getData(),
        groupCategoryReturn.getReturnCode(),
        groupCategoryReturn.getMessage());
  }

  @Operation(
      summary = "Export group categories table by search conditions",
      description = "Export group categories table",
      tags = "Group Category APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Update successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))),
    @ApiResponse(responseCode = "401", description = "Need authentication")
  })
  @GetMapping(value = "/export/search")
  public ResponseEntity<Resource> exportBySearchConditions(
      @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
      @RequestParam(defaultValue = "") String name,
      @RequestParam(defaultValue = "") String description,
      @RequestParam(required = false) String status,
      @RequestParam(defaultValue = "") List<String> remainColumns)
      throws IOException {
    FindGroupCategoryRequest request = new FindGroupCategoryRequest(name, description, status);
    ResponseEntity<Resource> response =
        groupCategoryService.generateExportTableBySearchConditions(
            userPrincipal.getEmail(), request, remainColumns);
    return response;
  }

  @Operation(
      summary = "Export group categories table",
      description = "Export group categories table",
      tags = "Group Category APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Update successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))),
    @ApiResponse(responseCode = "401", description = "Need authentication")
  })
  @GetMapping(value = "/export")
  public ResponseEntity<Resource> export(
      @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
      @RequestParam(defaultValue = "") List<String> remainColumns)
      throws IOException {
    ResponseEntity<Resource> response =
        groupCategoryService.generateExportTable(userPrincipal.getEmail(), remainColumns);
    return response;
  }

  @Operation(
      summary = "Get all permission's system",
      description = "Get all permission's system",
      tags = "Group Category APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Update successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))),
  })
  @GetMapping("/permissions")
  public ResponseEntity<?> getAllPermissions() {
    GroupCategory.Permission[] permissions = GroupCategory.Permission.values();
    EnumWithDescription[] enumsWithDescription = new EnumWithDescription[permissions.length];

    for (int i = 0; i < permissions.length; i++) {
      GroupCategory.Permission permission = permissions[i];
      enumsWithDescription[i] =
          new EnumWithDescription(permission.name(), permission.getDescription());
    }

    return ResponseEntity.ok(enumsWithDescription);
  }

  private static class EnumWithDescription {
    private final String name;
    private final String description;

    public EnumWithDescription(String name, String description) {
      this.name = name;
      this.description = description;
    }

    public String getName() {
      return name;
    }

    public String getDescription() {
      return description;
    }
  }
}
