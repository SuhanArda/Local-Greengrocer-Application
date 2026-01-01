package com.greengrocer.models;

/**
 * User model representing a system user (customer, carrier, or owner).
 * Maps to the UserInfo table in the database.
 */
public abstract class User {
    private int id;
    private String username;
    private String password;
    private UserRole role;
    private String fullName;
    private String address;
    private String phone;
    private String email;
    private boolean isActive;

    /**
     * Enum representing user roles in the system.
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
    public User() {
    }

    // Full constructor
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
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActive() {
        return isActive;
    }

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
