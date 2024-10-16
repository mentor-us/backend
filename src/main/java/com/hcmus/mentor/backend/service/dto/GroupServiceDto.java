package com.hcmus.mentor.backend.service.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public
class GroupServiceDto {
    Integer returnCode;
    String message;
    Object data;

    public GroupServiceDto(Integer returnCode, String message, Object data) {
        this.returnCode = returnCode;
        this.message = message;
        this.data = data;
    }
}