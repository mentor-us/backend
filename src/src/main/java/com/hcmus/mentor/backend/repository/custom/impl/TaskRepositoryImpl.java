package com.hcmus.mentor.backend.repository.custom.impl;

import com.hcmus.mentor.backend.domain.Task;
import com.hcmus.mentor.backend.repository.custom.TaskRepositoryCustom;
import com.querydsl.core.types.Expression;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

import static com.hcmus.mentor.backend.domain.QAssignee.assignee;
import static com.hcmus.mentor.backend.domain.QChannel.channel;
import static com.hcmus.mentor.backend.domain.QTask.task;
import static com.hcmus.mentor.backend.domain.QUser.user;

public class TaskRepositoryImpl extends QuerydslRepositorySupport implements TaskRepositoryCustom {
    private final EntityManager em;

    public TaskRepositoryImpl(EntityManager em) {
        super(Task.class);
        this.em = em;
    }

    @Override
    public List<Task> findAllChannelIdInAndAssigneeId(List<String> channelIds, String assigneeId) {
        JPAQuery<Task> query = findAllByChannelIdsInAndAssigneeIdQuery(task, channelIds, assigneeId);
        return query.fetch();
    }

    @Override
    public List<Task> findAllByChannelIdInAndAssignerId(List<String> channelIds, String assignerId) {
        return findAllByChannelIdInAndAssignerIdQuery(task, channelIds, assignerId).fetch();
    }

    @Override
    public List<Task> findAllOwnByChannelIdsAndUserId(List<String> channelIds, String userId) {
        return findAllOwnTasksByChannelIdAndUserIdQuery(channelIds, userId).fetch();
    }

    private JPAQuery<Task> findAllOwnTasksByChannelIdAndUserIdQuery(List<String> channelIds, String userId) {
        return new JPAQuery<Task>(em)
                .select(task)
                .from(task)
                .leftJoin(task.group, channel).fetchJoin()
                .leftJoin(task.assignees, assignee).fetchJoin()
                .leftJoin(assignee.user, user).fetchJoin()
                .where(channel.id.in(channelIds)
                        .and(user.id.eq(userId)
                                .or(task.assigner.id.eq(userId))));
    }

    private <T> JPAQuery<T> findAllByChannelIdsInAndAssigneeIdQuery(Expression<T> expression, List<String> channelIds, String assigneeId) {
        return new JPAQuery<Task>(em)
                .select(expression)
                .from(task)
                .leftJoin(task.group, channel).fetchJoin()
                .leftJoin(task.assignees, assignee).fetchJoin()
                .leftJoin(assignee.user, user).fetchJoin()
                .where(channel.id.in(channelIds).and(user.id.eq(assigneeId)));
    }

    private <T> JPAQuery<T> findAllByChannelIdInAndAssignerIdQuery(Expression<T> expression, List<String> channelIds, String assignedId) {
        return new JPAQuery<>(em)
                .select(expression)
                .from(task)
                .leftJoin(task.group, channel).fetchJoin()
                .leftJoin(task.assignees, assignee).fetchJoin()
                .leftJoin(task.assigner, user).fetchJoin()
                .where(channel.id.in(channelIds).and(user.id.eq(assignedId)));
    }
}