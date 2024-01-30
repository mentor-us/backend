package com.hcmus.mentor.backend.controller.payload.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Command for mentioning users in a message.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MentionUserCommand {


    /**
     * The ID of the message to which users are mentioned.
     */
    String messageId;

    /**
     * A list of receiver IDs representing users to be mentioned.
     */
    List<String> receiverIds;
}
