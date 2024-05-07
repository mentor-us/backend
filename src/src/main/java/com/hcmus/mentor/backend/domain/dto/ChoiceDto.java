package com.hcmus.mentor.backend.domain.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChoiceDto {
    private String id;

    private String name;

    @Builder.Default
    private List<String> voters = new ArrayList<>();

    public static ChoiceDto from(String name) {
        return ChoiceDto.builder().name(name).build();
    }

    public void update(ChoiceDto choice) {
        setName(choice.getName());
    }

    public void removeVoting(String voterId) {
        voters.remove(voterId);
    }

    public void doVoting(String voterId) {
        if (voters.contains(voterId)) {
            return;
        }
        voters.add(voterId);
    }
}
