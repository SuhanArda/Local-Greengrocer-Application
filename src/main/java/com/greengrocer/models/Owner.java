package com.greengrocer.models;

/**
 * Owner model representing an owner user.
 */
public class Owner extends User {

    public Owner() {
        super();
        this.setRole(UserRole.OWNER);
    }

    public Owner(int id, String username, String password, String fullName, String address, String phone, String email,
            boolean isActive) {
        super(id, username, password, UserRole.OWNER, fullName, address, phone, email, isActive);
    }
}
