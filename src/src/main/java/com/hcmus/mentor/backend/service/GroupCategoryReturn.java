package com.hcmus.mentor.backend.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GroupCategoryReturn {
  Integer returnCode;
  String message;
  Object data;
}
