package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.Task;
import com.hcmus.mentor.backend.domain.constant.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, String> {
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

    long countByGroupIdAndAssigneeIdsStatusIn(String groupId, TaskStatus status);

    long countByGroupIdAndAssigneeIdsUserIdInAndAssigneeIdsStatusIn(
            String groupId, String assigneeId, TaskStatus status);

    List<Task> findByGroupIdInAndAssigneeIdsContainingAndDeadlineBetween(List<String> groupIds, String userId, Date startTime, Date endTime);

    @Query("SELECT t FROM Task t " +
            "LEFT JOIN t.assignees a " +
            "LEFT JOIN a.user u " +
            "LEFT JOIN t.assigner assigner " +
            "WHERE t.group.id IN :groupIds " +
            "AND t.deadline > :currentDate " +
            "AND (u.id = :userId OR assigner.id = :userId) " +
            "ORDER BY t.deadline DESC")
    Page<Task> findTasksByCriteria(String userId, List<String> groupIds, Date currentDate, Pageable pageable);
}
