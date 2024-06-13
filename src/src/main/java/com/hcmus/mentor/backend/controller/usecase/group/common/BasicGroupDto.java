package com.hcmus.mentor.backend.controller.usecase.group.common;

import com.hcmus.mentor.backend.domain.constant.GroupStatus;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;

/**
 * @author duov
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BasicGroupDto {
    protected String id;
    protected Date createdDate;
    protected String creatorId;
    protected String description;
    protected Duration duration;
    protected String groupCategory;
    protected String imageUrl;
    protected String name;
    protected GroupStatus status;
    @Setter(AccessLevel.NONE)
    protected boolean stopWorking;
    protected LocalDateTime timeStart;
    protected LocalDateTime timeEnd;

    @Setter(AccessLevel.NONE)
    protected int totalMember;
    protected LocalDateTime updatedDate;

    public boolean getStopWorking() {
        return Arrays.asList(GroupStatus.DISABLED, GroupStatus.INACTIVE, GroupStatus.DELETED).contains(status);
    }
}
