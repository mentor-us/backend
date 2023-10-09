package com.hcmus.mentor.backend.controller;

import static com.hcmus.mentor.backend.payload.returnCode.AnalyticReturnCode.*;
import static com.hcmus.mentor.backend.payload.returnCode.InvalidPermissionCode.INVALID_PERMISSION_STRING;

import com.hcmus.mentor.backend.entity.Group;
import com.hcmus.mentor.backend.entity.User;
import com.hcmus.mentor.backend.payload.APIResponse;
import com.hcmus.mentor.backend.payload.request.FindGroupGeneralAnalyticRequest;
import com.hcmus.mentor.backend.payload.request.FindUserAnalyticRequest;
import com.hcmus.mentor.backend.payload.request.UpdateStudentInformationRequest;
import com.hcmus.mentor.backend.payload.response.analytic.GroupAnalyticResponse;
import com.hcmus.mentor.backend.payload.response.analytic.ImportGeneralInformationResponse;
import com.hcmus.mentor.backend.payload.response.analytic.SystemAnalyticChartResponse;
import com.hcmus.mentor.backend.payload.response.analytic.SystemAnalyticResponse;
import com.hcmus.mentor.backend.payload.response.groups.GroupGeneralResponse;
import com.hcmus.mentor.backend.repository.GroupRepository;
import com.hcmus.mentor.backend.security.CurrentUser;
import com.hcmus.mentor.backend.security.UserPrincipal;
import com.hcmus.mentor.backend.service.AnalyticService;
import com.hcmus.mentor.backend.util.RequestUtils;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.html2pdf.resolver.font.DefaultFontProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

@Tag(name = "Analytic APIs", description = "REST APIs for Analytic")
@RestController
@RequestMapping("/api/analytic")
@SecurityRequirement(name = "bearer")
public class AnalyticController {

  private final AnalyticService analyticService;

  private final ServletContext servletContext;
  private final GroupRepository groupRepository;

  public AnalyticController(
      AnalyticService analyticService,
      ServletContext servletContext,
      GroupRepository groupRepository) {
    this.analyticService = analyticService;
    this.servletContext = servletContext;
    this.groupRepository = groupRepository;
  }

