package com.hcmus.mentor.backend.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Document("refresh_token")
@Builder
public class RefreshToken {

    @Id
    private String id;

    private String refreshToken;

    private String userId;

    private Date issuedAt;

    private Date expiryDate;
}
