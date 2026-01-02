package com.greengrocer.models;

import java.time.LocalDateTime;

/**
 * Coupon model representing discount coupons.
 * Maps to the Coupons table in the database.
 * 
 * @author Suhan Arda Öner
 */
public class Coupon {
    /** Unique identifier for the coupon. */
    private int id;
    /** The coupon code string. */
    private String code;
    /** The discount value (percentage or fixed amount). */
    private double discountPercent;
    /** Minimum cart value required to use this coupon. */
    private double minCartValue;
    /** Maximum number of times this coupon can be used globally. */
    private int maxUses;
    /** Current number of times this coupon has been used. */
    private int currentUses;
    /** Optional user ID if the coupon is specific to a user. */
    private Integer userId;
    /** Start date and time for coupon validity. */
    private LocalDateTime validFrom;
    /** End date and time for coupon validity. */
    private LocalDateTime validUntil;
    /** Flag indicating if the coupon is currently active. */
    private boolean isActive;

    /**
     * Enum representing the type of discount.
     * 
     * @author Suhan Arda Öner
     */
    public enum DiscountType {
        /** Percentage based discount. */
        PERCENT,
        /** Fixed amount discount. */
        FIXED
    }

    /** The type of discount (PERCENT or FIXED). */
    private DiscountType discountType;

    /**
     * Default constructor.
     * Initializes default values: active=true, validFrom=now, currentUses=0,
     * discountType=PERCENT.
     * 
     * @author Suhan Arda Öner
     */
    public Coupon() {
        this.isActive = true;
        this.validFrom = LocalDateTime.now();
        this.currentUses = 0;
        this.discountType = DiscountType.PERCENT; // Default
    }

    /**
     * Full constructor for Coupon.
     *
     * @param id              the unique identifier
     * @param code            the coupon code
     * @param discountPercent the discount value
     * @param minCartValue    the minimum cart value required
     * @param maxUses         the maximum global uses
     * @param currentUses     the current usage count
     * @param userId          the specific user ID (can be null)
     * @param validFrom       the start validity date
     * @param validUntil      the end validity date
     * @param isActive        the active status
     * @param discountType    the type of discount
     * 
     * @author Suhan Arda Öner
     */
    public Coupon(int id, String code, double discountPercent, double minCartValue,
            int maxUses, int currentUses, Integer userId,
            LocalDateTime validFrom, LocalDateTime validUntil, boolean isActive, DiscountType discountType) {
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
        this.discountType = discountType;
    }

    // Getters and Setters

    /**
     * Gets the coupon ID.
     * 
     * @return the coupon ID
     * 
     * @author Suhan Arda Öner
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the coupon ID.
     * 
     * @param id the coupon ID to set
     * 
     * @author Suhan Arda Öner
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the coupon code.
     * 
     * @return the coupon code
     * 
     * @author Suhan Arda Öner
     */
    public String getCode() {
        return code;
    }

    /**
     * Sets the coupon code.
     * 
     * @param code the coupon code to set
     * 
     * @author Suhan Arda Öner
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Gets the discount value.
     * 
     * @return the discount value
     * 
     * @author Suhan Arda Öner
     */
    public double getDiscountPercent() {
        return discountPercent;
    }

    /**
     * Sets the discount value.
     * 
     * @param discountPercent the discount value to set
     * 
     * @author Suhan Arda Öner
     */
    public void setDiscountPercent(double discountPercent) {
        this.discountPercent = discountPercent;
    }

    /**
     * Gets the minimum cart value.
     * 
     * @return the minimum cart value
     * 
     * @author Suhan Arda Öner
     */
    public double getMinCartValue() {
        return minCartValue;
    }

    /**
     * Sets the minimum cart value.
     * 
     * @param minCartValue the minimum cart value to set
     * 
     * @author Suhan Arda Öner
     */
    public void setMinCartValue(double minCartValue) {
        this.minCartValue = minCartValue;
    }

    /**
     * Gets the maximum uses.
     * 
     * @return the maximum uses
     * 
     * @author Suhan Arda Öner
     */
    public int getMaxUses() {
        return maxUses;
    }

    /**
     * Sets the maximum uses.
     * 
     * @param maxUses the maximum uses to set
     * 
     * @author Suhan Arda Öner
     */
    public void setMaxUses(int maxUses) {
        this.maxUses = maxUses;
    }

