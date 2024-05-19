package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.Task;
import com.hcmus.mentor.backend.domain.constant.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, String> {

    Task findFirstByGroupIdInOrderByCreatedDateDesc(List<String> groupIds);

    Task findFirstByGroupIdAndAssignerIdOrderByCreatedDateDesc(String groupId, String assignerId);

    List<Task> findByGroupId(String groupId);

    List<Task> findAllByParentTaskId(String parentTask);

    List<Task> findByCreatedDateBetween(Date start, Date end);

    List<Task> findByGroupIdInAndCreatedDateBetween(List<String> groupIds, Date start, Date end);

    List<Task> findAllByGroupIdAndAssignerIdOrderByCreatedDateDesc(String groupId, String assignerId);

    List<Task> findAllByDeadlineBetween(Date date1, Date date2);

    List<Task> findAllByGroupIdIn(List<String> groupIds);

    List<Task> findAllByGroupId(String groupId);

    @Query("SELECT t FROM Task t " +
            "JOIN t.assignees assignees " +
            "WHERE assignees.id = :userId " +
            "AND t.deadline > :currentDate " +
            "ORDER BY t.deadline DESC")
    List<Task> findByAssigneeIdsUserId(String userId);

    @Query("SELECT t FROM Task t " +
            "JOIN t.assignees assignees " +
            "WHERE assignees.id = ?2 " +
            "AND t.id=?1 ")
    Boolean existsByIdAndAssigneeIdsUserId(String taskId, String userId);

    @Query("SELECT t FROM Task t " +
            "INNER JOIN t.assignees assignees " +
            "WHERE t.group.id IN ?1 " +
            "AND assignees.id IN ?2 ")
    List<Task> findAllByGroupIdInAndAssigneeIdsUserIdIn(List<String> groupIds, List<String> id);

    @Query("select count(t) from Task t join t.group channel where channel.id in :groupId and t.assigner.id = :userId")
    long countAllOwnTaskOfGroup(List<String> groupId, String userId);

    @Query("SELECT count(t) " +
            "FROM Task t " +
            "INNER JOIN t.assignees assignees " +
            "WHERE t.group.id IN ?1 AND assignees.id = ?2 AND assignees.status = ?3")
    long countAllOwnTaskOfGroupWithStatus(List<String> groupId, String userId, TaskStatus status);

    @Query("SELECT t " +
            "FROM Task t " +
            "INNER JOIN t.assignees assignees " +
            "WHERE (t.assigner.id = ?2 or assignees.id = ?2)" +
            "and t.group.id in ?1")
    List<Task> findAllByOwn(List<String> channelIds, String userId);

    @Query("SELECT t FROM Task t " +
            "INNER JOIN t.assignees a " +
            "INNER JOIN a.user u " +
            "INNER JOIN t.assigner assigner " +
            "WHERE t.group.id IN ?1 " +
            "AND (u.id = ?2 OR assigner.id = ?2) " +
            "AND t.deadline > ?3 " +
            "ORDER BY t.deadline ASC")
    List<Task> findAllAndHasUserAndDeadlineAfter(List<String> channelIds, String userId, LocalDateTime current);
}