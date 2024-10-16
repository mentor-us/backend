package com.hcmus.mentor.backend.repository.custom;

import com.hcmus.mentor.backend.domain.Task;

import java.util.List;

public interface TaskRepositoryCustom {
    List<Task> findAllChannelIdInAndAssigneeId(List<String> channelIds, String assigneeId);
    List<Task> findAllByChannelIdInAndAssignerId(List<String> channelIds, String assignerId);
    List<Task> findAllOwnByChannelIdsAndUserId(List<String> channelIds, String userId);
}