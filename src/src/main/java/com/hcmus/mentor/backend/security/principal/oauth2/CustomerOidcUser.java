package com.hcmus.mentor.backend.security.principal.oauth2;

import lombok.Getter;

import java.util.Map;

@Getter
public abstract class CustomerOidcUser {
    protected Map<String, Object> attributes;

    public CustomerOidcUser(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public abstract String getId();

    public abstract String getName();

    public abstract String getEmail();

    public abstract String getImageUrl();
}
