package com.hcmus.mentor.backend.security.principal.oauth2;

import com.hcmus.mentor.backend.controller.exception.OAuth2AuthenticationProcessingException;
import com.hcmus.mentor.backend.controller.payload.ReturnCodeConstants;
import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.security.principal.userdetails.CustomerUserDetails;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private static final Logger LOGGER = LogManager.getLogger(CustomOidcUserService.class);
    private final UserRepository userRepository;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);
        try {
            return processOAuth2User(userRequest, oidcUser);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            // Throwing an instance of AuthenticationException will trigger the
            // OAuth2AuthenticationFailureHandler
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OidcUser processOAuth2User(OidcUserRequest userRequest, OidcUser oidcUser) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oidcUser.getAttributes();

        CustomerOidcUser customerOidcUser = CustomerOidcUserFactory.getOAuth2UserInfo(registrationId, attributes);
        if (!StringUtils.hasLength(customerOidcUser.getEmail())) {
            throw new OAuth2AuthenticationProcessingException(ReturnCodeConstants.AUTHENTICATION_INVALID_EMAIL);
        }

        String email = customerOidcUser.getEmail();

        // If not found in email.
        User data = userRepository
                .findByEmail(email)
                .or(() -> userRepository.findByAdditionalEmailsContains(email))
                .orElseThrow(() -> new OAuth2AuthenticationProcessingException(ReturnCodeConstants.AUTHENTICATION_NOT_FOUND));

        if (!data.isStatus()) {
            throw new OAuth2AuthenticationProcessingException(ReturnCodeConstants.AUTHENTICATION_BLOCKED);
        }

        User user = updateExistingUser(data, customerOidcUser);
        return CustomerUserDetails.create(user, attributes);
    }

    private User updateExistingUser(User user, CustomerOidcUser customerOidcUser) {
        var name = user.getName();
        if (name == null || name.isEmpty() || name.equals(user.getInitialName())) {
            user.setName(customerOidcUser.getName());
        }

        var imageUrl = user.getImageUrl();
        if (imageUrl == null || imageUrl.isEmpty() || imageUrl.startsWith("http")) {
            user.setImageUrl(customerOidcUser.getImageUrl());
        }
//
//        if (!("https://graph.microsoft.com/v1.0/me/photo/$value")
//                .equals(customerOidcUser.getImageUrl())) {
//            imageUrl =
//                    (imageUrl == null || imageUrl.equals("")) ? customerOidcUser.getImageUrl() : imageUrl;
//        }
//        user.update(customerOidcUser);
        return userRepository.save(user);
    }
}
