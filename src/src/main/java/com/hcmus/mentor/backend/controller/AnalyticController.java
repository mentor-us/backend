package com.hcmus.mentor.backend.controller;

import com.hcmus.mentor.backend.controller.payload.ApiResponseDto;
import com.hcmus.mentor.backend.controller.payload.request.messages.FindGroupGeneralAnalyticRequest;
import com.hcmus.mentor.backend.controller.payload.request.users.FindUserAnalyticRequest;
import com.hcmus.mentor.backend.controller.payload.request.users.UpdateStudentInformationRequest;
import com.hcmus.mentor.backend.controller.payload.response.analytic.GroupAnalyticResponse;
import com.hcmus.mentor.backend.controller.payload.response.analytic.ImportGeneralInformationResponse;
import com.hcmus.mentor.backend.controller.payload.response.analytic.SystemAnalyticChartResponse;
import com.hcmus.mentor.backend.controller.payload.response.analytic.SystemAnalyticResponse;
import com.hcmus.mentor.backend.controller.payload.response.groups.GroupGeneralResponse;
import com.hcmus.mentor.backend.domain.Group;
import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.domain.constant.GroupStatus;
import com.hcmus.mentor.backend.repository.GroupRepository;
import com.hcmus.mentor.backend.security.principal.CurrentUser;
import com.hcmus.mentor.backend.security.principal.userdetails.CustomerUserDetails;
import com.hcmus.mentor.backend.service.AnalyticAttribute;
import com.hcmus.mentor.backend.service.AnalyticService;
import com.hcmus.mentor.backend.util.RequestUtils;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.html2pdf.resolver.font.DefaultFontProvider;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Analytic controller.
 */
@Tag(name = "analytic")
@RestController
@RequestMapping("api/analytic")
@SecurityRequirement(name = "bearer")
@RequiredArgsConstructor
public class AnalyticController {

    private final AnalyticService analyticService;
    private final GroupRepository groupRepository;

    /**
     * Get general information of the system or a specific group category.
     *
     * @param customerUserDetails Current user's principal information.
     * @param groupCategoryId     Optional group category ID to filter the information.
     * @return APIResponse containing the system or group category general information.
     */
    @GetMapping("")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<SystemAnalyticResponse> get(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @RequestParam(required = false) String groupCategoryId) {
        String emailUser = customerUserDetails.getEmail();
        return groupCategoryId == null
                ? analyticService.getGeneralInformation(emailUser)
                : analyticService.getGeneralInformationByGroupCategory(emailUser, groupCategoryId);
    }

    /**
     * Get general information of the system or a specific group category for a specified time range.
     *
     * @param customerUserDetails Current user's principal information.
     * @param monthStart          Start month for the time range.
     * @param yearStart           Start year for the time range.
     * @param monthEnd            End month for the time range.
     * @param yearEnd             End year for the time range.
     * @param groupCategoryId     Optional group category ID to filter the information.
     * @return APIResponse containing the system or group category general information for the specified time range.
     * @throws ParseException if an error occurs while parsing date information.
     */
    @GetMapping("chart")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<SystemAnalyticChartResponse> getDataForChart(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @RequestParam int monthStart,
            @RequestParam int yearStart,
            @RequestParam int monthEnd,
            @RequestParam int yearEnd,
            @RequestParam(required = false) String groupCategoryId)
            throws ParseException {
        String emailUser = customerUserDetails.getEmail();
        return groupCategoryId == null
                ? analyticService.getDataForChart(emailUser, monthStart, yearStart, monthEnd, yearEnd)
                : analyticService.getDataForChartByGroupCategory(
                emailUser, monthStart, yearStart, monthEnd, yearEnd, groupCategoryId);
    }

    /**
     * Get analytic information for a specific group.
     *
     * @param customerUserDetails Current user's principal information.
     * @param groupId             ID of the group to retrieve analytic information for.
     * @return APIResponse containing the analytic information for the specified group.
     */
    @GetMapping("{groupId}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<GroupAnalyticResponse> getGroupAnalytic(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @PathVariable String groupId) {
        String emailUser = customerUserDetails.getEmail();
        return analyticService.getGroupAnalytic(emailUser, groupId);
    }

