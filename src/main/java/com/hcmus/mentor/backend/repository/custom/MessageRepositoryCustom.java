package com.hcmus.mentor.backend.repository.custom;

import com.hcmus.mentor.backend.domain.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MessageRepositoryCustom {
    Page<Message> findAllByChannelIdWithPagination(Pageable pageable, String channelId);
}