package com.hcmus.mentor.backend.service.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public
class RoleServiceDto {
    Integer returnCode;
    String message;
    Object data;

    public RoleServiceDto(Integer returnCode, String message, Object data) {
        this.returnCode = returnCode;
        this.message = message;
        this.data = data;
    }
}
