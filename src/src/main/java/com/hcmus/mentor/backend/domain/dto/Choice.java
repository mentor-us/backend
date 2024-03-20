package com.hcmus.mentor.backend.domain.dto;

import com.hcmus.mentor.backend.domain.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@Entity
@Table(name = "choices")
public class Choice {

    @Builder.Default
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String id = UUID.randomUUID().toString();

    private String name;

    @Builder.Default
    @OneToMany
    @JoinColumn(name = "voter_id")
    private List<User> voters = new ArrayList<>();

    public Choice() {

    }

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
        for (User user : voters) {
            if (user.getId().equals(voterId)) {
                return;
            }
        }
        // TODO: Add user to voters
//        voters.add();
    }
}
