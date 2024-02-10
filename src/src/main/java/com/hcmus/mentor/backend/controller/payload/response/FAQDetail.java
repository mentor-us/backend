package com.hcmus.mentor.backend.controller.payload.response;

import com.hcmus.mentor.backend.controller.payload.response.groups.GroupDetailResponse;
import com.hcmus.mentor.backend.controller.payload.response.users.ShortProfile;
import com.hcmus.mentor.backend.domain.Faq;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FAQDetail {

    private String id;

    private String question;

    private String answer;

    private Date createdDate;

    private Date updatedDate;

    private ShortProfile creator;

    private GroupDetailResponse group;

    @Builder.Default
    private List<String> topics = new ArrayList<>();

    private List<String> voters;

    public static FAQDetail from(Faq faq, ShortProfile creator, GroupDetailResponse group) {
        return FAQDetail.builder()
                .id(faq.getId())
                .question(faq.getQuestion())
                .answer(faq.getAnswer())
                .createdDate(faq.getCreatedDate())
                .updatedDate(faq.getUpdatedDate())
                .creator(creator)
                .group(group)
                .voters(faq.getVoters())
                .build();
    }
}
