package com.hcmus.mentor.backend.payload.request;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
public class SignUpRequest {
    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;
}
