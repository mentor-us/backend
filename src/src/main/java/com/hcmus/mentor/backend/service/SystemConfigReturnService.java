package com.hcmus.mentor.backend.service;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfigReturnService {
  Integer returnCode;
  String message;
  Object data;
}
