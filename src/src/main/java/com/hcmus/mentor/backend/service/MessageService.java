package com.hcmus.mentor.backend.service;

import com.hcmus.mentor.backend.controller.payload.request.ReactMessageRequest;
import com.hcmus.mentor.backend.controller.payload.request.SendFileRequest;
import com.hcmus.mentor.backend.controller.payload.request.SendImagesRequest;
import com.hcmus.mentor.backend.controller.payload.response.messages.MessageDetailResponse;
import com.hcmus.mentor.backend.controller.payload.response.messages.MessageResponse;
import com.hcmus.mentor.backend.domain.*;

import java.util.List;

/**
 * The {@code MessageService} interface provides methods for managing and retrieving messages in a messaging system.
 */
public interface MessageService {

    /**
     * Find a message by its ID.
     *
     * @param id The ID of the message.
     * @return The detailed response for the found message.
     */
    Message find(String id);

    /**
     * Get messages of a group.
     *
     * @param viewerId The ID of the viewer.
     * @param groupId  The ID of the group.
     * @param page     Page number to return.
     * @param size     Required page size (amount of items returned at a time).
     * @return The list of detailed responses for group messages.
     */
    List<MessageDetailResponse> getGroupMessages(String viewerId, String groupId, int page, int size);

    /**
     * Find messages of a group based on a query string.
     *
     * @param groupId The ID of the group.
     * @param query   The query string to search for in messages.
     * @param page    Page number for pagination.
     * @param size    Required page size (amount of items returned at a time).
     * @return The list of message responses based on the query.
     */
    List<MessageResponse> findGroupMessagesByText(String groupId, String query, int page, int size);

    /**
     * Get the last message of a group.
     *
     * @param groupId The ID of the group.
     * @return The ID of the last message in the group.
     */
    String getLastGroupMessage(String groupId);

    /**
     * React to a message.
     *
     * @param request The request containing information for reacting to the message.
     */
    void reactMessage(ReactMessageRequest request);

    /**
     * Remove a reaction from a message.
     *
     * @param messageId The ID of the message from which the reaction will be removed.
     * @param senderId  The ID of the sender whose reaction will be removed.
     */
    void removeReaction(String messageId, String senderId);

    /**
     * Calculate total reactions for a message.
     *
     * @param message The message for which total reactions will be calculated.
     * @return The total reactions for the message.
     */
    MessageDetailResponse.TotalReaction calculateTotalReactionMessage(Message message);

    /**
     * Save a general message.
     *
     * @param data The data representing the message.
     * @return The saved message.
     */
    Message saveMessage(Message data);


    /**
     * Save a task message.
     *
     * @param task The task for which a message will be saved.
     * @return The saved message.
     */
    Message saveTaskMessage(Task task);

    /**
     * Save a vote message.
     *
     * @param vote The vote for which a message will be saved.
     * @return The saved message.
     */
    Message saveVoteMessage(Vote vote);

    /**
     * Save an image message.
     *
     * @param request The request containing information for saving the image message.
     * @return The saved message.
     */
    Message saveImageMessage(SendImagesRequest request);

    /**
     * Save a file message.
     *
     * @param request The request containing information for saving the file message.
     * @return The saved message.
     */
    Message saveFileMessage(SendFileRequest request);

    /**
     * Fulfill messages by populating additional details based on the viewer.
     *
     * @param messages The list of messages to fulfill.
     * @param viewerId The ID of the viewer.
     * @return The list of detailed responses for fulfilled messages.
     */
    List<MessageDetailResponse> fulfillMessages(List<MessageResponse> messages, String viewerId);

    /**
     * Fulfill a text message by populating additional details.
     *
     * @param message The text message to fulfill.
     * @return The detailed response for the fulfilled text message.
     */
    MessageDetailResponse fulfillTextMessage(MessageResponse message);

    /**
     * Fulfill a meeting message by populating additional details.
     *
     * @param message The meeting message to fulfill.
     * @return The detailed response for the fulfilled meeting message.
     */
    MessageDetailResponse fulfillMeetingMessage(MessageResponse message);

    /**
     * Fulfill a task message by populating additional details.
     *
     * @param message The task message to fulfill.
     * @return The detailed response for the fulfilled task message.
     */
    MessageDetailResponse fulfillTaskMessage(MessageResponse message);

    /**
     * Fulfill a reaction by populating additional details.
     *
     * @param reaction The reaction to fulfill.
     * @param reactor  The user who reacted.
     * @return The fulfilled reaction.
     */
    Reaction fulfillReaction(Reaction reaction, User reactor);
}