    /**
     * Export groups' general analytic information.
     *
     * @param customerUserDetails Current user's principal information.
     * @param remainColumns       List of columns to include in the export.
     * @return ResponseEntity with the exported resource.
     * @throws IOException if an I/O error occurs.
     */
    @GetMapping("groups/export")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<Resource> exportGroups(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @RequestParam(defaultValue = "") List<String> remainColumns)
            throws IOException {
        String emailUser = customerUserDetails.getEmail();
        return analyticService.generateExportGroupsTable(emailUser, remainColumns);
    }


    /**
     * Exports analytic data for groups based on search conditions.
     *
     * <p>This operation allows the export of analytic data for groups based on specified search conditions.
     *
     * @param customerUserDetails The principal information of the current user.
     * @param groupName           The name of the group (optional).
     * @param status              The status of the group (optional).
     * @param groupCategory       The category of the group (optional).
     * @param timeStart           The start date and time for filtering (optional).
     * @param timeEnd             The end date and time for filtering (optional).
     * @param remainColumns       A list of columns to include in the export (default is an empty list).
     * @return ResponseEntity<Resource> containing the exported groups' analytic data.
     * @throws IOException If an I/O error occurs during the export process.
     */
    @GetMapping("groups/export/search")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<Resource> exportGroupsBySearchConditions(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @RequestParam(required = false) String groupName,
            @RequestParam(required = false) GroupStatus status,
            @RequestParam(required = false) String groupCategory,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
            Date timeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
            Date timeEnd,
            @RequestParam(defaultValue = "") List<String> remainColumns)
            throws IOException {
        String emailUser = customerUserDetails.getEmail();
        var request = new FindGroupGeneralAnalyticRequest(groupName, groupCategory, status, timeStart, timeEnd);
        return analyticService.generateExportGroupsTableBySearchConditions(emailUser, request, remainColumns);
    }

    /**
     * Exports analytic data for a specific group.
     *
     * <p>This operation allows the export of analytic data for a specific group identified by the provided groupId.
     *
     * @param customerUserDetails The principal information of the current user.
     * @param groupId             The unique identifier of the group.
     * @param remainColumns       A list of columns to include in the export (default is an empty list).
     * @return ResponseEntity<Resource> containing the exported group's analytic data.
     * @throws IOException If an I/O error occurs during the export process.
     */
    @GetMapping("{groupId}/export")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<Resource> exportGroup(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @PathVariable String groupId,
            @RequestParam(defaultValue = "") List<String> remainColumns)
            throws IOException {
        String emailUser = customerUserDetails.getEmail();
        return analyticService.generateExportGroupTable(emailUser, groupId, remainColumns);
    }

    /**
     * Exports analytic data for a specific group based on search conditions.
     *
     * <p>This operation allows the export of analytic data for a specific group identified by the provided groupId,
     * considering specified search conditions like user name, email, role, and time range.
     *
     * @param customerUserDetails The principal information of the current user.
     * @param groupId             The unique identifier of the group.
     * @param name                The name of the user (optional).
     * @param email               The email of the user (optional).
     * @param role                The role of the user (optional).
     * @param timeStart           The start date and time of the time range (optional).
     * @param timeEnd             The end date and time of the time range (optional).
     * @param remainColumns       A list of columns to include in the export (default is an empty list).
     * @return ResponseEntity<Resource> containing the exported group's analytic data based on search conditions.
     * @throws IOException If an I/O error occurs during the export process.
     */
    @GetMapping("{groupId}/export/search")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<Resource> exportGroupBySearchConditions(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
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
        String emailUser = customerUserDetails.getEmail();
        var request = new FindUserAnalyticRequest(name, email, role, timeStart, timeEnd);
        return analyticService.generateExportGroupTableBySearchConditions(emailUser, groupId, request, remainColumns);
    }

