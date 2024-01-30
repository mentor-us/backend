package com.hcmus.mentor.backend.controller.payload.request.groups;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class AddMentorsRequest {
    List<String> emails;
}
