package com.hcmus.mentor.backend.security.oauth2;

import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.controller.exception.OAuth2AuthenticationProcessingException;
import com.hcmus.mentor.backend.controller.payload.returnCode.AuthenticationErrorCode;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.security.oauth2.user.OAuth2UserInfo;
import com.hcmus.mentor.backend.security.oauth2.user.OAuth2UserInfoFactory;
import com.hcmus.mentor.backend.security.UserPrincipal;
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
import java.util.Optional;

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

        OAuth2UserInfo oAuth2UserInfo =
                OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, attributes);
        if (StringUtils.isEmpty(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationProcessingException(AuthenticationErrorCode.INVALID_EMAIL);
        }

        String email = oAuth2UserInfo.getEmail();

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (!userOptional.isPresent()) {
            throw new OAuth2AuthenticationProcessingException(AuthenticationErrorCode.NOT_FOUND);
        }

        User data = userOptional.get();
        if (!data.isStatus()) {
            throw new OAuth2AuthenticationProcessingException(AuthenticationErrorCode.BLOCKED);
        }
        User user = updateExistingUser(data, oAuth2UserInfo);
        return UserPrincipal.create(user, attributes);
    }

    private User updateExistingUser(User user, OAuth2UserInfo oAuth2UserInfo) {
        user.update(oAuth2UserInfo);
        return userRepository.save(user);
    }
}
