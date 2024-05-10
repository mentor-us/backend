package com.hcmus.mentor.backend.domainservice;

import com.hcmus.mentor.backend.domain.constant.GroupStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class GroupDomainService {

    public GroupStatus getGroupStatus(LocalDateTime start, LocalDateTime end) {
        var groupStatus = GroupStatus.ACTIVE;

        var now = LocalDateTime.now(ZoneOffset.UTC);
        if (start.isBefore(now) && end.isBefore(now)) {
            groupStatus = GroupStatus.OUTDATED;
        }
        if (start.isAfter(now) && end.isAfter(now)) {
            groupStatus = GroupStatus.INACTIVE;
        }

        return groupStatus;
    }
}
