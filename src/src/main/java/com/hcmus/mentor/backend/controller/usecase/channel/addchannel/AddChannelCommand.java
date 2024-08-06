package com.hcmus.mentor.backend.controller.usecase.channel.addchannel;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.domain.Channel;
import com.hcmus.mentor.backend.domain.constant.ChannelType;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.util.List;

/**
 * Command for adding a new channel.
 */
@Getter
@Setter
@Builder
public class AddChannelCommand implements Command<Channel> {

    /**
     * The name of the channel.
     */
    @NotBlank(message = "Tên kênh không được để trống")
    @Length(max = 255, message = "Tên kênh không được quá 255 ký tự")
    private String channelName;

    /**
     * s
     * The description of the channel.
     */
    private String description;

    /**
     * The type of the channel.
     */
    private ChannelType type;

    /**
     * The ID of the group to which the channel belongs.
     */
    private String groupId;

    /**
     * The ID of the creator of the channel.
     */
    private String creatorId;

    /**
     * The IDs of the users associated with the channel.
     */
    private List<String> userIds;
}
