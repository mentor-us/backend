package com.hcmus.mentor.backend.controller.usecase.group.getgroupworkspace;

import com.hcmus.mentor.backend.domain.constant.ChannelStatus;
import com.hcmus.mentor.backend.domain.constant.ChannelType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceChannelDto {

    private String id;
    private String name;
    private String description;
    private Date createdDate = new Date();
    private Date updatedDate = new Date();
    private List<String> userIds = new ArrayList<>();
    private ChannelStatus status = ChannelStatus.ACTIVE;
    private ChannelType type = ChannelType.PUBLIC;
    private String creatorId;
    private Boolean hasNewMessage;
    private String newMessage;
    private String newMessageId;
    private String imageUrl;
    private List<String> pinnedMessageIds = new ArrayList<>();
    private String parentId;
    private Boolean marked;
}
