package com.hcmus.mentor.backend.service;

import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.controller.payload.APIResponse;
import com.hcmus.mentor.backend.controller.payload.request.FindGroupGeneralAnalyticRequest;
import com.hcmus.mentor.backend.controller.payload.request.FindUserAnalyticRequest;
import com.hcmus.mentor.backend.controller.payload.request.UpdateStudentInformationRequest;
import com.hcmus.mentor.backend.controller.payload.response.analytic.GroupAnalyticResponse;
import com.hcmus.mentor.backend.controller.payload.response.analytic.ImportGeneralInformationResponse;
import com.hcmus.mentor.backend.controller.payload.response.analytic.SystemAnalyticChartResponse;
import com.hcmus.mentor.backend.controller.payload.response.analytic.SystemAnalyticResponse;
import com.hcmus.mentor.backend.controller.payload.response.groups.GroupGeneralResponse;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.context.WebContext;

/**
 * Analytic service.
 */
public interface AnalyticService {
    APIResponse<SystemAnalyticResponse> getGeneralInformation(String emailUser);

    APIResponse<SystemAnalyticResponse> getGeneralInformationByGroupCategory(
            String emailUser, String groupCategoryId);

    APIResponse<SystemAnalyticChartResponse> getDataForChart(
            String emailUser, int monthStart, int yearStart, int monthEnd, int yearEnd)
            throws ParseException;

    APIResponse<SystemAnalyticChartResponse> getDataForChartByGroupCategory(
            String emailUser,
            int monthStart,
            int yearStart,
            int monthEnd,
            int yearEnd,
            String groupCategoryId)
            throws ParseException;

    APIResponse<GroupAnalyticResponse> getGroupAnalytic(String emailUser, String groupId);

    ResponseEntity<Resource> generateExportGroupTable(
            List<GroupAnalyticResponse.Member> members, List<String> remainColumns) throws IOException;

    ResponseEntity<Resource> generateExportGroupTable(
            String emailUser, String groupId, List<String> remainColumns) throws IOException;

    ResponseEntity<Resource> generateExportGroupTableBySearchConditions(
            String emailUser, String groupId, FindUserAnalyticRequest request, List<String> remainColumns)
            throws IOException;

    APIResponse<Page<GroupGeneralResponse>> getGroupGeneralAnalytic(
            String emailUser, Pageable pageRequest);

    ResponseEntity<Resource> generateExportGroupsTable(String emailUser, List<String> remainColumns)
            throws IOException;

    ResponseEntity<Resource> generateExportGroupsTableBySearchConditions(
            String emailUser, FindGroupGeneralAnalyticRequest request, List<String> remainColumns)
            throws IOException;

    APIResponse<Page<GroupGeneralResponse>> findGroupGeneralAnalytic(
            String emailUser, Pageable pageRequest, FindGroupGeneralAnalyticRequest request);

    APIResponse<List<GroupAnalyticResponse.Member>> findUserAnalytic(
            String emailUser, String groupId, FindUserAnalyticRequest request);

    APIResponse<Map<String, String>> importData(String emailUser, MultipartFile file, String type)
            throws IOException;

    APIResponse<List<ImportGeneralInformationResponse>> importMultipleData(
            String emailUser, MultipartFile file) throws IOException;

    APIResponse<User> updateStudentInformation(
            String emailUser, String userId, UpdateStudentInformationRequest request);

    String exportGroupReport(String exporterEmail, String groupId, WebContext context);

    byte[] getGroupLog(String exporterEmail, String groupId, List<AnalyticAttribute> attributes)
            throws IOException;
}
