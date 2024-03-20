package com.hcmus.mentor.backend.domain;

import com.hcmus.mentor.backend.domain.constant.GroupCategoryPermission;
import com.hcmus.mentor.backend.domain.constant.GroupCategoryStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Builder
@Entity
@Table(name = "groups_categories")
public class GroupCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;

    private String description;

    private String iconUrl;

    @Builder.Default
    private Date createdDate = new Date();

    @Builder.Default
    private GroupCategoryStatus status = GroupCategoryStatus.ACTIVE;

    @Builder.Default
    @ElementCollection
    private List<GroupCategoryPermission> permissions = new ArrayList<>();

    public GroupCategory() {

    }

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
