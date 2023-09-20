package com.hcmus.mentor.backend.entity;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Emoji {

    private Type id;

    private Integer total;

    public enum Type {
        LIKE,
        LOVE_EYE,
        SMILE,
        CRY_FACE,
        CURIOUS,
        ANGRY_FACE
    }

    public void react() {
        total = total + 1;
        setTotal(total);
    }
}
