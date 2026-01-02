package com.greengrocer.models;

/**
 * User model representing a system user (customer, carrier, or owner).
 * Maps to the UserInfo table in the database.
 * 
 * @author Suhan Arda Öner
 */
public abstract class User {
    /** Unique identifier for the user. */
    private int id;
    /** Username for login. */
    private String username;
    /** Hashed password for login. */
    private String password;
    /** Role of the user (CUSTOMER, CARRIER, OWNER). */
    private UserRole role;
    /** Full name of the user. */
    private String fullName;
    /** Address of the user. */
    private String address;
    /** Phone number of the user. */
    private String phone;
    /** Email address of the user. */
    private String email;
    /** Flag indicating if the user account is active. */
    private boolean isActive;

    /**
     * Enum representing user roles in the system.
     * 
     * @author Suhan Arda Öner
     */
    public enum UserRole {
        CUSTOMER("customer"),
        CARRIER("carrier"),
        OWNER("owner");

        private final String value;

        UserRole(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static UserRole fromString(String text) {
            for (UserRole role : UserRole.values()) {
                if (role.value.equalsIgnoreCase(text)) {
                    return role;
                }
            }
            throw new IllegalArgumentException("Unknown role: " + text);
        }
    }

    // Default constructor
    /**
     * Default constructor.
     * 
     * @author Suhan Arda Öner
     */
    public User() {
    }

    /**
     * Full constructor for User.
     *
     * @param id       the unique identifier
     * @param username the username
     * @param password the hashed password
     * @param role     the user role
     * @param fullName the full name
     * @param address  the address
     * @param phone    the phone number
     * @param email    the email address
     * @param isActive the active status
     * 
     * @author Suhan Arda Öner
     */
    public User(int id, String username, String password, UserRole role,
            String fullName, String address, String phone, String email,
            boolean isActive) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.isActive = isActive;
    }

    // Getters and Setters
    // Getters and Setters

    /**
     * Gets the user ID.
     * 
     * @return the user ID
     * 
     * @author Suhan Arda Öner
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the user ID.
     * 
     * @param id the user ID to set
     * 
     * @author Suhan Arda Öner
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the username.
     * 
     * @return the username
     * 
     * @author Suhan Arda Öner
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username.
     * 
     * @param username the username to set
     * 
     * @author Suhan Arda Öner
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the hashed password.
     * 
     * @return the hashed password
     * 
     * @author Suhan Arda Öner
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the hashed password.
     * 
     * @param password the hashed password to set
     * 
     * @author Suhan Arda Öner
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the user role.
     * 
     * @return the user role
     * 
     * @author Suhan Arda Öner
     */
    public UserRole getRole() {
        return role;
    }

    /**
     * Sets the user role.
     * 
     * @param role the user role to set
     * 
     * @author Suhan Arda Öner
     */
    public void setRole(UserRole role) {
        this.role = role;
    }

    /**
     * Gets the full name.
     * 
     * @return the full name
     * 
     * @author Suhan Arda Öner
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Sets the full name.
     * 
     * @param fullName the full name to set
     * 
     * @author Suhan Arda Öner
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Gets the address.
     * 
     * @return the address
     * 
     * @author Suhan Arda Öner
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the address.
     * 
     * @param address the address to set
     * 
     * @author Suhan Arda Öner
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Gets the phone number.
     * 
     * @return the phone number
     * 
     * @author Suhan Arda Öner
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the phone number.
     * 
     * @param phone the phone number to set
     * 
     * @author Suhan Arda Öner
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Gets the email address.
     * 
     * @return the email address
     * 
     * @author Suhan Arda Öner
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address.
     * 
     * @param email the email address to set
     * 
     * @author Suhan Arda Öner
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Checks if the user is active.
     * 
     * @return true if active, false otherwise
     * 
     * @author Suhan Arda Öner
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Sets the active status.
     * 
     * @param active the active status to set
     * 
     * @author Suhan Arda Öner
     */
    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", role=" + role +
                ", fullName='" + fullName + '\'' +
                '}';
    }
}
