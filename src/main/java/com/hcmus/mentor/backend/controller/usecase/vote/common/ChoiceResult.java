package com.hcmus.mentor.backend.controller.usecase.vote.common;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChoiceResult {

    private String id;

    private String name;

    @Builder.Default
    private List<String> voters = new ArrayList<>();

}