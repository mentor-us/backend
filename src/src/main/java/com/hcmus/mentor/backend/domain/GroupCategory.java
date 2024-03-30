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
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "icon_url")
    private String iconUrl;

    @Builder.Default
    @Column(name = "created_date", nullable = false)
    private Date createdDate = new Date();

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private GroupCategoryStatus status = GroupCategoryStatus.ACTIVE;

    @Builder.Default
    @ElementCollection(targetClass = GroupCategoryPermission.class)
    @CollectionTable(name = "group_category_permissions", joinColumns = @JoinColumn(name = "category_id"))
    @Column(name = "permission", nullable = false)
    @Enumerated(EnumType.STRING)
    private List<GroupCategoryPermission> permissions = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<Group> groups = new ArrayList<>();

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
