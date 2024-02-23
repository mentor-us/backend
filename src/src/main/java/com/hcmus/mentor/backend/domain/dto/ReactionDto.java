package com.hcmus.mentor.backend.domain.dto;

import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.domain.constant.EmojiType;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReactionDto implements Serializable {

    private String userId;

    private String name;

    private String imageUrl;

    @Builder.Default
    private List<EmojiDto> data = new ArrayList<>();

    private Integer total;

    public void react(EmojiType type) {
        Optional<EmojiDto> emojiWrapper = data.stream().filter(e -> type.equals(e.getId())).findFirst();
        if (!emojiWrapper.isPresent()) {
            EmojiDto newEmoji = EmojiDto.builder().id(type).total(1).build();
            data.add(newEmoji);
            total = total + 1;
            return;
        }
        EmojiDto emoji = emojiWrapper.get();
        emoji.react();
        total = total + 1;
    }

    public void update(User reactor) {
        this.name = reactor.getName();

        String imageUrl = reactor.getImageUrl();
        if (reactor.getImageUrl() != null
                && "https://graph.microsoft.com/v1.0/me/photo/$value".equals(reactor.getImageUrl())) {
            imageUrl = null;
        }
        this.imageUrl = imageUrl;
    }
}
