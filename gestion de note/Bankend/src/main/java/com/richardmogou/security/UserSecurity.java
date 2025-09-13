package com.richardmogou.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Component for handling user security checks.
 * Used in @PreAuthorize annotations to verify if the current user
 * is the same as the requested user ID.
 */
@Component("userSecurity")
public class UserSecurity {

    /**
     * Checks if the current authenticated user is the same as the requested user ID.
     * 
     * @param userId The ID of the user to check against
     * @return true if the current user is the same as the requested user, false otherwise
     */
    public boolean isCurrentUser(Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        try {
            // The principal name should be the user ID as a string
            Long currentUserId = Long.parseLong(authentication.getName());
            return userId.equals(currentUserId);
        } catch (NumberFormatException e) {
            // If the principal name is not a valid Long, return false
            return false;
        }
    }
}
