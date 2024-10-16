package com.hcmus.mentor.backend.service;

import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;

public interface ShareService {
    void pingGroup(String groupId);

    void pingChannel(String channelId);

    boolean isValidTemplate(Workbook workbook, int numOfSheetInTemplate, List<String> nameHeaders);
}