    /**
     * Exports general analytic information for groups.
     *
     * <p>This operation allows the export of general analytic information for groups, including details such as
     * group names, categories, and statuses. The exported data is paginated based on the provided page and pageSize parameters.
     *
     * @param customerUserDetails The principal information of the current user.
     * @param page                The page number for pagination (default is 0).
     * @param pageSize            The number of items per page for pagination (default is 25).
     * @return APIResponse<Page < GroupGeneralResponse>> containing the exported groups' general analytic data.
     */
    @GetMapping("groups")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Page<GroupGeneralResponse>> all(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int pageSize) {
        String emailUser = customerUserDetails.getEmail();
        Pageable pageRequest = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdDate"));
        return analyticService.getGroupGeneralAnalytic(emailUser, pageRequest);
    }

    /**
     * Imports multiple training points.
     *
     * <p>This operation allows the import of multiple training points from the specified file.
     *
     * @param customerUserDetails The principal information of the current user.
     * @param file                The multipart file containing the data to be imported.
     * @return APIResponse<Map < String, String>> containing information about the import process.
     * @throws IOException If an I/O exception occurs during the import process.
     */
    @PostMapping("import-training-point")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Map<String, String>> importTrainingPoint(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @RequestBody MultipartFile file)
            throws IOException {
        String emailUser = customerUserDetails.getEmail();
        return analyticService.importData(emailUser, file, "TRAINING_POINT");
    }

    /**
     * Import multiple records with English certifications.
     *
     * <p>This operation allows the import of multiple records with English certifications from the specified file.</p>
     *
     * @param customerUserDetails The principal information of the current user.
     * @param file                The multipart file containing the data to be imported.
     * @return APIResponse containing information about the import process.
     * @throws IOException If an I/O exception occurs during the import process.
     */
    @PostMapping("import-has-english-cert")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Map<String, String>> importHasEnglishCert(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @RequestBody MultipartFile file)
            throws IOException {
        String emailUser = customerUserDetails.getEmail();
        return analyticService.importData(emailUser, file, "HAS_ENGLISH_CERT");
    }

    /**
     * Import multiple records representing studying points.
     *
     * <p>This operation allows the import of multiple records with English certifications from the specified file.</p>
     *
     * @param customerUserDetails The principal information of the current user.
     * @param file                The multipart file containing the data to be imported.
     * @return APIResponse containing information about the import process.
     * @throws IOException If an I/O exception occurs during the import process.
     */
    @PostMapping("import-studying-point")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Map<String, String>> importStudyingPoint(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @RequestBody MultipartFile file)
            throws IOException {
        String emailUser = customerUserDetails.getEmail();
        return analyticService.importData(emailUser, file, "STUDYING_POINT");
    }

    /**
     * Import multiple records representing training points, English certificates, and studying points.
     * <p>
     * This operation allows the import of multiple records representing various types of analytics
     * from the specified file.
     *
     * @param customerUserDetails The principal information of the current user.
     * @param file                The multipart file containing the data to be imported.
     * @return APIResponse containing a list of ImportGeneralInformationResponse for each imported record.
     * @throws IOException If an I/O exception occurs during the import process.
     */
    @PostMapping("import-multiple")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<List<ImportGeneralInformationResponse>> importMultiple(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @RequestBody MultipartFile file)
            throws IOException {
        String emailUser = customerUserDetails.getEmail();
        return analyticService.importMultipleData(emailUser, file);
    }

    /**
     * Updates the training point, English certification, and studying point for a user.
     *
     * @param customerUserDetails The current user's principal information.
     * @param userId              The ID of the user to update.
     * @param request             The request containing the updated student information.
     * @return An APIResponse containing the updated user information.
     * @throws IOException If an I/O exception occurs.
     */
    @PatchMapping("{userId}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<User> update(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @PathVariable String userId,
            @RequestBody UpdateStudentInformationRequest request)
            throws IOException {
        String emailUser = customerUserDetails.getEmail();
        return analyticService.updateStudentInformation(emailUser, userId, request);
    }

