package com.greengrocer.utils;

import com.greengrocer.models.User;

/**
 * Singleton class for managing the current user's session.
 * <p>
 * Tracks the currently logged-in {@link User} and provides convenience methods
 * for checking roles (Customer, Carrier, Owner) and retrieving user details.
 * </p>
 * 
 * @author Burak Özevin
 */
public class SessionManager {
    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {
    }

    /**
     * Retrieves the singleton instance of the SessionManager.
     *
     * @return The single {@link SessionManager} instance.
     * 
     * @author Burak Özevin
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Gets the currently logged-in user.
     *
     * @return The current {@link User}, or {@code null} if no user is logged in.
     * 
     * @author Burak Özevin
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Sets the currently logged-in user.
     *
     * @param user The {@link User} to set as logged in.
     * 
     * @author Burak Özevin
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /**
     * Checks if a user is currently logged in.
     *
     * @return {@code true} if a user is logged in; {@code false} otherwise.
     * 
     * @author Burak Özevin
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Logs out the current user.
     * <p>
     * Clears the current user reference.
     * </p>
     * 
     * @author Burak Özevin
     */
    public void logout() {
        this.currentUser = null;
    }

    /**
     * Checks if the current user has the CUSTOMER role.
     *
     * @return {@code true} if the user is a customer; {@code false} otherwise.
     * 
     * @author Burak Özevin
     */
    public boolean isCustomer() {
        return currentUser != null && currentUser.getRole() == User.UserRole.CUSTOMER;
    }

    /**
     * Checks if the current user has the CARRIER role.
     *
     * @return {@code true} if the user is a carrier; {@code false} otherwise.
     * 
     * 
     * @author Burak Özevin
     */
    public boolean isCarrier() {
        return currentUser != null && currentUser.getRole() == User.UserRole.CARRIER;
    }

    /**
     * Checks if the current user has the OWNER role.
     *
     * @return {@code true} if the user is the owner; {@code false} otherwise.
     * 
     * @author Burak Özevin
     */
    public boolean isOwner() {
        return currentUser != null && currentUser.getRole() == User.UserRole.OWNER;
    }

    /**
     * Retrieves the ID of the current user.
     *
     * @return The user ID, or {@code -1} if no user is logged in.
     * 
     * @author Burak Özevin
     */
    public int getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : -1;
    }
}
