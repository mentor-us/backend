package com.hcmus.mentor.backend.controller.payload;

public class ReturnCodeConstants {
    public static final Integer SUCCESS = 200;

    public static final Integer INVALID_PERMISSION = 100;
    public static final String INVALID_PERMISSION_STRING = "Invalid permission";

    // Region: User

    public static final Integer USER_DUPLICATE_USER = 107;
    public static final Integer USER_NOT_FOUND = 104;
    public static final Integer USER_DUPLICATE_EMAIL = 109;
    public static final Integer USER_NOT_ENOUGH_FIELDS = 111;
    public static final Integer USER_INVALID_TEMPLATE = 112;

    // EndRegion: User

    // Region: Group

    public static final Integer GROUP_NOT_FOUND = 204;
    public static final Integer GROUP_DUPLICATE_GROUP = 207;
    public static final Integer GROUP_GROUP_CATEGORY_NOT_FOUND = 208;
    public static final Integer GROUP_TIME_END_BEFORE_TIME_START = 209;
    public static final Integer GROUP_DUPLICATE_EMAIL = 210;
    public static final Integer GROUP_NOT_ENOUGH_FIELDS = 211;
    public static final Integer GROUP_MENTEE_NOT_FOUND = 213;
    public static final Integer GROUP_MENTOR_NOT_FOUND = 214;
    public static final Integer GROUP_TIME_END_TOO_FAR_FROM_TIME_START = 215;
    public static final Integer GROUP_TIME_START_TOO_FAR_FROM_NOW = 216;
    public static final Integer GROUP_TIME_END_BEFORE_NOW = 217;
    public static final Integer GROUP_INVALID_EMAILS = 218;
    public static final Integer GROUP_INVALID_DOMAINS = 219;
    public static final Integer GROUP_INVALID_ROLE = 220;
    public static final Integer GROUP_INVALID_TEMPLATE = 221;

    // EndRegion: Group

    // Region: Notification

    public static final Integer NOTIFICATION_NOT_FOUND = 304;

    // EndRegion: Notification

    // Region: Group Category

    public static final Integer GROUP_CATEGORY_DUPLICATE_GROUP_CATEGORY = 407;
    public static final Integer GROUP_CATEGORY_NOT_FOUND = 404;
    public static final Integer GROUP_CATEGORY_NOT_ENOUGH_FIELDS = 411;

    // EndRegion: Group Category

    // Region: Task

    public static final Integer TASK_NOT_FOUND_GROUP = 501;
    public static final Integer TASK_NOT_FOUND_PARENT_TASK = 502;
    public static final Integer TASK_NOT_FOUND_USER_IN_GROUP = 503;
    public static final Integer TASK_NOT_FOUND = 504;
    public static final Integer TASK_NOT_ENOUGH_FIELDS = 511;

    // EndRegion: Task

    // Region: Role

    public static final Integer ROLE_DUPLICATE_ROLE = 607;
    public static final Integer ROLE_NOT_FOUND = 604;
    public static final Integer ROLE_NOT_FOUND_PERMISSION = 602;

    // EndRegion: Role

    // Region: Authentication

    public static final String AUTHENTICATION_UNAUTHORIZED = "701";
    public static final String AUTHENTICATION_NOT_FOUND = "704";
    public static final String AUTHENTICATION_BLOCKED = "705";
    public static final String AUTHENTICATION_INVALID_EMAIL = "706";

    // EndRegion: Authentication

    // Region: System Config

    public static final Integer SYSTEM_CONFIG_INVALID_TYPE = 801;
    public static final Integer SYSTEM_CONFIG_INVALID_DOMAIN = 802;
    public static final Integer SYSTEM_CONFIG_INVALID_MAX_YEAR = 803;
    public static final Integer SYSTEM_CONFIG_NOT_FOUND = 804;

    // EndRegion: System Config

    // Region: Analytic

    public static final Integer ANALYTIC_INVALID_TIME_RANGE = 901;
    public static final String ANALYTIC_INVALID_TIME_RANGE_STRING = "Invalid time range";
    public static final Integer ANALYTIC_NOT_FOUND_GROUP = 902;
    public static final String ANALYTIC_NOT_FOUND_GROUP_STRING = "902";
    public static final Integer ANALYTIC_NOT_FOUND_USER = 903;
    public static final String ANALYTIC_NOT_FOUND_USER_STRING = "Not found users";
    public static final Integer ANALYTIC_INVALID_VALUE = 904;
    public static final String ANALYTIC_INVALID_VALUE_STRING = "Invalid values";

    // EndRegion: Analytic

    private ReturnCodeConstants() {
    }
}
