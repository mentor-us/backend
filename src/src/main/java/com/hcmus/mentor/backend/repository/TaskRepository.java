package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.Task;
import com.hcmus.mentor.backend.domain.constant.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;

public interface TaskRepository extends MongoRepository<Task, String> {
    List<Task> findByGroupId(String groupId);

    List<Task> findByAssigneeIdsUserId(String userId);

    Boolean existsByIdAndAssigneeIdsUserId(String taskId, String userId);

    List<Task> findAllByParentTask(String parentTask);

    Page<Task> findAllByGroupIdInAndAssigneeIdsUserIdInAndDeadlineGreaterThan(
            List<String> groupIds, List<String> id, Date now, PageRequest pageRequest);

    List<Task> findAllByGroupIdInAndAssigneeIdsUserIdIn(List<String> groupIds, List<String> id);

    @Aggregation(pipeline = {
            "{$match: {$and: [{groupId: {$in: ?0}}, {$or: [{assignerId: ?1}, {assigneeIds: {$elemMatch: {userId: ?1}}} ]}]}}",
            "{$sort: {createdDate: -1}}"
    })
    List<Task> findAllOwnTasks(List<String> groupId, String userId);

    List<Task> findAllByGroupIdInAndAssigneeIdsUserIdInAndDeadlineGreaterThanAndDeadlineLessThan(
            List<String> groupIds, List<String> id, Date start, Date end);

    long countByCreatedDateBetween(Date start, Date end);

    List<Task> findByCreatedDateBetween(Date start, Date end);

    List<Task> findByGroupIdInAndCreatedDateBetween(List<String> groupIds, Date start, Date end);

    Task findFirstByGroupIdOrderByCreatedDateDesc(String groupId);

    List<Task> findByGroupIdAndAssignerId(String groupId, String assignerId);

    long countByGroupIdAndAssigneeIdsUserIdIn(String groupId, String assigneeId);

    Task findFirstByGroupIdAndAssignerIdOrderByCreatedDateDesc(String groupId, String assignerId);

    Task findFirstByGroupIdAndAssigneeIdsUserIdInOrderByCreatedDateDesc(
            String groupId, String assigneeId);

    List<Task> findAllByGroupIdAndAssigneeIdsUserIdInOrderByCreatedDateDesc(
            String groupId, List<String> userIds);

    List<Task> findAllByGroupIdAndAssignerIdOrderByCreatedDateDesc(String groupId, String assignerId);

    List<Task> findAllByDeadlineBetween(Date date1, Date date2);

    List<Task> findAllByGroupIdIn(List<String> groupIds);

    List<Task> findAllByGroupId(String groupId);

    long countByGroupIdAndAssigneeIdsStatusIn(String groupId, TaskStatus status);

    long countByGroupIdAndAssigneeIdsUserIdInAndAssigneeIdsStatusIn(
            String groupId, String assigneeId, TaskStatus status);
}
