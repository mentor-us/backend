package com.hcmus.mentor.backend.controller.payload.response.users;

import com.hcmus.mentor.backend.domain.User;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShortProfile {

    private String id;

    private String name;

    private String imageUrl;

    public ShortProfile(User user){
        this.id = user.getId();
        this.name = user.getName();
        this.imageUrl = user.getImageUrl();
    }
}
