package com.greengrocer.models;

import java.time.LocalDateTime;

/**
 * Coupon model representing discount coupons.
 * Maps to the Coupons table in the database.
 */
public class Coupon {
    private int id;
    private String code;
    private double discountPercent;
    private double minCartValue;
    private int maxUses;
    private int currentUses;
    private Integer userId;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private boolean isActive;

    // Default constructor
    public Coupon() {
        this.isActive = true;
        this.validFrom = LocalDateTime.now();
        this.currentUses = 0;
    }

    // Full constructor
    public Coupon(int id, String code, double discountPercent, double minCartValue,
            int maxUses, int currentUses, Integer userId,
            LocalDateTime validFrom, LocalDateTime validUntil, boolean isActive) {
        this.id = id;
        this.code = code;
        this.discountPercent = discountPercent;
        this.minCartValue = minCartValue;
        this.maxUses = maxUses;
        this.currentUses = currentUses;
        this.userId = userId;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.isActive = isActive;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public double getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(double discountPercent) {
        this.discountPercent = discountPercent;
    }

    public double getMinCartValue() {
        return minCartValue;
    }

    public void setMinCartValue(double minCartValue) {
        this.minCartValue = minCartValue;
    }

    public int getMaxUses() {
        return maxUses;
    }

    public void setMaxUses(int maxUses) {
        this.maxUses = maxUses;
    }

    public int getCurrentUses() {
        return currentUses;
    }

    public void setCurrentUses(int currentUses) {
        this.currentUses = currentUses;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public LocalDateTime getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDateTime validFrom) {
        this.validFrom = validFrom;
    }

    public LocalDateTime getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDateTime validUntil) {
        this.validUntil = validUntil;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    /**
     * Check if the coupon is valid for use.
     * 
     * @return true if coupon can be used
     */
    public boolean isValid() {
        if (!isActive)
            return false;

        LocalDateTime now = LocalDateTime.now();
        if (validFrom != null && now.isBefore(validFrom))
            return false;
        if (validUntil != null && now.isAfter(validUntil))
            return false;
        if (currentUses >= maxUses)
            return false;

        return true;
    }

    /**
     * Check if coupon can be applied to a cart with given value.
     * 
     * @param cartValue the cart subtotal
     * @return true if coupon can be applied
     */
    public boolean canApply(double cartValue) {
        return isValid() && cartValue >= minCartValue;
    }

    /**
     * Calculate discount amount for a given cart value.
     * 
     * @param cartValue the cart subtotal
     * @return discount amount
     */
    public double calculateDiscount(double cartValue) {
        if (!canApply(cartValue))
            return 0;
        return cartValue * (discountPercent / 100.0);
    }

    /**
     * Get remaining uses.
     * 
     * @return number of remaining uses
     */
    public int getRemainingUses() {
        return Math.max(0, maxUses - currentUses);
    }

    @Override
    public String toString() {
        return "Coupon{" +
                "code='" + code + '\'' +
                ", discountPercent=" + discountPercent +
                ", isValid=" + isValid() +
                '}';
    }
}
