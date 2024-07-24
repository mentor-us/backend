package com.hcmus.mentor.backend.domain.constant;

public enum UserRole {
    ADMIN,
    SUPER_ADMIN,
    ROLE_USER,
    USER;

   public static String getRoleName(UserRole role) {
       return switch (role) {
           case ADMIN -> "Quản trị viên";
           case SUPER_ADMIN -> "Quản trị viên cấp cao";
           default -> "Người dùng";
       };
    }
}