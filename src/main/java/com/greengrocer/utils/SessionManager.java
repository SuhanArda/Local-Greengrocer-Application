package com.greengrocer.utils;

import com.greengrocer.models.User;

/**
 * Session manager for tracking the currently logged-in user.
 * Implements singleton pattern.
 */
public class SessionManager {
    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {
    }

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public void logout() {
        this.currentUser = null;
    }

    public boolean isCustomer() {
        return currentUser != null && currentUser.getRole() == User.UserRole.CUSTOMER;
    }

    public boolean isCarrier() {
        return currentUser != null && currentUser.getRole() == User.UserRole.CARRIER;
    }

    public boolean isOwner() {
        return currentUser != null && currentUser.getRole() == User.UserRole.OWNER;
    }

    public int getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : -1;
    }
}
