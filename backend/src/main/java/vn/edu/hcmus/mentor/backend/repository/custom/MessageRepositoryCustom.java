package vn.edu.hcmus.mentor.backend.repository.custom;

import vn.edu.hcmus.mentor.backend.domain.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MessageRepositoryCustom {
    Page<Message> findAllByChannelIdWithPagination(Pageable pageable, String channelId);
}