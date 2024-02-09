package com.hcmus.mentor.backend.domain;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Emoji {

    private EmojiType id;

    private Integer total;

    public void react() {
        total = total + 1;
        setTotal(total);
    }

}
