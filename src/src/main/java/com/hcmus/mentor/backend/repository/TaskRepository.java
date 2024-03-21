package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.Task;
import com.hcmus.mentor.backend.domain.constant.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;

public interface TaskRepository extends MongoRepository<Task, String> {
    List<Task> findByChannelId(String channelId);

    List<Task> findByAssigneeIdsUserId(String userId);

    Boolean existsByIdAndAssigneeIdsUserId(String taskId, String userId);

    List<Task> findAllByParentTask(String parentTask);

    Page<Task> findAllByChannelIdInAndAssigneeIdsUserIdInAndDeadlineGreaterThan(
            List<String> channelIds, List<String> id, Date now, PageRequest pageRequest);

    List<Task> findAllByChannelIdInAndAssigneeIdsUserIdIn(List<String> channelIds, List<String> id);


    long countByCreatedDateBetween(Date start, Date end);

    List<Task> findByCreatedDateBetween(Date start, Date end);

    List<Task> findByChannelIdInAndCreatedDateBetween(List<String> channelIds, Date start, Date end);

    Task findFirstByChannelIdOrderByCreatedDateDesc(String channelId);

    List<Task> findByChannelIdAndAssignerId(String channelId, String assignerId);

    long countByChannelIdAndAssigneeIdsUserIdIn(String channelId, String assigneeId);

    Task findFirstByChannelIdAndAssignerIdOrderByCreatedDateDesc(String channelId, String assignerId);

    Task findFirstByChannelIdAndAssigneeIdsUserIdInOrderByCreatedDateDesc(
            String channelId, String assigneeId);

    List<Task> findAllByChannelIdAndAssigneeIdsUserIdInOrderByCreatedDateDesc(
            String channelId, List<String> userIds);

    List<Task> findAllByChannelIdAndAssignerIdOrderByCreatedDateDesc(String channelId, String assignerId);

    List<Task> findAllByDeadlineBetween(Date date1, Date date2);

    List<Task> findAllByChannelIdIn(List<String> channelIds);

    List<Task> findAllByChannelId(String channelId);

    long countByChannelIdAndAssigneeIdsStatusIn(String channelId, TaskStatus status);

    long countByChannelIdAndAssigneeIdsUserIdInAndAssigneeIdsStatusIn(
            String channelId, String assigneeId, TaskStatus status);
}
