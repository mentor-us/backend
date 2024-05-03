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

    public void react(EmojiType type, Integer totalEmoji) {
        Optional<EmojiDto> emojiWrapper = data.stream().filter(e -> type.equals(e.getId())).findFirst();
        if (emojiWrapper.isEmpty()) {
            EmojiDto newEmoji = EmojiDto.builder().id(type).total(totalEmoji).build();
            data.add(newEmoji);
            total = total + totalEmoji;
            return;
        }
        EmojiDto emoji = emojiWrapper.get();
        emoji.react();
        total = total + totalEmoji;
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