package com.hcmus.mentor.backend.controller.usecase.group.togglemarkmentee;

import an.awesome.pipelinr.Command;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Command to toggle marked mentee.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToggleMarkMenteeCommand implements Command<Void>{

    /**
     * Current user id.
     */
    private String currentUserId;

    /**
     * Group id.
     */
    private String groupId;

    /**
     * Mentee id.
     */
    private String menteeId;

    /**
     * Marked status.
     */
    private boolean isMarked;
}
