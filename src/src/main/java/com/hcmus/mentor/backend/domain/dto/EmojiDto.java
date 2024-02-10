package com.hcmus.mentor.backend.domain.dto;

import com.hcmus.mentor.backend.domain.constant.EmojiType;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmojiDto {

    private EmojiType id;

    private Integer total;

    public void react() {
        total = total + 1;
        setTotal(total);
    }

}
