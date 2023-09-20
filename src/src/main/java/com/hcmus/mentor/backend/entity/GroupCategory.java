package com.hcmus.mentor.backend.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document("group_category")
public class GroupCategory {

    @Id
    private String id;

    private String name;

    private String description;

    private String iconUrl;
    @Builder.Default
    private Date createdDate = new Date();
    @Builder.Default
    private Status status = Status.ACTIVE;
    public enum Status {
        ACTIVE,
        DELETED
    }

    @Builder.Default
    private List<Permission> permissions = new ArrayList<>();

    public enum Permission {
        SEND_FILES("Quyền gửi file"),
        TASK_MANAGEMENT("Quyền quản lí công việc");

        private final String description;

        Permission(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public void update(String name, String description, String iconUrl, List<Permission> permissions) {
        if(name != null){
            this.setName(name);
        }
        if(description != null){
            this.setDescription(description);
        }
        if(iconUrl != null) {
            this.setIconUrl(iconUrl);
        }
        if(permissions != null) {
            this.setPermissions(permissions);
        }
    }
}
