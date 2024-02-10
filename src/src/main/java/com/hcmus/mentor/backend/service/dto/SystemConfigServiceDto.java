package com.hcmus.mentor.backend.service.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfigServiceDto {
    Integer returnCode;
    String message;
    Object data;
}