  @Operation(
      summary = "Get general information of system / groupCategory",
      description = "Get general information of system / groupCategory",
      tags = "Analytic APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Get information successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = ApiResponse.class)))),
    @ApiResponse(responseCode = INVALID_PERMISSION_STRING, description = "Invalid permission"),
  })
  @GetMapping({""})
  public APIResponse<SystemAnalyticResponse> get(
      @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
      @RequestParam(required = false) String groupCategoryId) {
    String emailUser = userPrincipal.getEmail();
    return groupCategoryId == null
        ? analyticService.getGeneralInformation(emailUser)
        : analyticService.getGeneralInformationByGroupCategory(emailUser, groupCategoryId);
  }

  @Operation(
      summary = "Get general information of system / groupCategory(by month)",
      description = "Get general information of system / groupCategory(by month)",
      tags = "Analytic APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Get information successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = ApiResponse.class)))),
    @ApiResponse(responseCode = INVALID_PERMISSION_STRING, description = "Invalid permission"),
    @ApiResponse(responseCode = INVALID_TIME_RANGE_STRING, description = "Invalid time range"),
  })
  @GetMapping({"/chart"})
  public APIResponse<SystemAnalyticChartResponse> getDataForChart(
      @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
      @RequestParam int monthStart,
      @RequestParam int yearStart,
      @RequestParam int monthEnd,
      @RequestParam int yearEnd,
      @RequestParam(required = false) String groupCategoryId)
      throws ParseException {
    String emailUser = userPrincipal.getEmail();
    return groupCategoryId == null
        ? analyticService.getDataForChart(emailUser, monthStart, yearStart, monthEnd, yearEnd)
        : analyticService.getDataForChartByGroupCategory(
            emailUser, monthStart, yearStart, monthEnd, yearEnd, groupCategoryId);
  }

  @Operation(
      summary = "Get group's analytic",
      description = "Get group's analytic",
      tags = "Analytic APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Get information successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = ApiResponse.class)))),
    @ApiResponse(responseCode = INVALID_PERMISSION_STRING, description = "Invalid permission"),
  })
  @GetMapping({"/{groupId}"})
  public APIResponse<GroupAnalyticResponse> getGroupAnalytic(
      @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
      @PathVariable String groupId) {
    String emailUser = userPrincipal.getEmail();
    return analyticService.getGroupAnalytic(emailUser, groupId);
  }

  @Operation(
      summary = "Export groups general analytic",
      description = "Export groups general analytic",
      tags = "Analytic APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Export successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = ApiResponse.class)))),
    @ApiResponse(responseCode = INVALID_PERMISSION_STRING, description = "Invalid permission"),
  })
  @GetMapping({"/groups/export"})
  public ResponseEntity<Resource> exportGroups(
      @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
      @RequestParam(defaultValue = "") List<String> remainColumns)
      throws IOException {
    String emailUser = userPrincipal.getEmail();
    ResponseEntity<Resource> response =
        analyticService.generateExportGroupsTable(emailUser, remainColumns);
    return response;
  }

  @Operation(
      summary = "Export groups general analytic by search conditions",
      description = "Export groups general analytic",
      tags = "Analytic APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Export successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = ApiResponse.class)))),
    @ApiResponse(responseCode = INVALID_PERMISSION_STRING, description = "Invalid permission"),
  })
  @GetMapping({"/groups/export/search"})
  public ResponseEntity<Resource> exportGroupsBySearchConditions(
      @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
      @RequestParam(required = false) String groupName,
      @RequestParam(required = false) Group.Status status,
      @RequestParam(required = false) String groupCategory,
      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
          Date timeStart,
      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
          Date timeEnd,
      @RequestParam(defaultValue = "") List<String> remainColumns)
      throws IOException {
    String emailUser = userPrincipal.getEmail();
    FindGroupGeneralAnalyticRequest request =
        new FindGroupGeneralAnalyticRequest(groupName, groupCategory, status, timeStart, timeEnd);
    ResponseEntity<Resource> response =
        analyticService.generateExportGroupsTableBySearchConditions(
            emailUser, request, remainColumns);
    return response;
  }

  @Operation(
      summary = "Export group's analytic",
      description = "Export group's analytic",
      tags = "Analytic APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Export successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = ApiResponse.class)))),
    @ApiResponse(responseCode = INVALID_PERMISSION_STRING, description = "Invalid permission"),
  })
  @GetMapping({"/{groupId}/export"})
  public ResponseEntity<Resource> exportGroup(
      @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
      @PathVariable String groupId,
      @RequestParam(defaultValue = "") List<String> remainColumns)
      throws IOException {
    String emailUser = userPrincipal.getEmail();
    ResponseEntity<Resource> response =
        analyticService.generateExportGroupTable(emailUser, groupId, remainColumns);
    return response;
  }

  @Operation(
      summary = "Export group's analytic by search conditions",
      description = "Export group's analytic",
      tags = "Analytic APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Export successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = ApiResponse.class)))),
    @ApiResponse(responseCode = INVALID_PERMISSION_STRING, description = "Invalid permission"),
  })
  @GetMapping({"/{groupId}/export/search"})
  public ResponseEntity<Resource> exportGroupBySearchCondotions(
      @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
      @PathVariable String groupId,
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String email,
      @RequestParam(required = false) FindUserAnalyticRequest.Role role,
      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
          Date timeStart,
      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
          Date timeEnd,
      @RequestParam(defaultValue = "") List<String> remainColumns)
      throws IOException {
    String emailUser = userPrincipal.getEmail();
    FindUserAnalyticRequest request =
        new FindUserAnalyticRequest(name, email, role, timeStart, timeEnd);
    ResponseEntity<Resource> response =
        analyticService.generateExportGroupTableBySearchConditions(
            emailUser, groupId, request, remainColumns);
    return response;
  }

  @Operation(
      summary = "Export groups general analytic",
      description = "Export groups general analytic",
      tags = "Analytic APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Import successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = ApiResponse.class)))),
    @ApiResponse(responseCode = INVALID_PERMISSION_STRING, description = "Invalid permission"),
  })
  @GetMapping({"/groups"})
  public APIResponse<Page<GroupGeneralResponse>> all(
      @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "25") int pageSize) {
    String emailUser = userPrincipal.getEmail();
    Pageable pageRequest =
        PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdDate"));
    return analyticService.getGroupGeneralAnalytic(emailUser, pageRequest);
  }

  @Operation(
      summary = "Import multiple training point",
      description = "Import multiple training point",
      tags = "Analytic APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Import successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = ApiResponse.class)))),
    @ApiResponse(responseCode = INVALID_PERMISSION_STRING, description = "Invalid permission"),
    @ApiResponse(responseCode = NOT_FOUND_USER_STRING, description = "Not found user"),
    @ApiResponse(responseCode = INVALID_VALUE_STRING, description = "Invalid value"),
  })
  @PostMapping({"/import-training-point"})
  public APIResponse<Map<String, String>> importTrainingPoint(
      @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
      @RequestBody MultipartFile file)
      throws IOException {
    String emailUser = userPrincipal.getEmail();
    return analyticService.importData(emailUser, file, "TRAINING_POINT");
  }

  @Operation(
      summary = "Import multiple has English certs",
      description = "Import multiple has English certs",
      tags = "Analytic APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Import successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = ApiResponse.class)))),
    @ApiResponse(responseCode = INVALID_PERMISSION_STRING, description = "Invalid permission"),
    @ApiResponse(responseCode = NOT_FOUND_USER_STRING, description = "Not found user"),
    @ApiResponse(responseCode = INVALID_VALUE_STRING, description = "Invalid value"),
  })
  @PostMapping({"/import-has-english-cert"})
  public APIResponse<Map<String, String>> importHasEnglishCert(
      @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
      @RequestBody MultipartFile file)
      throws IOException {
    String emailUser = userPrincipal.getEmail();
    return analyticService.importData(emailUser, file, "HAS_ENGLISH_CERT");
  }

  @Operation(
      summary = "Import multiple studying points",
      description = "Import multiple studying points",
      tags = "Analytic APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Get information successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = ApiResponse.class)))),
    @ApiResponse(responseCode = INVALID_PERMISSION_STRING, description = "Invalid permission"),
    @ApiResponse(responseCode = NOT_FOUND_USER_STRING, description = "Not found user"),
    @ApiResponse(responseCode = INVALID_VALUE_STRING, description = "Invalid value"),
  })
  @PostMapping({"/import-studying-point"})
  public APIResponse<Map<String, String>> importStudyingPoint(
      @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
      @RequestBody MultipartFile file)
      throws IOException {
    String emailUser = userPrincipal.getEmail();
    return analyticService.importData(emailUser, file, "STUDYING_POINT");
  }

  @Operation(
      summary = "Import multiple training points, has English certs studying points",
      description = "Import multiple training points, has English certs studying points",
      tags = "Analytic APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Get information successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = ApiResponse.class)))),
    @ApiResponse(responseCode = INVALID_PERMISSION_STRING, description = "Invalid permission"),
    @ApiResponse(responseCode = NOT_FOUND_USER_STRING, description = "Not found user"),
    @ApiResponse(responseCode = INVALID_VALUE_STRING, description = "Invalid value"),
  })
  @PostMapping({"/import-multiple"})
  public APIResponse<List<ImportGeneralInformationResponse>> importMultiple(
      @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
      @RequestBody MultipartFile file)
      throws IOException {
    String emailUser = userPrincipal.getEmail();
    return analyticService.importMultipleData(emailUser, file);
  }

  @Operation(
      summary = "Update training point, has English cert, studying point",
      description = "Update training point, has English cert, studying point",
      tags = "Analytic APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Get information successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = ApiResponse.class)))),
    @ApiResponse(responseCode = INVALID_PERMISSION_STRING, description = "Invalid permission"),
    @ApiResponse(responseCode = NOT_FOUND_USER_STRING, description = "Not found user"),
    @ApiResponse(responseCode = INVALID_VALUE_STRING, description = "Invalid value"),
  })
  @PatchMapping({"/{userId}"})
  public APIResponse<User> update(
      @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
      @PathVariable String userId,
      @RequestBody UpdateStudentInformationRequest request)
      throws IOException {
    String emailUser = userPrincipal.getEmail();
    return analyticService.updateStudentInformation(emailUser, userId, request);
  }

  @Operation(
      summary = "Find groups in analytic",
      description = "Find groups in analytic",
      tags = "Analytic APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Get information successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = ApiResponse.class)))),
    @ApiResponse(responseCode = INVALID_PERMISSION_STRING, description = "Invalid permission"),
  })
  @GetMapping({"/find"})
  public APIResponse<Page<GroupGeneralResponse>> findGroups(
      @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "25") int pageSize,
      @RequestParam(required = false) String groupName,
      @RequestParam(required = false) String groupCategory,
      @RequestParam(required = false) Group.Status status,
      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
          Date timeStart,
      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
          Date timeEnd)
      throws IOException {
    String emailUser = userPrincipal.getEmail();
    FindGroupGeneralAnalyticRequest request =
        new FindGroupGeneralAnalyticRequest(groupName, groupCategory, status, timeStart, timeEnd);
    Pageable pageRequest =
        PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdDate"));

    return analyticService.findGroupGeneralAnalytic(emailUser, pageRequest, request);
  }

  @Operation(
      summary = "Find users in analytic",
      description = "Find users in analytic",
      tags = "Analytic APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Get information successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = ApiResponse.class)))),
    @ApiResponse(responseCode = INVALID_PERMISSION_STRING, description = "Invalid permission"),
  })
  @GetMapping({"/find/{groupId}"})
  public APIResponse<List<GroupAnalyticResponse.Member>> findUsers(
      @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
      @PathVariable String groupId,
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String email,
      @RequestParam(required = false) FindUserAnalyticRequest.Role role,
      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
          Date timeStart,
      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
          Date timeEnd)
      throws IOException {
    String emailUser = userPrincipal.getEmail();
    FindUserAnalyticRequest request =
        new FindUserAnalyticRequest(name, email, role, timeStart, timeEnd);
    return analyticService.findUserAnalytic(emailUser, groupId, request);
  }

  @Operation(
      summary = "Export group report",
      description = "Export report of analytic information in a group",
      tags = "Analytic APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Export successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = ApiResponse.class)))),
    @ApiResponse(responseCode = INVALID_PERMISSION_STRING, description = "Invalid permission"),
  })
  @GetMapping("/report")
  public ResponseEntity<?> getGroupReport(
      @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
      @RequestParam String groupId,
      HttpServletRequest request,
      HttpServletResponse response) {
    JakartaServletWebApplication application =
        JakartaServletWebApplication.buildApplication(request.getServletContext());
    IWebExchange exchange = application.buildExchange(request, response);
    WebContext context = new WebContext(exchange);

    String reportHtml =
        analyticService.exportGroupReport(userPrincipal.getEmail(), groupId, context);
    if (reportHtml == null) {
      return ResponseEntity.badRequest().build();
    }

    ByteArrayOutputStream target = new ByteArrayOutputStream();
    ConverterProperties converterProperties = new ConverterProperties();
    converterProperties.setBaseUri("http://localhost:8080");
    converterProperties.setFontProvider(new DefaultFontProvider());
    HtmlConverter.convertToPdf(reportHtml, target, converterProperties);
    /* extract output as bytes */
    return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(target.toByteArray());
  }

  @Operation(
      summary = "Export group log",
      description = "Export file log of group",
      tags = "Analytic APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Export successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = ApiResponse.class)))),
    @ApiResponse(responseCode = INVALID_PERMISSION_STRING, description = "Invalid permission"),
  })
  @GetMapping("/log")
  public ResponseEntity<?> getGroupLog(
      @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
      @RequestParam String groupId,
      @RequestParam String[] query)
      throws IOException {
    List<AnalyticService.AnalyticAttribute> attributes =
        Stream.of(query)
            .map(AnalyticService.AnalyticAttribute::valueOf)
            .collect(Collectors.toList());
    byte[] content = analyticService.getGroupLog(userPrincipal.getEmail(), groupId, attributes);
    if (content == null) {
      return ResponseEntity.badRequest().build();
    }

    String fileName = null;
    Optional<Group> groupWrapper = groupRepository.findById(groupId);
    if (groupWrapper.isPresent()) {
      Group group = groupWrapper.get();
      fileName = "log-" + RequestUtils.toSlug(group.getName()) + ".xlsx";
    }
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
        .body(content);
  }
}
