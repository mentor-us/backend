package com.hcmus.mentor.backend.controller.payload.request.faqs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ImportFAQsRequest {

    private String fromGroupId;

    private List<String> faqIds;
}
