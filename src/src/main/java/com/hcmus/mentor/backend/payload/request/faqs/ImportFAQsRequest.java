package com.hcmus.mentor.backend.payload.request.faqs;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ImportFAQsRequest {

  private String fromGroupId;

  private List<String> faqIds;
}
