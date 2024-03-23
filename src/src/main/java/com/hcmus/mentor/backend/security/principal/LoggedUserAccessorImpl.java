package com.hcmus.mentor.backend.security.principal;

import com.hcmus.mentor.backend.security.principal.userdetails.CustomerUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class LoggedUserAccessorImpl implements LoggedUserAccessor {

    @Override
    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomerUserDetails)) {
            return null;
        }

        return ((CustomerUserDetails) principal).getId();
    }

    @Override
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return authentication != null;
    }
}
