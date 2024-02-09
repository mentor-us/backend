package com.hcmus.mentor.backend.domain;

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
    private GroupCategoryStatus status = GroupCategoryStatus.ACTIVE;
    @Builder.Default
    private List<GroupCategoryPermission> permissions = new ArrayList<>();

    public void update(
            String name, String description, String iconUrl, List<GroupCategoryPermission> permissions) {
        if (name != null) {
            this.setName(name);
        }
        if (description != null) {
            this.setDescription(description);
        }
        if (iconUrl != null) {
            this.setIconUrl(iconUrl);
        }
        if (permissions != null) {
            this.setPermissions(permissions);
        }
    }

}
