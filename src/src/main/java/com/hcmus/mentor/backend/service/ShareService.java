package com.hcmus.mentor.backend.service;

import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;

public interface ShareService {
    public void pingGroup(String groupId);

    public void pingChannel(String channelId);

    public boolean isValidTemplate(Workbook workbook, int numOfSheetInTemplate, List<String> nameHeaders);
}
