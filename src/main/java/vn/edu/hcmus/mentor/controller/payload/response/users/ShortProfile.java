package vn.edu.hcmus.mentor.controller.payload.response.users;

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
}