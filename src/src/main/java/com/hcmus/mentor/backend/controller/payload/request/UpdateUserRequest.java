package com.hcmus.mentor.backend.controller.payload.request;

import com.hcmus.mentor.backend.domain.UserGender;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRequest {
    @NotNull
    private String name;
    private String imageUrl;
    private String phone;
    private Date birthDate;
    private String personalEmail;

    @Size(min = 0, max = 1)
    private UserGender gender;
}
