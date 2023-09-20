package com.hcmus.mentor.backend.payload.request;

import com.hcmus.mentor.backend.entity.Vote;
import lombok.*;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class CreateVoteRequest {

    private String question;

    private String groupId;

    private String creatorId;

    private Date timeEnd;

    private List<Vote.Choice> choices;
}
