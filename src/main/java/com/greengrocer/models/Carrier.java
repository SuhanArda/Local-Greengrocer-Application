package com.greengrocer.models;

/**
 * Carrier model representing a carrier user.
 * 
 * @author Suhan Arda Öner
 */
public class Carrier extends User {

    /**
     * Default constructor for Carrier.
     * Sets the user role to CARRIER.
     * 
     * @author Suhan Arda Öner
     */
    public Carrier() {
        super();
        this.setRole(UserRole.CARRIER);
    }

    /**
     * Full constructor for Carrier.
     *
     * @param id       the unique identifier for the carrier
     * @param username the username for login
     * @param password the password for login
     * @param fullName the full name of the carrier
     * @param address  the address of the carrier
     * @param phone    the phone number of the carrier
     * @param email    the email address of the carrier
     * @param isActive the active status of the carrier account
     * 
     * @author Suhan Arda Öner
     */
    public Carrier(int id, String username, String password, String fullName, String address, String phone,
            String email,
            boolean isActive) {
        super(id, username, password, UserRole.CARRIER, fullName, address, phone, email, isActive);
    }
}
