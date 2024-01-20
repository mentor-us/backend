package com.hcmus.mentor.backend.controller.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
