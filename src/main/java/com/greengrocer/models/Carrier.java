package com.greengrocer.models;

/**
 * Carrier model representing a carrier user.
 */
public class Carrier extends User {

    public Carrier() {
        super();
        this.setRole(UserRole.CARRIER);
    }

    public Carrier(int id, String username, String password, String fullName, String address, String phone,
            String email,
            boolean isActive) {
        super(id, username, password, UserRole.CARRIER, fullName, address, phone, email, isActive);
    }
}