    /**
     * Finds groups in analytic based on specified criteria.
     *
     * @param customerUserDetails The current user's principal information.
     * @param page                The page number for pagination (default is 0).
     * @param pageSize            The size of each page (default is 25).
     * @param groupName           The name of the group (optional).
     * @param groupCategory       The category of the group (optional).
     * @param status              The status of the group (optional).
     * @param timeStart           The start time for filtering (optional).
     * @param timeEnd             The end time for filtering (optional).
     * @return An APIResponse containing a paginated list of GroupGeneralResponse.
     * @throws IOException If an I/O exception occurs.
     */
    @GetMapping("find")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Page<GroupGeneralResponse>> findGroups(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int pageSize,
            @RequestParam(required = false) String groupName,
            @RequestParam(required = false) String groupCategory,
            @RequestParam(required = false) GroupStatus status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
            Date timeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
            Date timeEnd)
            throws IOException {
        String emailUser = customerUserDetails.getEmail();
        FindGroupGeneralAnalyticRequest request =
                new FindGroupGeneralAnalyticRequest(groupName, groupCategory, status, timeStart, timeEnd);
        Pageable pageRequest =
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdDate"));

        return analyticService.findGroupGeneralAnalytic(emailUser, pageRequest, request);
    }

    /**
     * Finds users in analytic within a specific group based on specified criteria.
     *
     * @param customerUserDetails The current user's principal information.
     * @param groupId             The ID of the group to search within.
     * @param name                The name of the user (optional).
     * @param email               The email of the user (optional).
     * @param role                The role of the user (optional).
     * @param timeStart           The start time for filtering (optional).
     * @param timeEnd             The end time for filtering (optional).
     * @return An APIResponse containing a list of GroupAnalyticResponse.Member.
     * @throws IOException If an I/O exception occurs.
     */
    @GetMapping("find/{groupId}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<List<GroupAnalyticResponse.Member>> findUsers(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @PathVariable String groupId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) FindUserAnalyticRequest.Role role,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
            Date timeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
            Date timeEnd)
            throws IOException {
        String emailUser = customerUserDetails.getEmail();
        FindUserAnalyticRequest request =
                new FindUserAnalyticRequest(name, email, role, timeStart, timeEnd);
        return analyticService.findUserAnalytic(emailUser, groupId, request);
    }

    /**
     * Exports a group report in PDF format.
     *
     * @param customerUserDetails The current user's principal information.
     * @param groupId             The ID of the group to export the report for.
     * @param request             The HTTP servlet request.
     * @param response            The HTTP servlet response.
     * @return ResponseEntity containing the exported group report in PDF format.
     */
    @GetMapping("report")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<?> getGroupReport(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @RequestParam String groupId,
            HttpServletRequest request,
            HttpServletResponse response) {
        JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(request.getServletContext());
        IWebExchange exchange = application.buildExchange(request, response);
        WebContext context = new WebContext(exchange);

        String reportHtml = analyticService.exportGroupReport(customerUserDetails.getEmail(), groupId, context);
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

    /**
     * Exports a group log in Excel format.
     *
     * @param customerUserDetails The current user's principal information.
     * @param groupId             The ID of the group to export the log for.
     * @param query               The array of query parameters for log attributes.
     * @return ResponseEntity containing the exported group log in Excel format.
     * @throws IOException If an I/O exception occurs.
     */
    @GetMapping("log")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ResponseEntity<?> getGroupLog(
            @Parameter(hidden = true) @CurrentUser CustomerUserDetails customerUserDetails,
            @RequestParam String groupId,
            @RequestParam String[] query)
            throws IOException {
        List<AnalyticAttribute> attributes =
                Stream.of(query).map(AnalyticAttribute::valueOf).toList();
        byte[] content = analyticService.getGroupLog(customerUserDetails.getEmail(), groupId, attributes);
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