package com.greengrocer.models;

/**
 * Customer model representing a customer user.
 * 
 * @author Suhan Arda Öner
 */
public class Customer extends User {
    /** Loyalty points accumulated by the customer. */
    private int loyaltyPoints;
    /** Total number of orders placed by the customer. */
    private int totalOrders;

    /**
     * Default constructor for Customer.
     * Sets the user role to CUSTOMER.
     * 
     * @author Suhan Arda Öner
     */
    public Customer() {
        super();
        this.setRole(UserRole.CUSTOMER);
    }

    /**
     * Full constructor for Customer.
     *
     * @param id            the unique identifier
     * @param username      the username
     * @param password      the password
     * @param fullName      the full name
     * @param address       the address
     * @param phone         the phone number
     * @param email         the email address
     * @param loyaltyPoints the loyalty points
     * @param totalOrders   the total number of orders
     * @param isActive      the active status
     * 
     * @author Suhan Arda Öner
     */
    public Customer(int id, String username, String password, String fullName, String address, String phone,
            String email,
            int loyaltyPoints, int totalOrders, boolean isActive) {
        super(id, username, password, UserRole.CUSTOMER, fullName, address, phone, email, isActive);
        this.loyaltyPoints = loyaltyPoints;
        this.totalOrders = totalOrders;
    }

    /**
     * Gets the loyalty points.
     * 
     * @return the loyalty points
     * 
     * @author Suhan Arda Öner
     */
    public int getLoyaltyPoints() {
        return loyaltyPoints;
    }

    /**
     * Sets the loyalty points.
     * 
     * @param loyaltyPoints the loyalty points to set
     * 
     * @author Suhan Arda Öner
     */
    public void setLoyaltyPoints(int loyaltyPoints) {
        this.loyaltyPoints = loyaltyPoints;
    }

    /**
     * Gets the total number of orders.
     * 
     * @return the total orders
     * 
     * @author Suhan Arda Öner
     */
    public int getTotalOrders() {
        return totalOrders;
    }

    /**
     * Sets the total number of orders.
     * 
     * @param totalOrders the total orders to set
     * 
     * @author Suhan Arda Öner
     */
    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    /**
     * Calculate loyalty discount percentage based on total orders.
     * 
     * @return discount percentage (0, 5, 10, or 15)
     * 
     * @author Suhan Arda Öner
     */
    public double getLoyaltyDiscount() {
        if (totalOrders >= 20)
            return 15.0;
        if (totalOrders >= 10)
            return 10.0;
        if (totalOrders >= 5)
            return 5.0;
        return 0.0;
    }
}
