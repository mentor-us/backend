package com.hcmus.mentor.backend.security.handler;

import com.hcmus.mentor.backend.security.principal.userdetails.CustomerUserDetails;
import com.hcmus.mentor.backend.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FirebaseClearingLogoutHandler implements LogoutHandler {

    private final NotificationService notificationService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        var loggedUser = (CustomerUserDetails) authentication.getPrincipal();
        var userId = loggedUser.getId();

        notificationService.unsubscribeNotification(userId);
    }
}
