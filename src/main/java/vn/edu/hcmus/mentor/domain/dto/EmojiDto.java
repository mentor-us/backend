package vn.edu.hcmus.mentor.domain.dto;

import vn.edu.hcmus.mentor.domain.constant.EmojiType;
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
