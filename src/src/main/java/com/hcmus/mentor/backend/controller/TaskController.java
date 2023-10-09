package com.hcmus.mentor.backend.controller;

import com.hcmus.mentor.backend.entity.Task;
import com.hcmus.mentor.backend.payload.APIResponse;
import com.hcmus.mentor.backend.payload.request.AddTaskRequest;
import com.hcmus.mentor.backend.payload.request.UpdateStatusByMentorRequest;
import com.hcmus.mentor.backend.payload.request.UpdateTaskRequest;
import com.hcmus.mentor.backend.payload.response.tasks.TaskAssigneeResponse;
import com.hcmus.mentor.backend.payload.response.tasks.TaskDetailResponse;
import com.hcmus.mentor.backend.payload.response.tasks.TaskResponse;
import com.hcmus.mentor.backend.payload.response.users.ProfileResponse;
import com.hcmus.mentor.backend.payload.returnCode.TaskReturnCode;
import com.hcmus.mentor.backend.security.CurrentUser;
import com.hcmus.mentor.backend.security.UserPrincipal;
import com.hcmus.mentor.backend.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Task APIs", description = "REST APIs for Task collections")
@RestController
@RequestMapping("/api/tasks")
@SecurityRequirement(name = "bearer")
public class TaskController {

  private final TaskService taskService;

  public TaskController(TaskService taskService) {
    this.taskService = taskService;
  }

