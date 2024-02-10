package com.hcmus.mentor.backend.controller;

import com.hcmus.mentor.backend.controller.payload.ApiResponseDto;
import com.hcmus.mentor.backend.controller.payload.request.AddTaskRequest;
import com.hcmus.mentor.backend.controller.payload.request.UpdateStatusByMentorRequest;
import com.hcmus.mentor.backend.controller.payload.request.UpdateTaskRequest;
import com.hcmus.mentor.backend.controller.payload.response.tasks.TaskAssigneeResponse;
import com.hcmus.mentor.backend.controller.payload.response.tasks.TaskDetailResponse;
import com.hcmus.mentor.backend.controller.payload.response.tasks.TaskResponse;
import com.hcmus.mentor.backend.controller.payload.response.users.ProfileResponse;
import com.hcmus.mentor.backend.domain.Task;
import com.hcmus.mentor.backend.domain.constant.TaskStatus;
import com.hcmus.mentor.backend.security.CurrentUser;
import com.hcmus.mentor.backend.security.UserPrincipal;
import com.hcmus.mentor.backend.service.TaskServiceImpl;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Task controller.
 */
@Tag(name = "task")
@RestController
@RequestMapping("api/tasks")
@SecurityRequirement(name = "bearer")
@RequiredArgsConstructor
public class TaskController {

    private final TaskServiceImpl taskService;

    /**
     * Retrieve fully detailed information of a task.
     *
     * @param userPrincipal The current user's principal information.
     * @param id            The ID of the task to retrieve.
     * @return APIResponse containing the detailed information of the task or an error response.
     */
    @GetMapping(value = "{id}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<TaskDetailResponse> get(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal, @PathVariable String id) {
        String emailUser = userPrincipal.getEmail();
        TaskServiceImpl.TaskReturnService taskReturn = taskService.getTask(emailUser, id);
        return new ApiResponseDto(
                taskReturn.getData(), taskReturn.getReturnCode(), taskReturn.getMessage());
    }

    /**
     * Add a new task to a group.
     *
     * @param userPrincipal The current user's principal information.
     * @param request       The request object containing information about the new task.
     * @return APIResponse containing the added task or an error response.
     */
    @PostMapping("")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Task> add(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @RequestBody AddTaskRequest request) {
        String emailUser = userPrincipal.getEmail();
        TaskServiceImpl.TaskReturnService taskReturn = taskService.addTask(emailUser, request);
        return new ApiResponseDto(
                taskReturn.getData(), taskReturn.getReturnCode(), taskReturn.getMessage());
    }

    /**
     * Update an existing task with new information.
     *
     * @param userPrincipal The current user's principal information.
     * @param id            The ID of the task to update.
     * @param request       The request object containing the updated information for the task.
     * @return APIResponse containing the updated task or an error response.
     */
    @PatchMapping("{id}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Task> update(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @PathVariable String id,
            @RequestBody UpdateTaskRequest request) {
        TaskServiceImpl.TaskReturnService taskReturn = taskService.updateTask(userPrincipal, id, request);
        return new ApiResponseDto(
                taskReturn.getData(), taskReturn.getReturnCode(), taskReturn.getMessage());
    }

    /**
     * Mentee update status of a task.
     *
     * @param userPrincipal The current user's principal information.
     * @param id            The ID of the task to update the status.
     * @param status        The new status for the task.
     * @return APIResponse containing the updated task or an error response.
     */
    @PatchMapping("{id}/{status}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Task> updateStatus(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @PathVariable String id,
            @PathVariable TaskStatus status) {
        String emailUser = userPrincipal.getEmail();
        TaskServiceImpl.TaskReturnService taskReturn = taskService.updateStatus(emailUser, id, status);
        return new ApiResponseDto(
                taskReturn.getData(), taskReturn.getReturnCode(), taskReturn.getMessage());
    }

    /**
     * Mentor update status of any task in a group.
     *
     * @param userPrincipal The current user's principal information.
     * @param id            The ID of the task to update the status.
     * @param request       The request object containing the updated status for the task.
     * @return APIResponse containing the updated task or an error response.
     */
    @PatchMapping("mentor/{id}/status")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<Task> updateStatusByMentor(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @PathVariable String id,
            @RequestBody UpdateStatusByMentorRequest request) {
        String emailUser = userPrincipal.getEmail();
        TaskServiceImpl.TaskReturnService taskReturn =
                taskService.updateStatusByMentor(emailUser, id, request);
        return new ApiResponseDto(
                taskReturn.getData(), taskReturn.getReturnCode(), taskReturn.getMessage());
    }

