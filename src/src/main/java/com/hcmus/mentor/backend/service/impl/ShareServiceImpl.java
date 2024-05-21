package com.hcmus.mentor.backend.service.impl;

import com.hcmus.mentor.backend.controller.exception.DomainException;
import com.hcmus.mentor.backend.repository.ChannelRepository;
import com.hcmus.mentor.backend.repository.GroupRepository;
import com.hcmus.mentor.backend.service.ShareService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ShareServiceImpl implements ShareService {
    private final GroupRepository groupRepository;
    private final ChannelRepository channelRepository;

    /**
     * @param groupId Group id
     */
    @Override
    public void pingGroup(String groupId) {
        var group = groupRepository.findById(groupId).orElseThrow(() -> new DomainException("Group not found"));
        group.setUpdatedDate(LocalDateTime.now(ZoneOffset.UTC));
        groupRepository.save(group);
    }

    /**
     * @param channelId Channel id
     */
    @Override
    public void pingChannel(String channelId) {
        var channel = channelRepository.findById(channelId).orElseThrow(() -> new DomainException("Channel not found"));
        channel.setUpdatedDate(LocalDateTime.now(ZoneOffset.UTC));
        channelRepository.save(channel);

        pingGroup(channel.getGroup().getId());
    }

    public boolean isValidTemplate(Workbook workbook, int numOfSheetInTemplate, List<String> nameHeaders) {
        if (workbook.getNumberOfSheets() != numOfSheetInTemplate) {
            return false;
        }
        Sheet sheet = workbook.getSheet("Data");
        if (sheet == null) {
            return false;
        }

        Row row = sheet.getRow(0);
        return isValidHeader(row, nameHeaders);
    }

    private boolean isValidHeader(Row row, List<String> nameHeaders) {
        for (int i = 0; i < nameHeaders.size(); i++) {
            if (row.getCell(i) == null || !row.getCell(i).getStringCellValue().equals(nameHeaders.get(i))) {
                return false;
            }
        }
        return true;
    }
}
