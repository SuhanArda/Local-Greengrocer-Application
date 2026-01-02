package com.greengrocer.models;

import java.time.LocalDateTime;

/**
 * Model representing the relationship between a customer and a coupon.
 * Tracks per-customer coupon usage.
 */
public class CustomerCouponUsage {
    /** Unique identifier for the usage record. */
    private int id;
    /** ID of the customer. */
    private int customerId;
    /** ID of the coupon. */
    private int couponId;
    /** Number of uses remaining for this customer for this coupon. */
    private int usesRemaining;
    /** Timestamp when the coupon was assigned/used. */
    private LocalDateTime assignedAt;

    // Associated Coupon object (for display purposes)
    /** The coupon object associated with this usage record. */
    private Coupon coupon;

    /**
     * Default constructor.
     * Initializes assignedAt to current time.
     * 
     * @author Suhan Arda Öner
     */
    public CustomerCouponUsage() {
        this.assignedAt = LocalDateTime.now();
    }

    /**
     * Full constructor for CustomerCouponUsage.
     *
     * @param id            the unique identifier
     * @param customerId    the customer ID
     * @param couponId      the coupon ID
     * @param usesRemaining the number of uses remaining
     * @param assignedAt    the assignment timestamp
     * 
     * @author Suhan Arda Öner
     */
    public CustomerCouponUsage(int id, int customerId, int couponId, int usesRemaining, LocalDateTime assignedAt) {
        this.id = id;
        this.customerId = customerId;
        this.couponId = couponId;
        this.usesRemaining = usesRemaining;
        this.assignedAt = assignedAt;
    }

    // Getters and Setters

    /**
     * Gets the usage ID.
     * 
     * @return the usage ID
     * 
     * @author Suhan Arda Öner
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the usage ID.
     * 
     * @param id the usage ID to set
     * 
     * @author Suhan Arda Öner
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the customer ID.
     * 
     * @return the customer ID
     * 
     * @author Suhan Arda Öner
     */
    public int getCustomerId() {
        return customerId;
    }

    /**
     * Sets the customer ID.
     * 
     * @param customerId the customer ID to set
     * 
     * @author Suhan Arda Öner
     */
    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    /**
     * Gets the coupon ID.
     * 
     * @return the coupon ID
     * 
     * @author Suhan Arda Öner
     */
    public int getCouponId() {
        return couponId;
    }

    /**
     * Sets the coupon ID.
     * 
     * @param couponId the coupon ID to set
     * 
     * @author Suhan Arda Öner
     */
    public void setCouponId(int couponId) {
        this.couponId = couponId;
    }

    /**
     * Gets the remaining uses.
     * 
     * @return the remaining uses
     * 
     * @author Suhan Arda Öner
     */
    public int getUsesRemaining() {
        return usesRemaining;
    }

    /**
     * Sets the remaining uses.
     * 
     * @param usesRemaining the remaining uses to set
     * 
     * @author Suhan Arda Öner
     */
    public void setUsesRemaining(int usesRemaining) {
        this.usesRemaining = usesRemaining;
    }

    /**
     * Gets the assignment timestamp.
     * 
     * @return the assignment timestamp
     * 
     * @author Suhan Arda Öner
     */
    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    /**
     * Sets the assignment timestamp.
     * 
     * @param assignedAt the assignment timestamp to set
     * 
     * @author Suhan Arda Öner
     */
    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    /**
     * Gets the associated coupon.
     * 
     * @return the coupon object
     * 
     * @author Suhan Arda Öner
     */
    public Coupon getCoupon() {
        return coupon;
    }

    /**
     * Sets the associated coupon.
     * 
     * @param coupon the coupon object to set
     * 
     * @author Suhan Arda Öner
     */
    public void setCoupon(Coupon coupon) {
        this.coupon = coupon;
    }

    /**
     * Check if the customer can still use this coupon.
     * 
     * @return true if uses remaining > 0 and coupon is valid
     * 
     * @author Suhan Arda Öner
     */
    public boolean canUse() {
        return usesRemaining > 0 && (coupon == null || coupon.isValid());
    }

    /**
     * Decrement the uses remaining by 1.
     * 
     * @return true if successful, false if no uses remaining
     * 
     * @author Suhan Arda Öner
     */
    public boolean use() {
        if (usesRemaining > 0) {
            usesRemaining--;
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "CustomerCouponUsage{" +
                "customerId=" + customerId +
                ", couponId=" + couponId +
                ", usesRemaining=" + usesRemaining +
                '}';
    }
}
