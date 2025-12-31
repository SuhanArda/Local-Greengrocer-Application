package com.greengrocer.models;

/**
 * Customer model representing a customer user.
 */
public class Customer extends User {
    private int loyaltyPoints;
    private int totalOrders;

    public Customer() {
        super();
        this.setRole(UserRole.CUSTOMER);
    }

    public Customer(int id, String username, String password, String fullName, String address, String phone,
            String email,
            int loyaltyPoints, int totalOrders, boolean isActive) {
        super(id, username, password, UserRole.CUSTOMER, fullName, address, phone, email, isActive);
        this.loyaltyPoints = loyaltyPoints;
        this.totalOrders = totalOrders;
    }

    public int getLoyaltyPoints() {
        return loyaltyPoints;
    }

    public void setLoyaltyPoints(int loyaltyPoints) {
        this.loyaltyPoints = loyaltyPoints;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    /**
     * Calculate loyalty discount percentage based on total orders.
     * 
     * @return discount percentage (0, 5, 10, or 15)
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
