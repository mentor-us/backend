package com.hcmus.mentor.backend.security.principal.oauth2;

import java.util.Map;

public class AppleCustomerOidcUser extends CustomerOidcUser {
    public AppleCustomerOidcUser(Map<String, Object> attributes) {
        super(attributes);
    }

    /**
     * @return
     */
    @Override
    public String getId() {
        return (String) attributes.get("sub");
    }

    /**
     * @return
     */
    @Override
    public String getName() {
        return (String) attributes.get("name");
    }

    /**
     * @return
     */
    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    /**
     * @return
     */
    @Override
    public String getImageUrl() {
        return (String) attributes.get("picture");
    }
}
