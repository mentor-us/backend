package com.hcmus.mentor.backend.payload.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class JoinOutRoomRequest {

    @NotNull
    @NotBlank
    private String groupId;

    @NotNull
    @NotBlank
    private String userId;
}
