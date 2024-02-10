package com.hcmus.mentor.backend.domain.constant;

public enum GroupCategoryPermission {
    SEND_FILES("Quyền gửi file"),
    TASK_MANAGEMENT("Quyền quản lí công việc"),
    MEETING_MANAGEMENT("Quyền quản lý lịch hẹn"),
    BOARD_MANAGEMENT("Quyền quản lý bảng tin"),
    FAQ_MANAGEMENT("Quyền quản lý câu hỏi thường gặp"),
    GROUP_SETTINGS("Quyền cài đặt nhóm");

    private final String description;

    GroupCategoryPermission(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