    /**
     * Delete an existing task (Only mentor).
     *
     * @param userPrincipal The current user's principal information.
     * @param id            The ID of the task to delete.
     * @return APIResponse containing the result of the delete operation or an error response.
     */
    @DeleteMapping("{id}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto delete(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal, @PathVariable String id) {
        TaskServiceImpl.TaskReturnService taskReturn = taskService.deleteTask(userPrincipal, id);
        return new ApiResponseDto(
                taskReturn.getData(), taskReturn.getReturnCode(), taskReturn.getMessage());
    }

    /**
     * Retrieve all tasks of a group (mentor and mentee in group).
     *
     * @param userPrincipal The current user's principal information.
     * @param groupId       The ID of the group for which tasks are retrieved.
     * @return APIResponse containing a list of task details or an error response.
     */
    @GetMapping("group/{groupId}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<List<TaskDetailResponse>> getByGroupId(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @PathVariable String groupId) {
        String emailUser = userPrincipal.getEmail();
        TaskServiceImpl.TaskReturnService taskReturn = taskService.getTasksByGroupId(emailUser, groupId);
        return new ApiResponseDto(
                taskReturn.getData(), taskReturn.getReturnCode(), taskReturn.getMessage());
    }

    /**
     * Retrieve all tasks assigned to a user.
     *
     * @param userPrincipal The current user's principal information.
     * @return APIResponse containing a list of task details or an error response.
     */
    @GetMapping("user")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<List<TaskDetailResponse>> getByUserId(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal) {
        String emailUser = userPrincipal.getEmail();
        TaskServiceImpl.TaskReturnService taskReturn = taskService.getTasksByEmailUser(emailUser);
        return new ApiResponseDto(
                taskReturn.getData(), taskReturn.getReturnCode(), taskReturn.getMessage());
    }

    /**
     * Get the assigner of a task.
     *
     * @param userPrincipal The current user's principal information.
     * @param id            The ID of the task to get the assigner.
     * @return APIResponse containing the profile of the task's assigner or an error response.
     */
    @GetMapping("{id}/assigner")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<ProfileResponse> getAssigner(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal, @PathVariable String id) {
        String emailUser = userPrincipal.getEmail();
        TaskServiceImpl.TaskReturnService taskReturn = taskService.getTaskAssigner(emailUser, id);
        return new ApiResponseDto(
                taskReturn.getData(), taskReturn.getReturnCode(), taskReturn.getMessage());
    }

    /**
     * Get the assignees of a task.
     *
     * @param userPrincipal The current user's principal information.
     * @param id            The ID of the task to get the assignees.
     * @return APIResponse containing a list of task assignees or an error response.
     */
    @GetMapping("{id}/assignees")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<List<TaskAssigneeResponse>> getAssignees(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal, @PathVariable String id) {
        String emailUser = userPrincipal.getEmail();
        TaskServiceImpl.TaskReturnService taskReturn = taskService.getTaskAssigneesWrapper(emailUser, id);
        return new ApiResponseDto(
                taskReturn.getData(), taskReturn.getReturnCode(), taskReturn.getMessage());
    }

    /**
     * Get all tasks assigned and owned by the current user.
     *
     * @param userPrincipal The current user's principal information.
     * @param groupId       The ID of the group to filter tasks.
     * @return APIResponse containing a list of task responses or an error response.
     */
    @GetMapping("own")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<List<TaskResponse>> getAllOwnTask(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @RequestParam("groupId") String groupId) {
        TaskServiceImpl.TaskReturnService taskReturn =
                taskService.getAllOwnTasks(groupId, userPrincipal.getId());
        return new ApiResponseDto(
                taskReturn.getData(), taskReturn.getReturnCode(), taskReturn.getMessage());
    }

    /**
     * Get all tasks assigned to the current user.
     *
     * @param userPrincipal The current user's principal information.
     * @param groupId       The ID of the group to filter tasks.
     * @return APIResponse containing a list of task responses or an error response.
     */
    @GetMapping("assigned")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<List<TaskResponse>> getAllOwnAssignedTask(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @RequestParam("groupId") String groupId) {
        TaskServiceImpl.TaskReturnService taskReturn =
                taskService.wrapOwnAssignedTasks(groupId, userPrincipal.getId());
        return new ApiResponseDto(
                taskReturn.getData(), taskReturn.getReturnCode(), taskReturn.getMessage());
    }

    /**
     * Get all tasks assigned by the current user to others.
     *
     * @param userPrincipal The current user's principal information.
     * @param groupId       The ID of the group to filter tasks.
     * @return APIResponse containing a list of task responses or an error response.
     */
    @GetMapping("assigning")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401", description = "Need authentication")
    public ApiResponseDto<List<TaskResponse>> getAllOwnAssignedByMeTask(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @RequestParam("groupId") String groupId) {
        TaskServiceImpl.TaskReturnService taskReturn =
                taskService.wrapAssignedByMeTasks(groupId, userPrincipal.getId());
        return new ApiResponseDto(
                taskReturn.getData(), taskReturn.getReturnCode(), taskReturn.getMessage());
    }
}
