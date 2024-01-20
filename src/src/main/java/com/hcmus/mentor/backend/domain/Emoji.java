package com.hcmus.mentor.backend.domain;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Emoji {

    private Type id;

    private Integer total;

    public void react() {
        total = total + 1;
        setTotal(total);
    }

    public enum Type {
        LIKE,
        LOVE_EYE,
        SMILE,
        CRY_FACE,
        CURIOUS,
        ANGRY_FACE
    }
}
