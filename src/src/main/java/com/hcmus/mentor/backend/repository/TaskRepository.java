package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.Task;
import com.hcmus.mentor.backend.domain.constant.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, String> {
    List<Task> findByGroupId(String groupId);

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

    List<Task> findAllByParentTaskId(String parentTask);

//    Page<Task> findAllByGroupIdInAndAssigneeIdsUserIdInAndDeadlineGreaterThan(List<String> groupIds, List<String> id, Date now, PageRequest pageRequest);

    @Query("SELECT t FROM Task t " +
            "JOIN t.assignees assignees " +
            "WHERE t.group.id IN ?1 " +
            "AND assignees.id IN ?2 ")
    List<Task> findAllByGroupIdInAndAssigneeIdsUserIdIn(List<String> groupIds, List<String> id);

//    List<Task> findAllByGroupIdInAndAssigneeIdsUserIdInAndDeadlineGreaterThanAndDeadlineLessThan(List<String> groupIds, List<String> id, Date start, Date end);

//    long countByCreatedDateBetween(Date start, Date end);

    List<Task> findByCreatedDateBetween(Date start, Date end);

    List<Task> findByGroupIdInAndCreatedDateBetween(List<String> groupIds, Date start, Date end);

//    Task findFirstByGroupIdOrderByCreatedDateDesc(String groupId);

    Task findFirstByGroupIdInOrderByCreatedDateDesc(List<String> groupIds);

//    List<Task> findByGroupIdAndAssignerId(String groupId, String assignerId);

//    long countByGroupIdAndAssigneeIdsUserIdIn(String groupId, String assigneeId);

    @Query("select count(t) from Task t join t.group channel where channel.id in :groupId and t.assigner.id = :userId")
    long countAllOwnTaskOfGroup(List<String> groupId, String userId);

    @Query("SELECT count(t) " +
            "FROM Task t " +
            "JOIN t.assignees assignees " +
            "WHERE t.group.id IN ?1 AND assignees.id = ?2 AND assignees.status = ?3")
    long countAllOwnTaskOfGroupWithStatus(List<String> groupId, String userId, TaskStatus status);

    Task findFirstByGroupIdAndAssignerIdOrderByCreatedDateDesc(String groupId, String assignerId);

//    Task findFirstByGroupIdAndAssigneeIdsUserIdInOrderByCreatedDateDesc(String groupId, String assigneeId);

//    @Query("SELECT t " +
//            "FROM Task t " +
//            "JOIN t.assignees assignees " +
//            "WHERE t.group.id IN :groupId AND assignees.id = :assigneeId " +
//            "ORDER BY t.createdDate DESC ")
//    Optional<Task> findLatestOwnTaskByGroup(List<String> groupId, String assigneeId);

//    List<Task> findAllByGroupIdAndAssigneeIdsUserIdInOrderByCreatedDateDesc(String groupId, List<String> userIds);

    List<Task> findAllByGroupIdAndAssignerIdOrderByCreatedDateDesc(String groupId, String assignerId);

    List<Task> findAllByDeadlineBetween(Date date1, Date date2);

    List<Task> findAllByGroupIdIn(List<String> groupIds);

    List<Task> findAllByGroupId(String groupId);

//    long countByGroupIdAndAssigneeIdsStatusIn(String groupId, TaskStatus status);

//    long countByGroupIdAndAssigneeIdsUserIdInAndAssigneeIdsStatusIn(String groupId, String assigneeId, TaskStatus status);

    @Query("SELECT t, assignees " +
            "FROM Task t " +
            "JOIN t.assignees assignees " +
            "WHERE t.group.id = ?1 " +
            "AND assignees.id = ?2 " +
            "AND (t.deadline BETWEEN ?3 AND ?4) " )
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
