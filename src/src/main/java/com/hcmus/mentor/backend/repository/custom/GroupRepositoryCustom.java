package com.hcmus.mentor.backend.repository.custom;

import com.hcmus.mentor.backend.domain.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * @author duov
 */
public interface GroupRepositoryCustom {

    Page<Group> findAllByCreatorId(Pageable pageable, String creatorId);

    List<Group> findAllByCreatorId(String creatorId);

    List<Group> findAllByCreatorIdOrderByCreatedDate(String creatorId);

    Page<Group> findAllWithPagination(Pageable pageable);
}
