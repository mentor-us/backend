package vn.edu.hcmus.mentor.repository.custom;

import vn.edu.hcmus.mentor.domain.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MessageRepositoryCustom {
    Page<Message> findAllByChannelIdWithPagination(Pageable pageable, String channelId);
}