    /**
     * Gets the current uses.
     * 
     * @return the current uses
     * 
     * @author Suhan Arda Öner
     */
    public int getCurrentUses() {
        return currentUses;
    }

    /**
     * Sets the current uses.
     * 
     * @param currentUses the current uses to set
     * 
     * @author Suhan Arda Öner
     */
    public void setCurrentUses(int currentUses) {
        this.currentUses = currentUses;
    }

    /**
     * Gets the user ID.
     * 
     * @return the user ID
     * 
     * @author Suhan Arda Öner
     */
    public Integer getUserId() {
        return userId;
    }

    /**
     * Sets the user ID.
     * 
     * @param userId the user ID to set
     * 
     * @author Suhan Arda Öner
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    /**
     * Gets the valid from date.
     * 
     * @return the valid from date
     * 
     * @author Suhan Arda Öner
     */
    public LocalDateTime getValidFrom() {
        return validFrom;
    }

    /**
     * Sets the valid from date.
     * 
     * @param validFrom the valid from date to set
     * 
     * @author Suhan Arda Öner
     */
    public void setValidFrom(LocalDateTime validFrom) {
        this.validFrom = validFrom;
    }

    /**
     * Gets the valid until date.
     * 
     * @return the valid until date
     * 
     * @author Suhan Arda Öner
     */
    public LocalDateTime getValidUntil() {
        return validUntil;
    }

    /**
     * Sets the valid until date.
     * 
     * @param validUntil the valid until date to set
     * 
     * @author Suhan Arda Öner
     */
    public void setValidUntil(LocalDateTime validUntil) {
        this.validUntil = validUntil;
    }

    /**
     * Checks if the coupon is active.
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

    /**
     * Gets the discount type.
     * 
     * @return the discount type
     * 
     * @author Suhan Arda Öner
     */
    public DiscountType getDiscountType() {
        return discountType;
    }

    /**
     * Sets the discount type.
     * 
     * @param discountType the discount type to set
     * 
     * @author Suhan Arda Öner
     */
    public void setDiscountType(DiscountType discountType) {
        this.discountType = discountType;
    }

    /**
     * Check if the coupon is valid for use.
     * 
     * @return true if coupon can be used
     * 
     * @author Suhan Arda Öner
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
     * 
     * @author Suhan Arda Öner
     */
    public boolean canApply(double cartValue) {
        return isValid() && cartValue >= minCartValue;
    }

    /**
     * Calculate discount amount for a given cart value.
     * Applies limits:
     * - Percent: Max 50%
     * - Fixed: Max 250 TL
     * - Result cannot make total &lt; minCartValue (mathematically clamped)
     * 
     * @param cartValue the cart subtotal
     * @return discount amount
     * 
     * @author Suhan Arda Öner
     */
    public double calculateDiscount(double cartValue) {
        if (!canApply(cartValue))
            return 0;

        double discountAmount = 0;

        if (discountType == DiscountType.PERCENT) {
            // Limit percent to 50%
            double effectivePercent = Math.min(discountPercent, 50.0);
            discountAmount = cartValue * (effectivePercent / 100.0);
        } else {
            // Fixed amount, limit to 250 TL
            double effectiveAmount = Math.min(discountPercent, 250.0); // reusing discountPercent field for amount
            discountAmount = effectiveAmount;
        }

        // Ensure discount doesn't bring total below minCartValue
        // Rule: Discount must not reduce the cart total below the minimum cart amount
        // This implies: (cartValue - discount) >= minCartValue
        // So: discount <= cartValue - minCartValue

        double maxAllowedDiscount = Math.max(0, cartValue - minCartValue);

        // If the calculated discount is too high, cap it so the final price is
        // minCartValue.
        // This means if calculated discount is too high, we cap it so the final price
        // is minCartValue.

        return Math.min(discountAmount, maxAllowedDiscount);
    }

    /**
     * Get remaining uses.
     * 
     * @return number of remaining uses
     * 
     * @author Suhan Arda Öner
     */
    public int getRemainingUses() {
        return Math.max(0, maxUses - currentUses);
    }

    @Override
    public String toString() {
        return "Coupon{" +
                "code='" + code + '\'' +
                ", type=" + discountType +
                ", value=" + discountPercent +
                ", isValid=" + isValid() +
                '}';
    }
}
