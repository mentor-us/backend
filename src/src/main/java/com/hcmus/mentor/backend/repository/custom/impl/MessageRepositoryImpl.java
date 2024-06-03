package com.hcmus.mentor.backend.repository.custom.impl;

import com.hcmus.mentor.backend.domain.Message;
import com.hcmus.mentor.backend.repository.custom.MessageRepositoryCustom;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

public class MessageRepositoryImpl extends QuerydslRepositorySupport implements MessageRepositoryCustom {

    private final EntityManager em;

    public MessageRepositoryImpl(EntityManager em) {
        super(Message.class);
        this.em = em;
    }


    @Override
    public Page<Message> findAllByChannelIdWithPagination(Pageable pageable, String channelId) {
        return null;
    }

//    private JPAQuery<Message> findAll(String channelId) {
//        return new JPAQuery<Message>(em)
//                .select(message)
//                .from(message)
//                .leftJoin(message.file, file).fetchJoin()
//                .leftJoin(message.sender, user).fetchJoin()
//                .leftJoin(message.channel, channel).fetchJoin()
//                .leftJoin(message.file, file).fetchJoin()
//                .leftJoin(message.images, ).fetchJoin()
//                .where(channel.id.eq(channelId));
//    }
}