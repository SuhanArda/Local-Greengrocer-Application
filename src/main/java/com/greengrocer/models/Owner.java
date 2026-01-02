package com.greengrocer.models;

/**
 * Owner model representing an owner user.
 */
public class Owner extends User {

    /**
     * Default constructor for Owner.
     * Sets the user role to OWNER.
     */
    public Owner() {
        super();
        this.setRole(UserRole.OWNER);
    }

    /**
     * Full constructor for Owner.
     *
     * @param id       the unique identifier
     * @param username the username
     * @param password the password
     * @param fullName the full name
     * @param address  the address
     * @param phone    the phone number
     * @param email    the email address
     * @param isActive the active status
     */
    public Owner(int id, String username, String password, String fullName, String address, String phone, String email,
            boolean isActive) {
        super(id, username, password, UserRole.OWNER, fullName, address, phone, email, isActive);
    }
}
