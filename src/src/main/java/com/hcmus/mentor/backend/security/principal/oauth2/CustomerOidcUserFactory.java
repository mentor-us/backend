package com.hcmus.mentor.backend.security.principal.oauth2;

import com.hcmus.mentor.backend.controller.exception.OAuth2AuthenticationProcessingException;
import com.hcmus.mentor.backend.domain.constant.AuthProvider;

import java.util.Map;

public class CustomerOidcUserFactory {

    private CustomerOidcUserFactory() {
        throw new IllegalStateException("Utility class");
    }

    public static CustomerOidcUser getOAuth2UserInfo(
            String registrationId, Map<String, Object> attributes) {
        if (registrationId.equalsIgnoreCase(AuthProvider.google.toString())) {
            return new GoogleCustomerOidcUser(attributes);
        } else if (registrationId.equalsIgnoreCase(AuthProvider.azure.toString())) {
            return new AzureCustomerOidcUser(attributes);
        } else {
            throw new OAuth2AuthenticationProcessingException(
                    "Sorry! Login with " + registrationId + " is not supported yet.");
        }
    }
}
