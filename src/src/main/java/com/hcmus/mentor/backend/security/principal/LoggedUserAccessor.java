package com.hcmus.mentor.backend.security.principal;

/**
 * Logged user accessor routines.
 */
public interface LoggedUserAccessor {

    /**
     * Get current logged user identifier.
     *
     * @return Current user identifier.
     */
    String getCurrentUserId();

    /**
     * Return true if there is any user authenticated
     *
     * @return Returns <c>true</c> if there is authenticated user
     */
    boolean isAuthenticated();
}
