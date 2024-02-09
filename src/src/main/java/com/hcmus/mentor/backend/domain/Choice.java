package com.hcmus.mentor.backend.domain;

import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Choice {

    @Builder.Default
    private String id = UUID.randomUUID().toString();

    private String name;

    @Builder.Default
    private List<String> voters = new ArrayList<>();

    public static Choice from(String name) {
        return Choice.builder().name(name).build();
    }

    public void update(Choice choice) {
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
