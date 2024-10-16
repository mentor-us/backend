package com.hcmus.mentor.backend.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfigServiceDto {
    Integer returnCode;
    String message;
    Object data;
}