  @Operation(
      summary = "Retrieve task data",
      description = "Retrieve fully information of task",
      tags = "Task APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Retrieve successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))),
    @ApiResponse(responseCode = "401", description = "Need authentication"),
    @ApiResponse(
        responseCode = TaskReturnCode.NOT_FOUND_GROUP_STRING,
        description = "Not found group"),
    @ApiResponse(
        responseCode = TaskReturnCode.NOT_FOUND_PARENT_TASK_STRING,
        description = "Not found parent task"),
    @ApiResponse(
        responseCode = TaskReturnCode.NOT_FOUND_USER_IN_GROUP_STRING,
        description = "Not found user in group"),
    @ApiResponse(
        responseCode = TaskReturnCode.NOT_FOUND_TASK_STRING,
        description = "Not found task"),
    @ApiResponse(
        responseCode = TaskReturnCode.NOT_ENOUGH_FIELDS_STRING,
        description = "Not enough required field input")
  })
  @GetMapping(value = "/{id}")
  public APIResponse<TaskDetailResponse> get(
      @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal, @PathVariable String id) {
    String emailUser = userPrincipal.getEmail();
    TaskService.TaskReturnService taskReturn = taskService.getTask(emailUser, id);
    return new APIResponse(
        taskReturn.getData(), taskReturn.getReturnCode(), taskReturn.getMessage());
  }

  @Operation(
      summary = "Add new task",
      description = "Create new task in a group",
      tags = "Task APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Add successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))),
    @ApiResponse(responseCode = "401", description = "Need authentication"),
    @ApiResponse(
        responseCode = TaskReturnCode.NOT_FOUND_GROUP_STRING,
        description = "Not found group"),
    @ApiResponse(
        responseCode = TaskReturnCode.NOT_FOUND_PARENT_TASK_STRING,
        description = "Not found parent task"),
    @ApiResponse(
        responseCode = TaskReturnCode.NOT_FOUND_USER_IN_GROUP_STRING,
        description = "Not found user in group"),
    @ApiResponse(
        responseCode = TaskReturnCode.NOT_FOUND_TASK_STRING,
        description = "Not found task"),
    @ApiResponse(
        responseCode = TaskReturnCode.NOT_ENOUGH_FIELDS_STRING,
        description = "Not enough required field input")
  })
  @PostMapping(value = "")
  public APIResponse<Task> add(
      @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
      @RequestBody AddTaskRequest request) {
    String emailUser = userPrincipal.getEmail();
    TaskService.TaskReturnService taskReturn = taskService.addTask(emailUser, request);
    return new APIResponse(
        taskReturn.getData(), taskReturn.getReturnCode(), taskReturn.getMessage());
  }

  @Operation(
      summary = "Update an existing task",
      description = "Update an existing task with new information",
      tags = "Task APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Update successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))),
    @ApiResponse(responseCode = "401", description = "Need authentication"),
    @ApiResponse(
        responseCode = TaskReturnCode.NOT_FOUND_GROUP_STRING,
        description = "Not found group"),
    @ApiResponse(
        responseCode = TaskReturnCode.NOT_FOUND_PARENT_TASK_STRING,
        description = "Not found parent task"),
    @ApiResponse(
        responseCode = TaskReturnCode.NOT_FOUND_USER_IN_GROUP_STRING,
        description = "Not found user in group"),
    @ApiResponse(
        responseCode = TaskReturnCode.NOT_FOUND_TASK_STRING,
        description = "Not found task"),
    @ApiResponse(
        responseCode = TaskReturnCode.NOT_ENOUGH_FIELDS_STRING,
        description = "Not enough required field input")
  })
  @PatchMapping(value = "/{id}")
  public APIResponse<Task> update(
      @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
      @PathVariable String id,
      @RequestBody UpdateTaskRequest request) {
    String emailUser = userPrincipal.getEmail();
    TaskService.TaskReturnService taskReturn = taskService.updateTask(userPrincipal, id, request);
    return new APIResponse(
        taskReturn.getData(), taskReturn.getReturnCode(), taskReturn.getMessage());
  }

  @Operation(
      summary = "Mentee update status of task",
      description = "Assignees can update status of their own assigned task",
      tags = "Task APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Update status successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))),
    @ApiResponse(responseCode = "401", description = "Need authentication"),
    @ApiResponse(
        responseCode = TaskReturnCode.NOT_FOUND_GROUP_STRING,
        description = "Not found group"),
    @ApiResponse(
        responseCode = TaskReturnCode.NOT_FOUND_PARENT_TASK_STRING,
        description = "Not found parent task"),
    @ApiResponse(
        responseCode = TaskReturnCode.NOT_FOUND_USER_IN_GROUP_STRING,
        description = "Not found user in group"),
    @ApiResponse(
        responseCode = TaskReturnCode.NOT_FOUND_TASK_STRING,
        description = "Not found task"),
    @ApiResponse(
        responseCode = TaskReturnCode.NOT_ENOUGH_FIELDS_STRING,
        description = "Not enough required field input")
  })
  @PatchMapping(value = "/{id}/{status}")
  public APIResponse<Task> updateStatus(
      @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
      @PathVariable String id,
      @PathVariable Task.Status status) {
    String emailUser = userPrincipal.getEmail();
    TaskService.TaskReturnService taskReturn = taskService.updateStatus(emailUser, id, status);
    return new APIResponse(
        taskReturn.getData(), taskReturn.getReturnCode(), taskReturn.getMessage());
  }

  @Operation(
      summary = "Mentor update status",
      description = "Mentor can update status of any task in group",
      tags = "Task APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Retrieve successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))),
    @ApiResponse(responseCode = "401", description = "Need authentication"),
    @ApiResponse(
        responseCode = TaskReturnCode.NOT_FOUND_GROUP_STRING,
        description = "Not found group"),
    @ApiResponse(
        responseCode = TaskReturnCode.NOT_FOUND_PARENT_TASK_STRING,
        description = "Not found parent task"),
    @ApiResponse(
        responseCode = TaskReturnCode.NOT_FOUND_USER_IN_GROUP_STRING,
        description = "Not found user in group"),
    @ApiResponse(
        responseCode = TaskReturnCode.NOT_FOUND_TASK_STRING,
        description = "Not found task"),
    @ApiResponse(
        responseCode = TaskReturnCode.NOT_ENOUGH_FIELDS_STRING,
        description = "Not enough required field input")
  })
  @PatchMapping(value = "/mentor/{id}/status")
  public APIResponse<Task> updateStatusByMentor(
      @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
      @PathVariable String id,
      @RequestBody UpdateStatusByMentorRequest request) {
    String emailUser = userPrincipal.getEmail();
    TaskService.TaskReturnService taskReturn =
        taskService.updateStatusByMentor(emailUser, id, request);
    return new APIResponse(
        taskReturn.getData(), taskReturn.getReturnCode(), taskReturn.getMessage());
  }

  @Operation(
      summary = "Delete an existing task (Only mentor)",
      description = "Only mentors can have permission to delete a task on system",
      tags = "Task APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Delete successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))),
    @ApiResponse(responseCode = "401", description = "Need authentication"),
    @ApiResponse(
        responseCode = TaskReturnCode.NOT_FOUND_GROUP_STRING,
        description = "Not found group"),
    @ApiResponse(
        responseCode = TaskReturnCode.NOT_FOUND_PARENT_TASK_STRING,
        description = "Not found parent task"),
    @ApiResponse(
        responseCode = TaskReturnCode.NOT_FOUND_USER_IN_GROUP_STRING,
        description = "Not found user in group"),
    @ApiResponse(
        responseCode = TaskReturnCode.NOT_FOUND_TASK_STRING,
        description = "Not found task"),
    @ApiResponse(
        responseCode = TaskReturnCode.NOT_ENOUGH_FIELDS_STRING,
        description = "Not enough required field input")
  })
  @DeleteMapping(value = "/{id}")
  public APIResponse delete(
      @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal, @PathVariable String id) {
    String emailUser = userPrincipal.getEmail();
    TaskService.TaskReturnService taskReturn = taskService.deleteTask(userPrincipal, id);
    return new APIResponse(
        taskReturn.getData(), taskReturn.getReturnCode(), taskReturn.getMessage());
  }

  @Operation(
      summary = "Retrieve all tasks of a group (mentor and mentee in group)",
      description = "",
      tags = "Task APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Retrieve successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))),
    @ApiResponse(responseCode = "401", description = "Need authentication"),
    @ApiResponse(
        responseCode = TaskReturnCode.NOT_FOUND_GROUP_STRING,
        description = "Not found group"),
    @ApiResponse(
        responseCode = TaskReturnCode.NOT_FOUND_PARENT_TASK_STRING,
        description = "Not found parent task"),
    @ApiResponse(
        responseCode = TaskReturnCode.NOT_FOUND_USER_IN_GROUP_STRING,
        description = "Not found user in group"),
    @ApiResponse(
        responseCode = TaskReturnCode.NOT_FOUND_TASK_STRING,
        description = "Not found task"),
    @ApiResponse(
        responseCode = TaskReturnCode.NOT_ENOUGH_FIELDS_STRING,
        description = "Not enough required field input")
  })
  @GetMapping(value = "/group/{groupId}")
  public APIResponse<List<TaskDetailResponse>> getByGroupId(
      @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
      @PathVariable String groupId) {
    String emailUser = userPrincipal.getEmail();
    TaskService.TaskReturnService taskReturn = taskService.getTasksByGroupId(emailUser, groupId);
    return new APIResponse(
        taskReturn.getData(), taskReturn.getReturnCode(), taskReturn.getMessage());
  }

  @Operation(
      summary = "Retrieve all tasks of user",
      description = "Retrieve all tasks has been assigned to user through by user id",
      tags = "Task APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Retrieve successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))),
    @ApiResponse(responseCode = "401", description = "Need authentication"),
    @ApiResponse(
        responseCode = TaskReturnCode.NOT_FOUND_GROUP_STRING,
        description = "Not found group"),
    @ApiResponse(
        responseCode = TaskReturnCode.NOT_FOUND_PARENT_TASK_STRING,
        description = "Not found parent task"),
    @ApiResponse(
        responseCode = TaskReturnCode.NOT_FOUND_USER_IN_GROUP_STRING,
        description = "Not found user in group"),
    @ApiResponse(
        responseCode = TaskReturnCode.NOT_FOUND_TASK_STRING,
        description = "Not found task"),
    @ApiResponse(
        responseCode = TaskReturnCode.NOT_ENOUGH_FIELDS_STRING,
        description = "Not enough required field input")
  })
  @GetMapping(value = "/user")
  public APIResponse<List<TaskDetailResponse>> getByUserId(
      @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal) {
    String emailUser = userPrincipal.getEmail();
    TaskService.TaskReturnService taskReturn = taskService.getTasksByEmailUser(emailUser);
    return new APIResponse(
        taskReturn.getData(), taskReturn.getReturnCode(), taskReturn.getMessage());
  }

  @Operation(summary = "Get task assigner", description = "Get task's assigner", tags = "Task APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Get task's assigner successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))),
    @ApiResponse(responseCode = "401", description = "Need authentication"),
    @ApiResponse(
        responseCode = TaskReturnCode.NOT_FOUND_TASK_STRING,
        description = "Not found task"),
  })
  @GetMapping(value = "/{id}/assigner")
  public APIResponse<ProfileResponse> getAssigner(
      @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal, @PathVariable String id) {
    String emailUser = userPrincipal.getEmail();
    TaskService.TaskReturnService taskReturn = taskService.getTaskAssigner(emailUser, id);
    return new APIResponse(
        taskReturn.getData(), taskReturn.getReturnCode(), taskReturn.getMessage());
  }

  @Operation(summary = "Get task assigner", description = "Get task's assigner", tags = "Task APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Get task's assigner successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))),
    @ApiResponse(responseCode = "401", description = "Need authentication"),
    @ApiResponse(
        responseCode = TaskReturnCode.NOT_FOUND_TASK_STRING,
        description = "Not found task"),
  })
  @GetMapping(value = "/{id}/assignees")
  public APIResponse<List<TaskAssigneeResponse>> getAssignees(
      @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal, @PathVariable String id) {
    String emailUser = userPrincipal.getEmail();
    TaskService.TaskReturnService taskReturn = taskService.getTaskAssigneesWrapper(emailUser, id);
    return new APIResponse(
        taskReturn.getData(), taskReturn.getReturnCode(), taskReturn.getMessage());
  }

  @Operation(
      summary = "Get your own tasks",
      description = "Get all tasks that I assigned and have been assigned to me",
      tags = "Task APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Get successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))),
    @ApiResponse(responseCode = "401", description = "Need authentication"),
    @ApiResponse(
        responseCode = TaskReturnCode.NOT_FOUND_TASK_STRING,
        description = "Not found task"),
  })
  @GetMapping(value = "/own")
  public APIResponse<List<TaskResponse>> getAllOwnTask(
      @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
      @RequestParam("groupId") String groupId) {
    TaskService.TaskReturnService taskReturn =
        taskService.getAllOwnTasks(groupId, userPrincipal.getId());
    return new APIResponse(
        taskReturn.getData(), taskReturn.getReturnCode(), taskReturn.getMessage());
  }

  @Operation(
      summary = "Get your assigned tasks",
      description = "Get all tasks have been assigned to me",
      tags = "Task APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Get successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))),
    @ApiResponse(responseCode = "401", description = "Need authentication"),
    @ApiResponse(
        responseCode = TaskReturnCode.NOT_FOUND_TASK_STRING,
        description = "Not found task"),
  })
  @GetMapping(value = "/assigned")
  public APIResponse<List<TaskResponse>> getAllOwnAssignedTask(
      @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
      @RequestParam("groupId") String groupId) {
    TaskService.TaskReturnService taskReturn =
        taskService.wrapOwnAssignedTasks(groupId, userPrincipal.getId());
    return new APIResponse(
        taskReturn.getData(), taskReturn.getReturnCode(), taskReturn.getMessage());
  }

  @Operation(
      summary = "Get tasks that you assigned to someone",
      description = "Get all tasks that I assigned to someone",
      tags = "Task APIs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Get successfully",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = APIResponse.class)))),
    @ApiResponse(responseCode = "401", description = "Need authentication"),
    @ApiResponse(
        responseCode = TaskReturnCode.NOT_FOUND_TASK_STRING,
        description = "Not found task"),
  })
  @GetMapping(value = "/assigning")
  public APIResponse<List<TaskResponse>> getAllOwnAssignedByMeTask(
      @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
      @RequestParam("groupId") String groupId) {
    TaskService.TaskReturnService taskReturn =
        taskService.wrapAssignedByMeTasks(groupId, userPrincipal.getId());
    return new APIResponse(
        taskReturn.getData(), taskReturn.getReturnCode(), taskReturn.getMessage());
  }
}
