package com.hcmus.mentor.backend.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class SendFirebaseNotificationEvent extends ApplicationEvent {

    private final List<String> tokens;
    private final String title;
    private final String body;
    private final Map<String, String> data;

    public SendFirebaseNotificationEvent(Object source, List<String> tokens, String title, String body, Map<String, String> data) {
        super(source);

        this.tokens = tokens;
        this.title = title;
        this.body = body;
        this.data = data;
    }
}
