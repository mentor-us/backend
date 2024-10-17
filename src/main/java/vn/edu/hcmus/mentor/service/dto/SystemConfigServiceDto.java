package vn.edu.hcmus.mentor.service.dto;

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
