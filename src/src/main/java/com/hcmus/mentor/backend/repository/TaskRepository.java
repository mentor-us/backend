package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.entity.Task;
import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TaskRepository extends MongoRepository<Task, String> {
  List<Task> findByGroupId(String groupId);

  List<Task> findByAssigneeIdsUserId(String userId);

  Boolean existsByIdAndAssigneeIdsUserId(String taskId, String userId);

  List<Task> findAllByParentTask(String parentTask);

  Page<Task> findAllByGroupIdInAndAssigneeIdsUserIdInAndDeadlineGreaterThan(
      List<String> groupIds, List<String> id, Date now, PageRequest pageRequest);

  List<Task> findAllByGroupIdInAndAssigneeIdsUserIdIn(List<String> groupIds, List<String> id);

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

  long countByGroupIdAndAssigneeIdsStatusIn(String groupId, Task.Status status);

  long countByGroupIdAndAssigneeIdsUserIdInAndAssigneeIdsStatusIn(
      String groupId, String assigneeId, Task.Status status);
}
