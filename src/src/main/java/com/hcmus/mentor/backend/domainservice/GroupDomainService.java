package com.hcmus.mentor.backend.domainservice;

import com.hcmus.mentor.backend.domain.constant.GroupStatus;
import com.hcmus.mentor.backend.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class GroupDomainService {

    private final SystemConfigRepository systemConfigRepository;

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

    public boolean isStartAndEndTimeValid(LocalDateTime start, LocalDateTime end) {
        int maxYear = 7;

        var maxYearKey = systemConfigRepository.findByKey("valid_max_year").orElse(null);
        if (maxYearKey != null) {
            maxYear = Integer.parseInt((String) maxYearKey.getValue());
        }

        if (end.isBefore(start)) {
            return false;
        }
        if (end.isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
            return false;
        }

        return ChronoUnit.YEARS.between(start, end) <= maxYear && ChronoUnit.YEARS.between(start, LocalDateTime.now(ZoneOffset.UTC)) <= maxYear;
    }
}
