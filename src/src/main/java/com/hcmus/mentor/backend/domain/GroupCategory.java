package com.hcmus.mentor.backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hcmus.mentor.backend.domain.constant.GroupCategoryPermission;
import com.hcmus.mentor.backend.domain.constant.GroupCategoryStatus;
import com.hcmus.mentor.backend.util.DateUtils;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Builder
@Entity
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "groups_categories")
public class GroupCategory implements Serializable {

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
    private Date createdDate = DateUtils.getDateNowAtUTC() ;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private GroupCategoryStatus status = GroupCategoryStatus.ACTIVE;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "permission", nullable = false)
    @ElementCollection(targetClass = GroupCategoryPermission.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "group_category_permissions", joinColumns = @JoinColumn(name = "category_id"))
    private List<GroupCategoryPermission> permissions = new ArrayList<>();

    @Builder.Default
    @JsonIgnore
    @ToString.Exclude
    @OneToMany(mappedBy = "groupCategory", fetch = FetchType.LAZY)
    private List<Group> groups = new ArrayList<>();

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