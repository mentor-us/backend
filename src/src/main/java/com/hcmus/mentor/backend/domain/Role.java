package com.hcmus.mentor.backend.domain;

import com.hcmus.mentor.backend.controller.payload.request.UpdateRoleRequest;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document("role")
public class Role {

    @Id
    private String id;

    private String name;

    private String description;

    @Builder.Default
    private List<String> permissions = new ArrayList<>();

    public void update(UpdateRoleRequest request) {
        name = request.getName();
        description = request.getDescription();
        permissions = request.getPermissions();
    }
}
